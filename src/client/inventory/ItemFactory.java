/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import tools.DatabaseConnection;
import tools.Pair;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

/**
 *
 * @author Flav
 */
public enum ItemFactory {

    INVENTORY(1, false),
    STORAGE(2, true),
    CASH_EXPLORER(3, true),
    CASH_CYGNUS(4, true),
    CASH_ARAN(5, true),
    MERCHANT(6, false),
    CASH_OVERALL(7, true);
    private final int value;
    private final boolean account;
    private static final Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.ITEM, true);

    private ItemFactory(int value, boolean account) {
        this.value = value;
        this.account = account;
    }

    public int getValue() {
        return value;
    }

    public List<Pair<Item, MapleInventoryType>> loadItems(int id, boolean login) throws SQLException {
        if(value != 6) return loadItemsCommon(id, login);
        else return loadItemsMerchant(id, login);
    }
    
    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        saveItems(items, null, id, con);
    }
    
    public synchronized void saveItems(List<Pair<Item, MapleInventoryType>> items, List<Short> bundlesList, int id, Connection con) throws SQLException {
        if(value != 6) saveItemsCommon(items, id, con);
        else saveItemsMerchant(items, bundlesList, id, con);
    }
    
    private static Equip loadEquipFromResultSet(ResultSet rs) throws SQLException {
        Equip equip = new Equip(rs.getInt("itemid"), (short) rs.getInt("position"));
        equip.setOwner(rs.getString("owner"));
        equip.setQuantity((short) rs.getInt("quantity"));
        equip.setAcc((short) rs.getInt("acc"));
        equip.setAvoid((short) rs.getInt("avoid"));
        equip.setDex((short) rs.getInt("dex"));
        equip.setHands((short) rs.getInt("hands"));
        equip.setHp((short) rs.getInt("hp"));
        equip.setInt((short) rs.getInt("int"));
        equip.setJump((short) rs.getInt("jump"));
        equip.setVicious((short) rs.getInt("vicious"));
        equip.setFlag((byte) rs.getInt("flag"));
        equip.setLuk((short) rs.getInt("luk"));
        equip.setMatk((short) rs.getInt("matk"));
        equip.setMdef((short) rs.getInt("mdef"));
        equip.setMp((short) rs.getInt("mp"));
        equip.setSpeed((short) rs.getInt("speed"));
        equip.setStr((short) rs.getInt("str"));
        equip.setWatk((short) rs.getInt("watk"));
        equip.setWdef((short) rs.getInt("wdef"));
        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
        equip.setLevel((byte) rs.getByte("level"));
        equip.setItemExp(rs.getInt("itemexp"));
        equip.setItemLevel(rs.getByte("itemlevel"));
        equip.setExpiration(rs.getLong("expiration"));
        equip.setGiftFrom(rs.getString("giftFrom"));
        equip.setRingId(rs.getInt("ringid"));
        
        return equip;
    }
    
    public static List<Pair<Item, Integer>> loadEquippedItems(int id, boolean isAccount, boolean login) throws SQLException {
        List<Pair<Item, Integer>> items = new ArrayList<>();
        
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
        query.append("(SELECT id, accountid FROM characters) AS accountterm ");
        query.append("RIGHT JOIN ");
        query.append("(SELECT * FROM (`inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`))) AS equipterm");
        query.append(" ON accountterm.id=equipterm.characterid ");
        query.append("WHERE accountterm.`");
        query.append(isAccount ? "accountid" : "characterid");
        query.append("` = ?");
        query.append(login ? " AND `inventorytype` = " + MapleInventoryType.EQUIPPED.getType() : "");
        
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                ps.setInt(1, id);
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer cid = rs.getInt("characterid");
                        items.add(new Pair<Item, Integer>(loadEquipFromResultSet(rs), cid));
                    }
                }
            }
        }
        
        return items;
    }
    
    private List<Pair<Item, MapleInventoryType>> loadItemsCommon(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();
		
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");

            if (login) {
                query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
            }

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                    items.add(new Pair<Item, MapleInventoryType>(loadEquipFromResultSet(rs), mit));
                } else {
                    Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                    item.setOwner(rs.getString("owner"));
                    item.setExpiration(rs.getLong("expiration"));
                    item.setGiftFrom(rs.getString("giftFrom"));
                    item.setFlag((byte) rs.getInt("flag"));
                    items.add(new Pair<>(item, mit));
                }
            }
            
            rs.close();
            ps.close();
            con.close();
        } finally {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
        return items;
    }

    private void saveItemsCommon(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        lock.lock();
        try {
            StringBuilder query = new StringBuilder();
            query.append("DELETE `inventoryitems`, `inventoryequipment` FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");
            ps = con.prepareStatement(query.toString());
            ps.setInt(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            if (!items.isEmpty()) {
                for (Pair<Item, MapleInventoryType> pair : items) {
                    Item item = pair.getLeft();
                    MapleInventoryType mit = pair.getRight();
                    ps.setInt(1, value);
                    ps.setString(2, account ? null : String.valueOf(id));
                    ps.setString(3, account ? String.valueOf(id) : null);
                    ps.setInt(4, item.getItemId());
                    ps.setInt(5, mit.getType());
                    ps.setInt(6, item.getPosition());
                    ps.setInt(7, item.getQuantity());
                    ps.setString(8, item.getOwner());
                    ps.setInt(9, item.getPetId());
                    ps.setInt(10, item.getFlag());
                    ps.setLong(11, item.getExpiration());
                    ps.setString(12, item.getGiftFrom());
                    ps.executeUpdate();

                    pse = con.prepareStatement("INSERT INTO `inventoryequipment` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                        rs = ps.getGeneratedKeys();

			if (!rs.next()) {
                            throw new RuntimeException("Inserting item failed.");
                        }

                        pse.setInt(1, rs.getInt(1));			
			rs.close();
						
                        Equip equip = (Equip) item;
                        pse.setInt(2, equip.getUpgradeSlots());
                        pse.setInt(3, equip.getLevel());
                        pse.setInt(4, equip.getStr());
                        pse.setInt(5, equip.getDex());
                        pse.setInt(6, equip.getInt());
                        pse.setInt(7, equip.getLuk());
                        pse.setInt(8, equip.getHp());
                        pse.setInt(9, equip.getMp());
                        pse.setInt(10, equip.getWatk());
                        pse.setInt(11, equip.getMatk());
                        pse.setInt(12, equip.getWdef());
                        pse.setInt(13, equip.getMdef());
                        pse.setInt(14, equip.getAcc());
                        pse.setInt(15, equip.getAvoid());
                        pse.setInt(16, equip.getHands());
                        pse.setInt(17, equip.getSpeed());
                        pse.setInt(18, equip.getJump());
                        pse.setInt(19, 0);
                        pse.setInt(20, equip.getVicious());
                        pse.setInt(21, equip.getItemLevel());
                        pse.setInt(22, equip.getItemExp());
                        pse.setInt(23, equip.getRingId());
                        pse.executeUpdate();
                    }
                    
                    pse.close();
                }
            }
			
            ps.close();
        } finally {
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
            if (pse != null && !pse.isClosed()) {
                pse.close();
            }
            if(rs != null && !rs.isClosed()) {
		rs.close();
            }
			
            lock.unlock();
        }
    }
    
    private List<Pair<Item, MapleInventoryType>> loadItemsMerchant(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();
		
        PreparedStatement ps = null, ps2 = null;
        ResultSet rs = null, rs2 = null;
        Connection con = DatabaseConnection.getConnection();
        try {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");

            if (login) {
                query.append(" AND `inventorytype` = ").append(MapleInventoryType.EQUIPPED.getType());
            }

            ps = con.prepareStatement(query.toString());
            ps.setInt(1, value);
            ps.setInt(2, id);
            rs = ps.executeQuery();

            while (rs.next()) {
                ps2 = con.prepareStatement("SELECT `bundles` FROM `inventorymerchant` WHERE `inventoryitemid` = ?");
                ps2.setInt(1, rs.getInt("inventoryitemid"));
                rs2 = ps2.executeQuery();
                
                short bundles = 0;
                if(rs2.next()) {
                    bundles = rs2.getShort("bundles");
                }
                
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                    items.add(new Pair<Item, MapleInventoryType>(loadEquipFromResultSet(rs), mit));
                } else {
                    if(bundles > 0) {
                        Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short)(bundles * rs.getInt("quantity")), rs.getInt("petid"));
                        item.setOwner(rs.getString("owner"));
                        item.setExpiration(rs.getLong("expiration"));
                        item.setGiftFrom(rs.getString("giftFrom"));
                        item.setFlag((byte) rs.getInt("flag"));
                        items.add(new Pair<>(item, mit));
                    }
                }
                
                rs2.close();
                ps2.close();
            }

            rs.close();
            ps.close();
            con.close();
        } finally {
            if (rs2 != null && !rs2.isClosed()) {
                rs2.close();
            }
            if (ps2 != null && !ps2.isClosed()) {
                ps2.close();
            }
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
            if (con != null && !con.isClosed()) {
                con.close();
            }
        }
        return items;
    }

    private void saveItemsMerchant(List<Pair<Item, MapleInventoryType>> items, List<Short> bundlesList, int id, Connection con) throws SQLException {
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;

        lock.lock();
        try {
            ps = con.prepareStatement("DELETE FROM `inventorymerchant` WHERE `characterid` = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            
            StringBuilder query = new StringBuilder();
            query.append("DELETE `inventoryitems`, `inventoryequipment` FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `type` = ? AND `");
            query.append(account ? "accountid" : "characterid").append("` = ?");
            ps = con.prepareStatement(query.toString());
            ps.setInt(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO `inventoryitems` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            if (!items.isEmpty()) {
                int i = 0;
                for (Pair<Item, MapleInventoryType> pair : items) {
                    Item item = pair.getLeft();
                    Short bundles = bundlesList.get(i);
                    MapleInventoryType mit = pair.getRight();
                    i++;
                    
                    ps.setInt(1, value);
                    ps.setString(2, account ? null : String.valueOf(id));
                    ps.setString(3, account ? String.valueOf(id) : null);
                    ps.setInt(4, item.getItemId());
                    ps.setInt(5, mit.getType());
                    ps.setInt(6, item.getPosition());
                    ps.setInt(7, item.getQuantity());
                    ps.setString(8, item.getOwner());
                    ps.setInt(9, item.getPetId());
                    ps.setInt(10, item.getFlag());
                    ps.setLong(11, item.getExpiration());
                    ps.setString(12, item.getGiftFrom());
                    ps.executeUpdate();

                    rs = ps.getGeneratedKeys();
                    if (!rs.next()) {
                        throw new RuntimeException("Inserting item failed.");
                    }

                    int genKey = rs.getInt(1);
                    rs.close();
                    
                    pse = con.prepareStatement("INSERT INTO `inventorymerchant` VALUES (DEFAULT, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                    pse.setInt(1, genKey);
                    pse.setInt(2, id);
                    pse.setInt(3, bundles);
                    pse.executeUpdate();
                    pse.close();

                    if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                        pse = con.prepareStatement("INSERT INTO `inventoryequipment` VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        pse.setInt(1, genKey);
						
                        Equip equip = (Equip) item;
                        pse.setInt(2, equip.getUpgradeSlots());
                        pse.setInt(3, equip.getLevel());
                        pse.setInt(4, equip.getStr());
                        pse.setInt(5, equip.getDex());
                        pse.setInt(6, equip.getInt());
                        pse.setInt(7, equip.getLuk());
                        pse.setInt(8, equip.getHp());
                        pse.setInt(9, equip.getMp());
                        pse.setInt(10, equip.getWatk());
                        pse.setInt(11, equip.getMatk());
                        pse.setInt(12, equip.getWdef());
                        pse.setInt(13, equip.getMdef());
                        pse.setInt(14, equip.getAcc());
                        pse.setInt(15, equip.getAvoid());
                        pse.setInt(16, equip.getHands());
                        pse.setInt(17, equip.getSpeed());
                        pse.setInt(18, equip.getJump());
                        pse.setInt(19, 0);
                        pse.setInt(20, equip.getVicious());
                        pse.setInt(21, equip.getItemLevel());
                        pse.setInt(22, equip.getItemExp());
                        pse.setInt(23, equip.getRingId());
                        pse.executeUpdate();
                        
                        pse.close();
                    }
                }
            }
			
            ps.close();
        } finally {
            if (ps != null && !ps.isClosed()) {
                ps.close();
            }
            if (pse != null && !pse.isClosed()) {
                pse.close();
            }
            if(rs != null && !rs.isClosed()) {
		rs.close();
            }
			
            lock.unlock();
        }
    }
}
/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package client.inventory;

import constants.ExpTable;
import java.awt.Point;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import tools.DatabaseConnection;
import server.MapleItemInformationProvider;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import client.MapleCharacter;
import client.inventory.manipulator.MapleCashidGenerator;
import java.sql.Connection;
import tools.MaplePacketCreator;
import tools.Pair;

/**
 *
 * @author Matze
 */
public class MaplePet extends Item {
    private String name;
    private int uniqueid;
    private int closeness = 0;
    private byte level = 1;
    private int fullness = 100;
    private int Fh;
    private Point pos;
    private int stance;
    private boolean summoned;

    private MaplePet(int id, short position, int uniqueid) {
        super(id, position, (short) 1);
        this.uniqueid = uniqueid;
        this.pos = new Point(0, 0);
    }

    public static MaplePet loadFromDb(int itemid, short position, int petid) {
        try {
            MaplePet ret = new MaplePet(itemid, position, petid);
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT name, level, closeness, fullness, summoned FROM pets WHERE petid = ?"); // Get pet details..
            ps.setInt(1, petid);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret.setName(rs.getString("name"));
            ret.setCloseness(Math.min(rs.getInt("closeness"), 30000));
            ret.setLevel((byte) Math.min(rs.getByte("level"), 30));
            ret.setFullness(Math.min(rs.getInt("fullness"), 100));
            ret.setSummoned(rs.getInt("summoned") == 1);
            rs.close();
            ps.close();
            con.close();
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteFromDb() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE `petid` = ?");
            ps.setInt(1, this.getUniqueId());
            ps.executeUpdate();
            ps.close();
            con.close();
            
            MapleCashidGenerator.freeCashId(this.getUniqueId());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveToDb() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, summoned = ? WHERE petid = ?");
            ps.setString(1, getName());
            ps.setInt(2, getLevel());
            ps.setInt(3, getCloseness());
            ps.setInt(4, getFullness());
            ps.setInt(5, isSummoned() ? 1 : 0);
            ps.setInt(6, getUniqueId());
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int createPet(int itemid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, summoned) VALUES (?, ?, 1, 0, 100, 0)");
            int ret = MapleCashidGenerator.generateCashId();
            ps.setInt(1, ret);
            ps.setString(2, MapleItemInformationProvider.getInstance().getName(itemid));
            ps.executeUpdate();
            ps.close();
            con.close();
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int createPet(int itemid, byte level, int closeness, int fullness) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, summoned) VALUES (?, ?, ?, ?, ?, 0)");
            int ret = MapleCashidGenerator.generateCashId();
            ps.setInt(1, ret);
            ps.setString(2, MapleItemInformationProvider.getInstance().getName(itemid));
            ps.setByte(3, level);
            ps.setInt(4, closeness);
            ps.setInt(5, fullness);
            ps.executeUpdate();
            ps.close();
            con.close();
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUniqueId() {
        return uniqueid;
    }

    public void setUniqueId(int id) {
        this.uniqueid = id;
    }

    public int getCloseness() {
        return closeness;
    }

    public void setCloseness(int closeness) {
        this.closeness = closeness;
    }

    public byte getLevel() {
        return level;
    }
    
    public void gainClosenessFullness(MapleCharacter owner, int incCloseness, int incFullness, int type) {
        byte slot = owner.getPetIndex(this);
        boolean enjoyed;
        
        //will NOT increase pet's closeness if tried to feed pet with 100% fullness
        if (fullness < 100 || incFullness == 0) {   //incFullness == 0: command given
            int newFullness = fullness + incFullness;
            if (newFullness > 100) newFullness = 100;
            fullness = newFullness;
            
            if (incCloseness > 0 && closeness < 30000) {
                int newCloseness = closeness + incCloseness;
                if (newCloseness > 30000) newCloseness = 30000;
                
                closeness = newCloseness;
                while(newCloseness >= ExpTable.getClosenessNeededForLevel(level)) {
                    level += 1;
                    owner.getClient().announce(MaplePacketCreator.showOwnPetLevelUp(slot));
                    owner.getMap().broadcastMessage(MaplePacketCreator.showPetLevelUp(owner, slot));
                }
            }
            
            enjoyed = true;
        } else {
            int newCloseness = closeness - 1;
            if (newCloseness < 0) newCloseness = 0;

            closeness = newCloseness;
            if (level > 1 && newCloseness < ExpTable.getClosenessNeededForLevel(level - 1)) {
                level -= 1;
            }
            
            enjoyed = false;
        }
        
        owner.getMap().broadcastMessage(MaplePacketCreator.commandResponse(owner.getId(), slot, type, enjoyed));
        saveToDb();
        
        Item petz = owner.getInventory(MapleInventoryType.CASH).getItem(getPosition());
        if (petz != null)
            owner.forceUpdateItem(petz);
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public int getFullness() {
        return fullness;
    }

    public void setFullness(int fullness) {
        this.fullness = fullness;
    }

    public int getFh() {
        return Fh;
    }

    public void setFh(int Fh) {
        this.Fh = Fh;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public int getStance() {
        return stance;
    }

    public void setStance(int stance) {
        this.stance = stance;
    }

    public boolean isSummoned() {
        return summoned;
    }

    public void setSummoned(boolean yes) {
        this.summoned = yes;
    }

    public Pair<Integer, Boolean> canConsume(int itemId) {
        return MapleItemInformationProvider.getInstance().canPetConsume(this.getItemId(), itemId);
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof AbsoluteLifeMovement) {
                    this.setPos(((LifeMovement) move).getPosition());
                }
                this.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

public final class MonsterBook {
    private int specialCard;
    private int normalCard = 0;
    private int bookLevel = 1;
    private Map<Integer, Integer> cards = new LinkedHashMap<>();

    public void addCard(final MapleClient c, final int cardid) {
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), MaplePacketCreator.showForeginCardEffect(c.getPlayer().getId()), false);
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            if (all.getKey() == cardid) {
                if (all.getValue() > 4) {
                    c.announce(MaplePacketCreator.addCard(true, cardid, all.getValue()));
                } else {
                    all.setValue(all.getValue() + 1);
                    c.announce(MaplePacketCreator.addCard(false, cardid, all.getValue()));
                    c.announce(MaplePacketCreator.showGainCard());
                    calculateLevel();
                }
                return;
            }
        }
        cards.put(cardid, 1);
        c.announce(MaplePacketCreator.addCard(false, cardid, 1));
        c.announce(MaplePacketCreator.showGainCard());
        calculateLevel();
        c.getPlayer().saveToDB();
    }

    private void calculateLevel() {
        bookLevel = (int) Math.max(1, Math.sqrt((normalCard + specialCard) / 5));
    }

    public int getBookLevel() {
        return bookLevel;
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public int getTotalCards() {
        return specialCard + normalCard;
    }

    public int getNormalCard() {
        return normalCard;
    }

    public int getSpecialCard() {
        return specialCard;
    }

    public void loadCards(final int charid) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT cardid, level FROM monsterbook WHERE charid = ? ORDER BY cardid ASC")) {
            ps.setInt(1, charid);
            try (ResultSet rs = ps.executeQuery()) {
                int cardid, level;
                while (rs.next()) {
                    cardid = rs.getInt("cardid");
                    level = rs.getInt("level");
                    if (cardid / 1000 >= 2388) {
                        specialCard++;
                    } else {
                        normalCard++;
                    }
                    cards.put(cardid, level);
                }
            }
        }
        calculateLevel();
    }

    public void saveCards(final int charid) {
        if (cards.isEmpty()) {
            return;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();
            boolean first = true;
            StringBuilder query = new StringBuilder();
            for (Entry<Integer, Integer> all : cards.entrySet()) {
                if (first) {
                    query.append("INSERT INTO monsterbook VALUES (");
                    first = false;
                } else {
                    query.append(",(");
                }
                query.append(charid);
                query.append(", ");
                query.append(all.getKey());
                query.append(", ");
                query.append(all.getValue());
                query.append(")");
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
        }
    }
}

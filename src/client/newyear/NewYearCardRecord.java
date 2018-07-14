/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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
package client.newyear;

import client.MapleCharacter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import net.server.Server;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.MaplePacketCreator;

/**
 *
 * @author Ronan (credits to Eric for showing the New Year opcodes and handler layout)
 */
public class NewYearCardRecord {
    private int id;
    
    private int senderId; 
    private String senderName; 
    private boolean senderDiscardCard; 
    
    private int receiverId; 
    private String receiverName; 
    private boolean receiverDiscardCard; 
    private boolean receiverReceivedCard;
    
    private String stringContent;
    private long dateSent = 0;
    private long dateReceived = 0;
    
    private ScheduledFuture<?> sendTask = null;
    
    public NewYearCardRecord(int senderid, String sender, int receiverid, String receiver, String message) {
        this.id = -1;
        
        this.senderId = senderid;
        this.senderName = sender;
        this.senderDiscardCard = false;
        
        this.receiverId = receiverid;
        this.receiverName = receiver;
        this.receiverDiscardCard = false;
        this.receiverReceivedCard = false;
        
        this.stringContent = message;
        
        this.dateSent = System.currentTimeMillis();
        this.dateReceived = 0;
    }
    
    private void setExtraNewYearCardRecord(int id, boolean senderDiscardCard, boolean receiverDiscardCard, boolean receiverReceivedCard, long dateSent, long dateReceived) {
        this.id = id;
        this.senderDiscardCard = senderDiscardCard;
        this.receiverDiscardCard = receiverDiscardCard;
        this.receiverReceivedCard = receiverReceivedCard;
        
        this.dateSent = dateSent;
        this.dateReceived = dateReceived;
    }
    
    public void setId(int cardid) {
        this.id = cardid;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getSenderId() {
        return senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public boolean isSenderCardDiscarded() {
        return senderDiscardCard;
    }
    
    public int getReceiverId() {
        return receiverId;
    }
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public boolean isReceiverCardDiscarded() {
        return receiverDiscardCard;
    }
    
    public boolean isReceiverCardReceived() {
        return receiverReceivedCard;
    }
    
    public String getMessage() {
        return stringContent;
    }
    
    public long getDateSent() {
        return dateSent;
    }
    
    public long getDateReceived() {
        return dateReceived;
    }
            
    public static void saveNewYearCard(NewYearCardRecord newyear) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO newyear VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, newyear.senderId);
                ps.setString(2, newyear.senderName);
                ps.setInt(3, newyear.receiverId);
                ps.setString(4, newyear.receiverName);
                
                ps.setString(5, newyear.stringContent);
                
                ps.setBoolean(6, newyear.senderDiscardCard);
                ps.setBoolean(7, newyear.receiverDiscardCard);
                ps.setBoolean(8, newyear.receiverReceivedCard);
                
                ps.setLong(9, newyear.dateSent);
                ps.setLong(10, newyear.dateReceived);
        
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    newyear.id = rs.getInt(1);
                }
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static void updateNewYearCard(NewYearCardRecord newyear) {
        newyear.receiverReceivedCard = true;
        newyear.dateReceived = System.currentTimeMillis();
        
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE newyear SET received=1, timereceived=? WHERE id=?")) {
                ps.setLong(1, newyear.dateReceived);
                ps.setInt(2, newyear.id);
        
                ps.executeUpdate();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static NewYearCardRecord loadNewYearCard(int cardid) {
        NewYearCardRecord nyc = Server.getInstance().getNewYearCard(cardid);
        if(nyc != null) return nyc;
        
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM newyear WHERE id = ?")) {
                ps.setInt(1, cardid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        NewYearCardRecord newyear = new NewYearCardRecord(rs.getInt("senderid"), rs.getString("sendername"), rs.getInt("receiverid"), rs.getString("receivername"), rs.getString("message"));
                        newyear.setExtraNewYearCardRecord(rs.getInt("id"), rs.getBoolean("senderdiscard"), rs.getBoolean("receiverdiscard"), rs.getBoolean("received"), rs.getLong("timesent"), rs.getLong("timereceived"));
                        
                        Server.getInstance().setNewYearCard(newyear);
                        return newyear;
                    }
                }
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        
        return null;
    }
    
    public static void loadPlayerNewYearCards(MapleCharacter chr) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM newyear WHERE senderid = ? OR receiverid = ?")) {
                ps.setInt(1, chr.getId());
                ps.setInt(2, chr.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        NewYearCardRecord newyear = new NewYearCardRecord(rs.getInt("senderid"), rs.getString("sendername"), rs.getInt("receiverid"), rs.getString("receivername"), rs.getString("message"));
                        newyear.setExtraNewYearCardRecord(rs.getInt("id"), rs.getBoolean("senderdiscard"), rs.getBoolean("receiverdiscard"), rs.getBoolean("received"), rs.getLong("timesent"), rs.getLong("timereceived"));
                        
                        chr.addNewYearRecord(newyear);
                    }
                }
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static void printNewYearRecords(MapleCharacter chr) {
        chr.dropMessage(5, "New Years: " + chr.getNewYearRecords().size());
        
        for(NewYearCardRecord nyc : chr.getNewYearRecords()) {
            chr.dropMessage(5, "-------------------------------");
            
            chr.dropMessage(5, "Id: " + nyc.id);
            
            chr.dropMessage(5, "Sender id: " + nyc.senderId);
            chr.dropMessage(5, "Sender name: " + nyc.senderName);
            chr.dropMessage(5, "Sender discard: " + nyc.senderDiscardCard);
            
            chr.dropMessage(5, "Receiver id: " + nyc.receiverId);
            chr.dropMessage(5, "Receiver name: " + nyc.receiverName);
            chr.dropMessage(5, "Receiver discard: " + nyc.receiverDiscardCard);
            chr.dropMessage(5, "Received: " + nyc.receiverReceivedCard);
            
            chr.dropMessage(5, "Message: " + nyc.stringContent);
            chr.dropMessage(5, "Date sent: " + nyc.dateSent);
            chr.dropMessage(5, "Date recv: " + nyc.dateReceived);
        }
    }
    
    public void startNewYearCardTask() {
        if(sendTask != null) return;
        
        sendTask = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                Server server = Server.getInstance();
                
                int world = server.getCharacterWorld(receiverId);
                if(world == -1) {
                    sendTask.cancel(false);
                    sendTask = null;
                    
                    return;
                }
                
                MapleCharacter target = server.getWorld(world).getPlayerStorage().getCharacterById(receiverId);
                if(target != null && target.isLoggedinWorld()) {
                    target.announce(MaplePacketCreator.onNewYearCardRes(target, NewYearCardRecord.this, 0xC, 0));
                }
            }
        }, 1000 * 60 * 60); //1 Hour
    }
    
    public void stopNewYearCardTask() {
        if(sendTask != null) {
            sendTask.cancel(false);
            sendTask = null;
        }
    }
    
    private static void deleteNewYearCard(int id) {
        Server.getInstance().removeNewYearCard(id);
        
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM newyear WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    
    public static void removeAllNewYearCard(boolean send, MapleCharacter chr) {
        int cid = chr.getId();
        
        /* not truly needed since it's going to be hard removed from the DB
        String actor = (send ? "sender" : "receiver");
        
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE newyear SET " + actor + "id = 1, received = 0 WHERE " + actor + "id = ?")) {
                ps.setInt(1, cid);
                ps.executeUpdate();
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        */
        
        Set<NewYearCardRecord> set = new HashSet<>(chr.getNewYearRecords());
        for(NewYearCardRecord nyc : set) {
            if(send) {
                if(nyc.senderId == cid) {
                    nyc.senderDiscardCard = true;
                    nyc.receiverReceivedCard = false;
                    
                    chr.removeNewYearRecord(nyc);
                    deleteNewYearCard(nyc.id);
                    
                    chr.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(chr, nyc, 0xE, 0));

                    MapleCharacter other = chr.getClient().getWorldServer().getPlayerStorage().getCharacterById(nyc.getReceiverId());
                    if(other != null && other.isLoggedinWorld()) {
                        other.removeNewYearRecord(nyc);
                        other.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(other, nyc, 0xE, 0));

                        other.dropMessage(6, "[NEW YEAR] " + chr.getName() + " threw away the New Year card.");
                    }
                }
            } else {
                if(nyc.receiverId == cid) {
                    nyc.receiverDiscardCard = true;
                    nyc.receiverReceivedCard = false;
                    
                    chr.removeNewYearRecord(nyc);
                    deleteNewYearCard(nyc.id);
                    
                    chr.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(chr, nyc, 0xE, 0));

                    MapleCharacter other = chr.getClient().getWorldServer().getPlayerStorage().getCharacterById(nyc.getSenderId());
                    if(other != null && other.isLoggedinWorld()) {
                        other.removeNewYearRecord(nyc);
                        other.getMap().broadcastMessage(MaplePacketCreator.onNewYearCardRes(other, nyc, 0xE, 0));

                        other.dropMessage(6, "[NEW YEAR] " + chr.getName() + " threw away the New Year card.");
                    }
                }
            }
        }
    }
    
    public static void startPendingNewYearCardRequests() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM newyear WHERE timereceived = 0 AND senderdiscard = 0")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        NewYearCardRecord newyear = new NewYearCardRecord(rs.getInt("senderid"), rs.getString("sendername"), rs.getInt("receiverid"), rs.getString("receivername"), rs.getString("message"));
                        newyear.setExtraNewYearCardRecord(rs.getInt("id"), rs.getBoolean("senderdiscard"), rs.getBoolean("receiverdiscard"), rs.getBoolean("received"), rs.getLong("timesent"), rs.getLong("timereceived"));
                        
                        Server.getInstance().setNewYearCard(newyear);
                        newyear.startNewYearCardTask();
                    }
                }
            }
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}

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
package net.server;

import net.server.worker.CouponWorker;
import net.server.worker.RankingWorker;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

import net.MapleServerHandler;
import net.mina.MapleCodecFactory;
import net.server.channel.Channel;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.world.World;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.CashShop.CashItemFactory;
import server.TimerManager;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.Pair;
import client.MapleCharacter;
import client.SkillFactory;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.Calendar;
import server.quest.MapleQuest;

public class Server implements Runnable {
    private static final Set<Integer> activeFly = new HashSet<>();
    private static final Map<Integer, Integer> couponRates = new LinkedHashMap<>();
    private static final List<Integer> activeCoupons = new LinkedList<>();
    
    private IoAcceptor acceptor;
    private List<Map<Integer, String>> channels = new LinkedList<>();
    private List<World> worlds = new ArrayList<>();
    private final Properties subnetInfo = new Properties();
    private static Server instance = null;
    private List<Pair<Integer, String>> worldRecommendedList = new LinkedList<>();
    private final Map<Integer, MapleGuild> guilds = new LinkedHashMap<>();
    private final Lock shutdownLock = new ReentrantLock();
    private final PlayerBuffStorage buffStorage = new PlayerBuffStorage();
    private final Map<Integer, MapleAlliance> alliances = new LinkedHashMap<>();
    private boolean online = false;
    public static long uptime = System.currentTimeMillis();
    
    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public boolean isOnline() {
        return online;
    }

    public List<Pair<Integer, String>> worldRecommendedList() {
        return worldRecommendedList;
    }

    /*
    public void removeChannel(int worldid, int channel) {   //lol don't!
        channels.remove(channel);

        World world = worlds.get(worldid);
        if (world != null) {
            world.removeChannel(channel);
        }
    }
    */

    public Channel getChannel(int world, int channel) {
        return worlds.get(world).getChannel(channel);
    }

    public List<Channel> getChannelsFromWorld(int world) {
        return worlds.get(world).getChannels();
    }

    public List<Channel> getAllChannels() {
        List<Channel> channelz = new ArrayList<>();
        for (World world : worlds) {
            for (Channel ch : world.getChannels()) {
                channelz.add(ch);
            }
        }
        return channelz;
    }

    public String getIP(int world, int channel) {
        return channels.get(world).get(channel);
    }
    
    private long getTimeLeftForNextHour() {
        Calendar nextHour = Calendar.getInstance();
        nextHour.add(Calendar.HOUR, 1);
        nextHour.set(Calendar.MINUTE, 0);
        nextHour.set(Calendar.SECOND, 0);
        
        return Math.max(0, nextHour.getTimeInMillis() - System.currentTimeMillis());
    }
    
    public Map<Integer, Integer> getCouponRates() {
        return couponRates;
    }
    
    private void loadCouponRates(Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement("SELECT couponid, rate FROM nxcoupons");
        ResultSet rs = ps.executeQuery();
        
        while(rs.next()) {
            int cid = rs.getInt("couponid");
            int rate = rs.getInt("rate");
            
            couponRates.put(cid, rate);
        }
        
        rs.close();
        ps.close();
    }
    
    public List<Integer> getActiveCoupons() {
        synchronized(activeCoupons) {
            return activeCoupons;
        }
    }
    
    public void commitActiveCoupons() {
        for(World world: getWorlds()) {
            for(MapleCharacter chr: world.getPlayerStorage().getAllCharacters()) {
                if(!chr.isLoggedin()) continue;

                chr.updateCouponRates();
            }
        }
    }
    
    public void toggleCoupon(Integer couponId) {
        if(ItemConstants.isRateCoupon(couponId)) {
            synchronized(activeCoupons) {
                if(activeCoupons.contains(couponId)) {
                    activeCoupons.remove(couponId);
                }
                else {
                    activeCoupons.add(couponId);
                }

                commitActiveCoupons();
            }
        }
    }
    
    public void updateActiveCoupons() throws SQLException {
        synchronized(activeCoupons) {
            activeCoupons.clear();
            Calendar c = Calendar.getInstance();

            int weekDay = c.get(Calendar.DAY_OF_WEEK);
            int hourDay = c.get(Calendar.HOUR_OF_DAY);

            Connection con = null;
            try {
                con = DatabaseConnection.getConnection();

                int weekdayMask = (1 << weekDay);
                PreparedStatement ps = con.prepareStatement("SELECT couponid FROM nxcoupons WHERE (activeday & ?) = ? AND starthour <= ? AND endhour > ?");
                ps.setInt(1, weekdayMask);
                ps.setInt(2, weekdayMask);
                ps.setInt(3, hourDay);
                ps.setInt(4, hourDay);

                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    activeCoupons.add(rs.getInt("couponid"));
                }

                rs.close();
                ps.close();

                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();

                try {
                    if(con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (SQLException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("world.ini"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Please start create_server.bat");
            System.exit(0);
        }

        System.out.println("MapleSolaxia v" + ServerConstants.VERSION + " starting up.\r\n");


        if(ServerConstants.SHUTDOWNHOOK)
            Runtime.getRuntime().addShutdownHook(new Thread(shutdown(false)));
        
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement("UPDATE accounts SET loggedin = 0");
            ps.executeUpdate();
            ps.close();
            ps = c.prepareStatement("UPDATE characters SET HasMerchant = 0");
            ps.executeUpdate();
            ps.close();
            
            loadCouponRates(c);
            updateActiveCoupons();
            
            c.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        
        TimerManager tMan = TimerManager.getInstance();
        tMan.start();
        tMan.register(tMan.purge(), ServerConstants.PURGING_INTERVAL);//Purging ftw...
        
        long timeLeft = getTimeLeftForNextHour();
        tMan.register(new CouponWorker(), ServerConstants.COUPON_INTERVAL, timeLeft);
        tMan.register(new RankingWorker(), ServerConstants.RANKING_INTERVAL, timeLeft);
        
        long timeToTake = System.currentTimeMillis();
        SkillFactory.loadAllSkills();
        System.out.println("Skills loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");

        timeToTake = System.currentTimeMillis();
        //MapleItemInformationProvider.getInstance().getAllItems(); //unused, rofl

        CashItemFactory.getSpecialCashItems();
        System.out.println("Items loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds");
        
	timeToTake = System.currentTimeMillis();
	MapleQuest.loadAllQuest();
	System.out.println("Quest loaded in " + ((System.currentTimeMillis() - timeToTake) / 1000.0) + " seconds\r\n");
		
        try {
            Integer worldCount = Math.min(ServerConstants.WORLD_NAMES.length, Integer.parseInt(p.getProperty("worlds")));
            
            for (int i = 0; i < worldCount; i++) {
                System.out.println("Starting world " + i);
                World world = new World(i,
                        Integer.parseInt(p.getProperty("flag" + i)),
                        p.getProperty("eventmessage" + i),
                        ServerConstants.EXP_RATE,
                        ServerConstants.DROP_RATE,
                        ServerConstants.MESO_RATE,
                        ServerConstants.BOSS_DROP_RATE);

                worldRecommendedList.add(new Pair<>(i, p.getProperty("whyamirecommended" + i)));
                worlds.add(world);
                channels.add(new LinkedHashMap<Integer, String>());
                for (int j = 0; j < Integer.parseInt(p.getProperty("channels" + i)); j++) {
                    int channelid = j + 1;
                    Channel channel = new Channel(i, channelid);
                    world.addChannel(channel);
                    channels.get(i).put(channelid, channel.getIP());
                }
                world.setServerMessage(p.getProperty("servermessage" + i));
                System.out.println("Finished loading world " + i + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();//For those who get errors
            System.out.println("Error in moople.ini, start CreateINI.bat to re-make the file.");
            System.exit(0);
        }

        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        acceptor.setHandler(new MapleServerHandler());
        try {
            acceptor.bind(new InetSocketAddress(8484));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("Listening on port 8484\r\n\r\n");

        System.out.println("Solaxia is now online.\r\n");
        online = true;
    }

    public void shutdown() {
    	try {
	        TimerManager.getInstance().stop();
	        acceptor.unbind();
    	} catch (NullPointerException e) {
    		FilePrinter.printError(FilePrinter.EXCEPTION_CAUGHT, e);
    	}
        System.out.println("Server offline.");
        System.exit(0);// BOEIEND :D
    }

    public static void main(String args[]) {
        Server.getInstance().run();
    }

    public Properties getSubnetInfo() {
        return subnetInfo;
    }

    public MapleAlliance getAlliance(int id) {
        synchronized (alliances) {
            if (alliances.containsKey(id)) {
                return alliances.get(id);
            }
            return null;
        }
    }

    public void addAlliance(int id, MapleAlliance alliance) {
        synchronized (alliances) {
            if (!alliances.containsKey(id)) {
                alliances.put(id, alliance);
            }
        }
    }

    public void disbandAlliance(int id) {
        synchronized (alliances) {
            MapleAlliance alliance = alliances.get(id);
            if (alliance != null) {
                for (Integer gid : alliance.getGuilds()) {
                    guilds.get(gid).setAllianceId(0);
                }
                alliances.remove(id);
            }
        }
    }

    public void allianceMessage(int id, final byte[] packet, int exception, int guildex) {
        MapleAlliance alliance = alliances.get(id);
        if (alliance != null) {
            for (Integer gid : alliance.getGuilds()) {
                if (guildex == gid) {
                    continue;
                }
                MapleGuild guild = guilds.get(gid);
                if (guild != null) {
                    guild.broadcast(packet, exception);
                }
            }
        }
    }

    public boolean addGuildtoAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.addGuild(guildId);
            guilds.get(guildId).setAllianceId(aId);
            return true;
        }
        return false;
    }

    public boolean removeGuildFromAlliance(int aId, int guildId) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.removeGuild(guildId);
            guilds.get(guildId).setAllianceId(0);
            return true;
        }
        return false;
    }

    public boolean setAllianceRanks(int aId, String[] ranks) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setRankTitle(ranks);
            return true;
        }
        return false;
    }

    public boolean setAllianceNotice(int aId, String notice) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.setNotice(notice);
            return true;
        }
        return false;
    }

    public boolean increaseAllianceCapacity(int aId, int inc) {
        MapleAlliance alliance = alliances.get(aId);
        if (alliance != null) {
            alliance.increaseCapacity(inc);
            return true;
        }
        return false;
    }

    public Set<Integer> getChannelServer(int world) {
        return new HashSet<>(channels.get(world).keySet());
    }

    public byte getHighestChannelId() {
        byte highest = 0;
        for (Iterator<Integer> it = channels.get(0).keySet().iterator(); it.hasNext();) {
            Integer channel = it.next();
            if (channel != null && channel.intValue() > highest) {
                highest = channel.byteValue();
            }
        }
        return highest;
    }

    public int createGuild(int leaderId, String name) {
        return MapleGuild.createGuild(leaderId, name);
    }
    
    public MapleGuild getGuildByName(String name) {
        synchronized (guilds) {
            for(MapleGuild mg: guilds.values()) {
                if(mg.getName().equalsIgnoreCase(name)) {
                    return mg;
                }
            }
            
            return null;
        }
    }
    
    public MapleGuild getGuild(int id) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }
            
            return null;
        }
    }

    public MapleGuild getGuild(int id, int world) {
            return getGuild(id, world, null);
    }
    
    public MapleGuild getGuild(int id, int world, MapleCharacter mc) {
        synchronized (guilds) {
            if (guilds.get(id) != null) {
                return guilds.get(id);
            }
            MapleGuild g = new MapleGuild(id, world);
            if (g.getId() == -1) {
                return null;
            }
            
            if(mc != null) {
                mc.setMGC(g.getMGC(mc.getId()));
                if(g.getMGC(mc.getId()) == null) System.out.println("null for " + mc.getName() + " when loading " + id);
                g.getMGC(mc.getId()).setCharacter(mc);
                g.setOnline(mc.getId(), true, mc.getClient().getChannel());
            }
            
            guilds.put(id, g);
            return g;
        }
    }

    public void clearGuilds() {//remake
        synchronized (guilds) {
            guilds.clear();
        }
        //for (List<Channel> world : worlds.values()) {
        //reloadGuildCharacters();
    }

    public void setGuildMemberOnline(MapleCharacter mc, boolean bOnline, int channel) {
        MapleGuild g = getGuild(mc.getGuildId(), mc.getWorld(), mc);
        g.setOnline(mc.getId(), bOnline, channel);
    }

    public int addGuildMember(MapleGuildCharacter mgc, MapleCharacter chr) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            return g.addGuildMember(mgc, chr);
        }
        return 0;
    }

    public boolean setGuildAllianceId(int gId, int aId) {
        MapleGuild guild = guilds.get(gId);
        if (guild != null) {
            guild.setAllianceId(aId);
            return true;
        }
        return false;
    }
    
    public void resetAllianceGuildPlayersRank(int gId) {
        guilds.get(gId).resetAllianceGuildPlayersRank();
    }

    public void leaveGuild(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.leaveGuild(mgc);
        }
    }

    public void guildChat(int gid, String name, int cid, String msg) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.guildChat(name, cid, msg);
        }
    }

    public void changeRank(int gid, int cid, int newRank) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRank(cid, newRank);
        }
    }

    public void expelMember(MapleGuildCharacter initiator, String name, int cid) {
        MapleGuild g = guilds.get(initiator.getGuildId());
        if (g != null) {
            g.expelMember(initiator, name, cid);
        }
    }

    public void setGuildNotice(int gid, String notice) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildNotice(notice);
        }
    }

    public void memberLevelJobUpdate(MapleGuildCharacter mgc) {
        MapleGuild g = guilds.get(mgc.getGuildId());
        if (g != null) {
            g.memberLevelJobUpdate(mgc);
        }
    }

    public void changeRankTitle(int gid, String[] ranks) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.changeRankTitle(ranks);
        }
    }

    public void setGuildEmblem(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.setGuildEmblem(bg, bgcolor, logo, logocolor);
        }
    }

    public void disbandGuild(int gid) {
        synchronized (guilds) {
            MapleGuild g = guilds.get(gid);
            g.disbandGuild();
            guilds.remove(gid);
        }
    }

    public boolean increaseGuildCapacity(int gid) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            return g.increaseCapacity();
        }
        return false;
    }

    public void gainGP(int gid, int amount) {
        MapleGuild g = guilds.get(gid);
        if (g != null) {
            g.gainGP(amount);
        }
    }
	
    public void guildMessage(int gid, byte[] packet) {
        guildMessage(gid, packet, -1);
    }
	
    public void guildMessage(int gid, byte[] packet, int exception) {
        MapleGuild g = guilds.get(gid);
        if(g != null) {
            g.broadcast(packet, exception);
        }
    }

    public PlayerBuffStorage getPlayerBuffStorage() {
        return buffStorage;
    }

    public void deleteGuildCharacter(MapleCharacter mc) {
        setGuildMemberOnline(mc, false, (byte) -1);
        if (mc.getMGC().getGuildRank() > 1) {
            leaveGuild(mc.getMGC());
        } else {
            disbandGuild(mc.getMGC().getGuildId());
        }
    }
    
    public void deleteGuildCharacter(MapleGuildCharacter mgc) {
        if(mgc.getCharacter() != null) setGuildMemberOnline(mgc.getCharacter(), false, (byte) -1);
        if (mgc.getGuildRank() > 1) {
            leaveGuild(mgc);
        } else {
            disbandGuild(mgc.getGuildId());
        }
    }

    public void reloadGuildCharacters(int world) {
        World worlda = getWorld(world);
        for (MapleCharacter mc : worlda.getPlayerStorage().getAllCharacters()) {
            if (mc.getGuildId() > 0) {
                setGuildMemberOnline(mc, true, worlda.getId());
                memberLevelJobUpdate(mc.getMGC());
            }
        }
        worlda.reloadGuildSummary();
    }

    public void broadcastMessage(final byte[] packet) {
        for (Channel ch : getChannelsFromWorld(0)) {
            ch.broadcastPacket(packet);
        }
    }

    public void broadcastGMMessage(final byte[] packet) {
        for (Channel ch : getChannelsFromWorld(0)) {
            ch.broadcastGMPacket(packet);
        }
    }
    
    public boolean isGmOnline() {
        for (Channel ch : getChannelsFromWorld(0)) {
        	for (MapleCharacter player : ch.getPlayerStorage().getAllCharacters()) {
        		if (player.isGM()){
        			return true;
        		}
        	}
        }
        return false;
    }
    
    public void changeFly(Integer accountid, boolean canFly) {
        if(canFly) {
            activeFly.add(accountid);
        } else {
            activeFly.remove(accountid);
        }
    }
    
    public boolean canFly(Integer accountid) {
        return activeFly.contains(accountid);
    }
    
    public World getWorld(int id) {
        return worlds.get(id);
    }

    public List<World> getWorlds() {
        return worlds;
    }

    public final Runnable shutdown(final boolean restart) {//no player should be online when trying to shutdown!
        return new Runnable() {
            @Override
            public void run() {
                shutdownLock.lock();
                
                try {
                    System.out.println((restart ? "Restarting" : "Shutting down") + " the server!\r\n");
                    if (getWorlds() == null) return;//already shutdown
                    for (World w : getWorlds()) {
                        w.shutdown();
                    }
                    /*for (World w : getWorlds()) {
                        while (w.getPlayerStorage().getAllCharacters().size() > 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                System.err.println("FUCK MY LIFE");
                            }
                        }
                    }
                    for (Channel ch : getAllChannels()) {
                        while (ch.getConnectedClients() > 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                System.err.println("FUCK MY LIFE");
                            }
                        }
                    }*/

                    TimerManager.getInstance().purge();
                    TimerManager.getInstance().stop();

                    for (Channel ch : getAllChannels()) {
                        while (!ch.finishedShutdown()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                ie.printStackTrace();
                                System.err.println("FUCK MY LIFE");
                            }
                        }
                    }
                    worlds.clear();
                    worlds = null;
                    channels.clear();
                    channels = null;
                    worldRecommendedList.clear();
                    worldRecommendedList = null;

                    System.out.println("Worlds + Channels are offline.");
                    acceptor.unbind();
                    acceptor = null;
                    if (!restart) {
                        System.exit(0);
                    } else {
                        System.out.println("\r\nRestarting the server....\r\n");
                        try {
                            instance.finalize();//FUU I CAN AND IT'S FREE
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                        instance = null;
                        System.gc();
                        getInstance().run();//DID I DO EVERYTHING?! D:
                    }
                } finally {
                    shutdownLock.unlock();
                }
            }
        };
    }
}
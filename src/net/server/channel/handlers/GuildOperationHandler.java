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
package net.server.channel.handlers;

import net.server.guild.MapleGuildResponse;
import net.server.guild.MapleGuild;
import constants.GameConstants;
import constants.ServerConstants;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import java.util.Iterator;
import tools.MaplePacketCreator;
import client.MapleCharacter;
import net.server.Server;
import net.server.guild.MapleAlliance;

public final class GuildOperationHandler extends AbstractMaplePacketHandler {
    private boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLowerCase(name.charAt(i)) && !Character.isUpperCase(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void respawnPlayer(MapleCharacter mc) {
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.removePlayerFromMap(mc.getId()), false);
        mc.getMap().broadcastMessage(mc, MaplePacketCreator.spawnPlayerMapObject(mc), false);
    }

    private class Invited {
        public String name;
        public int gid;
        public long expiration;

        public Invited(String n, int id) {
            name = n.toLowerCase();
            gid = id;
            expiration = currentServerTime() + 60 * 60 * 1000;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Invited)) {
                return false;
            }
            Invited oth = (Invited) other;
            return (gid == oth.gid && name.equals(oth));
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 83 * hash + this.gid;
            return hash;
        }
    }
    private java.util.List<Invited> invited = new java.util.LinkedList<Invited>();
    private long nextPruneTime = currentServerTime() + 20 * 60 * 1000;

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (currentServerTime() >= nextPruneTime) {
            Iterator<Invited> itr = invited.iterator();
            Invited inv;
            while (itr.hasNext()) {
                inv = itr.next();
                if (currentServerTime() >= inv.expiration) {
                    itr.remove();
                }
            }
            nextPruneTime = currentServerTime() + 20 * 60 * 1000;
        }
        MapleCharacter mc = c.getPlayer();
        byte type = slea.readByte();
        int allianceId = -1;
        switch (type) {
            case 0x00:
                //c.announce(MaplePacketCreator.showGuildInfo(mc));
                break;
            case 0x02:
                if (mc.getGuildId() > 0 || mc.getMapId() != 200000301) {
                    c.getPlayer().dropMessage(1, "You cannot create a new Guild while in one.");
                    return;
                }
                if (mc.getMeso() < ServerConstants.CREATE_GUILD_COST) {
                    c.getPlayer().dropMessage(1, "You do not have " + GameConstants.numberWithCommas(ServerConstants.CREATE_GUILD_COST) + " mesos to create a Guild.");
                    return;
                }
                String guildName = slea.readMapleAsciiString();
                if (!isGuildNameAcceptable(guildName)) {
                    c.getPlayer().dropMessage(1, "The Guild name you have chosen is not accepted.");
                    return;
                }
                
                int gid = Server.getInstance().createGuild(mc.getId(), guildName);
                if (gid == 0) {
                    c.announce(MaplePacketCreator.genericGuildMessage((byte) 0x1c));
                    return;
                }
                mc.gainMeso(-ServerConstants.CREATE_GUILD_COST, true, false, true);
                
                mc.getMGC().setGuildId(gid);
                Server.getInstance().getGuild(mc.getGuildId(), mc.getWorld(), mc);  // initialize guild structure
                Server.getInstance().changeRank(gid, mc.getId(), 1);
                
                c.announce(MaplePacketCreator.showGuildInfo(mc));
                
                c.getPlayer().dropMessage(1, "You have successfully created a Guild.");
                respawnPlayer(mc);
                break;
            case 0x05:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    return;
                }
                String name = slea.readMapleAsciiString();
                MapleGuildResponse mgr = MapleGuild.sendInvite(c, name);
                if (mgr != null) {
                    c.announce(mgr.getPacket());
                } else {
                    Invited inv = new Invited(name, mc.getGuildId());
                    if (!invited.contains(inv)) {
                        invited.add(inv);
                    }
                }
                break;
            case 0x06:
                if (mc.getGuildId() > 0) {
                    System.out.println("[hax] " + mc.getName() + " attempted to join a guild when s/he is already in one.");
                    return;
                }
                gid = slea.readInt();
                int cid = slea.readInt();
                if (cid != mc.getId()) {
                    System.out.println("[hax] " + mc.getName() + " attempted to join a guild with a different character id.");
                    return;
                }
                name = mc.getName().toLowerCase();
                Iterator<Invited> itr = invited.iterator();
                boolean bOnList = false;
                while (itr.hasNext()) {
                    Invited inv = itr.next();
                    if (gid == inv.gid && name.equals(inv.name)) {
                        bOnList = true;
                        itr.remove();
                        break;
                    }
                }
                if (!bOnList) {
                    System.out.println("[hax] " + mc.getName() + " is trying to join a guild that never invited him/her (or that the invitation has expired)");
                    return;
                }
                mc.getMGC().setGuildId(gid); // joins the guild
                mc.getMGC().setGuildRank(5); // start at lowest rank
                mc.getMGC().setAllianceRank(5);
                
                int s = Server.getInstance().addGuildMember(mc.getMGC(), mc);
                if (s == 0) {
                    c.getPlayer().dropMessage(1, "The Guild you are trying to join is already full.");
                    mc.getMGC().setGuildId(0);
                    return;
                }
                
                c.announce(MaplePacketCreator.showGuildInfo(mc));
                
                allianceId = mc.getGuild().getAllianceId();
                if(allianceId > 0) Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                
                mc.saveGuildStatus(); // update database
                respawnPlayer(mc);
                break;
            case 0x07:
                allianceId = mc.getGuild().getAllianceId();
                
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if (cid != mc.getId() || !name.equals(mc.getName()) || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " tried to quit guild under the name \"" + name + "\" and current guild id of " + mc.getGuildId() + ".");
                    return;
                }
                c.announce(MaplePacketCreator.updateGP(mc.getGuildId(), 0));
                Server.getInstance().leaveGuild(mc.getMGC());
                
                c.announce(MaplePacketCreator.showGuildInfo(null));
                if(allianceId > 0) Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                
                mc.getMGC().setGuildId(0);
                mc.saveGuildStatus();
                respawnPlayer(mc);
                break;
            case 0x08:
                allianceId = mc.getGuild().getAllianceId();
                
                cid = slea.readInt();
                name = slea.readMapleAsciiString();
                if (mc.getGuildRank() > 2 || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " is trying to expel without rank 1 or 2.");
                    return;
                }
                
                Server.getInstance().expelMember(mc.getMGC(), name, cid);
                if(allianceId > 0) Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                break;
            case 0x0d:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1) {
                    System.out.println("[hax] " + mc.getName() + " tried to change guild rank titles when s/he does not have permission.");
                    return;
                }
                String ranks[] = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = slea.readMapleAsciiString();
                }
                
                Server.getInstance().changeRankTitle(mc.getGuildId(), ranks);
                break;
            case 0x0e:
                cid = slea.readInt();
                byte newRank = slea.readByte();
                if (mc.getGuildRank() > 2 || (newRank <= 2 && mc.getGuildRank() != 1) || mc.getGuildId() <= 0) {
                    System.out.println("[hax] " + mc.getName() + " is trying to change rank outside of his/her permissions.");
                    return;
                }
                if (newRank <= 1 || newRank > 5) {
                    return;
                }
                Server.getInstance().changeRank(mc.getGuildId(), cid, newRank);
                break;
            case 0x0f:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1 || mc.getMapId() != 200000301) {
                    System.out.println("[hax] " + mc.getName() + " tried to change guild emblem without being the guild leader.");
                    return;
                }
                if (mc.getMeso() < ServerConstants.CHANGE_EMBLEM_COST) {
                    c.announce(MaplePacketCreator.serverNotice(1, "You do not have " + GameConstants.numberWithCommas(ServerConstants.CHANGE_EMBLEM_COST) + " mesos to change the Guild emblem."));
                    return;
                }
                short bg = slea.readShort();
                byte bgcolor = slea.readByte();
                short logo = slea.readShort();
                byte logocolor = slea.readByte();
                Server.getInstance().setGuildEmblem(mc.getGuildId(), bg, bgcolor, logo, logocolor);
                
                if (mc.getGuild() != null && mc.getGuild().getAllianceId() > 0) {
                    MapleAlliance alliance = mc.getAlliance();
                    Server.getInstance().allianceMessage(alliance.getId(), MaplePacketCreator.getGuildAlliances(alliance, c.getWorld()), -1, -1);
                }
                
                mc.gainMeso(-ServerConstants.CHANGE_EMBLEM_COST, true, false, true);
                respawnPlayer(mc);
                break;
            case 0x10:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    if(mc.getGuildId() <= 0) System.out.println("[hax] " + mc.getName() + " tried to change guild notice while not in a guild.");
                    return;
                }
                String notice = slea.readMapleAsciiString();
                if (notice.length() > 100) {
                    return;
                }
                Server.getInstance().setGuildNotice(mc.getGuildId(), notice);
                break;
            default:
                System.out.println("Unhandled GUILD_OPERATION packet: \n" + slea.toString());
        }
    }
}

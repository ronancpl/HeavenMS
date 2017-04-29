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
package scripting.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptException;

import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.TimerManager;
import server.expeditions.MapleExpedition;
import server.maps.MapleMap;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;

import client.MapleCharacter;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Array;

/**
 *
 * @author Matze
 */
public class EventManager {
    private Invocable iv;
    private Channel cserv;
    private Map<String, EventInstanceManager> instances = new HashMap<String, EventInstanceManager>();
    private Properties props = new Properties();
    private String name;
    private ScheduledFuture<?> schedule = null;

    public EventManager(Channel cserv, Invocable iv, String name) {
        this.iv = iv;
        this.cserv = cserv;
        this.name = name;
    }

    public void cancel() {
        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public void schedule(String methodName, long delay) {
        schedule(methodName, null, delay);
    }

    public void schedule(final String methodName, final EventInstanceManager eim, long delay) {
        schedule = TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (ScriptException | NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
    }

    public void cancelSchedule() {
        if(schedule != null)
            schedule.cancel(false);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return TimerManager.getInstance().scheduleAtTimestamp(new Runnable() {
            public void run() {
                try {
                    iv.invokeFunction(methodName, (Object) null);
                } catch (ScriptException | NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, timestamp);
    }

    public Channel getChannelServer() {
        return cserv;
    }

    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name);
        instances.put(name, ret);
        return ret;
    }

    public void disposeInstance(String name) {
        instances.remove(name);
    }

    public Invocable getIv() {
        return iv;
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
    
    public void setProperty(String key, int value) {
        props.setProperty(key, value + "");
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    public String getName() {
        return name;
    }

    //Expedition method: starts an expedition
    public void startInstance(MapleExpedition exped) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerExpedition(exped);
            exped.start();
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Regular method: player 
    public void startInstance(MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerPlayer(chr);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    //PQ method: starts a PQ
    public void startInstance(MapleParty party, MapleMap map) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", (Object) null));
            eim.registerParty(party, map);
            party.setEligibleMembers(null);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //PQ method: starts a PQ with a difficulty level, requires function setup(difficulty, leaderid) instead of setup()
    public void startInstance(MapleParty party, MapleMap map, int difficulty) {
        try {
            EventInstanceManager eim = (EventInstanceManager) (iv.invokeFunction("setup", difficulty, party.getLeader().getId()));
            eim.registerParty(party, map);
            party.setEligibleMembers(null);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //non-PQ method for starting instance
    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            iv.invokeFunction("setup", eim);
            eim.setProperty("leader", leader);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<MaplePartyCharacter> getEligibleParty(MapleParty party) {
        if (party == null) {
            return(new ArrayList<>());
        }
        try {
            Object p = iv.invokeFunction("getEligibleParty", party.getPartyMembers());
            
            if(p != null) {
                List<MaplePartyCharacter> lmpc = new ArrayList<>((List<MaplePartyCharacter>) p);
                party.setEligibleMembers(lmpc);
                return lmpc;
            }
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        return(new ArrayList<>());
    }
    
    public void clearPQ(EventInstanceManager eim, MapleMap toMap) {
        try {
            iv.invokeFunction("clearPQ", eim, toMap);
        } catch (ScriptException | NoSuchMethodException ex) {
            Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public MapleMonster getMonster(int mid) {
        return(MapleLifeFactory.getMonster(mid));
    }
}

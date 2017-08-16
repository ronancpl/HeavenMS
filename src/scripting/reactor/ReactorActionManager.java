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
package scripting.reactor;

import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptException;
import scripting.AbstractPlayerInteraction;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import server.MapleItemInformationProvider;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapMonitor;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;

/**
 * @author Lerk
 * @author Ronan
 */
public class ReactorActionManager extends AbstractPlayerInteraction {
    private MapleReactor reactor;
    private MapleClient client;
    private Invocable iv;

    public ReactorActionManager(MapleClient c, MapleReactor reactor, Invocable iv) {
        super(c);
        this.reactor = reactor;
        this.client = c;
        this.iv = iv;
    }

    public void hitReactor() {
        reactor.hitReactor(client);
    }
    
    public void destroyNpc(int npcId) {
        reactor.getMap().destroyNPC(npcId);
    }

    public void dropItems() {
        dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }
    
    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        dropItems((int)reactor.getPosition().getX(), (int)reactor.getPosition().getY(), meso, mesoChance, minMeso, maxMeso, minItems);
    }

    public void dropItems(int posX, int posY, boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<ReactorDropEntry> chances = getDropChances();
        List<ReactorDropEntry> items = new LinkedList<>();
        int numItems = 0;
        if (meso && Math.random() < (1 / (double) mesoChance)) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
        }
        Iterator<ReactorDropEntry> iter = chances.iterator();
        while (iter.hasNext()) {
            ReactorDropEntry d = iter.next();
            if (Math.random() < (c.getPlayer().getDropRate() / (double) d.chance)) {
                numItems++;
                items.add(d);
            }
        }
        while (items.size() < minItems) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
            numItems++;
        }
        java.util.Collections.shuffle(items);
        
        final Point dropPos = new Point(posX, posY);
        dropPos.x -= (12 * numItems);
        
        for (ReactorDropEntry d : items) {
            if (d.itemId == 0) {
                int range = maxMeso - minMeso;
                int displayDrop = (int) (Math.random() * range) + minMeso;
                int mesoDrop = (displayDrop * client.getWorldServer().getMesoRate());
                reactor.getMap().spawnMesoDrop(mesoDrop, reactor.getMap().calcDropPos(dropPos, reactor.getPosition()), reactor, client.getPlayer(), false, (byte) 2);
            } else {
                Item drop;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (ii.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(d.itemId, (short) 0, (short) 1);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                }
                
                reactor.getMap().dropFromReactor(getPlayer(), reactor, drop, dropPos, (short)d.questid);
            }
            dropPos.x += 25;
        }
    }

    private List<ReactorDropEntry> getDropChances() {
        return ReactorScriptManager.getInstance().getDrops(reactor.getId());
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    public void createMapMonitor(int mapId, String portal) {
        new MapMonitor(client.getChannelServer().getMapFactory().getMap(mapId), portal);
    }

    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    public void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            reactor.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public Point getPosition() {
        Point pos = reactor.getPosition();
        pos.y -= 10;
        return pos;
    }
    
    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }
    
    public void spawnNpc(int npcId, Point pos) {
        spawnNpc(npcId, pos, reactor.getMap());
    }
    
    public void hitMonsterWithReactor(int id, int hitsToKill) {  // until someone comes with a better solution, why not?
        MapleMonster mm = reactor.getMap().getMonsterById(id);
        if(mm != null) {
            int damage = (int)Math.ceil(mm.getMaxHp() / hitsToKill);
            reactor.getMap().damageMonster(this.getPlayer(), mm, damage);
        }
    }

    public MapleReactor getReactor() {
        return reactor;
    }

    public void spawnFakeMonster(int id) {
        reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), getPosition());
    }
    
    public ScheduledFuture<?> schedule(String methodName, long delay) {
        return schedule(methodName, null, delay);
    }

    public ScheduledFuture<?> schedule(final String methodName, final EventInstanceManager eim, long delay) {
        return TimerManager.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                } catch (ScriptException | NoSuchMethodException ex) {
                    Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }, delay);
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
}
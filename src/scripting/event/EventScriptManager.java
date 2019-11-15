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

import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import net.server.channel.Channel;
import scripting.AbstractScriptManager;

/**
 *
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager {

    private class EventEntry {

        public EventEntry(NashornScriptEngine iv, EventManager em) {
            this.iv = iv;
            this.em = em;
        }
        public NashornScriptEngine iv;
        public EventManager em;
    }
    
    private static EventEntry fallback;
    private Map<String, EventEntry> events = new ConcurrentHashMap<>();
    private boolean active = false;
    
    public EventScriptManager(Channel cserv, String[] scripts) {
        super();
        for (String script : scripts) {
            if (!script.equals("")) {
                NashornScriptEngine iv = getScriptEngine("event/" + script + ".js");
                events.put(script, new EventEntry(iv, new EventManager(cserv, iv, script)));
            }
        }
        
        init();
        fallback = events.remove("0_EXAMPLE");
    }

    public EventManager getEventManager(String event) {
        EventEntry entry = events.get(event);
        if (entry == null) {
            return fallback.em;
        }
        return entry.em;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public final void init() {
        for (EventEntry entry : events.values()) {
            try {
                entry.iv.put("em", entry.em);
                entry.iv.invokeFunction("init", (Object) null);
            } catch (Exception ex) {
                Logger.getLogger(EventScriptManager.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error on script: " + entry.em.getName());
            }
        }
        
        active = events.size() > 1; // bootup loads only 1 script
    }

    private void reloadScripts() {
        Set<Entry<String, EventEntry>> eventEntries = new HashSet<>(events.entrySet());
        if (eventEntries.isEmpty()) {
            return;
        }

        Channel cserv = eventEntries.iterator().next().getValue().em.getChannelServer();
        for (Entry<String, EventEntry> entry : eventEntries) {
            String script = entry.getKey();
            NashornScriptEngine iv = getScriptEngine("event/" + script + ".js");
            events.put(script, new EventEntry(iv, new EventManager(cserv, iv, script)));
        }
    }

    public void reload() {
        cancel();
        reloadScripts();
        init();
    }

    public void cancel() {
        active = false;
        for (EventEntry entry : events.values()) {
            entry.em.cancel();
        }
    }
    
    public void dispose() {
        if (events.isEmpty()) {
            return;
        }
        
        Set<EventEntry> eventEntries = new HashSet<>(events.values());
        events.clear();
        
        active = false;
        for (EventEntry entry : eventEntries) {
            entry.em.cancel();
        }
    }
}

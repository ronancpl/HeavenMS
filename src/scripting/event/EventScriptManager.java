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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.server.channel.Channel;
import scripting.AbstractScriptManager;

/**
 *
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager {
    
    private class EventEntry {
        public EventEntry(Invocable iv, EventManager em) {
            this.iv = iv;
            this.em = em;
        }
        public Invocable iv;
        public EventManager em;
    }
    private Map<String, EventEntry> events = new LinkedHashMap<>();

    public EventScriptManager(Channel cserv, String[] scripts) {
        super();
        for (String script : scripts) {
            if (!script.equals("")) {
                Invocable iv = getInvocable("event/" + script + ".js", null);
                events.put(script, new EventEntry(iv, new EventManager(cserv, iv, script)));
            }
        }
    }

    public EventManager getEventManager(String event) {
        EventEntry entry = events.get(event);
        if (entry == null) {
            return null;
        }
        return entry.em;
    }

    public void init() {
        for (EventEntry entry : events.values()) {
            try {
                ((ScriptEngine) entry.iv).put("em", entry.em);
                entry.iv.invokeFunction("init", (Object) null);
            } catch (Exception ex) {
                Logger.getLogger(EventScriptManager.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Error on script: " + entry.em.getName());
            }
        }
    }
    
    private void reloadScripts() {
        if (events.isEmpty()) return;
        
        Channel cserv = events.values().iterator().next().em.getChannelServer();
        for (Entry<String, EventEntry> entry : events.entrySet()) {
            String script = entry.getKey();
            Invocable iv = getInvocable("event/" + script + ".js", null);
            events.put(script, new EventEntry(iv, new EventManager(cserv, iv, script)));
        }
    }
    
    public void reload() {
    	cancel();
        reloadScripts();
    	init();
    }

    public void cancel() {
        for (EventEntry entry : events.values()) {
            entry.em.cancel();
        }
    }
}
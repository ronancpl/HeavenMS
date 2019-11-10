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
package scripting.map;

import client.MapleCharacter;
import client.MapleClient;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import scripting.AbstractScriptManager;
import tools.FilePrinter;

public class MapScriptManager extends AbstractScriptManager {

    private static MapScriptManager instance = new MapScriptManager();
    
    public static MapScriptManager getInstance() {
        return instance;
    }
    
    private Map<String, NashornScriptEngine> scripts = new HashMap<>();
    private ScriptEngineFactory sef;

    private MapScriptManager() {
        ScriptEngineManager sem = new ScriptEngineManager();
        sef = sem.getEngineByName("javascript").getFactory();
    }

    public void reloadScripts() {
        scripts.clear();
    }

    public boolean runMapScript(MapleClient c, String mapScriptPath, boolean firstUser) {
        if (firstUser) {
            MapleCharacter chr = c.getPlayer();
            int mapid = chr.getMapId();
            if (chr.hasEntered(mapScriptPath, mapid)) {
                return false;
            } else {
                chr.enteredScript(mapScriptPath, mapid);
            }
        }
        
        NashornScriptEngine iv = scripts.get(mapScriptPath);
        if (iv != null) {
            try {
                iv.invokeFunction("start", new MapScriptMethods(c));
                return true;
            } catch (final ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        
        try {
            iv = getScriptEngine("map/" + mapScriptPath + ".js");
            if (iv == null) {
                return false;
            }
            
            scripts.put(mapScriptPath, iv);
            iv.invokeFunction("start", new MapScriptMethods(c));
            return true;
        } catch (final UndeclaredThrowableException | ScriptException ute) {
            FilePrinter.printError(FilePrinter.MAP_SCRIPT + mapScriptPath + ".txt", ute);
        } catch (final Exception e) {
            FilePrinter.printError(FilePrinter.MAP_SCRIPT + mapScriptPath + ".txt", e);
        }
        
        return false;
    }
}
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
package scripting;

import client.MapleClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import constants.ServerConstants;
import tools.FilePrinter;

/**
 *
 * @author Matze
 */
public abstract class AbstractScriptManager {

    protected ScriptEngine engine;
    private ScriptEngineManager sem;

    protected AbstractScriptManager() {
        sem = new ScriptEngineManager();
    }

    protected Invocable getInvocable(String path, MapleClient c) {
        path = "scripts/" + path;
        engine = null;
        if (c != null) {
            try {
                engine = c.getScriptEngine(path);
            } catch (NullPointerException npe) {
                c = null;   // player disconnected
            }
        }
        if (engine == null) {
            File scriptFile = new File(path);
            if (!scriptFile.exists()) {
                return null;
            }
            engine = sem.getEngineByName("javascript");
            if (c != null) {
                c.setScriptEngine(path, engine);
            }
            try (FileReader fr = new FileReader(scriptFile)) {
            	if (ServerConstants.JAVA_8){
            		engine.eval("load('nashorn:mozilla_compat.js');" + System.lineSeparator());
            	}
                engine.eval(fr);
            } catch (final ScriptException | IOException t) {
                FilePrinter.printError(FilePrinter.INVOCABLE + path.substring(12, path.length()), t, path);
                return null;
            }
        }

        return (Invocable) engine;
    }

    protected void resetContext(String path, MapleClient c) {
        c.removeScriptEngine("scripts/" + path);
    }
}

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
package scripting.quest;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import scripting.AbstractScriptManager;
import server.quest.MapleQuest;
import tools.FilePrinter;
import constants.game.GameConstants;
import client.MapleClient;
import client.MapleQuestStatus;

/**
 *
 * @author RMZero213
 */
public class QuestScriptManager extends AbstractScriptManager {
    
        private static QuestScriptManager instance = new QuestScriptManager();

	public static QuestScriptManager getInstance() {
		return instance;
	}
    
	private Map<MapleClient, QuestActionManager> qms = new HashMap<>();
	private Map<MapleClient, NashornScriptEngine> scripts = new HashMap<>();
        
        private NashornScriptEngine getQuestScriptEngine(MapleClient c, short questid) {
                NashornScriptEngine iv = getScriptEngine("quest/" + questid + ".js", c);
                if (iv == null && GameConstants.isMedalQuest(questid)) {
                        iv = getScriptEngine("quest/medalQuest.js", c);   // start generic medal quest
                }

                return iv;
        }
        
	public void start(MapleClient c, short questid, int npc) {
                MapleQuest quest = MapleQuest.getInstance(questid);
                try {
                        QuestActionManager qm = new QuestActionManager(c, questid, npc, true);
                        if (qms.containsKey(c)) {
                                return;
                        }
                        if(c.canClickNPC()) {
                                qms.put(c, qm);
                                
                                if (!quest.hasScriptRequirement(false)) {   // lack of scripted quest checks found thanks to Mali, Resinate
                                        qm.dispose();
                                        return;
                                }
                                
                                NashornScriptEngine iv = getQuestScriptEngine(c, questid);
                                if (iv == null) {
                                        FilePrinter.printError(FilePrinter.QUEST_UNCODED, "START Quest " + questid + " is uncoded.");
                                        qm.dispose();
                                        return;
                                }
                                
                                iv.put("qm", qm);
                                scripts.put(c, iv);
                                c.setClickedNPC();
                                iv.invokeFunction("start", (byte) 1, (byte) 0, 0);
                        }
                } catch (final UndeclaredThrowableException ute) {
                        FilePrinter.printError(FilePrinter.QUEST + questid + ".txt", ute);
                        dispose(c);
                } catch (final Throwable t) {
                        FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", t);
                        dispose(c);
                }
	}

	public void start(MapleClient c, byte mode, byte type, int selection) {
		NashornScriptEngine iv = scripts.get(c);
		if (iv != null) {
			try {
				c.setClickedNPC();
				iv.invokeFunction("start", mode, type, selection);
			} catch (final UndeclaredThrowableException ute) {
				FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", ute);
				dispose(c);
			} catch (final Throwable t) {
				FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", t);
				dispose(c);
			}
		}
	}
        
	public void end(MapleClient c, short questid, int npc) {
		MapleQuest quest = MapleQuest.getInstance(questid);
		if (!c.getPlayer().getQuest(quest).getStatus().equals(MapleQuestStatus.Status.STARTED) || !c.getPlayer().getMap().containsNPC(npc)) {
			dispose(c);
			return;
		}
		try {
			QuestActionManager qm = new QuestActionManager(c, questid, npc, false);
			if (qms.containsKey(c)) {
				return;
			}
			if(c.canClickNPC()){
				qms.put(c, qm);
				
                                if (!quest.hasScriptRequirement(true)) {
                                        qm.dispose();
                                        return;
                                }
                                
                                NashornScriptEngine iv = getQuestScriptEngine(c, questid);
                                if (iv == null) {
                                        FilePrinter.printError(FilePrinter.QUEST_UNCODED, "END Quest " + questid + " is uncoded.");
                                        qm.dispose();
                                        return;
                                }
				
				iv.put("qm", qm);
				scripts.put(c, iv);
				c.setClickedNPC();
				iv.invokeFunction("end", (byte) 1, (byte) 0, 0);
			}
		} catch (final UndeclaredThrowableException ute) {
			FilePrinter.printError(FilePrinter.QUEST + questid + ".txt", ute);
			dispose(c);
		} catch (final Throwable t) {
			FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", t);
			dispose(c);
		}
	}

	public void end(MapleClient c, byte mode, byte type, int selection) {
		NashornScriptEngine iv = scripts.get(c);
		if (iv != null) {
			try {
				c.setClickedNPC();
				iv.invokeFunction("end", mode, type, selection);
			} catch (final UndeclaredThrowableException ute) {
				FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", ute);
				dispose(c);
			} catch (final Throwable t) {
				FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", t);
				dispose(c);
			}
		}
	}

        public void raiseOpen(MapleClient c, short questid, int npc) {
                try {
                        QuestActionManager qm = new QuestActionManager(c, questid, npc, true);
                        if (qms.containsKey(c)) {
                                return;
                        }
                        if(c.canClickNPC()) {
                                qms.put(c, qm);
                                
                                NashornScriptEngine iv = getQuestScriptEngine(c, questid);
                                if (iv == null) {
                                        //FilePrinter.printError(FilePrinter.QUEST_UNCODED, "RAISE Quest " + questid + " is uncoded.");
                                        qm.dispose();
                                        return;
                                }
                                
                                iv.put("qm", qm);
                                scripts.put(c, iv);
                                c.setClickedNPC();
                                iv.invokeFunction("raiseOpen");
                        }
                } catch (final UndeclaredThrowableException ute) {
                        FilePrinter.printError(FilePrinter.QUEST + questid + ".txt", ute);
                        dispose(c);
                } catch (final Throwable t) {
                        FilePrinter.printError(FilePrinter.QUEST + getQM(c).getQuest() + ".txt", t);
                        dispose(c);
                }
	}

	public void dispose(QuestActionManager qm, MapleClient c) {
		qms.remove(c);
		scripts.remove(c);
                c.getPlayer().setNpcCooldown(System.currentTimeMillis());
		resetContext("quest/" + qm.getQuest() + ".js", c);
                c.getPlayer().flushDelayedUpdateQuests();
	}

	public void dispose(MapleClient c) {
		QuestActionManager qm = qms.get(c);
		if (qm != null) {
			dispose(qm, c);
		}
	}

	public QuestActionManager getQM(MapleClient c) {
		return qms.get(c);
	}
        
        public void reloadQuestScripts() {
                scripts.clear();
                qms.clear();
        }
}

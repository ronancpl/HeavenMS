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
/* Ronan
	Hidden Street - Developers' Headquarters (777777777)
	HeavenMS developer info.
 */

importPackage(Packages.tools);

var status;

var anthemSong = "Field/anthem/brazil";     // sound src: https://c7.rbxcdn.com/f91060652a6e9fbfbf92cb1418435448
var ambientSong = "Bgm04/Shinin'Harbor";

var feature_tree = [];
var feature_cursor;

var tabs = ["PQs", "Skills", "Quests", "Player Social Network", "Cash & Items", "Monsters, Maps & Reactors", "PQ potentials", "Player potentials", "Server potentials", "Admin/GM commands", "Localhost edits", "Project"];

function addFeature(feature) {
        feature_cursor.push(feature);
}

function writeFeatureTab_PQs() {
        addFeature("HPQ/KPQ/LPQ/LMPQ/OPQ/APQ/EllinPQ/PiratePQ.");
        addFeature("MagatiaPQ/HorntailPQ/TreasurePQ/ElnathPQ.");
        addFeature("CWKPQ as Expedition-based event.");
        addFeature("Scarga/Horntail/Showa/Balrog/Zakum/Pinkbean.");
        addFeature("GuildPQ & queue with multi-lobby systems available.");
        addFeature("Brand-new PQs: BossRushPQ, CafePQ.");
        addFeature("Mu Lung Dojo.");
        addFeature("Capt. Latanica with party fighting the boss.");
}

function writeFeatureTab_Skills() {
        addFeature("Reviewed many skills, such as Steal and M. Door.");
        addFeature("Maker skill features properly developed.");
        addFeature("Chair Mastery - map chair boosts HP/MP rec.");
}

function writeFeatureTab_Quests() {
        addFeature("Doll house quest functional.");
        addFeature("Quests are now rewarding items properly.");
        addFeature("Selection of rewards works properly.");
        addFeature("Loads of quests have been patched.");
        addFeature("Aran questline has been reviewed.");
        addFeature("Reviewed several 4th job skill questlines.");
        addFeature("Rewarding system now looks up for item stacking.");
        addFeature("3rd job quiz with all 40-question pool available.");
}

function writeFeatureTab_PlayerSocialNetwork() {
        addFeature("Guild and Alliance system fully functional.");
        addFeature("Party for novices-only.");
        addFeature("Thoroughly reviewed P. Shops and H. Merchants.");
        addFeature("Transactions on Merchs instantly announced to owner.");
        addFeature("Game minirooms with semi-functional pw system.");
        addFeature("Proper item pickup cooldown on non-owned items.");
        addFeature("Improved ranking system, with daily movement.");
        addFeature("Automated support for Player NPCs and Hall of Fame.");
        addFeature("Engagement & Wedding system.");
}

function writeFeatureTab_CashItems() {
        addFeature("EXP/DROP/Cosmetic Coupons.");
        addFeature("EXP/DROP Coupon as buff effect during active time.");
        addFeature("Great deal of cash items functional.");
        addFeature("New town scroll: antibanish. Counters boss banishes.");
        addFeature("Inventory system checks for free slot & stack space.");
        addFeature("Storage with 'Arrange Items' feature functional.");
        addFeature("Further improved Karma scissors.");
        addFeature("Scroll for Spikes on Shoes.");
        addFeature("Scroll for Cold Protection.");
        addFeature("Vega's spell.");
        addFeature("Owl of Minerva.");
        addFeature("Pet item ignore.");
        addFeature("New Year's card.");
        addFeature("Kite.");
        addFeature("Cash Shop surprise.");
        addFeature("Maple Life.");
}

function writeFeatureTab_MonstersMapsReactors() {
        addFeature("Every monsterbook card is now droppable.");
        addFeature("Added meso drop data for many missing mobs.");
        addFeature("Monsterbook displays updated drop data info.");
        addFeature("Every skill/mastery book is now obtainable.");
        addFeature("Mobs now can drop more than one of the same equip.");
        addFeature("Implemented Zombify disease status.");
        addFeature("Added Boss HP Bar for dozens of bosses.");
        addFeature("Game will favor showing the targeted boss HPbar.");
        addFeature("Dmg overtime on maps and neutralizers functional.");
        addFeature("Boats, elevator and other travel mechanics functional.");
        addFeature("C. Balrog's boat approaching visual effect functional.");
        addFeature("PQs, Taxis and events warps players to random SPs.");
        addFeature("PQ boxes sprays items when opened, GMS-like.");
        addFeature("Reactors pick items up smartly from the field.");
        addFeature("Reviewed Masteria, W. Tour, N. Desert and Neo City.");
        addFeature("Giant Cake boss drops s. bags and Maple items.");
}

function writeFeatureTab_PQpotentials() {
        addFeature("Lobby system: Same channel, multiple PQ instances.");
        addFeature("Exped system: Many parties can join a same instance.");
        addFeature("Guild queue: guild registration for the GPQ.");
        addFeature("EIM Pool system: optimized instance loadouts.");
}

function writeFeatureTab_Playerpotentials() {
        addFeature("Adventurer Mount quests functional.");
        addFeature("All Equipment levels up.");
        addFeature("Player level rates.");
        addFeature("Gain fame by quests.");
        addFeature("Pet evolutions functional (not GMS-like).");
}

function writeFeatureTab_Serverpotentials() {
        addFeature("Multi-worlds.");
        addFeature("Inventory auto-gather and auto-sorting feature.");
        addFeature("Enhanced auto-pot system: smart pet potion handle.");
        addFeature("Enhanced buff system: best buffs effects takes place.");
        addFeature("Enhanced AP auto-assigner: focus on eqp demands.");
        addFeature("NPC crafters won't take items freely anymore.");
        addFeature("Duey: pkg rcvd popup and many delivery mechanics.");
        addFeature("Pet pickup gives preference to player attacks.");
        addFeature("Channel capacity bar and worlds with capacity check.");
        addFeature("Diseases visible for others, even after changing maps.");
        addFeature("Poison damage value visible for other players.");
        addFeature("M. book announcer displays info based on demand.");
        addFeature("Custom jail system.");
        addFeature("Custom buyback system, uses mesos / NX, via MTS.");
        addFeature("Delete Character.");
        addFeature("Autosaver (periodically saves player's data on DB).");
        addFeature("Fixed and randomized HP/MP growth rate available.");
        addFeature("Prevented 'NPC gone after some uptime' issue.");
        addFeature("AP assigning available for novices level 10 or below.");
        addFeature("Automatic account registration - thanks shavit!");
}

function writeFeatureTab_AdminGMcommands() {
        addFeature("Server commands layered by GM levels.");
        addFeature("Spawn Zakum/Horntail/Pinkbean.");
        addFeature("Several new commands.");
}

function writeFeatureTab_Localhostedits() {
        addFeature("Removed the 'n' NPC dialog issue.");
        addFeature("Removed caps for MATK, WMDEF, ACC and AVOID.");
        addFeature("Removed MTS block, buyback available anywhere.");
        addFeature("Removed party blocks for novices under level 10.");
        addFeature("Set a much more higher cap for SPEED.");
        addFeature("Removed AP usage block for novices.");
        addFeature("Removed attack gem block on non-weapons w/ Maker.");
        addFeature("Removed AP excess popup - thanks kevintjuh93!");
        addFeature("Removed 'GMs can't attack' - thanks kevintjuh93!");
        addFeature("Removed 'Gained a level!' - thanks PrinceReborn!");
}

function writeFeatureTab_Project() {
        addFeature("Organized project code.");
        addFeature("Highly updated drop data.");
        addFeature("Highly configurable server.");
        addFeature("Fixed/added many missing packet opcodes.");
        addFeature("Uncovered many opcodes throughout the source.");
        addFeature("Reviewed many Java aspects that needed attention.");
        addFeature("Protected many flaws with login management system.");
        addFeature("ThreadTracker: runtime tool for deadlock detection.");
        addFeature("Heavily reviewed future task management, spawning much less threads and relieving task overload on the TimerManager.");
}

function writeAllFeatures() {
        var re = /[ ,&\/]+/g;
    
        for(var i = 0; i < tabs.length; i++) {
                feature_cursor = [];

                var tabName = (tabs[i]).replace(re, "");
                eval("writeFeatureTab_" + tabName)();
        
                feature_tree.push(feature_cursor);
        }
}

function start() {
        cm.getPlayer().announce(MaplePacketCreator.musicChange(anthemSong));
        status = -1;
        writeAllFeatures();
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.getPlayer().announce(MaplePacketCreator.musicChange(ambientSong));
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.getPlayer().announce(MaplePacketCreator.musicChange(ambientSong));
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;
    
                if (status == 0) {
                        var sendStr = "HeavenMS was developed on the timespan of 3 years, based on where Solaxia left. On the meantime many nice features emerged, development aimed to get back the old GMS experience. Now many of these so-long missing features are gracefully presented to you in the shape of this server. Long live MapleStory!!\r\n\r\nThese are the features of #bHeavenMS#k:\r\n\r\n";
                        for(var i = 0; i < tabs.length; i++) {
                            sendStr += "#L" + i + "##b" + tabs[i] + "#k#l\r\n";
                        }

                        cm.sendSimple(sendStr);
                } else if(status == 1) {
                        var tabName;

                        for(var i = 0; i < tabs.length; i++) {
                            if(selection == i) {
                                tabName = feature_tree[i];
                                break;
                            }
                        }

                        var sendStr = "#b" + tabs[selection] + "#k:\r\n\r\n";
                        for(var i = 0; i < tabName.length; i++) {
                            sendStr += "  #L" + i + "# " + tabName[i];
                            sendStr += "#l\r\n";
                        }

                        cm.sendPrev(sendStr);
                } else {
                        cm.getPlayer().announce(MaplePacketCreator.musicChange(ambientSong));
                        cm.dispose();
                }
        }
}

function generateSelectionMenu(array) {
        var menu = "";
        for (var i = 0; i < array.length; i++) {
                menu += "#L" + i + "#" + array[i] + "#l\r\n";
        }
        return menu;
}
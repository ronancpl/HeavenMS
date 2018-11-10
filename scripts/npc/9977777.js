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

var tabs = ["PQs", "Skills", "Quests", "Player Social Network", "Cash & Items", "Monsters, Maps & Reactors", "PQ potentials", "Player potentials", "Server potentials", "Commands", "Custom NPCs", "Localhost edits", "Project"];

function addFeature(feature) {
        feature_cursor.push(feature);
}

function writeFeatureTab_PQs() {
        addFeature("HPQ/KPQ/LPQ/LMPQ/OPQ/APQ/EllinPQ/PiratePQ.");
        addFeature("RnJPQ/HorntailPQ/TreasurePQ/ElnathPQ/HolidayPQ.");
        addFeature("CWKPQ as Expedition-based instance.");
        addFeature("Scarga/Horntail/Showa/Balrog/Zakum/Pinkbean.");
        addFeature("GuildPQ & queue with multi-lobby system available.");
        addFeature("Brand-new PQs: BossRushPQ, CafePQ.");
        addFeature("Mu Lung Dojo.");
        addFeature("Capt. Latanica with party fighting the boss.");
}

function writeFeatureTab_Skills() {
        addFeature("Reviewed many skills, such as Steal and M. Door.");
        addFeature("Heal GMS-like: fixed HP gain & Heal skill packet.");
        addFeature("Improved battleship: HP visible and map-persistent.");
        addFeature("Maker skill features properly developed.");
        addFeature("Chair Mastery - map chair boosts HP/MP rec.");
        addFeature("Mu Lung Dojo skills functional.");
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
        addFeature("P. members' HPBar accounts HP gain on equips.");
        addFeature("Thoroughly reviewed P. Shops and H. Merchants.");
        addFeature("Transactions on Merchs instantly announced to owner.");
        addFeature("Game minirooms with functional pw system.");
        addFeature("Proper item pickup cooldown on non-owned items.");
        addFeature("Improved ranking system, with daily movement.");
        addFeature("Protected and improved face expression system.");
        addFeature("Automated support for Player NPCs and Hall of Fame.");
        addFeature("Engagement & Wedding system with ring effects.");
        addFeature("Equipments displays to everyone it's level & EXP info.");
        addFeature("Further improved the existent minigame mechanics.");
}

function writeFeatureTab_CashItems() {
        addFeature("EXP/DROP/Cosmetic Coupons.");
        addFeature("EXP/DROP Coupon as buff effect during active time.");
        addFeature("Great deal of cash items functional.");
        addFeature("Code coupons functional, with multi-items support.");
        addFeature("Merged unique ids for pets, rings and cash items.");
        addFeature("MapleTV mechanics stabilized and split by world.");
        addFeature("GMS-esque omok/match card drop chances.");
        addFeature("New town scroll: antibanish. Counters boss banishes.");
        addFeature("Inventory system checks for free slot & stack space.");
        addFeature("Storage with 'Arrange Items' feature functional.");
        addFeature("Close-quarters evaluation mode for items.");
        addFeature("Reviewed Karma scissors & Untradeable items.");
        addFeature("Reviewed an pet position issue within CASH inventory.");
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
        addFeature("Mobs only drop items collectable by the player/party.");
        addFeature("Mobs shouldn't fall from foothold too often now.");
        addFeature("Properly applying MP cost on non-skill mob moves.");
        addFeature("Implemented banish mob skill move.");
        addFeature("Redesigned HT mechanics: assemble & dmg taken.");
        addFeature("Implemented Zombify disease status.");
        addFeature("Added Boss HP Bar for dozens of bosses.");
        addFeature("Game will favor showing the targeted boss HPbar.");
        addFeature("Boss HPBar & Srv Message toggle - GabrielSin's idea.");
        addFeature("Dmg overtime on maps and neutralizers functional.");
        addFeature("Items will consistently stay within the walking area.");
        addFeature("Boats, elevator and other travel mechanics functional.");
        addFeature("C. Balrog's boat approaching visual effect functional.");
        addFeature("Maps having everlasting items no longer expires them.");
        addFeature("PQs, Taxis and events warps players to random SPs.");
        addFeature("Uncovered missing portal SFX on scripted portals.");
        addFeature("PQ boxes sprays items when opened, GMS-like.");
        addFeature("Reactors pick items up smartly from the field.");
        addFeature("Updated scripted portals, now with proper portal SFX.");
        addFeature("Reviewed Masteria, W. Tour, N. Desert and Neo City.");
        addFeature("Added world maps for M. Castle, W. Tour & Ellin areas.");
        addFeature("Added W. Tour & Masteria continents in the world map.");
        addFeature("Reviewed several issues with W. Map tooltips & links.");
        addFeature("Giant Cake boss drops s. bags and Maple items.");
}

function writeFeatureTab_PQpotentials() {
        addFeature("Advanced and well-safe PQ registration system.");
        addFeature("Lobby system: Same channel, multiple PQ instances.");
        addFeature("Exped system: Many parties can join a same instance.");
        addFeature("Guild queue: guild registration for the GPQ.");
        addFeature("EIM Pool system: optimized instance loadouts.");
        addFeature("Recall system: players can rejoin PQ after d/c.");
}

function writeFeatureTab_Playerpotentials() {
        addFeature("Adventurer Mount quests functional.");
        addFeature("All Equipment levels up.");
        addFeature("Player level rates.");
        addFeature("Gain fame by quests and event instances.");
        addFeature("Pet evolutions functional (not GMS-like).");
        addFeature("Reviewed keybinding system.");
        addFeature("Character slots per world/server-wide.");
        addFeature("Optional cash shop inventory separated by classes.");
}

function writeFeatureTab_Serverpotentials() {
        addFeature("Multi-worlds.");
        addFeature("Each world can hold it's own rates from server bootup.");
        addFeature("Dynamic World/Channel deployment.");
        addFeature("Inventory auto-gather and auto-sorting feature.");
        addFeature("Enhanced auto-pot system: smart pet potion handle.");
        addFeature("Enhanced buff system: best buffs effects takes place.");
        addFeature("Enhanced AP auto-assigner: focus on eqp demands.");
        addFeature("Enhanced inventory check: free slots smartly fetched.");
        addFeature("Enhanced petloot handler: no brute-force inv. checks.");
        addFeature("Players-appointed bestsellers for Owl and Cash Shop.");
        addFeature("Tweaked pet/mount hunger to a balanced growth rate.");
        addFeature("Consistent experience and meso gain system.");
        addFeature("NPC crafters won't take items freely anymore.");
        addFeature("Duey: pkg rcvd popup and many delivery mechanics.");
        addFeature("Pet pickup gives preference to player attacks.");
        addFeature("Channel capacity bar and worlds with capacity check.");
        addFeature("Diseases visible for others, even after changing maps.");
        addFeature("Persistent diseases. Players keeps status on login.");
        addFeature("Poison damage value visible for other players.");
        addFeature("M. book announcer displays info based on demand.");
        addFeature("Custom jail system.");
        addFeature("Custom buyback system, uses mesos / NX, via MTS.");
        addFeature("Delete Character.");
        addFeature("Smooth view-all-char, now showing all account chars.");
        addFeature("Centralized servertime, boosting handler performance.");
        addFeature("Centralized timestamping, unused rcvd timestamps.");
        addFeature("Autosaver (periodically saves player's data on DB).");
        addFeature("Fixed and randomized HP/MP growth rate available.");
        addFeature("Players' MaxHP/MaxMP method accounting equip gain.");
        addFeature("Prevented 'NPC gone after some uptime' issue.");
        addFeature("AP assigning available for novices level 10 or below.");
        addFeature("SP cap past tier-level, recovered after job upgrade.");
        addFeature("Bypassable PIN/PIC system for authenticated users.");
        addFeature("Automatic account registration - thanks shavit!");
}

function writeFeatureTab_Commands() {
        addFeature("Spawn Zakum/Horntail/Pinkbean.");
        addFeature("Several new commands.");
        addFeature("Rank command highlighting users by world or overall.");
        addFeature("Server commands layered by GM levels.");
        addFeature("Revamped command files layout - thanks Arthur L!");
        addFeature("Improved 'Search' performance & added map search.");
}

function writeFeatureTab_CustomNPCs() {
        addFeature("Spiegelmann: automatized rock-refiner.");
        addFeature("Asia: scroll & rarities shop NPC.");
        addFeature("Abdula: lists droppers of needed skill/mastery books.");
        addFeature("Agent E: accessory crafter.");
        addFeature("Dalair: automatized equipment-merger.");
        addFeature("Donation Box: automatized item-buyer.");
        addFeature("Coco & Ace of Hearts: C. scroll crafters.");
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
        addFeature("Highly configurable & optimized server.");
        addFeature("Fixed/added many missing packet opcodes.");
        addFeature("Uncovered many opcodes throughout the source.");
        addFeature("Reviewed many Java aspects that needed attention.");
        addFeature("Reviewed SQL data, eliminating duplicated entries.");
        addFeature("Improved login phase, using cache over DB queries.");
        addFeature("Protected many flaws with login management system.");
        addFeature("Developed a robust anti-exploit login coordinator.");
        addFeature("Usage of HikariCP to improve DB connection calls.");
        addFeature("Usage of Java Threadpool to improve runnable calls.");
        addFeature("Developed many survey tools for content profiling.");
        addFeature("ThreadTracker: runtime tool for deadlock detection.");
        addFeature("Channel, World and Server-wide timer management.");
        addFeature("Thoroughly reviewed encapsulation for player stats.");
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
                        var sendStr = "HeavenMS was developed on the timespan of 3 years, based on where Solaxia left. I'm glad to say the development itself had continuously been agraciated by dozens of contributors and cheerers (truly thanks for the trusting vow, guys & gals!).\r\n\r\nTalking about results: many nice features emerged, development aimed to get back the old GMS experience. Now many of these so-long missing features are gracefully presented to you in the shape of this server. Long live MapleStory!!\r\n\r\nThese are the features from #bHeavenMS#k:\r\n\r\n";
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
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
package client.command;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import net.MaplePacketHandler;
import net.PacketProcessor;
import net.server.Server;
import net.server.channel.Channel;
import net.server.world.World;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.npc.NPCScriptManager;
import scripting.portal.PortalScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShopFactory;
import server.TimerManager;
import server.events.gm.MapleEvent;
import server.expeditions.MapleExpedition;
import server.gachapon.MapleGachapon.Gachapon;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import tools.DatabaseConnection;
import tools.FilePrinter;
import tools.HexTool;
import tools.MapleLogger;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import client.MapleStat;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.Equip;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import constants.ServerConstants;
import java.util.ArrayList;
import server.life.SpawnPoint;
import server.maps.FieldLimit;

public class Commands {
        private static HashMap<String, Integer> gotomaps = new HashMap<String, Integer>();

	private static String[] tips = {
		"Please only use @gm in emergencies or to report somebody.",
		"To report a bug or make a suggestion, use the forum.",
		"Please do not use @gm to ask if a GM is online.",
		"Do not ask if you can receive help, just state your issue.",
		"Do not say 'I have a bug to report', just state it.",
	};

	private static String[] songs = {
		"Jukebox/Congratulation", 
		"Bgm00/SleepyWood", 
		"Bgm00/FloralLife", 
		"Bgm00/GoPicnic", 
		"Bgm00/Nightmare", 
		"Bgm00/RestNPeace",
		"Bgm01/AncientMove", 
		"Bgm01/MoonlightShadow", 
		"Bgm01/WhereTheBarlogFrom", 
		"Bgm01/CavaBien", 
		"Bgm01/HighlandStar", 
		"Bgm01/BadGuys",
		"Bgm02/MissingYou", 
		"Bgm02/WhenTheMorningComes", 
		"Bgm02/EvilEyes", 
		"Bgm02/JungleBook", 
		"Bgm02/AboveTheTreetops",
		"Bgm03/Subway", 
		"Bgm03/Elfwood", 
		"Bgm03/BlueSky", 
		"Bgm03/Beachway",
		"Bgm03/SnowyVillage",
		"Bgm04/PlayWithMe", 
		"Bgm04/WhiteChristmas", 
		"Bgm04/UponTheSky",
		"Bgm04/ArabPirate", 
		"Bgm04/Shinin'Harbor",
		"Bgm04/WarmRegard",
		"Bgm05/WolfWood", 
		"Bgm05/DownToTheCave", 
		"Bgm05/AbandonedMine", 
		"Bgm05/MineQuest",
		"Bgm05/HellGate",
		"Bgm06/FinalFight", 
		"Bgm06/WelcomeToTheHell",
		"Bgm06/ComeWithMe", 
		"Bgm06/FlyingInABlueDream", 
		"Bgm06/FantasticThinking",
		"Bgm07/WaltzForWork", 
		"Bgm07/WhereverYouAre", 
		"Bgm07/FunnyTimeMaker", 
		"Bgm07/HighEnough", 
		"Bgm07/Fantasia",
		"Bgm08/LetsMarch", 
		"Bgm08/ForTheGlory", 
		"Bgm08/FindingForest", 
		"Bgm08/LetsHuntAliens", 
		"Bgm08/PlotOfPixie",
		"Bgm09/DarkShadow", 
		"Bgm09/TheyMenacingYou", 
		"Bgm09/FairyTale", 
		"Bgm09/FairyTalediffvers",
		"Bgm09/TimeAttack",
		"Bgm10/Timeless", 
		"Bgm10/TimelessB", 
		"Bgm10/BizarreTales",
		"Bgm10/TheWayGrotesque",
		"Bgm10/Eregos",
		"Bgm11/BlueWorld", 
		"Bgm11/Aquarium",
		"Bgm11/ShiningSea",
		"Bgm11/DownTown", 
		"Bgm11/DarkMountain",
		"Bgm12/AquaCave", 
		"Bgm12/DeepSee", 
		"Bgm12/WaterWay", 
		"Bgm12/AcientRemain",
		"Bgm12/RuinCastle",
		"Bgm12/Dispute",
		"Bgm13/CokeTown", 
		"Bgm13/Leafre", 
		"Bgm13/Minar'sDream", 
		"Bgm13/AcientForest", 
		"Bgm13/TowerOfGoddess",
		"Bgm14/DragonLoad", 
		"Bgm14/HonTale", 
		"Bgm14/CaveOfHontale", 
		"Bgm14/DragonNest", 
		"Bgm14/Ariant", 
		"Bgm14/HotDesert",
		"Bgm15/MureungHill", 
		"Bgm15/MureungForest", 
		"Bgm15/WhiteHerb",
		"Bgm15/Pirate",
		"Bgm15/SunsetDesert", 
		"Bgm16/Duskofgod", 
		"Bgm16/FightingPinkBeen", 
		"Bgm16/Forgetfulness", 
		"Bgm16/Remembrance", 
		"Bgm16/Repentance", 
		"Bgm16/TimeTemple", 
		"Bgm17/MureungSchool1",
		"Bgm17/MureungSchool2", 
		"Bgm17/MureungSchool3",
		"Bgm17/MureungSchool4", 
		"Bgm18/BlackWing", 
		"Bgm18/DrillHall", 
		"Bgm18/QueensGarden",
		"Bgm18/RaindropFlower", 
		"Bgm18/WolfAndSheep",
		"Bgm19/BambooGym",
		"Bgm19/CrystalCave", 
		"Bgm19/MushCatle", 
		"Bgm19/RienVillage",
		"Bgm19/SnowDrop", 
		"Bgm20/GhostShip", 
		"Bgm20/NetsPiramid",
		"Bgm20/UnderSubway", 
		"Bgm21/2021year",
		"Bgm21/2099year", 
		"Bgm21/2215year", 
		"Bgm21/2230year",
		"Bgm21/2503year",
		"Bgm21/KerningSquare",
		"Bgm21/KerningSquareField", 
		"Bgm21/KerningSquareSubway", 
		"Bgm21/TeraForest",
		"BgmEvent/FunnyRabbit",
		"BgmEvent/FunnyRabbitFaster", 
		"BgmEvent/wedding", 
		"BgmEvent/weddingDance",
		"BgmEvent/wichTower",
		"BgmGL/amoria", 
		"BgmGL/Amorianchallenge", 
		"BgmGL/chapel", 
		"BgmGL/cathedral", 
		"BgmGL/Courtyard", 
		"BgmGL/CrimsonwoodKeep",
		"BgmGL/CrimsonwoodKeepInterior", 
		"BgmGL/GrandmastersGauntlet",
		"BgmGL/HauntedHouse", 
		"BgmGL/NLChunt",
		"BgmGL/NLCtown",
		"BgmGL/NLCupbeat", 
		"BgmGL/PartyQuestGL", 
		"BgmGL/PhantomForest", 
		"BgmJp/Feeling", 
		"BgmJp/BizarreForest", 
		"BgmJp/Hana",
		"BgmJp/Yume", 
		"BgmJp/Bathroom", 
		"BgmJp/BattleField", 
		"BgmJp/FirstStepMaster",
		"BgmMY/Highland",
		"BgmMY/KualaLumpur",
		"BgmSG/BoatQuay_field", 
		"BgmSG/BoatQuay_town", 
		"BgmSG/CBD_field",
		"BgmSG/CBD_town", 
		"BgmSG/Ghostship", 
		"BgmUI/ShopBgm", 
		"BgmUI/Title"
	};

	static {
		gotomaps.put("gmmap", 180000000);
		gotomaps.put("southperry", 60000);
		gotomaps.put("amherst", 1010000);
		gotomaps.put("henesys", 100000000);
		gotomaps.put("ellinia", 101000000);
		gotomaps.put("perion", 102000000);
		gotomaps.put("kerning", 103000000);
		gotomaps.put("lith", 104000000);
		gotomaps.put("sleepywood", 105040300);
		gotomaps.put("florina", 110000000);
		gotomaps.put("orbis", 200000000);
		gotomaps.put("happy", 209000000);
		gotomaps.put("elnath", 211000000);
		gotomaps.put("ludi", 220000000);
		gotomaps.put("aqua", 230000000);
		gotomaps.put("leafre", 240000000);
		gotomaps.put("mulung", 250000000);
		gotomaps.put("herb", 251000000);
		gotomaps.put("omega", 221000000);
		gotomaps.put("korean", 222000000);
		gotomaps.put("nlc", 600000000);
		gotomaps.put("excavation", 990000000);
		gotomaps.put("pianus", 230040420);
		gotomaps.put("horntail", 240060200);
		gotomaps.put("mushmom", 100000005);
		gotomaps.put("griffey", 240020101);
		gotomaps.put("manon", 240020401);
		gotomaps.put("horseman", 682000001);
		gotomaps.put("balrog", 105090900);
		gotomaps.put("zakum", 211042300);
		gotomaps.put("papu", 220080001);
		gotomaps.put("showa", 801000000);
		gotomaps.put("guild", 200000301);
		gotomaps.put("shrine", 800000000);
		gotomaps.put("skelegon", 240040511);
		gotomaps.put("hpq", 100000200);
		gotomaps.put("ht", 240050400);
		gotomaps.put("fm", 910000000);
	}
        
        private static void hardsetItemStats(Equip equip, short stat) {
            equip.setStr(stat);
            equip.setDex(stat);
            equip.setInt(stat);
            equip.setLuk(stat);
            equip.setMatk(stat);
            equip.setWatk(stat);
            equip.setAcc(stat);
            equip.setAvoid(stat);
            equip.setJump(stat);
            equip.setSpeed(stat);
            equip.setWdef(stat);
            equip.setMdef(stat);
            equip.setHp(stat);
            equip.setMp(stat);
            
            byte flag = equip.getFlag();
            flag |= ItemConstants.UNTRADEABLE;
            equip.setFlag(flag);
        }
        
        public static boolean executeSolaxiaCommandLv0(Channel cserv, Server srv, MapleClient c, String[] sub) { //Player
                MapleCharacter player = c.getPlayer();
            
                switch(sub[0]) {
                case "help":
		case "commands":
                case "playercommands":
                        c.getAbstractPlayerInteraction().openNpc(9201143, "commands");
                        break;
                    
		case "time":
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("-GMT3"));
			player.yellowMessage("Solaxia Server Time: " + dateFormat.format(new Date()));
			break;
                    
		case "staff":
			player.yellowMessage("MapleSolaxia Staff");
			player.yellowMessage("Aria - Administrator");
			player.yellowMessage("Twdtwd - Administrator");
			player.yellowMessage("Exorcist - Developer");
			player.yellowMessage("SharpAceX - Developer");
			player.yellowMessage("Zygon - Freelance Developer");
			player.yellowMessage("SourMjolk - Game Master");
			player.yellowMessage("Kanade - Game Master");
			player.yellowMessage("Kitsune - Game Master");
                        player.yellowMessage("MapleSolaxiaV2 Staff");
                        player.yellowMessage("Ronan - Freelance Developer");
                        player.yellowMessage("Vcoc - Freelance Developer");
			break;
                    
		case "lastrestart":
		case "uptime":
			long milliseconds = System.currentTimeMillis() - Server.uptime;
			int seconds = (int) (milliseconds / 1000) % 60 ;
			int minutes = (int) ((milliseconds / (1000*60)) % 60);
			int hours   = (int) ((milliseconds / (1000*60*60)) % 24);
			int days    = (int) ((milliseconds / (1000*60*60*24)));
 			player.yellowMessage("Server has been online for " + days + " days " + hours + " hours " + minutes + " minutes and " + seconds + " seconds.");
			break;
                    
		case "gacha":
			Gachapon gacha = null;
			String search = joinStringFrom(sub, 1);
			String gachaName = "";
			String [] names = {"Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa Male", "Showa Spa Female", "New Leaf City", "Nautilus Harbor"};
			int [] ids = {9100100, 9100101, 9100102, 9100103, 9100104, 9100105, 9100106, 9100107, 9100109, 9100117};
			for (int i = 0; i < names.length; i++){
				if (search.equalsIgnoreCase(names[i])){
					gachaName = names[i];
					gacha = Gachapon.getByNpcId(ids[i]);
				}
			}
			if (gacha == null){
				player.yellowMessage("Please use @gacha <name> where name corresponds to one of the below:");
				for (String name : names){
					player.yellowMessage(name);
				}
                        break;
			}
			String output = "The #b" + gachaName + "#k Gachapon contains the following items.\r\n\r\n";
			for (int i = 0; i < 2; i++){
				for (int id : gacha.getItems(i)){
					output += "-" + MapleItemInformationProvider.getInstance().getName(id) + "\r\n";
				}
			}
			output += "\r\nPlease keep in mind that there are items that are in all gachapons and are not listed here.";
			c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, output, "00 00", (byte) 0));
			break;
                    
		case "whatdropsfrom":
			if (sub.length < 2) {
				player.dropMessage(5, "Please do @whatdropsfrom <monster name>");
                        break;
			}
			String monsterName = joinStringFrom(sub, 1);
			output = "";
			int limit = 3;
			Iterator<Pair<Integer, String>> listIterator = MapleMonsterInformationProvider.getMobsIDsFromName(monsterName).iterator();
			for (int i = 0; i < limit; i++) {
				if(listIterator.hasNext()) {
					Pair<Integer, String> data = listIterator.next();
					int mobId = data.getLeft();
					String mobName = data.getRight();
					output += mobName + " drops the following items:\r\n\r\n";
					for (MonsterDropEntry drop : MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId)){
						try {
							String name = MapleItemInformationProvider.getInstance().getName(drop.itemId);
							if (name.equals("null") || drop.chance == 0){
								continue;
							}
							float chance = 1000000 / drop.chance / player.getDropRate();
							output += "- " + name + " (1/" + (int) chance + ")\r\n";
						} catch (Exception ex){
                                                        ex.printStackTrace();
							continue;
						}
					}
					output += "\r\n";
				}
			}
			c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, output, "00 00", (byte) 0));
			break;
                    
		case "whodrops":
			if (sub.length < 2) {
				player.dropMessage(5, "Please do @whodrops <item name>");
                        break;
			}
			String searchString = joinStringFrom(sub, 1);
			output = "";
			listIterator = MapleItemInformationProvider.getInstance().getItemDataByName(searchString).iterator();
			if(listIterator.hasNext()) {
				int count = 1;
				while(listIterator.hasNext() && count <= 3) {
					Pair<Integer, String> data = listIterator.next();
					output += "#b" + data.getRight() + "#k is dropped by:\r\n";
					try {
                                                Connection con = DatabaseConnection.getConnection();
						PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE itemid = ? LIMIT 50");
						ps.setInt(1, data.getLeft());
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							String resultName = MapleMonsterInformationProvider.getMobNameFromID(rs.getInt("dropperid"));
							if (resultName != null) {
								output += resultName + ", ";
							}
						}
						rs.close();
						ps.close();
                                                con.close();
					} catch (Exception e) {
						player.dropMessage("There was a problem retreiving the required data. Please try again.");
						e.printStackTrace();
						break;
					}
					output += "\r\n\r\n";
					count++;
				}
			} else {
				player.dropMessage(5, "The item you searched for doesn't exist.");
                        break;
			}
			c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, output, "00 00", (byte) 0));
			break;
                    
		case "dispose":
			NPCScriptManager.getInstance().dispose(c);
			c.announce(MaplePacketCreator.enableActions());
			c.removeClickedNPC();
			player.message("You've been disposed.");
			break;
                    
                case "equiplv":
                        player.showAllEquipFeatures();
                        break;

                case "showrates":
                        String showMsg = "#eEXP RATE#n" + "\r\n";
                        showMsg += "Server EXP Rate: #k" + c.getWorldServer().getExpRate() + "x#k" + "\r\n";
                        showMsg += "Player EXP Rate: #k" + player.getRawExpRate() + "x#k" + "\r\n";
                        if(player.getCouponExpRate() != 1) showMsg += "Coupon EXP Rate: #k" + player.getCouponExpRate() + "x#k" + "\r\n";
                        showMsg += "EXP Rate: #e#b" + player.getExpRate() + "x#k#n" + "\r\n";
                        
                        showMsg += "\r\n" + "#eMESO RATE#n" + "\r\n";
                        showMsg += "Server MESO Rate: #k" + c.getWorldServer().getMesoRate() + "x#k" + "\r\n";
                        showMsg += "Player MESO Rate: #k" + player.getRawMesoRate() + "x#k" + "\r\n";
                        if(player.getCouponMesoRate() != 1) showMsg += "Coupon MESO Rate: #k" + player.getCouponMesoRate() + "x#k" + "\r\n";
                        showMsg += "MESO Rate: #e#b" + player.getMesoRate() + "x#k#n" + "\r\n";
                        
                        showMsg += "\r\n" + "#eDROP RATE#n" + "\r\n";
                        showMsg += "Server DROP Rate: #k" + c.getWorldServer().getDropRate() + "x#k" + "\r\n";
                        showMsg += "Player DROP Rate: #k" + player.getRawDropRate() + "x#k" + "\r\n";
                        if(player.getCouponDropRate() != 1) showMsg += "Coupon DROP Rate: #k" + player.getCouponDropRate() + "x#k" + "\r\n";
                        showMsg += "DROP Rate: #e#b" + player.getDropRate() + "x#k#n" + "\r\n";
                    
                        showMsg += "\r\n" + "#eBOSSDROP RATE#n" + "\r\n";
                        showMsg += "Server BOSSDROP Rate: #e#b" + c.getWorldServer().getBossDropRate() + "x#k#n" + "\r\n";
                        
                        player.showHint(showMsg);
                    break;
                     
                case "rates":
                        String showMsg_ = "#eCHARACTER RATES#n" + "\r\n\r\n";
                        showMsg_ += "EXP Rate: #e#b" + player.getExpRate() + "x#k#n" + "\r\n";
                        showMsg_ += "MESO Rate: #e#b" + player.getMesoRate() + "x#k#n" + "\r\n";
                        showMsg_ += "DROP Rate: #e#b" + player.getDropRate() + "x#k#n" + "\r\n";
                        showMsg_ += "BOSSDROP Rate: #e#b" + c.getWorldServer().getBossDropRate() + "x#k#n" + "\r\n";
                        
                        player.showHint(showMsg_);
                    break;
                    
		case "online":
			for (Channel ch : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
				player.yellowMessage("Players in Channel " + ch.getId() + ":");
				for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
					if (!chr.isGM()) {
						player.message(" >> " + MapleCharacter.makeMapleReadable(chr.getName()) + " is at " + chr.getMap().getMapName() + ".");
					}
				}
			}
			break;
                    
		case "gm":
			if (sub.length < 3) { // #goodbye 'hi'
				player.dropMessage(5, "Your message was too short. Please provide as much detail as possible.");
				break;
			}
			String message = joinStringFrom(sub, 1);
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.sendYellowTip("[GM MESSAGE]:" + MapleCharacter.makeMapleReadable(player.getName()) + ": " + message));
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(1, message));
			FilePrinter.printError("gm.txt", MapleCharacter.makeMapleReadable(player.getName()) + ": " + message + "\r\n");
			player.dropMessage(5, "Your message '" + message + "' was sent to GMs.");
			player.dropMessage(5, tips[Randomizer.nextInt(tips.length)]);
			break;
                    
		case "bug":
                    
			if (sub.length < 2) {
				player.dropMessage(5, "Message too short and not sent. Please do @bug <bug>");
				break;
			}
			message = joinStringFrom(sub, 1);
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.sendYellowTip("[BUG]:" + MapleCharacter.makeMapleReadable(player.getName()) + ": " + message));
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(1, message));
			FilePrinter.printError("bug.txt", MapleCharacter.makeMapleReadable(player.getName()) + ": " + message + "\r\n");
			player.dropMessage(5, "Your bug '" + message + "' was submitted successfully to our developers. Thank you!");
			break;
		/*
                case "points":
			player.dropMessage(5, "You have " + c.getVotePoints() + " vote point(s).");
			if (c.hasVotedAlready()) {
				Date currentDate = new Date();
				int time = (int) ((int) 86400 - ((currentDate.getTime() / 1000) - c.getVoteTime())); //ugly as fuck
				hours = time / 3600;
				minutes = time % 3600 / 60;
				seconds = time % 3600 % 60;
				player.yellowMessage("You have already voted. You can vote again in " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds.");
			} else {
				player.yellowMessage("You are free to vote! Make sure to vote to gain a vote point!");
			}
			break;
                */
		case "joinevent":
			if(!FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
				MapleEvent event = c.getChannelServer().getEvent();
				if(event != null) {
					if(event.getMapId() != player.getMapId()) {
						if(event.getLimit() > 0) {
							player.saveLocation("EVENT");

							if(event.getMapId() == 109080000 || event.getMapId() == 109060001)
								player.setTeam(event.getLimit() % 2);

							event.minusLimit();

							player.changeMap(event.getMapId());
						} else {
							player.dropMessage("The limit of players for the event has already been reached.");
						}
					} else {
						player.dropMessage(5, "You are already in the event.");
					}
				} else {
					player.dropMessage(5, "There is currently no event in progress.");
				}
			} else {
				player.dropMessage(5, "You are currently in a map where you can't join an event.");
			}
			break;
                    
		case "leaveevent":
			int returnMap = player.getSavedLocation("EVENT");
			if(returnMap != -1) {
				if(player.getOla() != null) {
					player.getOla().resetTimes();
					player.setOla(null);
				}
				if(player.getFitness() != null) {
					player.getFitness().resetTimes();
					player.setFitness(null);
				}
				
				player.changeMap(returnMap);
				if(c.getChannelServer().getEvent() != null) {
					c.getChannelServer().getEvent().addLimit();
				}
			} else {
				player.dropMessage(5, "You are not currently in an event.");
			}
			break;
                    
		case "bosshp":
			for(MapleMonster monster : player.getMap().getMonsters()) {
				if(monster != null && monster.isBoss() && monster.getHp() > 0) {
					long percent = monster.getHp() * 100L / monster.getMaxHp();
					String bar = "[";
					for (int i = 0; i < 100; i++){
						bar += i < percent ? "|" : ".";
					}
					bar += "]";
					player.yellowMessage(monster.getName() + " (" + monster.getId() + ") has " + percent + "% HP left.");
					player.yellowMessage("HP: " + bar);
				}
			} 
			break;
                    
		case "ranks":
			PreparedStatement ps = null;
			ResultSet rs = null;
                        Connection con = null;
			try {
                                con = DatabaseConnection.getConnection();
				ps = con.prepareStatement("SELECT `characters`.`name`, `characters`.`level` FROM `characters` LEFT JOIN accounts ON accounts.id = characters.accountid WHERE `characters`.`gm` = '0' AND `accounts`.`banned` = '0' ORDER BY level DESC, exp DESC LIMIT 50");
				rs = ps.executeQuery();
				
				player.announce(MaplePacketCreator.showPlayerRanks(9010000, rs));
				ps.close();
				rs.close();
                                con.close();
			} catch(SQLException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if(ps != null && !ps.isClosed()) {
						ps.close();
					}
					if(rs != null && !rs.isClosed()) {
						rs.close();
					}
                                        if(con != null && !con.isClosed()) {
						con.close();
					}
				} catch (SQLException e) {
                                        e.printStackTrace();
				}
			}
			break;
                            
                default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommandLv1(Channel cserv, Server srv, MapleClient c, String[] sub) { //Donator
                MapleCharacter player = c.getPlayer();
            
                switch(sub[0]) {                
                case "buffme":
                        //GM Skills : Haste(Super) - Holy Symbol - Bless - Hyper Body - Echo of Hero
                        SkillFactory.getSkill(4101004).getEffect(SkillFactory.getSkill(4101004).getMaxLevel()).applyTo(player);
                        SkillFactory.getSkill(2311003).getEffect(SkillFactory.getSkill(2311003).getMaxLevel()).applyTo(player);
                        SkillFactory.getSkill(1301007).getEffect(SkillFactory.getSkill(1301007).getMaxLevel()).applyTo(player);
                        SkillFactory.getSkill(2301004).getEffect(SkillFactory.getSkill(2301004).getMaxLevel()).applyTo(player);
                        SkillFactory.getSkill(1005).getEffect(SkillFactory.getSkill(1005).getMaxLevel()).applyTo(player);
                        player.setHp(player.getMaxHp());
                        player.updateSingleStat(MapleStat.HP, player.getMaxHp());
                        player.setMp(player.getMaxMp());
                        player.updateSingleStat(MapleStat.MP, player.getMaxMp());
                                break;
                        
                case "goto":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !goto <map name>");
				break;
			}
                    
			if (gotomaps.containsKey(sub[1])) {
				MapleMap target = c.getChannelServer().getMapFactory().getMap(gotomaps.get(sub[1]));
				MaplePortal targetPortal = target.getPortal(0);
				if (player.getEventInstance() != null) {
					player.getEventInstance().removePlayer(player);
				}
				player.changeMap(target, targetPortal);
			} else {
				player.dropMessage(5, "That map does not exist.");
			}
                                break;
								
                case "recharge":
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        for (Item torecharge : c.getPlayer().getInventory(MapleInventoryType.USE).list()) {
                                if (ItemConstants.isThrowingStar(torecharge.getItemId())){
                                        torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                                        c.getPlayer().forceUpdateItem(torecharge);
                                } else if (ItemConstants.isArrow(torecharge.getItemId())){
                                        torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                                        c.getPlayer().forceUpdateItem(torecharge);
                                } else if (ItemConstants.isBullet(torecharge.getItemId())){
                                        torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                                        c.getPlayer().forceUpdateItem(torecharge);
                                } else if (ItemConstants.isConsumable(torecharge.getItemId())){
                                        torecharge.setQuantity(ii.getSlotMax(c, torecharge.getItemId()));
                                        c.getPlayer().forceUpdateItem(torecharge);
                                }
                        }
                        player.dropMessage(5, "USE Recharged.");
                                break;
								
                case "whereami":
			player.yellowMessage("Map ID: " + player.getMap().getId());
			player.yellowMessage("Players on this map:");
			for (MapleMapObject mmo : player.getMap().getPlayers()) {
				MapleCharacter chr = (MapleCharacter) mmo;
				player.dropMessage(5, ">> " + chr.getName());
			}
			player.yellowMessage("NPCs on this map:");
			for (MapleMapObject npcs : player.getMap().getMapObjects()) {
				if (npcs instanceof MapleNPC) {
					MapleNPC npc = (MapleNPC) npcs;
					player.dropMessage(5, ">> " + npc.getName() + " - " + npc.getId() + " - Oid: " + npc.getObjectId());
				}
			}
			player.yellowMessage("Monsters on this map:");
			for (MapleMapObject mobs : player.getMap().getMapObjects()) {
				if (mobs instanceof MapleMonster) {
					MapleMonster mob = (MapleMonster) mobs;
					if(mob.isAlive()){
						player.dropMessage(5, ">> " + mob.getName() + " - " + mob.getId());
					}
				}
			}
                                break;
                            
                default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommandLv2(Channel cserv, Server srv, MapleClient c, String[] sub) { //JrGM
                MapleCharacter player = c.getPlayer();
                MapleCharacter victim;
                Skill skill;
            
                switch(sub[0]) {
                case "hide":
                        SkillFactory.getSkill(9101004).getEffect(SkillFactory.getSkill(9101004).getMaxLevel()).applyTo(player);
                    break;
                    
                case "unhide":
                        SkillFactory.getSkill(9101004).getEffect(SkillFactory.getSkill(9101004).getMaxLevel()).applyTo(player);
                    break;
                        
                case "sp":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !sp [<playername>] <newsp>");
				break;
			}
                        
			if (sub.length == 2) {
                                int newSp = Integer.parseInt(sub[1]);
                                if(newSp < 0) newSp = 0;
                                else if(newSp > ServerConstants.MAX_AP) newSp = ServerConstants.MAX_AP;
                            
				player.setRemainingSp(newSp);
				player.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());
			} else {
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
                                
                                if(victim != null) {
                                        int newSp = Integer.parseInt(sub[2]);
                                        if(newSp < 0) newSp = 0;
                                        else if(newSp > ServerConstants.MAX_AP) newSp = ServerConstants.MAX_AP;
                                    
                                        victim.setRemainingSp(newSp);
                                        victim.updateSingleStat(MapleStat.AVAILABLESP, player.getRemainingSp());

                                        player.dropMessage(5, "SP given.");
                                } else {
                                        player.message("Player '" + sub[1] + "' could not be found on this channel.");
                                }
			}
                    break;
                    
                case "ap":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !ap [<playername>] <newap>");
				break;
			}
                        
			if (sub.length < 3) {
                                int newAp = Integer.parseInt(sub[1]);
                                if(newAp < 0) newAp = 0;
                                else if(newAp > ServerConstants.MAX_AP) newAp = ServerConstants.MAX_AP;
                            
				player.setRemainingAp(newAp);
				player.updateSingleStat(MapleStat.AVAILABLEAP, player.getRemainingAp());
			} else {
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
                                
                                if(victim != null) {
                                        int newAp = Integer.parseInt(sub[2]);
                                        if(newAp < 0) newAp = 0;
                                        else if(newAp > ServerConstants.MAX_AP) newAp = ServerConstants.MAX_AP;
                                        
                                        victim.setRemainingAp(newAp);
                                        victim.updateSingleStat(MapleStat.AVAILABLEAP, victim.getRemainingAp());
                                } else {
                                        player.message("Player '" + sub[1] + "' could not be found on this channel.");
                                }
			}
                    break;
                    
                case "empowerme":
			final int[] array = {2311003, 2301004, 1301007, 4101004, 2001002, 1101007, 1005, 2301003, 5121009, 1111002, 4111001, 4111002, 4211003, 4211005, 1321000, 2321004, 3121002};
			for (int i : array) {
				SkillFactory.getSkill(i).getEffect(SkillFactory.getSkill(i).getMaxLevel()).applyTo(player);
			}
                    break;
                    
                case "buffmap":
                        SkillFactory.getSkill(9101001).getEffect(SkillFactory.getSkill(9101001).getMaxLevel()).applyTo(player, true);
                        SkillFactory.getSkill(9101002).getEffect(SkillFactory.getSkill(9101002).getMaxLevel()).applyTo(player, true);
                        SkillFactory.getSkill(9101003).getEffect(SkillFactory.getSkill(9101003).getMaxLevel()).applyTo(player, true);
                        SkillFactory.getSkill(9101008).getEffect(SkillFactory.getSkill(9101008).getMaxLevel()).applyTo(player, true);
                        SkillFactory.getSkill(1005).getEffect(SkillFactory.getSkill(1005).getMaxLevel()).applyTo(player, true);
                    break;
                    
                case "buff":
                        if (sub.length < 2){
                                player.yellowMessage("Syntax: !buff <buffid>");
                                break;
			}
                        int skillid=Integer.parseInt(sub[1]);
                        
                        skill = SkillFactory.getSkill(skillid);
                        if(skill != null) skill.getEffect(skill.getMaxLevel()).applyTo(player);
                    break;
                    
                case "bomb":
			if (sub.length > 1){
				victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
                                
                                if(victim != null) {
                                        victim.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300166), victim.getPosition());
                                        Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, player.getName() + " used !bomb on " + victim.getName()));
                                } else {
                                        player.message("Player '" + sub[1] + "' could not be found on this world.");
                                }
			} else {
				player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(9300166), player.getPosition());
			}
                    break;
                    
                case "dc":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !dc <playername>");
				break;
			}
                    
			victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
			if (victim == null) {
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
				if (victim == null) {
					victim = player.getMap().getCharacterByName(sub[1]);
					if (victim != null) {
						try {//sometimes bugged because the map = null
							victim.getClient().disconnect(true, false);
							player.getMap().removePlayer(victim);
						} catch (Exception e) {
                                                        e.printStackTrace();
						}
					} else {
						break;
					}
				}
			}
			if (player.gmLevel() < victim.gmLevel()) {
				victim = player;
			}
			victim.getClient().disconnect(false, false);
                    break;
                    
                case "cleardrops":
			player.getMap().clearDrops(player);
                    break;
                    
                case "clearslot":
                        if (sub.length < 2) {
                                player.yellowMessage("Syntax: !clearslot <all, equip, use, setup, etc or cash.>");
                                break;
                        }
                        String type = sub[1];
                        if (type.equals("all")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("All Slots Cleared.");
                                }
                            else if (type.equals("equip")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("Equipment Slot Cleared.");
                                }
                            else if (type.equals("use")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("Use Slot Cleared.");
                                }
                            else if (type.equals("setup")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("Set-Up Slot Cleared.");
                                }
                            else if (type.equals("etc")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("ETC Slot Cleared.");
                                }
                            else if (type.equals("cash")) {
                                for (int i = 0; i < 101; i++) {
                                        Item tempItem = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((byte) i);
                                        if (tempItem == null)
                                                continue;
                                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (byte) i, tempItem.getQuantity(), false, true);
                                }
                                player.yellowMessage("Cash Slot Cleared.");
                                }
                            else player.yellowMessage("Slot" + type + " does not exist!");
                    break;
                    
                case "warp":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !warp <mapid>");
				break;
			}
                    
			try {
				MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(sub[1]));
				if (target == null) {
					player.yellowMessage("Map ID " + sub[1] + " is invalid.");
					break;
				}
				if (player.getEventInstance() != null) {
					player.getEventInstance().leftParty(player);
				}
				player.changeMap(target, target.getRandomPlayerSpawnpoint());
			} catch (Exception ex) {
				player.yellowMessage("Map ID " + sub[1] + " is invalid.");
				break;
			}
                    break;
                    
                case "warpto":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !warpto <playername> <mapid>");
				break;
			}
                    
			victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
			if (victim == null) {//If victim isn't on current channel or isnt a character try and find him by loop all channels on current world.
				for (Channel ch : srv.getChannelsFromWorld(c.getWorld())) {
					victim = ch.getPlayerStorage().getCharacterByName(sub[1]);
					if (victim != null) {
						break;//We found the person, no need to continue the loop.
					}
				}
			}
			if (victim != null) {//If target isn't null attempt to warp.
				//Remove warper from current event instance.
				if (player.getEventInstance() != null) {
					player.getEventInstance().unregisterPlayer(player);
				}
				//Attempt to join the victims warp instance.
				if (victim.getEventInstance() != null) {
					if (victim.getClient().getChannel() == player.getClient().getChannel()) {//just in case.. you never know...
						//victim.getEventInstance().registerPlayer(player);
						player.changeMap(victim.getEventInstance().getMapInstance(victim.getMapId()), victim.getMap().findClosestPortal(victim.getPosition()));
					} else {
						player.dropMessage("Please change to channel " + victim.getClient().getChannel());
					}
				} else {//If victim isn't in an event instance, just warp them.
					player.changeMap(victim.getMapId(), victim.getMap().findClosestPortal(victim.getPosition()));
				}
				if (player.getClient().getChannel() != victim.getClient().getChannel()) {//And then change channel if needed.
					player.dropMessage("Changing channel, please wait a moment.");
					player.getClient().changeChannel(victim.getClient().getChannel());
				}
			} else {
				player.dropMessage("Unknown player.");
			}
                    break;
                    
                case "warphere":
                case "summon":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !warphere <playername>");
				break;
			}
                    
			victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
			if (victim == null) {//If victim isn't on current channel, loop all channels on current world.
				for (Channel ch : srv.getChannelsFromWorld(c.getWorld())) {
					victim = ch.getPlayerStorage().getCharacterByName(sub[1]);
					if (victim != null) {
						break;//We found the person, no need to continue the loop.
					}
				}
			}
			if (victim != null) {
                                boolean changingEvent = true;
                            
				if (victim.getEventInstance() != null) {
                                        if(player.getEventInstance() != null && victim.getEventInstance().getLeaderId() == player.getEventInstance().getLeaderId()) {
                                                changingEvent = false;
                                        }
                                        else {
                                                victim.getEventInstance().unregisterPlayer(victim);
                                        }
				}
				//Attempt to join the warpers instance.
				if (player.getEventInstance() != null && changingEvent) {
					if (player.getClient().getChannel() == victim.getClient().getChannel()) {//just in case.. you never know...
						player.getEventInstance().registerPlayer(victim);
						victim.changeMap(player.getEventInstance().getMapInstance(player.getMapId()), player.getMap().findClosestPortal(player.getPosition()));
					} else {
						player.dropMessage("Target isn't on your channel, not able to warp into event instance.");
					}
				} else {//If victim isn't in an event instance or is in the same event instance as the one the caller is, just warp them.
					victim.changeMap(player.getMapId(), player.getMap().findClosestPortal(player.getPosition()));
				}
				if (player.getClient().getChannel() != victim.getClient().getChannel()) {//And then change channel if needed.
					victim.dropMessage("Changing channel, please wait a moment.");
					victim.getClient().changeChannel(player.getClient().getChannel());
				}
			} else {
				player.dropMessage("Unknown player.");
			}
                    break;
                
                case "gmshop":
			MapleShopFactory.getInstance().getShop(1337).sendShop(c);
                    break;
                    
                case "heal":
			player.setHpMp(30000);
                    break;
                    
                case "item":
                case "drop":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !item <itemid> <quantity>");
				break;
			}
                        
			int itemId = Integer.parseInt(sub[1]);
                        if(MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                                player.yellowMessage("Item id '" + sub[1] + "' does not exist.");
                                break;
                        }
                        
			short quantity = 1;
                        if(sub.length >= 3) quantity = Short.parseShort(sub[2]);
			
                        if (ItemConstants.isPet(itemId)) {
                                player.message("You cannot create a pet with this command.");
                                break;
                        }
                        
			if (sub[0].equals("item")) {
				byte flag = 0;
                                if(player.gmLevel() < 3) {
                                    flag |= ItemConstants.ACCOUNT_SHARING;
                                    flag |= ItemConstants.UNTRADEABLE;
                                }
                                
                                MapleInventoryManipulator.addById(c, itemId, quantity, player.getName(), -1, flag, -1);
			} else {
				Item toDrop;
				if (MapleItemInformationProvider.getInstance().getInventoryType(itemId) == MapleInventoryType.EQUIP) {
					toDrop = MapleItemInformationProvider.getInstance().getEquipById(itemId);
				} else {
					toDrop = new Item(itemId, (short) 0, quantity);
				}
                                
                                toDrop.setOwner(player.getName());
                                if(player.gmLevel() < 3) {
                                    byte b = toDrop.getFlag();
                                    b |= ItemConstants.ACCOUNT_SHARING;
                                    b |= ItemConstants.UNTRADEABLE;
                                    
                                    toDrop.setFlag(b);
                                }
                                
				c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
			}
                    break; 
                    
                case "level":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !level <newlevel>");
				break;
			}
                    
                        player.loseExp(player.getExp(), false, false);
                        player.setLevel(Math.min(Integer.parseInt(sub[1]), player.getMaxLevel()) - 1);
                        
                        player.resetPlayerRates();
                        if(ServerConstants.USE_ADD_RATES_BY_LEVEL == true) player.setPlayerRates();
                        player.setWorldRates();
                        
			player.levelUp(false);
                    break;
                    
		case "levelpro":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !levelpro <newlevel>");
				break;
			}
                    
			while (player.getLevel() < Math.min(255, Integer.parseInt(sub[1]))) {
				player.levelUp(false);
			}
                    break;
                    
                case "setstat":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !setstat <newstat>");
				break;
			}
                    
                        int x;
                        try {
                            x = Integer.parseInt(sub[1]);
                            
                            if(x > Short.MAX_VALUE) x = Short.MAX_VALUE;
                            else if(x < 0) x = 0;
                            
                            player.setStr(x);
                            player.setDex(x);
                            player.setInt(x);
                            player.setLuk(x);
                            player.updateSingleStat(MapleStat.STR, x);
                            player.updateSingleStat(MapleStat.DEX, x);
                            player.updateSingleStat(MapleStat.INT, x);
                            player.updateSingleStat(MapleStat.LUK, x);
                            
                        } catch(NumberFormatException nfe) {}
                    break;
                    
                case "maxstat":
			final String[] s = {"setstat", String.valueOf(Short.MAX_VALUE)};
			executeSolaxiaCommandLv2(cserv, srv, c, s);
                        player.loseExp(player.getExp(), false, false);
			player.setLevel(255);
                        
                        player.resetPlayerRates();
                        if(ServerConstants.USE_ADD_RATES_BY_LEVEL == true) player.setPlayerRates();
                        player.setWorldRates();
			
                        player.setFame(13337);
			player.setMaxHp(30000);
			player.setMaxMp(30000);
			player.updateSingleStat(MapleStat.LEVEL, 255);
			player.updateSingleStat(MapleStat.FAME, 13337);
			player.updateSingleStat(MapleStat.MAXHP, 30000);
			player.updateSingleStat(MapleStat.MAXMP, 30000);
                        player.yellowMessage("Stats maxed out.");
                    break;
                    
		case "maxskill":
			for (MapleData skill_ : MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/" + "String.wz")).getData("Skill.img").getChildren()) {
				try {
					skill = SkillFactory.getSkill(Integer.parseInt(skill_.getName()));
                                        player.changeSkillLevel(skill, (byte) skill.getMaxLevel(), skill.getMaxLevel(), -1);
				} catch (NumberFormatException nfe) {
                                        nfe.printStackTrace();
					break;
				} catch (NullPointerException npe) {
					continue;
				}
			}
                        
                        if(player.getJob().isA(MapleJob.ARAN1) || player.getJob().isA(MapleJob.LEGEND)) {
                                skill = SkillFactory.getSkill(5001005);
                                player.changeSkillLevel(skill, (byte) -1, -1, -1);
                        } else {
                                skill = SkillFactory.getSkill(21001001);
                                player.changeSkillLevel(skill, (byte) -1, -1, -1);
                        }
                        
                        player.yellowMessage("Skills maxed out.");
                    break;
                    
                case "mesos":
                        if (sub.length >= 2) {
                                player.gainMeso(Integer.parseInt(sub[1]), true);
                        }
                    break;
                    
                case "search":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !search <type> <name>");
				break;
			}
                    
			StringBuilder sb = new StringBuilder();
			
                        String search = joinStringFrom(sub, 2);
                        long start = System.currentTimeMillis();//for the lulz
                        MapleData data = null;
                        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File("wz/String.wz"));
                        if (!sub[1].equalsIgnoreCase("ITEM")) {
                                if (sub[1].equalsIgnoreCase("NPC")) {
                                        data = dataProvider.getData("Npc.img");
                                } else if (sub[1].equalsIgnoreCase("MOB") || sub[1].equalsIgnoreCase("MONSTER")) {
                                        data = dataProvider.getData("Mob.img");
                                } else if (sub[1].equalsIgnoreCase("SKILL")) {
                                        data = dataProvider.getData("Skill.img");
                                /*} else if (sub[1].equalsIgnoreCase("MAP")) {
                                        TODO
                                */
                                } else {
                                        sb.append("#bInvalid search.\r\nSyntax: '!search [type] [name]', where [type] is NPC, ITEM, MOB, or SKILL.");
                                }
                                if (data != null) {
                                        String name;
                                        for (MapleData searchData : data.getChildren()) {
                                                name = MapleDataTool.getString(searchData.getChildByPath("name"), "NO-NAME");
                                                if (name.toLowerCase().contains(search.toLowerCase())) {
                                                        sb.append("#b").append(Integer.parseInt(searchData.getName())).append("#k - #r").append(name).append("\r\n");
                                                }
                                        }
                                }
                        } else {
                                for (Pair<Integer, String> itemPair : MapleItemInformationProvider.getInstance().getAllItems()) {
                                        if (sb.length() < 32654) {//ohlol
                                                if (itemPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                                                        //#v").append(id).append("# #k- 
                                                        sb.append("#b").append(itemPair.getLeft()).append("#k - #r").append(itemPair.getRight()).append("\r\n");
                                                }
                                        } else {
                                                sb.append("#bCouldn't load all items, there are too many results.\r\n");
                                                break;
                                        }
                                }
                        }
                        if (sb.length() == 0) {
                                sb.append("#bNo ").append(sub[1].toLowerCase()).append("s found.\r\n");
                        }
                        sb.append("\r\n#kLoaded within ").append((double) (System.currentTimeMillis() - start) / 1000).append(" seconds.");//because I can, and it's free
			
			c.announce(MaplePacketCreator.getNPCTalk(9010000, (byte) 0, sb.toString(), "00 00", (byte) 0));
                    break;
                    
                case "jail":
                        if (sub.length < 2) {
				player.yellowMessage("Syntax: !jail <playername> [<minutes>]");
				break;
			}
                        
                        int minutesJailed = 5;
                        if(sub.length >= 3) {
                                minutesJailed = Integer.valueOf(sub[2]);
                                if(minutesJailed <= 0) {
                                        player.yellowMessage("Syntax: !jail <playername> [<minutes>]");
                                        break;
                                }
                        }
                    
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if (victim != null) {
                                victim.addJailExpirationTime(minutesJailed * 60 * 1000);
                            
                                int mapid = 300000012;
                                
                                if(victim.getMapId() != mapid) {    // those gone to jail won't be changing map anyway
                                        MapleMap target = cserv.getMapFactory().getMap(mapid);
                                        MaplePortal targetPortal = target.getPortal(0);
                                        victim.changeMap(target, targetPortal);
                                        player.message(victim.getName() + " was jailed for " + minutesJailed + " minutes.");
                                }
                                else {
                                        player.message(victim.getName() + "'s time in jail has been extended for " + minutesJailed + " minutes.");
                                }
                                
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break; 
                    
                case "unjail":
                        if (sub.length < 2) {
				player.yellowMessage("Syntax: !unjail <playername>");
				break;
			}
                        
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if (victim != null) {
                                if(victim.getJailExpirationTimeLeft() <= 0) {
                                    player.message("This player is already free.");
                                    break;
                                }
                                victim.removeJailExpirationTime();
                                victim.message("By lack of concrete proof you are now unjailed. Enjoy freedom!");
                                player.message(victim.getName() + " was unjailed.");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
                case "job":
			if (sub.length == 2) {
				player.changeJob(MapleJob.getById(Integer.parseInt(sub[1])));
				player.equipChanged();
			} else if (sub.length == 3) {
				victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
                                
                                if(victim != null) {
                                        victim.changeJob(MapleJob.getById(Integer.parseInt(sub[2])));
                                        player.equipChanged();
                                } else {
                                        player.message("Player '" + sub[1] + "' could not be found on this channel.");
                                }
			} else {
				player.message("Syntax: !job <job id> <opt: IGN of another person>");
			}
			break;
                            
                case "unbug":
                        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.enableActions());
                        break;
                    
                default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommandLv3(Channel cserv, Server srv, MapleClient c, String[] sub) { //GM
                MapleCharacter player = c.getPlayer();
                MapleCharacter victim;
            
                switch(sub[0]) {
                case "fly":
                        if (sub.length < 2) {
				player.yellowMessage("Syntax: !fly <on/off>");
				break;
			}
                        
                        Integer accid = c.getAccID();
                        
                        String sendStr = "";
                        if(sub[1].equalsIgnoreCase("on")) {
                                sendStr += "Enabled Fly feature (F1). With fly active, you cannot attack.";
                                if(!srv.canFly(accid)) sendStr += " Re-login to take effect.";
                            
                                srv.changeFly(c.getAccID(), true);
                        } else {
                                sendStr += "Disabled Fly feature. You can now attack.";
                                if(srv.canFly(accid)) sendStr += " Re-login to take effect.";
                                
                                srv.changeFly(c.getAccID(), false);
                        }
                        
                        player.dropMessage(6, sendStr);
                break;
                    
		case "spawn":
                        if (sub.length < 2) {
				player.yellowMessage("Syntax: !spawn <mobid>");
				break;
			}
                    
			MapleMonster monster = MapleLifeFactory.getMonster(Integer.parseInt(sub[1]));
			if (monster == null) {
				break;
			}
			if (sub.length > 2) {
				for (int i = 0; i < Integer.parseInt(sub[2]); i++) {
					player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), player.getPosition());
				}
			} else {
				player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(Integer.parseInt(sub[1])), player.getPosition());
			}
                    break;
  
		case "mutemap":
			if(player.getMap().isMuted()) {
				player.getMap().setMuted(false);
				player.dropMessage(5, "The map you are in has been un-muted.");
			} else {
				player.getMap().setMuted(true);
				player.dropMessage(5, "The map you are in has been muted.");
			}
                break;
                    
                case "checkdmg":
			victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                int maxBase = victim.calculateMaxBaseDamage(victim.getTotalWatk());
                                Integer watkBuff = victim.getBuffedValue(MapleBuffStat.WATK);
                                Integer matkBuff = victim.getBuffedValue(MapleBuffStat.MATK);
                                Integer blessing = victim.getSkillLevel(10000000 * player.getJobType() + 12);
                                if(watkBuff == null) watkBuff = 0;
                                if(matkBuff == null) matkBuff = 0;

                                player.dropMessage(5, "Cur Str: " + victim.getTotalStr() + " Cur Dex: " + victim.getTotalDex() + " Cur Int: " + victim.getTotalInt() + " Cur Luk: " + victim.getTotalLuk());
                                player.dropMessage(5, "Cur WATK: " + victim.getTotalWatk() + " Cur MATK: " + victim.getTotalMagic());
                                player.dropMessage(5, "Cur WATK Buff: " + watkBuff + " Cur MATK Buff: " + matkBuff + " Cur Blessing Level: " + blessing);
                                player.dropMessage(5, victim.getName() + "'s maximum base damage (before skills) is " + maxBase);
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this world.");
                        }
                    break;
                    
		case "inmap":
			String st = "";
			for (MapleCharacter chr : player.getMap().getCharacters()) {
				st += chr.getName() + " ";
			}
			player.message(st);
                    break;
         
		case "reloadevents":
			for (Channel ch : Server.getInstance().getAllChannels()) {
				ch.reloadEventScriptManager();
			}
			player.dropMessage(5, "Reloaded Events");
                    break;
                    
		case "reloaddrops":
			MapleMonsterInformationProvider.getInstance().clearDrops();
			player.dropMessage(5, "Reloaded Drops");
                    break;
                    
		case "reloadportals":
			PortalScriptManager.getInstance().reloadPortalScripts();
			player.dropMessage(5, "Reloaded Portals");
                    break;
                    
                case "reloadmap":
			MapleMap oldMap = c.getPlayer().getMap();
			MapleMap newMap = c.getChannelServer().getMapFactory().getMap(player.getMapId());
			for (MapleCharacter ch : oldMap.getCharacters()) {
				ch.changeMap(newMap);
			}
			oldMap = null;
			newMap.respawn();
                    break;

               case "hpmp":
                        victim = player;
                        int statUpdate = 1;
                        
                        if (sub.length >= 3) {
                                victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
                                statUpdate = Integer.valueOf(sub[2]);
                        } else if(sub.length == 2) {
                                statUpdate = Integer.valueOf(sub[1]);
                        } else {
				player.yellowMessage("Syntax: !sethpmp [<playername>] <value>");
                        }
                    
                        if(victim != null) {
                                victim.setHp(statUpdate);
                                victim.setMp(statUpdate);
                                victim.updateSingleStat(MapleStat.HP, statUpdate);
                                victim.updateSingleStat(MapleStat.MP, statUpdate);

                                victim.checkBerserk(victim.isHidden());
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this world.");
                        }
                    break;
                   
		case "music":
			if (sub.length < 2) {
				player.yellowMessage("Syntax: !music <song>");
				for (String s : songs){
					player.yellowMessage(s);
				}
				break;
			}
			String song = joinStringFrom(sub, 1); 
			for (String s : songs){
				if (s.equals(song)){
					player.getMap().broadcastMessage(MaplePacketCreator.musicChange(s));
					player.yellowMessage("Now playing song " + song + ".");
					break;
				}
			}
			player.yellowMessage("Song not found, please enter a song below.");
			for (String s : songs){
				player.yellowMessage(s);
			}
                    break;
                    
		case "monitor":
			if (sub.length < 1){
				player.yellowMessage("Syntax: !monitor <ign>");
				break;
			}
			victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
			if (victim == null){
				player.message("Player '" + sub[1] + "' could not be found on this world.");
				break;
			}
			boolean monitored = MapleLogger.monitored.contains(victim.getName());
			if (monitored){
				MapleLogger.monitored.remove(victim.getName());
			} else {
				MapleLogger.monitored.add(victim.getName());
			}
			player.yellowMessage(victim.getName() + " is " + (!monitored ? "now being monitored." : "no longer being monitored."));
			String message = player.getName() + (!monitored ? " has started monitoring " : " has stopped monitoring ") + victim.getName() + ".";
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, message));
                    break;
                    
		case "monitors":
			for (String ign : MapleLogger.monitored){
                        player.yellowMessage(ign + " is being monitored.");
			}
                    break;
                    
		case "ignore":
			if (sub.length < 1){
				player.yellowMessage("Syntax: !ignore <ign>");
				break;
			}
			victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
			if (victim == null){
				player.message("Player '" + sub[1] + "' could not be found on this world.");
				break;
			}
			boolean monitored_ = MapleLogger.ignored.contains(victim.getName());
			if (monitored_){
				MapleLogger.ignored.remove(victim.getName());
			} else {
				MapleLogger.ignored.add(victim.getName());
			}
			player.yellowMessage(victim.getName() + " is " + (!monitored_ ? "now being ignored." : "no longer being ignored."));
			String message_ = player.getName() + (!monitored_ ? " has started ignoring " : " has stopped ignoring ") + victim.getName() + ".";
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, message_));
                    break;
                    
		case "ignored":
			for (String ign : MapleLogger.ignored){
				player.yellowMessage(ign + " is being ignored.");
			}
                    break;
                    
		case "pos":
			float xpos = player.getPosition().x;
			float ypos = player.getPosition().y;
			float fh = player.getMap().getFootholds().findBelow(player.getPosition()).getId();
			player.dropMessage("Position: (" + xpos + ", " + ypos + ")");
			player.dropMessage("Foothold ID: " + fh);
                    break;
        
                case "togglecoupon":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !togglecoupon <itemid>");
				break;
			}
                        Server.getInstance().toggleCoupon(Integer.parseInt(sub[1]));
                    break;
   
		case "chat":
			player.toggleWhiteChat();
			player.message("Your chat is now " + (player.getWhiteChat() ? " white" : "normal") + ".");
                    break;
             
		case "fame":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !fame <playername> <gainfame>");
				break;
			}
                        
			victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.setFame(Integer.parseInt(sub[2]));
                                victim.updateSingleStat(MapleStat.FAME, victim.getFame());
                                player.message("FAME given.");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
		case "givenx":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !givenx <playername> <gainnx>");
				break;
			}
			
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.getCashShop().gainCash(1, Integer.parseInt(sub[2]));
                                player.message("NX given.");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
      
		case "givevp":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !givevp <playername> <gainvotepoint>");
				break;
			}
                        
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.getClient().addVotePoints(Integer.parseInt(sub[2]));
                                player.message("VP given.");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                
                case "givems":
                        if (sub.length < 3){
				player.yellowMessage("Syntax: !givems <playername> <gainms>");
				break;
			}
                        
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.gainMeso(Integer.parseInt(sub[2]), true);
                                player.message("MESO given.");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
		case "id":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !id <id>");
				break;
			}
			try {
				try (BufferedReader dis = new BufferedReader(new InputStreamReader(new URL("http://www.mapletip.com/search_java.php?search_value=" + sub[1] + "&check=true").openConnection().getInputStream()))) {
					String s;
					while ((s = dis.readLine()) != null) {
						player.dropMessage(s);
					}
				}
			} catch (Exception e) {
                                e.printStackTrace();
			}
                    break;
                    
		case "expeds":
			for (Channel ch : Server.getInstance().getChannelsFromWorld(0)) {
				if (ch.getExpeditions().size() == 0) {
					player.yellowMessage("No Expeditions in Channel " + ch.getId());
					continue;
				}
				player.yellowMessage("Expeditions in Channel " + ch.getId());
				int id = 0;
				for (MapleExpedition exped : ch.getExpeditions()) {
					id++;
					player.yellowMessage("> Expedition " + id);
					player.yellowMessage(">> Type: " + exped.getType().toString());
					player.yellowMessage(">> Status: " + (exped.isRegistering() ? "REGISTERING" : "UNDERWAY"));
					player.yellowMessage(">> Size: " + exped.getMembers().size());
					player.yellowMessage(">> Leader: " + exped.getLeader().getName());
					int memId = 2;
					for (MapleCharacter member : exped.getMembers()) {
						if (exped.isLeader(member)) {
							continue;
						}
						player.yellowMessage(">>> Member " + memId + ": " + member.getName());
						memId++;
					}
				}
			}
                    break;
                    
		case "kill":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !kill <playername>");
				break;
			}
			
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.setHpMp(0);
                                Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, player.getName() + " used !kill on " + victim.getName()));
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
		case "seed":
			if (player.getMapId() != 910010000) {
				player.yellowMessage("This command can only be used in HPQ.");
				break;
			}
			Point pos[] = {new Point(7, -207), new Point(179, -447), new Point(-3, -687), new Point(-357, -687), new Point(-538, -447), new Point(-359, -207)};
			int seed[] = {4001097, 4001096, 4001095, 4001100, 4001099, 4001098};
			for (int i = 0; i < pos.length; i++) {
				Item item = new Item(seed[i], (byte) 0, (short) 1);
				player.getMap().spawnItemDrop(player, player, item, pos[i], false, true);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
                    break;
                
                case "maxenergy":
                        c.getPlayer().setDojoEnergy(10000);
                        c.announce(MaplePacketCreator.getEnergy("energy", 10000));
                        break;
                    
		case "killall":
			List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
			MapleMap map = player.getMap();
			for (MapleMapObject monstermo : monsters) {
				monster = (MapleMonster) monstermo;
				if (!monster.getStats().isFriendly()) {
					map.killMonster(monster, player, true);
					//monster.giveExpToCharacter(player, monster.getExp() * c.getPlayer().getExpRate(), true, 1);
				}
			}
			player.dropMessage("Killed " + monsters.size() + " monsters.");
                    break;
           
		case "notice":
			Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[Notice] " + joinStringFrom(sub, 1)));
                    break;
                    
		case "rip":
			Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[RIP]: " + joinStringFrom(sub, 1)));
                    break;
                    
		case "openportal":
                        if (sub.length < 2){
                            player.yellowMessage("Syntax: !openportal <portalid>");
                            break;
			}
			player.getMap().getPortal(sub[1]).setPortalState(true);
                    break;
                    
                case "closeportal":
                        if (sub.length < 2){
                            player.yellowMessage("Syntax: !closeportal <portalid>");
                            break;
			}
			player.getMap().getPortal(sub[1]).setPortalState(false);
                    break;
                    
		case "pe":
			String packet = "";
			try {
				InputStreamReader is = new FileReader("pe.txt");
				Properties packetProps = new Properties();
				packetProps.load(is);
				is.close();
				packet = packetProps.getProperty("pe");
			} catch (IOException ex) {
                                ex.printStackTrace();
				player.yellowMessage("Failed to load pe.txt");
				break;
			}
			MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
			mplew.write(HexTool.getByteArrayFromHexString(packet));
			SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(mplew.getPacket()));
			short packetId = slea.readShort();
			final MaplePacketHandler packetHandler = PacketProcessor.getProcessor(0, c.getChannel()).getHandler(packetId);
			if (packetHandler != null && packetHandler.validateState(c)) {
				try {
					player.yellowMessage("Receiving: " + packet);
					packetHandler.handlePacket(slea, c);
				} catch (final Throwable t) {
					FilePrinter.printError(FilePrinter.PACKET_HANDLER + packetHandler.getClass().getName() + ".txt", t, "Error for " + (c.getPlayer() == null ? "" : "player ; " + c.getPlayer() + " on map ; " + c.getPlayer().getMapId() + " - ") + "account ; " + c.getAccountName() + "\r\n" + slea.toString());
					break;
				}
			}
                    break;
        
		case "startevent":
			int players = 50;
			if(sub.length > 1)
				players = Integer.parseInt(sub[1]);
			
			c.getChannelServer().setEvent(new MapleEvent(player.getMapId(), players));
			player.dropMessage(5, "The event has been set on " + player.getMap().getMapName() + " and will allow " + players + " players to join.");
                    break;
                    
		case "endevent":
			c.getChannelServer().setEvent(null);
			player.dropMessage(5, "You have ended the event. No more players may join.");
                    break;
                    
		case "online2":
			int total = 0;
			for (Channel ch : srv.getChannelsFromWorld(player.getWorld())) {
				int size = ch.getPlayerStorage().getAllCharacters().size();
				total += size;
				String s = "(Channel " + ch.getId() + " Online: " + size + ") : ";
				if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
					for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
						s += MapleCharacter.makeMapleReadable(chr.getName()) + ", ";
					}
					player.dropMessage(6, s.substring(0, s.length() - 2));
				}
			}
			player.dropMessage(6, "There are a total of " + total + " players online.");
                        player.showHint("Players online: #e#r" + total + "#k#n.");
                    break;
       
		case "warpsnowball":
			List<MapleCharacter> chars = new ArrayList<>(player.getMap().getCharacters());
			for (MapleCharacter chr : chars) {
				chr.changeMap(109060000, chr.getTeam());
			}
                    break;
                    
		case "ban":
			if (sub.length < 3) {
				player.yellowMessage("Syntax: !ban <IGN> <Reason> (Please be descriptive)");
				break;
			}
			String ign = sub[1];
			String reason = joinStringFrom(sub, 2);
			MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(ign);
			if (target != null) {
				String readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
				String ip = target.getClient().getSession().getRemoteAddress().toString().split(":")[0];
				//Ban ip
				PreparedStatement ps = null;
				try {
					Connection con = DatabaseConnection.getConnection();
					if (ip.matches("/[0-9]{1,3}\\..*")) {
						ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?, ?)");
						ps.setString(1, ip);
                                                ps.setString(2, String.valueOf(target.getClient().getAccID()));
                                                
						ps.executeUpdate();
						ps.close();
					}
                                        
                                        con.close();
				} catch (SQLException ex) {
                                        ex.printStackTrace();
					c.getPlayer().message("Error occured while banning IP address");
					c.getPlayer().message(target.getName() + "'s IP was not banned: " + ip);
				}
				target.getClient().banMacs();
				reason = c.getPlayer().getName() + " banned " + readableTargetName + " for " + reason + " (IP: " + ip + ") " + "(MAC: " + c.getMacs() + ")";
				target.ban(reason);
				target.yellowMessage("You have been banned by #b" + c.getPlayer().getName() + " #k.");
				target.yellowMessage("Reason: " + reason);
				c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
				final MapleCharacter rip = target;
				TimerManager.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
						rip.getClient().disconnect(false, false);
					}
				}, 5000); //5 Seconds
				Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[RIP]: " + ign + " has been banned."));
			} else if (MapleCharacter.ban(ign, reason, false)) {
				c.announce(MaplePacketCreator.getGMEffect(4, (byte) 0));
				Server.getInstance().broadcastMessage(MaplePacketCreator.serverNotice(6, "[RIP]: " + ign + " has been banned."));
			} else {
				c.announce(MaplePacketCreator.getGMEffect(6, (byte) 1));
			}
                    break;
                    
                case "unban":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !unban <playername>");
				break;
			}
                    
			try {
                                Connection con = DatabaseConnection.getConnection();
                                int aid = MapleCharacter.getAccountIdByName(sub[1]);
                                
                                PreparedStatement p = con.prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + aid);
                                p.executeUpdate();
                            
				p = con.prepareStatement("DELETE FROM ipbans WHERE aid = " + aid);
				p.executeUpdate();
                                        
                                p = con.prepareStatement("DELETE FROM macbans WHERE aid = " + aid);
				p.executeUpdate();
                                
                                con.close();
			} catch (Exception e) {
                                e.printStackTrace();
				player.message("Failed to unban " + sub[1]);
				return true;
			}
			player.message("Unbanned " + sub[1]);
                    break;

                case "healmap":
                        for (MapleCharacter mch : player.getMap().getCharacters()) {
                                if (mch != null) {
                                        mch.setHp(mch.getMaxHp());
                                        mch.updateSingleStat(MapleStat.HP, mch.getMaxHp());
                                        mch.setMp(mch.getMaxMp());
                                        mch.updateSingleStat(MapleStat.MP, mch.getMaxMp());
                                }
                        }
                    break;
                    
                case "healperson":
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.setHp(victim.getMaxHp());
                                victim.updateSingleStat(MapleStat.HP, victim.getMaxHp());
                                victim.setMp(victim.getMaxMp());
                                victim.updateSingleStat(MapleStat.MP, victim.getMaxMp());
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
                case "hurt":
                        victim = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                victim.setHp(1);
                                victim.updateSingleStat(MapleStat.HP, 1);
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this channel.");
                        }
                    break;
                    
                case "killmap":
                        for (MapleCharacter mch : player.getMap().getCharacters()) {
                                mch.setHp(0);
                                mch.updateSingleStat(MapleStat.HP, 0);
                        }
                    break;
                    
                case "night":
                        player.getMap().broadcastNightEffect();
                        player.yellowMessage("Done.");
                    break;
                    
                case "npc":
			if (sub.length < 2){
				player.yellowMessage("Syntax: !npc <npcid>");
				break;
			}
			MapleNPC npc = MapleLifeFactory.getNPC(Integer.parseInt(sub[1]));
			if (npc != null) {
				npc.setPosition(player.getPosition());
				npc.setCy(player.getPosition().y);
				npc.setRx0(player.getPosition().x + 50);
				npc.setRx1(player.getPosition().x - 50);
				npc.setFh(player.getMap().getFootholds().findBelow(c.getPlayer().getPosition()).getId());
				player.getMap().addMapObject(npc);
				player.getMap().broadcastMessage(MaplePacketCreator.spawnNPC(npc));
			}
			break;
                    
                case "face":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !face [<playername>] <faceid>");
				break;
			}
                    
                        try {
                                if (sub.length == 2) {
                                        int itemId = Integer.parseInt(sub[1]);
                                        if(!(itemId >= 20000 && itemId < 22000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                                                player.yellowMessage("Face id '" + sub[1] + "' does not exist.");
                                                break;
                                        }
                                    
                                        player.setFace(itemId);
                                        player.updateSingleStat(MapleStat.FACE, itemId);
                                        player.equipChanged();
                                } else {
                                        int itemId = Integer.parseInt(sub[2]);
                                        if(!(itemId >= 20000 && itemId < 22000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                                                player.yellowMessage("Face id '" + sub[2] + "' does not exist.");
                                                break;
                                        }
                                    
                                        victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
                                        if(victim == null) {
                                                victim.setFace(itemId);
                                                victim.updateSingleStat(MapleStat.FACE, itemId);
                                                victim.equipChanged();
                                        } else {
                                                player.yellowMessage("Player '" + sub[1] + "' has not been found on this channel.");
                                        }
                                }
                        } catch(Exception e) {}
                        
			break;
                    
                case "hair":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !hair [<playername>] <hairid>");
				break;
			}
                    
                        try {
                                if (sub.length == 2) {
                                        int itemId = Integer.parseInt(sub[1]);
                                        if(!(itemId >= 30000 && itemId < 35000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                                                player.yellowMessage("Hair id '" + sub[1] + "' does not exist.");
                                                break;
                                        }
                                    
                                        player.setHair(itemId);
                                        player.updateSingleStat(MapleStat.HAIR, itemId);
                                        player.equipChanged();
                                } else {
                                        int itemId = Integer.parseInt(sub[2]);
                                        if(!(itemId >= 30000 && itemId < 35000) || MapleItemInformationProvider.getInstance().getName(itemId) == null) {
                                                player.yellowMessage("Hair id '" + sub[2] + "' does not exist.");
                                                break;
                                        }
                                    
                                        victim = c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]);
                                        if(victim != null) {
                                                victim.setHair(itemId);
                                                victim.updateSingleStat(MapleStat.HAIR, itemId);
                                                victim.equipChanged();
                                        } else {
                                                player.yellowMessage("Player '" + sub[1] + "' has not been found on this channel.");
                                        }
                                }
                        } catch(Exception e) {}
			break;
                    
                default:
                        return false;
                }
                
                return true;
        }
        
        
        public static boolean executeSolaxiaCommandLv4(Channel cserv, Server srv, MapleClient c, String[] sub) { //SuperGM
                MapleCharacter player = c.getPlayer();
        
                switch(sub[0]) {
                    case "servermessage":
			c.getWorldServer().setServerMessage(joinStringFrom(sub, 1));
                        break;
                        
                    case "proitem":
                        if (sub.length < 3) {
                                player.yellowMessage("Syntax: !proitem <itemid> <statvalue>");
                                break;
                        }
                        
                        int itemid = Integer.parseInt(sub[1]);
                        short multiply = Short.parseShort(sub[2]);

                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        MapleInventoryType type = ii.getInventoryType(itemid);
                        if (type.equals(MapleInventoryType.EQUIP)) {
                                Item it = ii.getEquipById(itemid);
                                it.setOwner(player.getName());
                                
                                hardsetItemStats((Equip) it, multiply);
                                MapleInventoryManipulator.addFromDrop(c, it);
                        } else {
                                player.dropMessage("Make sure it's an equippable item.");
                        }
                        break;
                    
                    case "seteqstat":
                        if (sub.length < 2) {
                                player.yellowMessage("Syntax: !seteqstat <statvalue>");
                                break;
                        }
                        
                        int newStat = Integer.parseInt(sub[1]);
                        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
                        
                        for (byte i = 1; i <= equip.getSlotLimit(); i++) {
                                try {
                                        Equip eu = (Equip) equip.getItem(i);
                                        if(eu == null) continue;
                                        
                                        short incval = (short)newStat;
                                        eu.setWdef(incval);
                                        eu.setAcc(incval);
                                        eu.setAvoid(incval);
                                        eu.setJump(incval);
                                        eu.setMatk(incval);
                                        eu.setMdef(incval);
                                        eu.setHp(incval);
                                        eu.setMp(incval);
                                        eu.setSpeed(incval);
                                        eu.setWatk(incval);
                                        eu.setDex(incval);
                                        eu.setInt(incval);
                                        eu.setStr(incval);
                                        eu.setLuk(incval);
                                        
                                        byte flag = eu.getFlag();
                                        flag |= ItemConstants.UNTRADEABLE;
                                        eu.setFlag(flag);
                                        
                                        player.forceUpdateItem(eu);
                                } catch(Exception e){
                                        e.printStackTrace();
                                }
                        }
                        break;
                    
                    case "exprate":
                            if (sub.length < 2){
                                player.yellowMessage("Syntax: !exprate <newrate>");
                                break;
                            }
                            
                            int exprate = Math.max(Integer.parseInt(sub[1]), 1);
                            c.getWorldServer().setExpRate(exprate);
                            c.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, "[Rate] Exp Rate has been changed to " + exprate + "x."));
                        break;
                        
                    case "mesorate":
                            if (sub.length < 2){
                                player.yellowMessage("Syntax: !mesorate <newrate>");
                                break;
                            }
                            
                            int mesorate = Math.max(Integer.parseInt(sub[1]), 1);
                            c.getWorldServer().setMesoRate(mesorate);
                            c.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, "[Rate] Meso Rate has been changed to " + mesorate + "x."));
                        break;
                    
                    case "droprate":
                            if (sub.length < 2){
                                player.yellowMessage("Syntax: !droprate <newrate>");
                                break;
                            }		
                            
                            int droprate = Math.max(Integer.parseInt(sub[1]), 1);
                            c.getWorldServer().setDropRate(droprate);
                            c.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, "[Rate] Drop Rate has been changed to " + droprate + "x."));
                        break;
                    
                    case "bossdroprate":
                            if (sub.length < 2){
                                player.yellowMessage("Syntax: !bossdroprate <newrate>");
                                break;
                            }
                            
                            int bossdroprate = Math.max(Integer.parseInt(sub[1]), 1);
                            c.getWorldServer().setBossDropRate(bossdroprate);
                            c.getWorldServer().broadcastPacket(MaplePacketCreator.serverNotice(6, "[Rate] Boss Drop Rate has been changed to " + bossdroprate + "x."));
                        break;
                    
                    case "itemvac":
                            List<MapleMapObject> list = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
                            for (MapleMapObject item : list) {
                                MapleMapItem mapItem = (MapleMapItem) item;
                                int ownerId = mapItem.getOwner();
                                
                                if(ownerId <= 0 || player.getId() == ownerId || player.isPartyMember(ownerId) || System.currentTimeMillis() - mapItem.getDropTime() >= 15 * 1000) {
                                    player.pickupItem(item);
                                }
                            }
                        break;
                            
                    case "forcevac":
                            List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
                            for (MapleMapObject item : items) {
                                MapleMapItem mapItem = (MapleMapItem) item;
                                if (mapItem.getMeso() > 0) {
                                        player.gainMeso(mapItem.getMeso(), true);
                                } else if(mapItem.getItemId() == 4031865 || mapItem.getItemId() == 4031866) {
                                        // Add NX to account, show effect and make item disappear
                                        player.getCashShop().gainCash(1, mapItem.getItemId() == 4031865 ? 100 : 250);
                                } else if (mapItem.getItem().getItemId() >= 5000000 && mapItem.getItem().getItemId() <= 5000100) {
                                        int petId = MaplePet.createPet(mapItem.getItem().getItemId());
                                        if (petId == -1) {
                                                continue;
                                        }
                                        MapleInventoryManipulator.addById(c, mapItem.getItem().getItemId(), mapItem.getItem().getQuantity(), null, petId);
                                } else {
                                        MapleInventoryManipulator.addFromDrop(c, mapItem.getItem(), true);
                                }
                                
                                player.getMap().pickItemDrop(MaplePacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, player.getId()), mapItem);
                            }
                        break;
                            
                    case "zakum":
                            player.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), player.getPosition());
                            for (int x = 8800003; x < 8800011; x++) {
                                    player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(x), player.getPosition());
                            }
                        break;
                    
                    case "horntail":
                            final Point targetPoint = player.getPosition();
                            final MapleMap targetMap = player.getMap();
                        
                            targetMap.spawnHorntailOnGroundBelow(targetPoint);
                        break;
                    
                    case "pinkbean":
                        player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8820001), player.getPosition());
                        break;
                            
                    case "pap":
			player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8500001), player.getPosition());
                        break;
                    
                    case "pianus":
			player.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8510000), player.getPosition());
                        break;
                        
                    case "cake":
                        MapleMonster monster = MapleLifeFactory.getMonster(9400606);
                        if(sub.length > 1) {
                            int newHp = Integer.parseInt(sub[1]);
                            if(newHp < 0) newHp = Integer.MAX_VALUE;
                            
                            monster.getStats().setHp(newHp);
                            monster.setHp(newHp);
                        }
                        
                        player.getMap().spawnMonsterOnGroundBelow(monster, player.getPosition());
                        break;
                            
                    /*
                    case "playernpc":
                        if (sub.length < 3){
                            player.yellowMessage("Syntax: !playernpc <playername> <npcid>");
                            break;
                        }
                        player.playerNPC(c.getChannelServer().getPlayerStorage().getCharacterByName(sub[1]), Integer.parseInt(sub[2]));
			break;
                    */
                        
                    default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommandLv5(Channel cserv, Server srv, MapleClient c, String[] sub) { //Developer
                MapleCharacter player = c.getPlayer();
                MapleMonster monster;
        
                switch(sub[0]) {
                case "debugmonster":
			List<MapleMapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
			for (MapleMapObject monstermo : monsters) {
				monster = (MapleMonster) monstermo;
				player.message("Monster ID: " + monster.getId() + " Aggro target: " + ((monster.getController() != null) ? monster.getController().getName() : "<none>"));
			}
                    break;
                    
		
                    
                case "debugpacket":
                        player.getMap().broadcastMessage(MaplePacketCreator.customPacket(joinStringFrom(sub, 1)));
                        break;
                    
                case "debugportal":
                        MaplePortal portal = player.getMap().findClosestPortal(player.getPosition());
                        if(portal != null) player.dropMessage(6, "Closest portal: " + portal.getId() + " '" + portal.getName() + "' Type: " + portal.getType() + " --> toMap: " + portal.getTargetMapId() + " scriptname: '" + portal.getScriptName() + "' state: " + portal.getPortalState() + ".");
                        else player.dropMessage(6, "There is no portal on this map.");
                        
                        break;
                
                case "debugspawnpoint":
                        SpawnPoint sp = player.getMap().findClosestSpawnpoint(player.getPosition());
                        if(sp != null) player.dropMessage(6, "Closest mob spawn point: " + " Position: x " + sp.getPosition().getX() + " y " + sp.getPosition().getY() + " Spawns mobid: '" + sp.getMonsterId() + "' --> canSpawn: " + !sp.getDenySpawn() + " canSpawnRightNow: " + sp.shouldSpawn() + ".");
                        else player.dropMessage(6, "There is no mob spawn point on this map.");
                        
                        break;
                    
                case "debugpos":
                        player.dropMessage(6, "Current map position: (" + player.getPosition().getX() + ", " + player.getPosition().getY() + ").");
                        break;
                    
                case "debugmap":
                        player.dropMessage(6, "Current map id " + player.getMap().getId() + ", event: '" + ((player.getMap().getEventInstance() != null) ? player.getMap().getEventInstance().getName() : "null") + "'; Players: " + player.getMap().getAllPlayers().size() + ", Mobs: " + player.getMap().countMonsters() + ", Reactors: " + player.getMap().countReactors() + ", Items: " + player.getMap().countItems() + ", Objects: " + player.getMap().getMapObjects().size() + ".");
                        break;
                    
                case "debugmobsp":
                        player.getMap().reportMonsterSpawnPoints(player);
                        break;
                    
                case "debugevent":
                        if(player.getEventInstance() == null) player.dropMessage(6, "Player currently not in an event.");
                        else player.dropMessage(6, "Current event name: " + player.getEventInstance().getName() + ".");
                        
                        break;
                    
                case "debugareas":
                        player.dropMessage(6, "Configured areas on map " + player.getMapId() + ":");

                        byte index = 0;
                        for(Rectangle rect: player.getMap().getAreas()) {
                            player.dropMessage(6, "Id: " + index + " -> posX: " + rect.getX() + " posY: '" + rect.getY() + "' dX: " + rect.getWidth() + " dY: " + rect.getHeight() + ".");
                            index++;
                        }
                        
                        break;
                
                case "debugreactors":
                        player.dropMessage(6, "Current reactor states on map " + player.getMapId() + ":");

                        for(MapleMapObject mmo: player.getMap().getReactors()) {
                            MapleReactor mr = (MapleReactor) mmo;
                            player.dropMessage(6, "Id: " + mr.getId() + " Oid: " + mr.getObjectId() + " name: '" + mr.getName() + "' -> Type: " + mr.getReactorType() + " State: " + mr.getState() + " Event State: " + mr.getEventState() + " Position: x " + mr.getPosition().getX() + " y " + mr.getPosition().getY() + ".");
                        }
                        
                        break;
                    
                case "debugservercoupons":
                case "debugcoupons":
                        String s = "Currently active SERVER coupons: ";
                        for(Integer i : Server.getInstance().getActiveCoupons()) {
                            s += (i + " ");
                        }

                        player.dropMessage(6, s);
                        
                        break;
                    
                case "debugplayercoupons":
                        String st = "Currently active PLAYER coupons: ";
                        for(Integer i : player.getActiveCoupons()) {
                            st += (i + " ");
                        }

                        player.dropMessage(6, st);
                        
                        break;
                        
                case "debugtimer":
			TimerManager tMan = TimerManager.getInstance();
			player.dropMessage(6, "Total Task: " + tMan.getTaskCount() + " Current Task: " + tMan.getQueuedTasks() + " Active Task: " + tMan.getActiveCount() + " Completed Task: " + tMan.getCompletedTaskCount());
			break;
                    
                default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommandLv6(Channel cserv, Server srv, MapleClient c, String[] sub) { //Admin
                MapleCharacter player = c.getPlayer();
                MapleCharacter victim;
        
                switch(sub[0]) {
                case "setgmlevel":
                        if (sub.length < 3){
                                player.yellowMessage("Syntax: !setgmlevel <playername> <newlevel>");
                                break;
                        }

                        int newLevel = Integer.parseInt(sub[2]);
                        MapleCharacter target = cserv.getPlayerStorage().getCharacterByName(sub[1]);
                        if(target != null) {
                            target.setGMLevel(newLevel);
                            target.getClient().setGMLevel(newLevel);
                            
                            target.dropMessage("You are now a level " + newLevel + " GM. See @commands for a list of available commands.");
                            player.dropMessage(target + " is now a level " + newLevel + " GM.");
                        } else {
                            player.dropMessage("Player '"+ sub[1] +"' was not found on this channel.");
                        }
                        break;
                    
		case "warpworld":
                        if (sub.length < 2){
				player.yellowMessage("Syntax: !warpworld <worldid>");
				break;
			}
                    
			Server server = Server.getInstance();
			byte worldb = Byte.parseByte(sub[1]);
			if (worldb <= (server.getWorlds().size() - 1)) {
				try {
					String[] socket = server.getIP(worldb, c.getChannel()).split(":");
					c.getWorldServer().removePlayer(player);
					player.getMap().removePlayer(player);//LOL FORGOT THIS ><                    
					c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
					player.setWorld(worldb);
					player.saveToDB();//To set the new world :O (true because else 2 player instances are created, one in both worlds)
					c.announce(MaplePacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
				} catch (UnknownHostException | NumberFormatException ex) {
                                        ex.printStackTrace();
					player.message("Error when trying to change worlds, are you sure the world you are trying to warp to has the same amount of channels?");
				}

			} else {
				player.message("Invalid world; highest number available: " + (server.getWorlds().size() - 1));
			}
			break;
                    
		case "saveall":
			for (World world : Server.getInstance().getWorlds()) {
				for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
					chr.saveToDB();
				}
			}
			String message = player.getName() + " used !saveall.";
			Server.getInstance().broadcastGMMessage(MaplePacketCreator.serverNotice(5, message));
			player.message("All players saved successfully.");
			break;
                    
		case "dcall":
			for (World world : Server.getInstance().getWorlds()) {
				for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
					if (!chr.isGM()) {
						chr.getClient().disconnect(false, false);
					}
				}
			}
			player.message("All players successfully disconnected.");
			break;
                    
		case "mapplayers":
			String names = "";
			int map = player.getMapId();
			for (World world : Server.getInstance().getWorlds()) {
				for (MapleCharacter chr : world.getPlayerStorage().getAllCharacters()) {
					int curMap = chr.getMapId();
					String hp = Integer.toString(chr.getHp());
					String maxhp = Integer.toString(chr.getMaxHp());
					String name = chr.getName() + ": " + hp + "/" + maxhp;
					if (map == curMap) {
						names = names.equals("") ? name : (names + ", " + name);
					}
				}
			}
			player.message("These b lurkin: " + names);
			break;
                    
		case "getacc":
			if (sub.length < 2){
				player.yellowMessage("Syntax: !getacc <playername>");
				break;
			}
			victim = c.getWorldServer().getPlayerStorage().getCharacterByName(sub[1]);
                        if(victim != null) {
                                player.message(victim.getName() + "'s account name is " + victim.getClient().getAccountName() + ".");
                        } else {
                                player.message("Player '" + sub[1] + "' could not be found on this world.");
                        }
			break;

		case "shutdown":
		case "shutdownnow":
			int time = 60000;
			if (sub[0].equals("shutdownnow")) {
				time = 1;
			} else if (sub.length > 1) {
				time *= Integer.parseInt(sub[1]);
			}
                        
                        if(time > 1) {
                            int seconds = (int) (time / 1000) % 60 ;
                            int minutes = (int) ((time / (1000*60)) % 60);
                            int hours   = (int) ((time / (1000*60*60)) % 24);
                            int days    = (int) ((time / (1000*60*60*24)));

                            String strTime = "";
                            if(days > 0) strTime += days + " days, ";
                            if(hours > 0) strTime += hours + " hours, ";
                            strTime += minutes + " minutes, ";
                            strTime += seconds + " seconds";

                            for(World w : Server.getInstance().getWorlds()) {
                                for(MapleCharacter chr: w.getPlayerStorage().getAllCharacters()) {
                                    chr.dropMessage("Server is undergoing maintenance process, and will be shutdown in " + strTime + ". Prepare yourself to quit safely in the mean time.");
                                }
                            }
                        }
                        
                        
			TimerManager.getInstance().schedule(Server.getInstance().shutdown(false), time);
			break;

		case "clearquestcache":
			MapleQuest.clearCache();
			player.dropMessage(5, "Quest Cache Cleared.");
			break;
                    
		case "clearquest":
			if(sub.length < 1) {
				player.dropMessage(5, "Please include a quest ID.");
				break;
			}
			MapleQuest.clearCache(Integer.parseInt(sub[1]));
			player.dropMessage(5, "Quest Cache for quest " + sub[1] + " cleared.");
			break;
                            
                case "fred":
                        c.announce(MaplePacketCreator.fredrickMessage(Byte.valueOf(sub[1])));
                        break;
                    
                case "owl":
                        c.announce(MaplePacketCreator.getOwlMessage(Integer.valueOf(sub[1])));
                        break;
                    
                default:
                        return false;
                }
                
                return true;
        }
        
        public static boolean executeSolaxiaCommand(Channel cserv, Server srv, MapleClient c, String[] sub, int gmLevel) {
                if(gmLevel == -1) {
                        c.getPlayer().yellowMessage("Command '" + sub[0] + "' is not available. See @commands for a list of available commands.");
                        return false;
                }
                
                boolean executedCommand;
                switch(gmLevel) {
                case 0: //Player
                        executedCommand = executeSolaxiaCommandLv0(cserv, srv, c, sub);
                        break;

                case 1: //Donator
                        executedCommand = executeSolaxiaCommandLv1(cserv, srv, c, sub);
                        break;

                case 2: //JrGM
                        executedCommand = executeSolaxiaCommandLv2(cserv, srv, c, sub);
                        break;

                case 3: //GM
                        executedCommand = executeSolaxiaCommandLv3(cserv, srv, c, sub);
                        break;

                case 4: //SuperGM
                        executedCommand = executeSolaxiaCommandLv4(cserv, srv, c, sub);
                        break;

                case 5: //Developer
                        executedCommand = executeSolaxiaCommandLv5(cserv, srv, c, sub);
                        break;

                default:    //Admin
                        executedCommand = executeSolaxiaCommandLv6(cserv, srv, c, sub);
                }
            
                if(!executedCommand) return executeSolaxiaCommand(cserv, srv, c, sub, gmLevel - 1);
                else return true;
        }
        
        public static boolean executeSolaxiaPlayerCommand(MapleClient c, String[] sub, char heading) {
		Channel cserv = c.getChannelServer();
		Server srv = Server.getInstance();
                
                return executeSolaxiaCommand(cserv, srv, c, sub, c.getPlayer().gmLevel());
        }
        
        private static String joinStringFrom(String arr[], int start) {
		StringBuilder builder = new StringBuilder();
		for (int i = start; i < arr.length; i++) {
			builder.append(arr[i]);
			if (i != arr.length - 1) {
				builder.append(" ");
			}
		}
		return builder.toString();
	}
}
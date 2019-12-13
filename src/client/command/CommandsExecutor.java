/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package client.command;

import client.command.commands.gm0.*;
import client.command.commands.gm1.*;
import client.command.commands.gm2.*;
import client.command.commands.gm3.*;
import client.command.commands.gm4.*;
import client.command.commands.gm5.*;
import client.command.commands.gm6.*;

import client.MapleClient;

import tools.FilePrinter;
import tools.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class CommandsExecutor {
    
    public static CommandsExecutor instance = new CommandsExecutor();
    
    public static CommandsExecutor getInstance() {
        return instance;
    }
    
    private static final char USER_HEADING = '@';
    private static final char GM_HEADING = '!';
    
    public static boolean isCommand(MapleClient client, String content){
        char heading = content.charAt(0);
        if (client.getPlayer().isGM()){
            return heading == USER_HEADING || heading == GM_HEADING;
        }
        return heading == USER_HEADING;
    }

    private HashMap<String, Command> registeredCommands = new HashMap<>();
    private Pair<List<String>, List<String>> levelCommandsCursor;
    private List<Pair<List<String>, List<String>>> commandsNameDesc = new ArrayList<>();

    private CommandsExecutor(){
        registerLv0Commands();
        registerLv1Commands();
        registerLv2Commands();
        registerLv3Commands();
        registerLv4Commands();
        registerLv5Commands();
        registerLv6Commands();
    }

    public List<Pair<List<String>, List<String>>> getGmCommands() {
        return commandsNameDesc;
    }
    
    public void handle(MapleClient client, String message){
        if (client.tryacquireClient()) {
            try {
                handleInternal(client, message);
            } finally {
                client.releaseClient();
            }
        } else {
            client.getPlayer().dropMessage(5, "Try again in a while... Latest commands are currently being processed.");
        }
    }
    
    private void handleInternal(MapleClient client, String message){
        if (client.getPlayer().getMapId() == 300000012) {
            client.getPlayer().yellowMessage("You do not have permission to use commands while in jail.");
            return;
        }
        final String splitRegex = "[ ]";
        String[] splitedMessage = message.substring(1).split(splitRegex, 2);
        if (splitedMessage.length < 2) {
            splitedMessage = new String[]{splitedMessage[0], ""};
        }
        
        client.getPlayer().setLastCommandMessage(splitedMessage[1]);    // thanks Tochi & Nulliphite for noticing string messages being marshalled lowercase
        final String commandName = splitedMessage[0].toLowerCase();
        final String[] lowercaseParams = splitedMessage[1].toLowerCase().split(splitRegex);
        
        final Command command = registeredCommands.get(commandName);
        if (command == null){
            client.getPlayer().yellowMessage("Command '" + commandName + "' is not available. See @commands for a list of available commands.");
            return;
        }
        if (client.getPlayer().gmLevel() < command.getRank()){
            client.getPlayer().yellowMessage("You do not have permission to use this command.");
            return;
        }
        String[] params;
        if (lowercaseParams.length > 0 && !lowercaseParams[0].isEmpty()) {
            params = Arrays.copyOfRange(lowercaseParams, 0, lowercaseParams.length);
        } else {
            params = new String[]{};
        }
        
        command.execute(client, params);
        writeLog(client, message);
    }

    private void writeLog(MapleClient client, String command){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        FilePrinter.print(FilePrinter.USED_COMMANDS, client.getPlayer().getName() + " used: " + command + " on "
                + sdf.format(Calendar.getInstance().getTime()));
    }

    private void addCommandInfo(String name, Class<? extends Command> commandClass) {
        try {
            levelCommandsCursor.getRight().add(commandClass.newInstance().getDescription());
            levelCommandsCursor.getLeft().add(name);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addCommand(String[] syntaxs, Class<? extends Command> commandClass){
        for (String syntax : syntaxs){
            addCommand(syntax, 0, commandClass);
        }
    }
    private void addCommand(String syntax, Class<? extends Command> commandClass){
        //for (String syntax : syntaxs){
            addCommand(syntax, 0, commandClass);
        //}
    }

    private void addCommand(String[] surtaxes, int rank, Class<? extends Command> commandClass){
        for (String syntax : surtaxes){
            addCommand(syntax, rank, commandClass);
        }
    }

    private void addCommand(String syntax, int rank,  Class<? extends Command> commandClass){
        if (registeredCommands.containsKey(syntax.toLowerCase())){
            System.out.println("Error on register command with name: " + syntax + ". Already exists.");
            return;
        }
        
        String commandName = syntax.toLowerCase();
        addCommandInfo(commandName, commandClass);
        
        try {
            Command commandInstance = commandClass.newInstance();     // thanks Halcyon for noticing commands getting reinstanced every call
            commandInstance.setRank(rank);
            
            registeredCommands.put(commandName, commandInstance);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void registerLv0Commands(){
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand(new String[]{"help", "commands"}, HelpCommand.class);
        addCommand("droplimit", DropLimitCommand.class);
        addCommand("time", TimeCommand.class);
        addCommand("credits", StaffCommand.class);
        addCommand("buyback", BuyBackCommand.class);
        addCommand("uptime", UptimeCommand.class);
        addCommand("gacha", GachaCommand.class);
        addCommand("dispose", DisposeCommand.class);
        addCommand("changel", ChangeLanguageCommand.class);
        addCommand("equiplv",  EquipLvCommand.class);
        addCommand("showrates", ShowRatesCommand.class);
        addCommand("rates", RatesCommand.class);
        addCommand("online", OnlineCommand.class);
        addCommand("gm", GmCommand.class);
        addCommand("reportbug", ReportBugCommand.class);
        addCommand("points", ReadPointsCommand.class);
        addCommand("joinevent", JoinEventCommand.class);
        addCommand("leaveevent", LeaveEventCommand.class);
        addCommand("ranks", RanksCommand.class);
        addCommand("str", StatStrCommand.class);
        addCommand("dex", StatDexCommand.class);
        addCommand("int", StatIntCommand.class);
        addCommand("luk", StatLukCommand.class);
        addCommand("enableauth", EnableAuthCommand.class);
        addCommand("toggleexp", ToggleExpCommand.class);
        addCommand("mylawn", MapOwnerClaimCommand.class);
        addCommand("bosshp", BossHpCommand.class);
        addCommand("mobhp", MobHpCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }


    private void registerLv1Commands() {
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("whatdropsfrom", 1, WhatDropsFromCommand.class);
        addCommand("whodrops", 1, WhoDropsCommand.class);
        addCommand("buffme", 1, BuffMeCommand.class);
        addCommand("goto", 1, GotoCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }


    private void registerLv2Commands(){
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("recharge", 2, RechargeCommand.class);
        addCommand("whereami", 2, WhereaMiCommand.class);
        addCommand("hide", 2, HideCommand.class);
        addCommand("unhide", 2, UnHideCommand.class);
        addCommand("sp", 2, SpCommand.class);
        addCommand("ap", 2, ApCommand.class);
        addCommand("empowerme", 2, EmpowerMeCommand.class);
        addCommand("buffmap", 2, BuffMapCommand.class);
        addCommand("buff", 2, BuffCommand.class);
        addCommand("bomb", 2, BombCommand.class);
        addCommand("dc", 2, DcCommand.class);
        addCommand("cleardrops", 2, ClearDropsCommand.class);
        addCommand("clearslot", 2, ClearSlotCommand.class);
        addCommand("clearsavelocs", 2, ClearSavedLocationsCommand.class);
        addCommand("warp", 2, WarpCommand.class);
        addCommand(new String[]{"warphere", "summon"}, 2, SummonCommand.class);
        addCommand(new String[]{"warpto", "reach", "follow"}, 2, ReachCommand.class);
        addCommand("gmshop", 2, GmShopCommand.class);
        addCommand("heal", 2, HealCommand.class);
        addCommand("item", 2, ItemCommand.class);
        addCommand("drop", 2, ItemDropCommand.class);
        addCommand("level", 2, LevelCommand.class);
        addCommand("levelpro", 2, LevelProCommand.class);
        addCommand("setslot", 2, SetSlotCommand.class);
        addCommand("setstat", 2, SetStatCommand.class);
        addCommand("maxstat", 2, MaxStatCommand.class);
        addCommand("maxskill", 2, MaxSkillCommand.class);
        addCommand("resetskill", 2, ResetSkillCommand.class);
        addCommand("search", 2, SearchCommand.class);
        addCommand("jail", 2, JailCommand.class);
        addCommand("unjail", 2, UnJailCommand.class);
        addCommand("job", 2, JobCommand.class);
        addCommand("unbug", 2, UnBugCommand.class);
        addCommand("id", 2, IdCommand.class);
        addCommand("gachalist", GachaListCommand.class);
        addCommand("loot", LootCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv3Commands() {
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("debuff", 3, DebuffCommand.class);
        addCommand("fly", 3, FlyCommand.class);
        addCommand("spawn", 3, SpawnCommand.class);
        addCommand("mutemap", 3, MuteMapCommand.class);
        addCommand("checkdmg", 3, CheckDmgCommand.class);
        addCommand("inmap", 3, InMapCommand.class);
        addCommand("reloadevents", 3, ReloadEventsCommand.class);
        addCommand("reloaddrops", 3, ReloadDropsCommand.class);
        addCommand("reloadportals", 3, ReloadPortalsCommand.class);
        addCommand("reloadmap", 3, ReloadMapCommand.class);
        addCommand("reloadshops", 3, ReloadShopsCommand.class);
        addCommand("hpmp", 3, HpMpCommand.class);
        addCommand("maxhpmp", 3, MaxHpMpCommand.class);
        addCommand("music", 3, MusicCommand.class);
        addCommand("monitor", 3, MonitorCommand.class);
        addCommand("monitors", 3, MonitorsCommand.class);
        addCommand("ignore", 3, IgnoreCommand.class);
        addCommand("ignored", 3, IgnoredCommand.class);
        addCommand("pos", 3, PosCommand.class);
        addCommand("togglecoupon", 3, ToggleCouponCommand.class);
        addCommand("togglewhitechat", 3, ChatCommand.class);
        addCommand("fame", 3, FameCommand.class);
        addCommand("givenx", 3, GiveNxCommand.class);
        addCommand("givevp", 3, GiveVpCommand.class);
        addCommand("givems", 3, GiveMesosCommand.class);
        addCommand("giverp", 3, GiveRpCommand.class);
        addCommand("expeds", 3, ExpedsCommand.class);
        addCommand("kill", 3, KillCommand.class);
        addCommand("seed", 3, SeedCommand.class);
        addCommand("maxenergy", 3, MaxEnergyCommand.class);
        addCommand("killall", 3, KillAllCommand.class);
        addCommand("notice", 3, NoticeCommand.class);
        addCommand("rip", 3, RipCommand.class);
        addCommand("openportal", 3, OpenPortalCommand.class);
        addCommand("closeportal", 3, ClosePortalCommand.class);
        addCommand("pe", 3, PeCommand.class);
        addCommand("startevent", 3, StartEventCommand.class);
        addCommand("endevent", 3, EndEventCommand.class);
        addCommand("startmapevent", 3, StartMapEventCommand.class);
        addCommand("stopmapevent", 3, StopMapEventCommand.class);
        addCommand("online2", 3, OnlineTwoCommand.class);
        addCommand("ban", 3, BanCommand.class);
        addCommand("unban", 3, UnBanCommand.class);
        addCommand("healmap", 3, HealMapCommand.class);
        addCommand("healperson", 3, HealPersonCommand.class);
        addCommand("hurt", 3, HurtCommand.class);
        addCommand("killmap", 3, KillMapCommand.class);
        addCommand("night", 3, NightCommand.class);
        addCommand("npc", 3, NpcCommand.class);
        addCommand("face", 3, FaceCommand.class);
        addCommand("hair", 3, HairCommand.class);
        addCommand("startquest", 3, QuestStartCommand.class);
        addCommand("completequest", 3, QuestCompleteCommand.class);
        addCommand("resetquest", 3, QuestResetCommand.class);
        addCommand("timer", 3, TimerCommand.class);
        addCommand("timermap", 3, TimerMapCommand.class);
        addCommand("timerall", 3, TimerAllCommand.class);
        addCommand("warpmap", 3, WarpMapCommand.class);
        addCommand("warparea", 3, WarpAreaCommand.class);

        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv4Commands(){
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("servermessage", 4, ServerMessageCommand.class);
        addCommand("proitem", 4, ProItemCommand.class);
        addCommand("seteqstat", 4, SetEqStatCommand.class);
        addCommand("exprate", 4, ExpRateCommand.class);
        addCommand("mesorate", 4, MesoRateCommand.class);
        addCommand("droprate", 4, DropRateCommand.class);
        addCommand("bossdroprate", 4, BossDropRateCommand.class);
        addCommand("questrate", 4, QuestRateCommand.class);
        addCommand("travelrate", 4, TravelRateCommand.class);
        addCommand("fishrate", 4, FishingRateCommand.class);
        addCommand("itemvac", 4, ItemVacCommand.class);
        addCommand("forcevac", 4, ForceVacCommand.class);
        addCommand("zakum", 4, ZakumCommand.class);
        addCommand("horntail", 4, HorntailCommand.class);
        addCommand("pinkbean", 4, PinkbeanCommand.class);
        addCommand("pap", 4, PapCommand.class);
        addCommand("pianus", 4, PianusCommand.class);
        addCommand("cake", 4, CakeCommand.class);
        addCommand("playernpc", 4, PlayerNpcCommand.class);
        addCommand("playernpcremove", 4, PlayerNpcRemoveCommand.class);
        addCommand("pnpc", 4, PnpcCommand.class);
        addCommand("pnpcremove", 4, PnpcRemoveCommand.class);
        addCommand("pmob", 4, PmobCommand.class);
        addCommand("pmobremove", 4, PmobRemoveCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv5Commands(){
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("debug", 5, DebugCommand.class);
        addCommand("set", 5, SetCommand.class);
        addCommand("showpackets", 5, ShowPacketsCommand.class);
        addCommand("showmovelife", 5, ShowMoveLifeCommand.class);
        addCommand("showsessions", 5, ShowSessionsCommand.class);
        addCommand("iplist", 5, IpListCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }

    private void registerLv6Commands(){
        levelCommandsCursor = new Pair<>((List<String>) new ArrayList<String>(), (List<String>) new ArrayList<String>());
        
        addCommand("setgmlevel", 6, SetGmLevelCommand.class);
        addCommand("warpworld", 6, WarpWorldCommand.class);
        addCommand("saveall", 6, SaveAllCommand.class);
        addCommand("dcall", 6, DCAllCommand.class);
        addCommand("mapplayers", 6, MapPlayersCommand.class);
        addCommand("getacc", 6, GetAccCommand.class);
        addCommand("shutdown", 6, ShutdownCommand.class);
        addCommand("clearquestcache", 6, ClearQuestCacheCommand.class);
        addCommand("clearquest", 6, ClearQuestCommand.class);
        addCommand("supplyratecoupon", 6, SupplyRateCouponCommand.class);
        addCommand("spawnallpnpcs", 6, SpawnAllPNpcsCommand.class);
        addCommand("eraseallpnpcs", 6, EraseAllPNpcsCommand.class);
        addCommand("addchannel", 6, ServerAddChannelCommand.class);
        addCommand("addworld", 6, ServerAddWorldCommand.class);
        addCommand("removechannel", 6, ServerRemoveChannelCommand.class);
        addCommand("removeworld", 6, ServerRemoveWorldCommand.class);
        
        commandsNameDesc.add(levelCommandsCursor);
    }

}

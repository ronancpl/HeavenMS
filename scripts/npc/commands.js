/* @Author Ronan
 * @Author Vcoc
        Name: Steward
        Map(s): Foyer
        Info: Commands
        Script: commands.js
*/

var status;

var comm_lv6 = [];
var desc_lv6 = [];

var comm_lv5 = [];
var desc_lv5 = [];

var comm_lv4 = [];
var desc_lv4 = [];

var comm_lv3 = [];
var desc_lv3 = [];

var comm_lv2 = [];
var desc_lv2 = [];

var comm_lv1 = [];
var desc_lv1 = [];

var comm_lv0 = [];
var desc_lv0 = [];

var levels = ["Common", "Donator", "JrGM", "GM", "SuperGM", "Developer", "Admin"];

var comm_cursor, desc_cursor;

function addCommand(comm, desc) {
        comm_cursor.push(comm);
        desc_cursor.push(desc);
}

function writeSolaxiaCommandsLv6() {    //Admin
        comm_cursor = comm_lv6;
        desc_cursor = desc_lv6;

        addCommand("setgmlevel", "");
        addCommand("warpworld", "");
        addCommand("saveall", "");
        addCommand("dcall", "");
        addCommand("mapplayers", "");
        addCommand("getacc", "");
        addCommand("shutdown", "");
        addCommand("shutdownnow", "");
        addCommand("clearquestcache", "");
        addCommand("clearquest", "");
}

function writeSolaxiaCommandsLv5() {    //Developer
        comm_cursor = comm_lv5;
        desc_cursor = desc_lv5;

        addCommand("debugmonster", "");
        addCommand("debugpacket", "");
        addCommand("debugportal", "");
        addCommand("debugspawnpoint", "");
        addCommand("debugpos", "");
        addCommand("debugmap", "");
        addCommand("debugmobsp", "");
        addCommand("debugevent", "");
        addCommand("debugareas", "");
        addCommand("debugreactors", "");
        addCommand("debugcoupons", "");
        addCommand("debugplayercoupons", "");
        addCommand("debugtimer", "");
}

function writeSolaxiaCommandsLv4() {    //SuperGM
        comm_cursor = comm_lv4;
        desc_cursor = desc_lv4;

        addCommand("servermessage", "");
        addCommand("proitem", "");
        addCommand("seteqstat", "");
        addCommand("exprate", "");
        addCommand("mesorate", "");
        addCommand("droprate", "");
        addCommand("bossdroprate", "");
        addCommand("itemvac", "");
        addCommand("forcevac", "");
        addCommand("zakum", "");
        addCommand("horntail", "");
        addCommand("pinkbean", "");
        addCommand("pap", "");
        addCommand("pianus", "");
        addCommand("cake", "");
        //addCommand("playernpc", "");
}

function writeSolaxiaCommandsLv3() {    //GM
        comm_cursor = comm_lv3;
        desc_cursor = desc_lv3;

        addCommand("fly", "");
        addCommand("spawn", "");
        addCommand("mutemap", "");
        addCommand("checkdmg", "");
        addCommand("inmap", "");
        addCommand("reloadevents", "");
        addCommand("reloaddrops", "");
        addCommand("reloadportals", "");
        addCommand("reloadmap", "");
        addCommand("hpmp", "");
        addCommand("music", "");
        addCommand("monitor", "");
        addCommand("monitors", "");
        addCommand("ignore", "");
        addCommand("ignored", "");
        addCommand("pos", "");
        addCommand("togglecoupon", "");
        addCommand("chat", "");
        addCommand("fame", "");
        addCommand("givenx", "");
        addCommand("givevp", "");
        addCommand("givems", "");
        addCommand("id", "");
        addCommand("expeds", "");
        addCommand("kill", "");
        addCommand("seed", "");
        addCommand("maxenergy", "");
        addCommand("killall", "");
        addCommand("notice", "");
        addCommand("rip", "");
        addCommand("openportal", "");
        addCommand("closeportal", "");
        addCommand("pe", "");
        addCommand("startevent", "");
        addCommand("endevent", "");
        addCommand("online2", "");
        addCommand("warpsnowball", "");
        addCommand("ban", "");
        addCommand("unban", "");
        addCommand("healmap", "");
        addCommand("healperson", "");
        addCommand("hurt", "");
        addCommand("killmap", "");
        addCommand("night", "");
        addCommand("npc", "");
        addCommand("face", "");
        addCommand("hair", "");
}

function writeSolaxiaCommandsLv2() {    //JrGM
        comm_cursor = comm_lv2;
        desc_cursor = desc_lv2;

        addCommand("hide", "");
        addCommand("unhide", "");
        addCommand("sp", "");
        addCommand("ap", "");
        addCommand("empowerme", "");
        addCommand("buffmap", "");
        addCommand("buff", "");
        addCommand("bomb", "");
        addCommand("dc", "");
        addCommand("cleardrops", "");
        addCommand("clearslot", "");
        addCommand("warp", "");
        addCommand("warpto", "");
        addCommand("warphere", "");
        addCommand("gmshop", "");
        addCommand("heal", "");
        addCommand("item", "");
        addCommand("level", "");
        addCommand("levelpro", "");
        addCommand("setstat", "");
        addCommand("maxstat", "");
        addCommand("maxskill", "");
        addCommand("mesos", "");
        addCommand("search", "");
        addCommand("jail", "");
        addCommand("unjail", "");
        addCommand("job", "");
        addCommand("unbug", "");
}

function writeSolaxiaCommandsLv1() {    //Donator
        comm_cursor = comm_lv1;
        desc_cursor = desc_lv1;

        addCommand("buffme", "");
        addCommand("goto", "");
        addCommand("recharge", "");
        addCommand("whereami", "");
}

function writeSolaxiaCommandsLv0() {    //Common
        comm_cursor = comm_lv0;
        desc_cursor = desc_lv0;

        addCommand("commands", "");
        addCommand("droplimit", "");
        addCommand("time", "");
        addCommand("credits", "");
        addCommand("uptime", "");
        addCommand("gacha", "");
        addCommand("whatdropsfrom", "");
        addCommand("whodrops", "");
        addCommand("dispose", "");
        addCommand("equiplv", "");
        addCommand("showrates", "");
        addCommand("rates", "");
        addCommand("online", "");
        addCommand("gm", "");
        addCommand("bug", "");
	//addCommand("points", "");
        addCommand("joinevent", "");
        addCommand("leaveevent", "");
        addCommand("bosshp", "");
        addCommand("ranks", "");
}

function writeSolaxiaCommands() {
        writeSolaxiaCommandsLv0();  //Common
        writeSolaxiaCommandsLv1();  //Donator
        writeSolaxiaCommandsLv2();  //JrGM
        writeSolaxiaCommandsLv3();  //GM
        writeSolaxiaCommandsLv4();  //Developer
        writeSolaxiaCommandsLv5();  //SuperGM
        writeSolaxiaCommandsLv6();  //Admin
}

function start() {
        status = -1;
        writeSolaxiaCommands();
        action(1, 0, 0);
}

function action(mode, type, selection) {
        if (mode == -1) {
                cm.dispose();
        } else {
                if (mode == 0 && type > 0) {
                        cm.dispose();
                        return;
                }
                if (mode == 1)
                        status++;
                else
                        status--;

                if (status == 0) {
                        var sendStr = "There are all available commands for you:\r\n\r\n#b";
                        for(var i = 0; i <= cm.getPlayer().gmLevel(); i++) {
                            sendStr += "#L" + i + "#" + levels[i] + "#l\r\n";
                        }

                        cm.sendSimple(sendStr);
                } else if(status == 1) {
                        var lvComm, lvDesc;

                        if(selection == 0) {
                                lvComm = comm_lv0;
                                lvDesc = desc_lv0;
                        } else if(selection == 1) {
                                lvComm = comm_lv1;
                                lvDesc = desc_lv1;
                        } else if(selection == 2) {
                                lvComm = comm_lv2;
                                lvDesc = desc_lv2;
                        } else if(selection == 3) {
                                lvComm = comm_lv3;
                                lvDesc = desc_lv3;
                        } else if(selection == 4) {
                                lvComm = comm_lv4;
                                lvDesc = desc_lv4;
                        } else if(selection == 5) {
                                lvComm = comm_lv5;
                                lvDesc = desc_lv5;
                        } else {
                                lvComm = comm_lv6;
                                lvDesc = desc_lv6;
                        }

                        var sendStr = "The following commands are available for #b" + levels[selection] + "#k:\r\n\r\n";
                        for(var i = 0; i < lvComm.length; i++) {
                            sendStr += "  #L" + i + "# " + lvComm[i] + " - " + lvDesc[i];
                            sendStr += "#l\r\n";
                        }

                        cm.sendPrev(sendStr);
                } else {
                        cm.dispose();
                }
        }
}

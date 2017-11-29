/* @Author Ronan
        Name: Heracle
        Map(s): Guild Headquarters
        Info: Hall of Fame
        Script: credits.js
*/

var status;

var name_tree = [];
var role_tree = [];
var name_cursor, role_cursor;

var servers = ["HeavenMS", "MapleSolaxia", "MoopleDEV", "MetroMS", "BubblesDEV", "ThePackII", "OdinMS"];
var servers_history = [];

function addPerson(name, role) {
        name_cursor.push(name);
        role_cursor.push(role);
}

function setHistory(from, to) {
        servers_history.push([from, to]);
}

function writeServerStaff_HeavenMS() {
        addPerson("Ronan", "Developer");
        addPerson("Vcoc", "Freelance Developer");
        
        setHistory(2015, 2017);
}

function writeServerStaff_MapleSolaxia() {
        addPerson("Aria", "Administrator");
        addPerson("Twdtwd", "Administrator");
        addPerson("Exorcist", "Developer");
        addPerson("SharpAceX", "Developer");
        addPerson("Zygon", "Freelance Developer");
        addPerson("SourMjolk", "Game Master");
        addPerson("Kanade", "Game Master");
        addPerson("Kitsune", "Game Master");
        
        setHistory(2014, 2015);
}

function writeServerStaff_MoopleDEV() {
        addPerson("kevintjuh93", "Developer");
        setHistory(2010, 2010);
}

function writeServerStaff_MetroMS() {
        addPerson("Moongra", "Developer");
        setHistory(2009, 2010);
}

function writeServerStaff_BubblesDEV() {
        addPerson("Deagan", "Developer");
        setHistory(2009, 2009);
}

function writeServerStaff_ThePackII() {
        addPerson("Hofer", "Developer");
        setHistory(2008, 2009);
}

function writeServerStaff_OdinMS() {
        addPerson("Serpendiem", "Developer");
        setHistory(2007, 2008);
}

function writeAllServerStaffs() {
        for(var i = 0; i < servers.length; i++) {
                name_cursor = [];
                role_cursor = [];

                var srvName = servers[i];
                eval("writeServerStaff_" + srvName)();  // make sure the server names are lexicograffically EQUALS to the correspondent function.
        
                name_tree.push(name_cursor);
                role_tree.push(role_cursor);
        }
}

function start() {
        status = -1;
        writeAllServerStaffs();
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
                        var sendStr = "There is the history tree of all participating parties on the build of this server:\r\n\r\n";
                        for(var i = 0; i < servers.length; i++) {
                            var hist = servers_history[i];
                            
                            if(hist.length > 0) {
                                sendStr += "#L" + i + "##b" + servers[i] + "#k  --  " + ((hist[0] != hist[1]) ? hist[0] + " ~ " + hist[1] : hist[0]) + "#l\r\n";
                            } else {
                                sendStr += "#L" + i + "#" + servers[i] + "#l\r\n";
                            }
                        }

                        cm.sendSimple(sendStr);
                } else if(status == 1) {
                        var lvName, lvRole;

                        for(var i = 0; i < servers.length; i++) {
                            if(selection == i) {
                                lvName = name_tree[i];
                                lvRole = role_tree[i];
                                break;
                            }
                        }

                        var sendStr = "The staff of #b" + servers[selection] + "#k:\r\n\r\n";
                        for(var i = 0; i < lvName.length; i++) {
                            sendStr += "  #L" + i + "# " + lvName[i] + " - " + lvRole[i];
                            sendStr += "#l\r\n";
                        }

                        cm.sendPrev(sendStr);
                } else {
                        cm.dispose();
                }
        }
}

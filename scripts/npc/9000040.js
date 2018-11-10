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
/* Dalair
	Medal NPC.

        NPC Equipment Merger:
        * @author Ronan Lana
 */

importPackage(Packages.client.processor);
importPackage(Packages.constants);

var status;
var mergeFee = 50000;
var name;

function start() {
    status = -1;
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

        if(status == 0) {
            if (!Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
                cm.sendOk("The medal ranking system is currently unavailable...");
                cm.dispose();
                return;
            }
            
            var levelLimit = !cm.getPlayer().isCygnus() ? 160 : 110;
            var selStr = "The medal ranking system is currently unavailable... Therefore, I am providing the #bEquipment Merge#k service! ";
            
            if (!ServerConstants.USE_STARTER_MERGE && (cm.getPlayer().getLevel() < levelLimit || MakerProcessor.getMakerSkillLevel(cm.getPlayer()) < 3)) {
                selStr += "However, you must have #rMaker level 3#k and at least #rlevel 110#k (Cygnus Knight), #rlevel 160#k (other classes) and a fund of #r" + cm.numberWithCommas(mergeFee) + " mesos#k to use the service.";
                cm.sendOk(selStr);
                cm.dispose();
            } else if (cm.getMeso() < mergeFee) {
                selStr += "I'm sorry, but this service tax is of #r" + cm.numberWithCommas(mergeFee) + " mesos#k, which it seems you unfortunately don't have right now... Please, stop by again later.";
                cm.sendOk(selStr);
                cm.dispose();
            } else {
                selStr += "For the fee of #r" + cm.numberWithCommas(mergeFee) + "#k mesos, merge unnecessary equipments in your inventory into your currently equipped gears to get stat boosts into them, statups based on the attributes of the items used on the merge!";
                cm.sendNext(selStr);
            }
        } else if(status == 1) {
            selStr = "#rWARNING#b: Make sure you have your items ready to merge at the slots #rAFTER#b the item you have selected to merge.#k Any items #bunder#k the item selected will be merged thoroughly.\r\n\r\nNote that equipments receiving bonuses from merge are going to become #rUntradeable#k thereon, and equipments that already received the merge bonus #rcannot be used for merge#k.\r\n\r\n";
            cm.sendGetText(selStr);
        } else if(status == 2) {
            name = cm.getText();
            
            if (cm.getPlayer().mergeAllItemsFromName(name)) {
                cm.gainMeso(-mergeFee);
                cm.sendOk("Merging complete! Thanks for using the service and enjoy your new equipment stats.");
            } else {
                cm.sendOk("There is no #b'" + name + "'#k in your #bEQUIP#k inventory!");
            }
            
            cm.dispose();
        }
    }
}

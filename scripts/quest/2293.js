/*
	This file is part of the DietStory Maple Story Server
    Copyright (C) 2017
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
/* Author: Benji 
	NPC Name: 		Maestro Rho
	Map(s): 		Kerning Square Lobby
	Description: 	The Last Song
*/

importPackage(Packages.client);

var status = -1;

function start(mode, type, selection) {
    if(mode == -1 || (mode == 0 && status == 0)){
        qm.dispose();
        return;
    }
    else if(mode == 0)
        status--;
    else
        status++;

    if(status == 0)
    {
        qm.sendNext("Do you remember the last song that the Spirit of Rock played? I can think of a few songs that he may be imitating, so listen carefully and tell me which song it is. #bYou only get one chance,#k so please choose wisely.");
    }
    qm.forceStartQuest();
    qm.dispose();
}

function end(mode, type, selection)
{
    if(mode == -1 || (mode == 0 && status == 0)){
            qm.dispose();
            return;
        }
        else if(mode == 0)
            status--;
        else
            status++;

    if (status == 0)
    {
        qm.sendSimple("Here, I'll give you some samples. Please listen to them and choose one. Please listen carefully before making your choide.\r\n\
            \t#b#L1# Listen to song No. 1#l \r\n\
            \t#L2# Listen to Song No. 2#l \r\n\
            \t#L3# Listen to Song No. 3#l \r\n\
            \r\n\
            \t#e#L4# Enter the correct song.#l");
    }
    else if(status == 1)
    {
        if(selection == 1)
        {
            qm.playSound("Party1/Failed");
            qm.sendOk("Awkwardly familiar..."); 
            status = -1;  
        }
        else if(selection == 2)
        {
            qm.playSound("Coconut/Failed");
            qm.sendOk("Was it this?");
            status = -1;
        }
        else if(selection == 3)
        {
            qm.playSound("quest2293/Die");
            qm.sendOk("You heard that?");
            status = -1;
        }
        else if(selection == 4)
        {
            qm.sendGetNumber("Now, please tell me the answer. You only get #bone chance#k, so please choose wisely. Please enter #b1, 2, or 3#k in the window below.\r\n",1,1,3);
        }
    }
    else if(status == 2)
    {
        if(selection == 1)
        {
            qm.sendOk("Obviously you don't enjoy music.");
            qm.dispose();
        }
        else if(selection == 2)
        {
            qm.sendOk("I suppose you could get #b#eone#n#k more chance.");
            qm.dispose();
        }
        else if(selection == 3)
        {
            qm.sendOk("So that was the song he was playing... Well, it wasn't my song after all, but I'm glad I can know that now with certainty. Thank you so much.");
            qm.gainExp(32500);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
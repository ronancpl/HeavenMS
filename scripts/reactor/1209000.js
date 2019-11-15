/*
    This file is part of the HeavenMS MapleStory Server
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

/* @Author Ronan
 * 
 * 1200000.js: Bart reactor, Aerial Strike skill
*/

function act() {    // string visibility thanks to ProXAIMeRx & Glvelturall
    if (rm.isQuestStarted(6400)) {
        rm.setQuestProgress(6400, 1, 2);
        rm.setQuestProgress(6400, 6401, "q3");
    }
    
    rm.message("Real Bart has been found. Return to Jonathan through the portal.");
}
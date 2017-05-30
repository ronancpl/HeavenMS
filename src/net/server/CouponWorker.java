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
package net.server;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import constants.ServerConstants;
import tools.DatabaseConnection;

import net.server.world.World;
import client.MapleCharacter;

/**
 * @author Ronan
 * @info   Thread responsible for maintaining coupons EXP & DROP effects active
 */
public class CouponWorker implements Runnable {
    @Override
    public void run() {
        try {
            Server.getInstance().updateActiveCoupons();
            Server.getInstance().commitActiveCoupons();
        } catch(SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}

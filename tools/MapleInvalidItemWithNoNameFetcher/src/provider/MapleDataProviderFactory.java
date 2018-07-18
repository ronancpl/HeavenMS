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
package provider;

import java.io.File;
import java.io.IOException;
import provider.wz.WZFile;
import provider.wz.XMLWZFile;

public class MapleDataProviderFactory {
    private final static String wzPath = System.getProperty("wzpath");

    private static MapleDataProvider getWZ(File in, boolean provideImages) {
        if (in.getName().toLowerCase().endsWith("wz") && !in.isDirectory()) {
            try {
                return new WZFile(in, provideImages);
            } catch (IOException e) {
                throw new RuntimeException("Loading WZ File failed", e);
            }
        } else {
            return new XMLWZFile(in);
        }
    }

    public static MapleDataProvider getDataProvider(File in) {
        return getWZ(in, false);
    }

    public static MapleDataProvider getImageProvidingDataProvider(File in) {
        return getWZ(in, true);
    }

    public static File fileInWZPath(String filename) {
        return new File(wzPath, filename);
    }
}
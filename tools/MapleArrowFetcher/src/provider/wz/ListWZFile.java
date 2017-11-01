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
package provider.wz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import provider.MapleDataProviderFactory;
import tools.data.input.GenericLittleEndianAccessor;
import tools.data.input.InputStreamByteStream;
import tools.data.input.LittleEndianAccessor;

public class ListWZFile {
    private LittleEndianAccessor lea;
    private List<String> entries = new ArrayList<String>();
    private static Collection<String> modernImgs = new HashSet<String>();

    public static byte[] xorBytes(byte[] a, byte[] b) {
        byte[] wusched = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            wusched[i] = (byte) (a[i] ^ b[i]);
        }
        return wusched;
    }

    public ListWZFile(File listwz) throws FileNotFoundException {
        lea = new GenericLittleEndianAccessor(new InputStreamByteStream(new BufferedInputStream(new FileInputStream(listwz))));
        while (lea.available() > 0) {
            int l = lea.readInt() * 2;
            byte[] chunk = new byte[l];
            for (int i = 0; i < chunk.length; i++) {
                chunk[i] = lea.readByte();
            }
            lea.readChar();
            final String value = String.valueOf(WZTool.readListString(chunk));
            entries.add(value);
        }
        entries = Collections.unmodifiableList(entries);
    }

    public List<String> getEntries() {
        return entries;
    }

    public static void init() {
        final String listWz = System.getProperty("listwz");
        if (listWz != null) {
            ListWZFile listwz;
            try {
                listwz = new ListWZFile(MapleDataProviderFactory.fileInWZPath("List.wz"));
                modernImgs = new HashSet<String>(listwz.getEntries());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isModernImgFile(String path) {
        return modernImgs.contains(path);
    }
}

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
package maplenoitemnamefetcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;

/**
 *
 * @author RonanLana
 * 
 * This application finds itemids with inexistent name and description from
 * within the server-side XMLs, then identify them on a report file along
 * with a XML excerpt to be appended on the String.wz xml nodes. This program
 * assumes all equipids are depicted using 8 digits and item using 7 digits.
 * 
 * Estimated parse time: 2 minutes
 */
public class MapleNoItemNameFetcher {
    static String wzPath = "../../wz";
    static String newFile = "lib/result.txt";
    static String xmlFile = "lib/output.txt";

    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    
    static Map<Integer, String> itemsWzPath = new HashMap<>();
    static Map<Integer, ItemType> itemTypes = new HashMap<>();
    static Map<Integer, EquipType> equipTypes = new HashMap<>();
    
    static Map<Integer, ItemType> itemsWithNoNameProperty = new HashMap<>();
    static Set<Integer> equipsWithNoCashProperty = new HashSet<>();

    static Map<Integer, String> nameContentCache = new HashMap<>();
    static Map<Integer, String> descContentCache = new HashMap<>();
    
    static ItemType curType = ItemType.UNDEF;
    
    private enum ItemType {
        UNDEF, CASH, CONSUME, EQP, ETC, INS, PET
    }
    
    private enum EquipType {
        UNDEF, ACCESSORY, CAP, CAPE, COAT, FACE, GLOVE, HAIR, LONGCOAT, PANTS, PETEQUIP, RING, SHIELD, SHOES, TAMING, WEAPON
    }
    
    private static void processStringSubdirectoryData(MapleData subdirData, String subdirPath) {
        for(MapleData md : subdirData.getChildren()) {
            try {
                MapleData nameData = md.getChildByPath("name");
                MapleData descData = md.getChildByPath("desc");
                
                int itemId = Integer.parseInt(md.getName());
                if (nameData != null && descData != null) {
                    itemsWithNoNameProperty.remove(itemId);
                } else {
                    if (nameData != null) {
                        nameContentCache.put(itemId, MapleDataTool.getString("name", md));
                    } else if (descData != null) {
                        descContentCache.put(itemId, MapleDataTool.getString("desc", md));
                    }
                    
                    System.out.println("Found itemid on String.wz with no full property: " + subdirPath + subdirData.getName() + "/" + md.getName());
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Error reading string image: " + subdirPath + subdirData.getName() + "/" + md.getName());
            }
        }
    }
    
    private static void readStringSubdirectoryData(MapleData subdirData, int depth, String subdirPath) {
        if(depth > 0) {
            for (MapleData mDir : subdirData.getChildren()) {
                readStringSubdirectoryData(mDir, depth - 1, subdirPath + mDir.getName() + "/");
            }
        } else {
            processStringSubdirectoryData(subdirData, subdirPath);
        }
    }
    
    private static void readStringSubdirectoryData(MapleData subdirData, int depth) {
        readStringSubdirectoryData(subdirData, depth, "");
    }
    
    private static void readStringWZData() {
        System.out.println("Parsing String.wz...");
        MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
        
        MapleData cashStringData = stringData.getData("Cash.img");
        readStringSubdirectoryData(cashStringData, 0);
        
        MapleData consumeStringData = stringData.getData("Consume.img");
        readStringSubdirectoryData(consumeStringData, 0);
        
        MapleData eqpStringData = stringData.getData("Eqp.img");
        readStringSubdirectoryData(eqpStringData, 2);
        
        MapleData etcStringData = stringData.getData("Etc.img");
        readStringSubdirectoryData(etcStringData, 1);
        
        MapleData insStringData = stringData.getData("Ins.img");
        readStringSubdirectoryData(insStringData, 0);
        
        MapleData petStringData = stringData.getData("Pet.img");
        readStringSubdirectoryData(petStringData, 0);
    }
    
    private static boolean isTamingMob(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 1902 || itemType == 1912;
    }
    
    private static boolean isAccessory(int itemId) {
        return itemId >= 1110000 && itemId < 1140000;
    }
    
    private static ItemType getItemTypeFromDirectoryName(String dirName) {
        switch(dirName) {
            case "Cash":
                return ItemType.CASH;
                
            case "Consume":
                return ItemType.CONSUME;
                
            case "Etc":
                return ItemType.ETC;
                
            case "Install":
                return ItemType.INS;
                
            case "Pet":
                return ItemType.PET;
                
            default:
                return ItemType.UNDEF;
        }
    }
    
    private static EquipType getEquipTypeFromDirectoryName(String dirName) {
        switch(dirName) {
            case "Accessory":
                return EquipType.ACCESSORY;
            
            case "Cap":
                return EquipType.CAP;
            
            case "Cape":
                return EquipType.CAPE;
                    
            case "Coat":
                return EquipType.COAT;
                        
            case "Face":
                return EquipType.FACE;
                            
            case "Glove":
                return EquipType.GLOVE;
                                
            case "Hair":
                return EquipType.HAIR;
                                    
            case "Longcoat":
                return EquipType.LONGCOAT;
                                        
            case "Pants":
                return EquipType.PANTS;
                                            
            case "PetEquip":
                return EquipType.PETEQUIP;
                
            case "Ring":
                return EquipType.RING;
                    
            case "Shield":
                return EquipType.SHIELD;
                        
            case "Shoes":
                return EquipType.SHOES;
                            
            case "TamingMob":
                return EquipType.TAMING;
                                
            case "Weapon":
                return EquipType.WEAPON;
                
            default:
                return EquipType.UNDEF;
        }
    }
    
    private static String getStringDirectoryNameFromEquipType(EquipType eType) {
        switch(eType) {
            case ACCESSORY:
                return "Accessory";
            
            case CAP:
                return "Cap";
            
            case CAPE:
                return "Cape";
                    
            case COAT:
                return "Coat";
                        
            case FACE:
                return "Face";
                            
            case GLOVE:
                return "Glove";
                                
            case HAIR:
                return "Hair";
                                    
            case LONGCOAT:
                return "Longcoat";
                                        
            case PANTS:
                return "Pants";
                                            
            case PETEQUIP:
                return "PetEquip";
                
            case RING:
                return "Ring";
                    
            case SHIELD:
                return "Shield";
                        
            case SHOES:
                return "Shoes";
                            
            case TAMING:
                return "Taming";
                                
            case WEAPON:
                return "Weapon";
                
            default:
                return "Undefined";
        }
    }
    
    private static void readEquipNodeData(MapleDataProvider data, MapleDataDirectoryEntry mDir, String wzFileName, String dirName) {
        EquipType eqType = getEquipTypeFromDirectoryName(dirName);
        
        for(MapleDataFileEntry mFile : mDir.getFiles()) {
            String fileName = mFile.getName();

            try {
                int itemId = Integer.parseInt(fileName.substring(0, 8));        
                itemsWithNoNameProperty.put(itemId, curType);
                equipTypes.put(itemId, eqType);
                
                itemsWzPath.put(itemId, wzFileName + "/" + dirName + "/" + fileName);

                if(!isAccessory(itemId) && !isTamingMob(itemId)) {
                    try {
                        MapleData fileData = data.getData(dirName + "/" + fileName);
                        MapleData mdinfo = fileData.getChildByPath("info");
                        if( mdinfo.getChildByPath("cash") == null) {
                            equipsWithNoCashProperty.add(itemId);
                        }
                    } catch(NullPointerException npe) {
                        System.out.println("[SEVERE] " + mFile.getName() + " failed to load. Issue: " + npe.getMessage() + "\n\n");
                    }
                }
            } catch (Exception e) {}
        }
    }
    
    private static void readEquipWZData() {
        String wzFileName = "Character.wz";
        
        MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
        MapleDataDirectoryEntry root = data.getRoot();
        
        System.out.println("Parsing " + wzFileName + "...");
        for (MapleDataDirectoryEntry mDir : root.getSubdirectories()) {
            String dirName = mDir.getName();
            if(dirName.contentEquals("Dragon")) continue;
            
            readEquipNodeData(data, mDir, wzFileName, dirName);
        }
    }
    
    private static void readItemWZData() {
        String wzFileName = "Item.wz";
        
        MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
        MapleDataDirectoryEntry root = data.getRoot();
        
        System.out.println("Parsing " + wzFileName + "...");
        for (MapleDataDirectoryEntry mDir : root.getSubdirectories()) {
            String dirName = mDir.getName();
            if(dirName.contentEquals("Special")) continue;
            
            curType = getItemTypeFromDirectoryName(dirName);
            if(!dirName.contentEquals("Pet")) {
                for(MapleDataFileEntry mFile : mDir.getFiles()) {
                    String fileName = mFile.getName();

                    MapleData fileData = data.getData(dirName + "/" + fileName);
                    for(MapleData mData : fileData.getChildren()) {
                        try {
                            int itemId = Integer.parseInt(mData.getName());
                            itemsWithNoNameProperty.put(itemId, curType);
                            itemsWzPath.put(itemId, wzFileName + "/" + dirName + "/" + fileName);
                        } catch (Exception e) {
                            System.out.println("EXCEPTION on '" + mData.getName() + "' " + wzFileName + "/" + dirName + "/" + fileName);
                        }
                    }
                }
            } else {
                readEquipNodeData(data, mDir, wzFileName, dirName);
            }
        }
    }
    
    private static void printReportFileHeader() {
        printWriter.println(" # Report File autogenerated from the MapleInvalidItemWithNoNameFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static void printReportFileResults() {
        if(!itemsWithNoNameProperty.isEmpty()) {
            printWriter.println("Itemids with missing 'name' property: ");
            
            List<Integer> itemids = new ArrayList<>(itemsWithNoNameProperty.keySet());
            Collections.sort(itemids);
            
            for(Integer itemid : itemids) {
                printWriter.println("  " + itemid + " " + itemsWzPath.get(itemid));
            }
            printWriter.println();
        }
        
        if(!equipsWithNoCashProperty.isEmpty()) {
            printWriter.println("Equipids with missing 'cash' property: ");
            
            List<Integer> itemids = new ArrayList<>(equipsWithNoCashProperty);
            Collections.sort(itemids);
            
            for(Integer itemid : itemids) {
                printWriter.println("  " + itemid + " " + itemsWzPath.get(itemid));
            }
        }
    }
    
    private static Map<String, List<Integer>> filterMissingItemNames() {
        List<Integer> cashList = new ArrayList<>(20);
        List<Integer> consList = new ArrayList<>(20);
        List<Integer> eqpList = new ArrayList<>(20);
        List<Integer> etcList = new ArrayList<>(20);
        List<Integer> insList = new ArrayList<>(20);
        List<Integer> petList = new ArrayList<>(20);
        
        for(Entry<Integer, ItemType> ids : itemsWithNoNameProperty.entrySet()) {
            switch(ids.getValue()) {
                case CASH:
                    cashList.add(ids.getKey());
                    break;
                
                case CONSUME:
                    consList.add(ids.getKey());
                    break;
                    
                case EQP:
                    eqpList.add(ids.getKey());
                    break;
                    
                case ETC:
                    etcList.add(ids.getKey());
                    break;
                    
                case INS:
                    insList.add(ids.getKey());
                    break;
                    
                case PET:
                    petList.add(ids.getKey());
                    break;
            }
        }
        
        Map<String, List<Integer>> nameTags = new HashMap<>();
        nameTags.put("Cash.img", cashList);
        nameTags.put("Consume.img", consList);
        nameTags.put("Eqp.img", eqpList);
        nameTags.put("Etc.img", etcList);
        nameTags.put("Ins.img", insList);
        nameTags.put("Pet.img", petList);
        
        return nameTags;
    }
    
    private static void printOutputFileHeader() {
        printWriter.println(" # XML File autogenerated from the MapleInvalidItemWithNoNameFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data takes into account several data info from the server-side WZ.xmls.");
        printWriter.println();
    }
    
    private static String getMissingEquipName(int itemid) {
        String s = nameContentCache.get(itemid);
        if (s == null) {
            s = "MISSING NAME " + itemid;
        }
        
        return s;
    }
    
    private static String getMissingEquipDesc(int itemid) {
        String s = descContentCache.get(itemid);
        if (s == null) {
            s = "MISSING INFO " + itemid;
        }
        
        return s;
    }
    
    private static void writeMissingEquipInfo(Integer itemid) {
        printWriter.println("      <imgdir name=\"" + itemid + "\">");
        
        String s;
        s = getMissingEquipName(itemid);
        printWriter.println("        <string name=\"name\" value=\"" + s + "\"/>");
        
        s = getMissingEquipDesc(itemid);
        printWriter.println("        <string name=\"desc\" value=\"" + s + "\"/>");
        printWriter.println("      </imgdir>");
    }
    
    private static void writeEquipSubdirectoryHeader(EquipType eType) {
        printWriter.println("    <imgdir name=\"" + getStringDirectoryNameFromEquipType(eType) + "\">");
    }
    
    private static void writeEquipSubdirectoryFooter() {
        printWriter.println("    </imgdir>");
    }
    
    private static void writeEquipXMLHeader() {
        printWriter.println("  <imgdir name=\"Eqp\">");
    }
    
    private static void writeEquipXMLFooter() {
        printWriter.println("  </imgdir>");
    }
    
    private static void writeMissingItemInfo(Integer itemid) {
        printWriter.println("  <imgdir name=\"" + itemid + "\">");
        printWriter.println("    <string name=\"name\" value=\"MISSING NAME\"/>");
        printWriter.println("    <string name=\"desc\" value=\"MISSING INFO\"/>");
        printWriter.println("  </imgdir>");
    }
    
    private static void writeXMLHeader(String fileName) {
        printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        printWriter.println("<imgdir name=\"" + fileName + "\">");
    }
    
    private static void writeXMLFooter() {
        printWriter.println("</imgdir>");
    }
    
    private static void writeMissingEquipWZNode(EquipType eType, List<Integer> missingNames) {
        if(!missingNames.isEmpty()) {
            Collections.sort(missingNames);
            writeEquipSubdirectoryHeader(eType);
            
            for(Integer equipid : missingNames) {
                writeMissingEquipInfo(equipid);
            }
            
            writeEquipSubdirectoryFooter();
        }
    }
    
    private static void writeMissingStringWZNode(String nodePath, List<Integer> missingNames, boolean isEquip) {
        if(!missingNames.isEmpty()) {
            if(!isEquip) {
                Collections.sort(missingNames);

                printWriter.println(nodePath + ":");
                printWriter.println();

                writeXMLHeader(nodePath);

                for(Integer i : missingNames) {
                    writeMissingItemInfo(i);
                }

                writeXMLFooter();

                printWriter.println();
            } else {
                int arraySize = EquipType.values().length;
                
                List<Integer> equips[] = new List[arraySize];
                for(int i = 0; i < arraySize; i++) {
                    equips[i] = new ArrayList<>(42);
                }
                
                for(Integer itemid : missingNames) {
                    equips[equipTypes.get(itemid).ordinal()].add(itemid);
                }
                
                printWriter.println(nodePath + ":");
                printWriter.println();

                writeXMLHeader(nodePath);
                writeEquipXMLHeader();

                for(EquipType eType : EquipType.values()) {
                    writeMissingEquipWZNode(eType, equips[eType.ordinal()]);
                }

                writeEquipXMLFooter();
                writeXMLFooter();

                printWriter.println();
            }
        }
    }
    
    private static void writeMissingStringWZNames(Map<String, List<Integer>> missingNames) throws Exception {
        System.out.println("Writing remaining 'String.wz' names...");
        
        printWriter = new PrintWriter(xmlFile, "UTF-8");    
        printOutputFileHeader();
        
        String nodePaths[] = {"Cash.img", "Consume.img", "Eqp.img", "Etc.img", "Ins.img", "Pet.img"};
        for(int i = 0; i < nodePaths.length; i++) {
            writeMissingStringWZNode(nodePaths[i], missingNames.get(nodePaths[i]), i == 2);
        }
        
        printWriter.close();
    }
    
    public static void main(String[] args) {
        try {
            System.setProperty("wzpath", wzPath);
            
            curType = ItemType.EQP;
            readEquipWZData();
            
            curType = ItemType.UNDEF;
            readItemWZData();
            readStringWZData();             // calculates the diff and effectively holds all items with no name property on the WZ
            
            System.out.println("Reporting results...");
            printWriter = new PrintWriter(newFile, "UTF-8");
            printReportFileHeader();
            printReportFileResults();
            printWriter.close();
            
            Map<String, List<Integer>> missingNames = filterMissingItemNames();
            writeMissingStringWZNames(missingNames);
            
            System.out.println("Done!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

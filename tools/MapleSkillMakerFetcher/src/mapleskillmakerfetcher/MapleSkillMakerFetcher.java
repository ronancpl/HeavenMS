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
package mapleskillmakerfetcher;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.io.*;

/**
 * @author RonanLana
 *
 * The objective of this program is to uncover all maker data from the
 * ItemMaker.wz.xml files and generate a SQL file with every data info
 * for the Maker DB tables.
 * 
 */

public class MapleSkillMakerFetcher {
    static String host = "jdbc:mysql://localhost:3306/heavenms";
    static String driver = "com.mysql.jdbc.Driver";
    static String username = "root";
    static String password = "";

    static String fileName = "../../wz/Etc.wz/ItemMake.img.xml";
    static String newFile = "lib/MakerData.sql";

    static PrintWriter printWriter = null;
    static InputStreamReader fileReader = null;
    static BufferedReader bufferedReader = null;
    static byte status = 0;
    static byte state = 0;
    
    static int initialStringLength = 50;
    
    // maker data fields
    static int id = -1;
    static int itemid = -1;
    static int reqLevel = -1;
    static int reqMakerLevel = -1;
    static int reqItem = -1;
    static int reqMeso = -1;
    static int reqEquip = -1;
    static int catalyst = -1;
    static int quantity = -1;
    static int tuc = -1;
    
    static int recipePos = -1;
    static int recipeProb = -1;
    static int recipeCount = -1;
    static int recipeItem = -1;
    
    static List<int[]> recipeList = null;
    static List<int[]> randomList = null;
    static List<MapleMakerItemEntry> makerList = new ArrayList<>(100);
    
    private static void resetMakerDataFields() {
        reqLevel = 0;
        reqMakerLevel = 0;
        reqItem = 0;
        reqMeso = 0;
        reqEquip = 0;
        catalyst = 0;
        quantity = 0;
        tuc = 0;
        
        recipePos = 0;
        recipeProb = 0;
        recipeCount = 0;
        recipeItem = 0;
        
        recipeList = null;
        randomList = null;
    }
    
    private static String getName(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("name");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[initialStringLength];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        String s = d.trim();
        s.replaceFirst("^0+(?!$)", "");
        
        return(s);
    }
    
    private static String getValue(String token) {
        int i, j;
        char[] dest;
        String d;

        i = token.lastIndexOf("value");
        i = token.indexOf("\"", i) + 1; //lower bound of the string
        j = token.indexOf("\"", i);     //upper bound

        dest = new char[initialStringLength];
        token.getChars(i, j, dest, 0);

        d = new String(dest);
        String s = d.trim();
        s.replaceFirst("^0+(?!$)", "");
        
        return(s);
    }

    private static int[] generateRecipeItem() {
        int pair[] = new int[2];
        pair[0] = recipeItem;
        pair[1] = recipeCount;
        
        return pair;
    }
    
    private static int[] generateRandomItem() {
        int tuple[] = new int[3];
        tuple[0] = recipeItem;
        tuple[1] = recipeCount;
        tuple[2] = recipeProb;
        
        return tuple;
    }
    
    private static void simpleToken(String token) {
        if(token.contains("/imgdir")) {
            status -= 1;
        }
        else if(token.contains("imgdir")) {
            status += 1;
        }
    }
    
    private static void forwardCursor(int st) {
        String line = null;

        try {
            while(status >= st && (line = bufferedReader.readLine()) != null) {
                simpleToken(line);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void translateToken(String token) {
        String d;

        if(token.contains("/imgdir")) {
            status -= 1;
            
            if(status == 2) {   //close item maker data
                generateUpdatedItemFee();   // for equipments, this will try to update reqMeso to be conformant with the client.
                makerList.add(new MapleMakerItemEntry(id, itemid, reqLevel, reqMakerLevel, reqItem, reqMeso, reqEquip, catalyst, quantity, tuc, recipeCount, recipeItem, recipeList, randomList));
                resetMakerDataFields();
            } else if(status == 4) {    //close recipe/random item
                if(state == 0) recipeList.add(generateRecipeItem());
                else if(state == 1) randomList.add(generateRandomItem());
            }
        }
        else if(token.contains("imgdir")) {
            if(status == 1) {           //getting id
                d = getName(token);
                id = Integer.parseInt(d);
                System.out.println("Parsing maker id " + id);
            }
            else if(status == 2) {      //getting target item id
                d = getName(token);
                itemid = Integer.parseInt(d);
            }
            else if(status == 3) {
                d = getName(token);
                
                switch(d) {
                    case "recipe":
                        recipeList = new LinkedList<>();
                        state = 0;
                        break;
                        
                    case "randomReward":
                        randomList = new LinkedList<>();
                        state = 1;
                        break;
                                                
                    default:
                        forwardCursor(3);   // unused content, read until end of block
                        break;
                }
            }
            else if(status == 4) {  // inside recipe/random
                d = getName(token);
                recipePos = Integer.parseInt(d);
            }
            
            status += 1;
        } else {
            if(status == 3) {
                d = getName(token);
                
                switch(d) {
                    case "itemNum":
                        quantity = Integer.valueOf(getValue(token));
                        break;
                        
                    case "meso":
                        reqMeso = Integer.valueOf(getValue(token));
                        break;
                        
                    case "reqItem":
                        reqItem = Integer.valueOf(getValue(token));
                        break;
                            
                    case "reqLevel":
                        reqLevel = Integer.valueOf(getValue(token));
                        break;
                                
                    case "reqSkillLevel":
                        reqMakerLevel = Integer.valueOf(getValue(token));
                        break;
                                    
                    case "tuc":
                        tuc = Integer.valueOf(getValue(token));
                        break;
                                        
                    case "catalyst":
                        catalyst = Integer.valueOf(getValue(token));
                        break;
                                            
                    case "reqEquip":
                        reqEquip = Integer.valueOf(getValue(token));
                        break;
                        
                    default:
                        System.out.println("Unhandled case: '" + d + "'");
                        state = 2;
                        break;
                }
            }
            else if(status == 5) {  // inside recipe/random item
                d = getName(token);
                if(d.equals("item")) {
                    recipeItem = Integer.parseInt(getValue(token));
                } else {
                    if(state == 0) {
                        recipeCount = Integer.parseInt(getValue(token));
                    } else {
                        if(d.equals("itemNum")) {
                            recipeCount = Integer.parseInt(getValue(token));
                        } else {
                            recipeProb = Integer.parseInt(getValue(token));
                        }
                    }
                }
            }
        }
    }
    
    private static void generateUpdatedItemFee() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        float adjPrice = reqMeso;
        
        if(itemid < 2000000) {
            Map<String, Integer> stats = ii.getEquipStats(itemid);
            if(stats != null) {
                int val = itemid / 100000;
                
                if(val == 13 || val == 14) {    // is weapon-type
                    adjPrice /= 10;
                    adjPrice += reqMeso;

                    adjPrice /= 1000;
                    reqMeso = 1000 * (int) Math.floor(adjPrice);
                } else {
                    adjPrice /= ((stats.get("reqLevel") >= 108) ? 10 : 11);
                    adjPrice += reqMeso;

                    adjPrice /= 1000;
                    reqMeso = 1000 * (int) Math.ceil(adjPrice);
                }
            } else {
                System.out.println("null stats for itemid " + itemid);
            }
        } else {
            adjPrice /= 10;
            adjPrice += reqMeso;

            adjPrice /= 1000;
            reqMeso = 1000 * (int) Math.ceil(adjPrice);
        }
    }
    
    private static void WriteMakerTableFile() {
        printWriter.println(" # SQL File autogenerated from the MapleSkillMakerFetcher feature by Ronan Lana.");
        printWriter.println(" # Generated data is conformant with the ItemMake.img.xml file used to compile this.");
        printWriter.println();
        
        StringBuilder sb_create = new StringBuilder("INSERT IGNORE INTO `makercreatedata` (`id`, `itemid`, `req_level`, `req_maker_level`, `req_meso`, `req_item`, `req_equip`, `catalyst`, `quantity`, `tuc`) VALUES\r\n");
        StringBuilder sb_recipe = new StringBuilder("INSERT IGNORE INTO `makerrecipedata` (`itemid`, `req_item`, `count`) VALUES\r\n");
        StringBuilder sb_reward = new StringBuilder("INSERT IGNORE INTO `makerrewarddata` (`itemid`, `rewardid`, `quantity`, `prob`) VALUES\r\n");
        
        for(MapleMakerItemEntry it : makerList) {
            sb_create.append("  (" + it.id + ", " + it.itemid + ", " + it.reqLevel + ", " + it.reqMakerLevel + ", " + it.reqMeso + ", " + it.reqItem + ", " + it.reqEquip + ", " + it.catalyst + ", " + it.quantity + ", " + it.tuc + "),\r\n");
            
            if(it.recipeList != null) {
                for(int[] rit : it.recipeList) {
                   sb_recipe.append("  (" + it.itemid + ", " + rit[0] + ", " + rit[1] + "),\r\n");
                }
            }
            
            if(it.randomList != null) {
                for(int[] rit : it.randomList) {
                    sb_reward.append("  (" + it.itemid + ", " + rit[0] + ", " + rit[1] + ", " + rit[2] + "),\r\n");
                }
            }
        }
        
        sb_create.setLength(sb_create.length() - 3);
        sb_create.append(";\r\n");
        
        sb_recipe.setLength(sb_recipe.length() - 3);
        sb_recipe.append(";\r\n");
        
        sb_reward.setLength(sb_reward.length() - 3);
        sb_reward.append(";");
        
        printWriter.println(sb_create);
        printWriter.println(sb_recipe);
        printWriter.println(sb_reward);
    }

    private static void WriteMakerTableData() {
        // This will reference one line at a time
        String line = null;

        try {
            printWriter = new PrintWriter(newFile, "UTF-8");
            fileReader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
            bufferedReader = new BufferedReader(fileReader);

            resetMakerDataFields();
            
            while((line = bufferedReader.readLine()) != null) {
                translateToken(line);
            }
            
            WriteMakerTableFile();

            printWriter.close();
            bufferedReader.close();
            fileReader.close();
        }

        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
        }

        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WriteMakerTableData();
    }

}

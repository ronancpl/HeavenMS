/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dropspider;

import client.inventory.MapleInventoryType;
import constants.ItemConstants;

/**
 *
 * @author Simon
 */
public class DropEntry {
    private int version;
    private int item_id;
    private int monster_id;
    private int chance;
    private int mindrop;
    private int maxdrop;

    public DropEntry(int item_id, int monster_id, int version) {
        this.item_id = item_id;
        this.monster_id = monster_id;
        mindrop = 1;
        maxdrop = 1;
        chance = calculateChance(item_id);
        this.version = version;
    }

    private int calculateChance(int item_id) {
        MapleInventoryType mit = ItemConstants.getInventoryType(item_id);
        boolean boss = DataTool.isBoss(monster_id);
        int number = (item_id / 1000) % 1000;
        switch (mit) {
            case EQUIP:
                if (boss) {
                    return 40000;
                }
                return 700;
            case USE:
                if (boss) {
                    mindrop = 1;
                    maxdrop = 4;
                }
                switch (number) {
                    case 0: // normal potions
                        mindrop = 1;
                        if (version > 98) {
                            maxdrop = 5;
                        }
                        return 40000;
                    case 1: // watermelons, pills, speed potions, etc
                    case 2: // same thing
                        return 10000;
                    case 3: // advanced potions from crafting (should not drop)
                    case 4: // same thing
                    case 11: // poison mushroom
                    case 28: // cool items
                    case 30: // return scrolls
                    case 46: // gallant scrolls
                        return 0;
                    case 10: // strange potions like apples, eggs
                    case 12: // drakes blood, sap of ancient tree (rare use)
                    case 20: // salad, fried chicken, dews
                    case 22: // air bubbles and stuff. ALSO nependeath honey but oh well
                    case 50: // antidotes and stuff
                        return 3000;
                    case 290: // mastery books
                        if(boss)
                            return 40000;
                        else
                            return 1000;
                    case 40: // Scrolls
                    case 41: // Scrolls
                    case 43: // Scrolls
                    case 44: // Scrolls
                    case 48: // pet scrolls
                        if(boss)
                            return 10000;
                        else
                            return 750;
                    case 100: // summon bags
                    case 101: // summon bags
                    case 102: // summon bags
                    case 109: // summon bags
                    case 120: // pet food
                    case 211: // cliffs special potion
                    case 240: // rings
                    case 270: // pheromone, additional weird stuff
                    case 310: // teleport rock
                    case 320: // weird drops
                    case 390: // weird
                    case 430: // Scripted items
                    case 440: // jukebox
                    case 460: // magnifying glass
                    case 470: // golden hammer
                    case 490: // crystanol
                    case 500: // sp reset
                        return 0;
                    case 47: // tablets from dragon rider
                        return 220000;
                    case 49: // clean slats, potential scroll, ees
                    case 70: // throwing stars
                    case 210: // rare monster piece drops
                    case 330: // bullets
                        if(boss)
                            return 2500;
                        else
                            return 400;
                    case 60: // bow arrows
                    case 61: // crossbow arrows
                        mindrop = 10;
                        maxdrop = 50;
                        return 10000;
                    case 213: // boss transfrom
                        return 100000;
                    case 280: // skill books
                        if(boss)
                            return 20000;
                        else
                            return 1000;
                    case 381: // monster book things
                    case 382:
                    case 383:
                    case 384:
                    case 385:
                    case 386:
                    case 387:
                    case 388:
                        return 20000;
                    case 510: // recipes
                    case 511:
                    case 512:
                        return 10000;
                    default:
                        return 0;

                }
            case ETC:
                switch (number) {
                    case 0: // monster pieces
                        return 200000;
                    case 4: // crystal ores
                    case 130: // simulators
                    case 131: // manuals
                        return 3000;
                    case 30: // game pieces
                        return 10000;
                    case 32: // misc items
                        return 10000;
                    default:
                        return 7000;
                }
            default:
                return 7000;
        }
    }

    public String getQuerySegment() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(monster_id);
        sb.append(", ");
        sb.append(item_id);
        sb.append(", ");
        sb.append(mindrop);//min
        sb.append(", ");
        sb.append(maxdrop);//max
        sb.append(", ");
        sb.append(0);//quest
        sb.append(", ");
        sb.append(chance);
        sb.append(")");
        return sb.toString();
    }
}
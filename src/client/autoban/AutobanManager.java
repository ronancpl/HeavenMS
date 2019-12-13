/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.MapleCharacter;
import config.YamlConfig;
import java.util.HashMap;
import java.util.Map;
import net.server.Server;
import tools.FilePrinter;

/**
 *
 * @author kevintjuh93
 */
public class AutobanManager {
    private MapleCharacter chr;
    private Map<AutobanFactory, Integer> points = new HashMap<>();
    private Map<AutobanFactory, Long> lastTime = new HashMap<>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;
    private long spam[] = new long[20];
    private int timestamp[] = new int[20];
    private byte timestampcounter[] = new byte[20];


    public AutobanManager(MapleCharacter chr) {
        this.chr = chr;
    }

    public void addPoint(AutobanFactory fac, String reason) {
    	if (YamlConfig.config.server.USE_AUTOBAN) {
            if (chr.isGM() || chr.isBanned()){
                    return;
            }
            
            if (lastTime.containsKey(fac)) {
                if (lastTime.get(fac) < (Server.getInstance().getCurrentTime() - fac.getExpire())) {
                    points.put(fac, points.get(fac) / 2); //So the points are not completely gone.
                }
            }
            if (fac.getExpire() != -1)
                lastTime.put(fac, Server.getInstance().getCurrentTime());

            if (points.containsKey(fac)) {
                points.put(fac, points.get(fac) + 1);
            } else
                points.put(fac, 1);

            if (points.get(fac) >= fac.getMaximum()) {
                chr.autoban(reason);
            }
        }
        if (YamlConfig.config.server.USE_AUTOBAN_LOG) {
            // Lets log every single point too.
            FilePrinter.print(FilePrinter.AUTOBAN_WARNING, MapleCharacter.makeMapleReadable(chr.getName()) + " caused " + fac.name() + " " + reason);
        }
    }

    public void addMiss() {
        this.misses++;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses > 6) {
            samemisscount++;
        }
        if (samemisscount > 4)
        	chr.sendPolice("You will be disconnected for miss godmode.");
            //chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
        else if (samemisscount > 0)

        this.lastmisses = misses;
        this.misses = 0;
    }
    
    //Don't use the same type for more than 1 thing
    public void spam(int type) {
        this.spam[type] = Server.getInstance().getCurrentTime();
    }
    
    public void spam(int type, int timestamp) {
        this.spam[type] = timestamp;
    }

    public long getLastSpam(int type) {
        return spam[type];
    }

    /**
     * Timestamp checker
     *
     *  <code>type</code>:<br>
     * 1: Pet Food<br>
     * 2: InventoryMerge<br>
     * 3: InventorySort<br>
     * 4: SpecialMove<br>
     * 5: UseCatchItem<br>
     * 6: Item Drop<br>
     * 7: Chat<br>
     * 8: HealOverTimeHP<br>
     * 9: HealOverTimeMP<br>
     *
     * @param type type
     * @return Timestamp checker
     */
    public void setTimestamp(int type, int time, int times) {
        if (this.timestamp[type] == time) {  
            this.timestampcounter[type]++;
            if (this.timestampcounter[type] >= times) {
                if (YamlConfig.config.server.USE_AUTOBAN) {
                    chr.getClient().disconnect(false, false);
                }
                
                FilePrinter.print(FilePrinter.EXPLOITS, "Player " + chr + " was caught spamming TYPE " + type + " and has been disconnected.");
            }
        } else {
            this.timestamp[type] = time;
            this.timestampcounter[type] = 0;
        }
    }
}

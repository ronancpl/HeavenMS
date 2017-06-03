package server.events;
import client.MapleCharacter;
import java.util.*;
import server.life.MapleLifeFactory;
import java.awt.Point;
import server.maps.MapleMap;
import server.TimerManager;

/**
 *
 * @author FateJiki
 * @Mapid 105100300
 */
public class BalrogPQ {
    public static final int[] EasyBalrogParts = {8830002, 8830003, 8830000};
    public static final int[] HardBalrogParts = {8830000, 8830001, 8830002};
    public static List<MapleCharacter> candidates = new ArrayList<MapleCharacter>();
    public static boolean hasStarted = false;
    public static String partyLeader = "undefined";
    public static boolean balrogSpawned = false;
    public static long timeStamp = 0;
    public static byte channel = 1;
    
    public static void addCandidate(MapleCharacter chr){
        synchronized(candidates){
            candidates.add(chr);
        }
    }

    public static void warpAllCandidates(){
        for(MapleCharacter c : candidates){
            c.changeMap(105100300);
        }
    }

    public static boolean isFull(MapleCharacter chr){
        return chr.getClient().getChannelServer().getMapFactory().getMap(105100300).getCharacters().size() > 0;
    }

    public static void warpIn(MapleCharacter chr){
        if(hasStarted){
            chr.changeMap(105100300);
        }
    }

    public static void scheduleChecks(MapleMap map){
        final MapleMap fmap = map;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(new Runnable(){
            @Override
        public void run(){
                for(MapleCharacter chrs : fmap.getCharacters()){
                    chrs.changeMap(105100100);
                    chrs.message("You did not defeat the balrog in time..");
                    close();
                }
            }
        } , 60 * 60 * 1000);

                tMan.schedule(new Runnable(){
            @Override
        public void run(){
                if(fmap.getCharacters().size() <= 3){
                    if(fmap.getCharacters().size() > 0){
                        for(MapleCharacter chrs : fmap.getCharacters()){
                            chrs.message("[The Order]: What? You're down to that many mercenaries? I need to get you out of there.");
                            chrs.changeMap(105100100);
                        }
                    }
                    fmap.killAllMonsters();
                    close();
                }
            }
        } , 60 * 1000);
    }

    public static void open(MapleCharacter chr){
        channel = (byte)chr.getClient().getChannel();
        hasStarted = true;
        timeStamp = System.currentTimeMillis();
        scheduleChecks(chr.getClient().getChannelServer().getMapFactory().getMap(105100300));
    }

    public static int getSecondsLeft(){ // assuming the thing lasts 60 minutes
        int hour = 60 * 60; // 3600 seconds = 1hr
        long elapsed = System.currentTimeMillis() - timeStamp;
        int secondsLeft = (int)(hour - (elapsed / 1000));
        return secondsLeft;
    }

    public static void close(){
        hasStarted = false;
        balrogSpawned = false;
        partyLeader = "undefined";
        candidates.clear();
        timeStamp = 0;
    }
    public static void spawnBalrog(int mode, MapleCharacter chr){
        if(!balrogSpawned){
            for(int i = 0; i < HardBalrogParts.length; i++){
                chr.getClient().getChannelServer().getMapFactory().getMap(105100300).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(HardBalrogParts[i]), new Point(412, 258));
                balrogSpawned = true;
            }
        } else {
            // DO NUFFIN'
        }
    }
}  
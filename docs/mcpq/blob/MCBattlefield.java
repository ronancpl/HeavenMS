/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server.partyquest.mcpq;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import packet.creators.CarnivalPackets;
import packet.creators.PacketCreator;
import client.player.Player;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.SpawnPoint;
import server.life.Spawns;
import server.maps.Field;
import server.maps.FieldObject;
import server.maps.FieldObjectType;
import server.maps.reactors.MapleReactor;
import server.maps.reactors.MapleReactorFactory;
import server.partyquest.mcpq.MCField.MCTeam;
import static server.partyquest.mcpq.MCField.MCTeam.RED;
import server.partyquest.mcpq.MCWZData.MCGuardianGenPos;
import server.partyquest.mcpq.MCWZData.MCMobGenPos;


/**
 * Keeps track of guardians and spawns in MCPQ.
 *
 * @author s4nta
 */
public class MCBattlefield {

    private Field map;
    private MCWZData wzData;
    private int numGuardiansSpawned = 0;
    private int numMonstersSpawned = 0;
    // These map Guardian IDs (aka codes for status) to the guardian position.
    private Map<Integer, MCWZData.MCGuardianGenPos> redGuardianIdToPos = new HashMap<>();
    private Map<Integer, MCWZData.MCGuardianGenPos> blueGuardianIdToPos = new HashMap<>();
    // These map Reactor Object IDs to guardian objects.
    private Map<Integer, MCGuardian> redReactors = new HashMap<>();
    private Map<Integer, MCGuardian> blueReactors = new HashMap<>();
    // used for divided maps
    // we use an arraylist here for easier random element lookup.
    private List<MCWZData.MCGuardianGenPos> originalRedGuardianSpawns = new ArrayList<>();
    private List<MCWZData.MCGuardianGenPos> originalBlueGuardianSpawns = new ArrayList<>();
    // used for undivided map
    private List<MCWZData.MCGuardianGenPos> originalGuardianSpawns = new ArrayList<>();

    // used for divided maps
    // we use an arraylist here for easier random element lookup.
    private List<MCWZData.MCMobGenPos> originalRedSpawns = new ArrayList<>();
    private List<MCWZData.MCMobGenPos> originalBlueSpawns = new ArrayList<>();
    // use for undivided map
    private List<MCWZData.MCMobGenPos> originalUnifiedSpawns = new ArrayList<>();

    private List<SpawnPoint> originalSpawns = new ArrayList<>();
    private List<SpawnPoint> addedSpawns = new ArrayList<>();

    public MCBattlefield(Field battleInstance) {
        this.map = battleInstance;
        fetchCarnivalData();
        getOriginalSpawnPoints();
        populateGuardianSpawns();
        populateMobSpawns();
    }

    private void fetchCarnivalData() {
        wzData = this.map.getMCPQData();
        if (wzData == null) {
            MCTracker.log("[MCPQ] Fetching carnival failed for map " + map.getId());
        }
    }

    private void getOriginalSpawnPoints() {
        for (Spawns sp : this.map.getSpawnPoints()) {
            originalSpawns.add((SpawnPoint) sp);
        }
    }

    private void populateGuardianSpawns() {
        for (MCWZData.MCGuardianGenPos gpos : wzData.guardianGenPosList) {
            switch (gpos.team) {
                case 0:
                    originalRedGuardianSpawns.add(gpos);
                    break;
                case 1:
                    originalBlueGuardianSpawns.add(gpos);
                    break;
                default:
                    originalGuardianSpawns.add(gpos);
            }
        }
    }

    private void populateMobSpawns() {
        for (MCWZData.MCMobGenPos mpos : wzData.mobGenPosList) {
            switch (mpos.team) {
                case 0:
                    originalRedSpawns.add(mpos);
                    break;
                case 1:
                    originalBlueSpawns.add(mpos);
                    break;
                default:
                    originalUnifiedSpawns.add(mpos);
                    break;
            }
        }
    }

    public void addSpawn(Player chr, int num) {
        if (numMonstersSpawned > wzData.mobGenMax) {
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(3));
            return;
        }

        MCWZData.MCSummonMob mobToSummon = wzData.summons.get(num);
        MCWZData.MCMobGenPos spawnPos = getRandomSpawnPos(chr.getMCPQTeam());

        MCField.MCTeam team = chr.getMCPQTeam();
        if (spawnPos == null) { 
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(2));
            return;
        }

        int spendCp = mobToSummon.spendCP;
        if (spendCp > chr.getAvailableCP()) {
            readdSpawn(spawnPos, team);
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(1));
            return;
        }

        chr.getMCPQField().loseCP(chr, spendCp);
        this.map.broadcastMessage(CarnivalPackets.PlayerSummoned(MonsterCarnival.TAB_SPAWNS, num, chr.getName()));
        numMonstersSpawned++; // TODO: AtomicInteger this

        MapleMonster monster = MapleLifeFactory.getMonster(mobToSummon.id);
        Point pos = new Point(spawnPos.x, spawnPos.y);
        SpawnPoint sp = new SpawnPoint(monster, pos, mobToSummon.mobTime, chr.getTeam());

        addedSpawns.add(sp);
        updateMonsterBuffs();
    }

    public void useSkill(Player chr, int num) {
        if (!wzData.skills.containsKey(num)) {
            MCTracker.log("Attempting to use a null skill.");
            return;
        }
        int realSkill = wzData.skills.get(num);
        MCSkill skill = MCSkillFactory.getMCSkill(realSkill);
        // TODO: add skill cooldowns

        int spendCp = skill.getSpendCP();
        if (spendCp > chr.getAvailableCP()) {
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(1));
            return;
        }

        MCParty teamToApply = chr.getMCPQParty().getEnemy();
        boolean success = teamToApply.applyMCSkill(skill);

        if (success) {
            chr.getMCPQField().loseCP(chr, spendCp);
            map.broadcastMessage(CarnivalPackets.PlayerSummoned(MonsterCarnival.TAB_DEBUFF, num, chr.getName()));
        } else {
            chr.getClient().getSession().write(CarnivalPackets.CarnivalMessage(5));
        }
    }
    
    public void readdSpawn(MCWZData.MCMobGenPos pos, MCField.MCTeam team) {
        List<MCWZData.MCMobGenPos> lst = null;
        if (this.wzData.mapDivided) {
            if (null == team) {
                return;
            } else switch (team) {
                case RED:
                    lst = originalRedSpawns;
                    break;
                case BLUE:
                    lst = originalBlueSpawns;
                    break;
                default:
                    return;
            }
        } else {
            lst = originalUnifiedSpawns;
        }
        
        if (lst == null) {
            return;
        } 
        lst.add(pos);
    }

    public void spawnGuardian(Player chr, int num) {
        if (numGuardiansSpawned > wzData.guardianGenMax) {
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(3));
            return;
        }

        int guardianId = wzData.guardians.get(num);
        MCGuardian guardian = MCSkillFactory.getMCGuardian(guardianId);
        if (guardian == null) {
            MCTracker.log("Attempting to spawn invalid guardian.");
            return;
        }

        MCField.MCTeam team = chr.getMCPQTeam();
        if (team == MCField.MCTeam.RED) {
            if (redGuardianIdToPos.containsKey(guardianId)) {
                chr.getClient().announce(CarnivalPackets.CarnivalMessage(4));
                return;
            }
        } else if (team == MCField.MCTeam.BLUE) {
            if (blueGuardianIdToPos.containsKey(guardianId)) {
                chr.getClient().announce(CarnivalPackets.CarnivalMessage(4));
                return;
            }
        }
        int spendCp = guardian.getSpendCP();
        if (spendCp > chr.getAvailableCP()) {
            chr.getClient().announce(CarnivalPackets.CarnivalMessage(1));
            return;
        }

        chr.getMCPQField().loseCP(chr, spendCp);
        this.map.broadcastMessage(CarnivalPackets.PlayerSummoned(MonsterCarnival.TAB_GUARDIAN, num, chr.getName()));
        numGuardiansSpawned++; // TODO: AtomicInteger this
        MCWZData.MCGuardianGenPos genPos = getRandomGuardianPos(team);
        Point spawnPos = new Point(genPos.x, genPos.y);

        MapleReactor reactor;
        if (null == team) {
            return;
        } else switch (team) {
            case RED:
                reactor = new MapleReactor(MapleReactorFactory.getReactor(MonsterCarnival.GUARDIAN_RED), MonsterCarnival.GUARDIAN_RED);
                reactor.setPosition(spawnPos);
                redGuardianIdToPos.put(num, genPos);
                break;
            case BLUE:
                reactor = new MapleReactor(MapleReactorFactory.getReactor(MonsterCarnival.GUARDIAN_BLUE), MonsterCarnival.GUARDIAN_BLUE);
                reactor.setPosition(spawnPos);
                blueGuardianIdToPos.put(num, genPos);
                break;
            default:
                return;
        }

        reactor.setDelay(-1);
        map.spawnReactor(reactor);

        if (team == MCField.MCTeam.RED) {
            redReactors.put(reactor.getObjectId(), MCSkillFactory.getMCGuardian(num));
        } else {
            blueReactors.put(reactor.getObjectId(), MCSkillFactory.getMCGuardian(num));
        }

        map.setReactorState(reactor, (byte) 1); 
        updateMonsterBuffs();
    }

    public void onGuardianHit(Player chr, MapleReactor reactor) {
        if (MonsterCarnival.DEBUG) {
            System.out.println("STATE: " + reactor.getState());
        }
        MCField.MCTeam team = chr.getMCPQTeam();
        if (team == MCField.MCTeam.RED && reactor.getId() == MonsterCarnival.GUARDIAN_RED) {
            return;
        }
        if (team == MCField.MCTeam.BLUE && reactor.getId() == MonsterCarnival.GUARDIAN_BLUE) {
            return;
        }
        reactor.setState((byte) (reactor.getState() + 1));
        map.broadcastMessage(PacketCreator.TriggerReactor(reactor, reactor.getState()));

        if (reactor.getState() > 3) {
            int reactorObjId = reactor.getObjectId();
            map.destroyReactor(reactorObjId);

            MCGuardian guard;
            MCWZData.MCGuardianGenPos guardianGenPos;
            if (team == MCField.MCTeam.RED) {
                guard = blueReactors.remove(reactorObjId);
                guardianGenPos = blueGuardianIdToPos.remove(guard.getType());
            } else {
                guard = redReactors.remove(reactorObjId);
                guardianGenPos = redGuardianIdToPos.remove(guard.getType());
            }
            numGuardiansSpawned--;
            
            if (MonsterCarnival.DEBUG) {
                System.out.println("Removing reactor with x = " + guardianGenPos.x);
            }
            if (wzData.mapDivided) {
                if (team == MCField.MCTeam.RED) {
                    originalBlueGuardianSpawns.add(guardianGenPos);
                } else {
                    originalRedGuardianSpawns.add(guardianGenPos);
                }
            } else {
                originalGuardianSpawns.add(guardianGenPos);
            }

            if (MonsterCarnival.DEBUG) {
                System.out.println("Attempting to remove buff " + guard.getName());
            }
            updateMonsterBuffs();
        }
    }

    private MCGuardianGenPos getRandomGuardianPos(MCTeam team) {
        if (this.wzData.mapDivided) {
            if (null == team) {
                return null;
            } else switch (team) {
                case RED: {
                    int randIndex = (int) Math.floor(Math.random() * this.originalRedGuardianSpawns.size());
                    return originalRedGuardianSpawns.remove(randIndex);
                }
                case BLUE: {
                    int randIndex = (int) Math.floor(Math.random() * this.originalBlueGuardianSpawns.size());
                    return originalBlueGuardianSpawns.remove(randIndex);
                }
                default:
                    return null;
            }
        } else {
            int randIndex = (int) Math.floor(Math.random() * this.originalGuardianSpawns.size());
            return originalGuardianSpawns.remove(randIndex);
        }
    }

    private MCMobGenPos getRandomSpawnPos(MCTeam team) {
        List<MCMobGenPos> lst = null;
        if (this.wzData.mapDivided) {
            if (null == team) {
                return null;
            } else switch (team) {
                case RED:
                    lst = originalRedSpawns;
                    break;
                case BLUE:
                    lst = originalBlueSpawns;
                    break;
                default:
                    return null;
            }
        } else {
            lst = originalUnifiedSpawns;
        }
        
        if (lst == null) {
            return null;
        } 
        if (lst.isEmpty()) {
            return null;
        }
        int randIndex = (int) Math.floor(Math.random() * lst.size());
        return lst.remove(randIndex);
    }

    private void updateMonsterBuffs() {
        List<MCGuardian> redGuardians = new ArrayList<>();
        List<MCGuardian> blueGuardians = new ArrayList<>();

        for (MCGuardian g : this.redReactors.values()) {
            redGuardians.add(g);
            if (MonsterCarnival.DEBUG) {
                System.out.println("update buff red " + g.getMobSkillID());
            }
        }
        for (MCGuardian g : this.blueReactors.values()) {
            blueGuardians.add(g);
            if (MonsterCarnival.DEBUG) {
                System.out.println("update buff blue " + g.getMobSkillID());
            }
        }

        for (FieldObject mmo : map.getAllMonsters()) {
            if (mmo.getType() == FieldObjectType.MONSTER) {
                MapleMonster mob = ((MapleMonster) mmo);
                mob.dispel();

                if (mob.getTeam() == MCField.MCTeam.RED.code) {
                    applyGuardians(mob, redGuardians);
                } else if (mob.getTeam() == MCField.MCTeam.BLUE.code) {
                    applyGuardians(mob, blueGuardians);
                } else {
                    MCTracker.log("[MCPQ] Attempting to give guardians to mob without team.");
                }
            }
        }
    }

    private void giveMonsterBuffs(MapleMonster mob) {
        List<MCGuardian> redGuardians = new ArrayList<>();
        List<MCGuardian> blueGuardians = new ArrayList<>();

        for (MCGuardian g : this.redReactors.values()) {
            redGuardians.add(g);
            if (MonsterCarnival.DEBUG) {
                System.out.println("update buff red " + g.getMobSkillID());
            }
        }
        for (MCGuardian g : this.blueReactors.values()) {
            blueGuardians.add(g);
            if (MonsterCarnival.DEBUG) {
                System.out.println("update buff blue " + g.getMobSkillID());
            }
        }

        if (mob.getTeam() == MCField.MCTeam.RED.code) {
            applyGuardians(mob, redGuardians);
        } else if (mob.getTeam() == MCField.MCTeam.BLUE.code) {
            applyGuardians(mob, blueGuardians);
        } else {
            MCTracker.log("[MCPQ] Attempting to give guardians to mob without team.");
        }
    }

    private void applyGuardians(MapleMonster mob, List<MCGuardian> guardians) {
        for (MCGuardian g : guardians) {
            MobSkill sk = MobSkillFactory.getMobSkill(g.getMobSkillID(), g.getLevel());
            sk.applyEffect(null, mob, true);
        }
    }

    public void spawningTask() {
        for (SpawnPoint sp : originalSpawns) {
            if (sp.shouldSpawn()) {
                MapleMonster mob = sp.spawnMonster(this.map);
                giveMonsterBuffs(mob);
            }
        }
        for (SpawnPoint sp : addedSpawns) {
            if (sp.shouldSpawn()) {
                MapleMonster mob = sp.spawnMonster(this.map);
                giveMonsterBuffs(mob);
            }
        }
    }
}  

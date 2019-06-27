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
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.Skill;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import client.status.MonsterStatusEffect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.maps.MapleSummon;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SummonDamageHandler extends AbstractDealDamageHandler {
    
    public final class SummonAttackEntry {

        private int monsterOid;
        private int damage;
        private boolean magic;

        public SummonAttackEntry(int monsterOid, int damage, boolean magic) {
            this.monsterOid = monsterOid;
            this.damage = damage;
            this.magic = magic;
        }

        public int getMonsterOid() {
            return monsterOid;
        }

        public int getDamage() {
            return damage;
        }
        
        public boolean isMagic() {
            return magic;
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        MapleSummon summon = null;
        for (MapleSummon sum : player.getSummonsValues()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon == null) {
            return;
        }
        Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        slea.skip(4);
        List<SummonAttackEntry> allDamage = new ArrayList<>();
        byte direction = slea.readByte();
        int numAttacked = slea.readByte();
        slea.skip(8); //Thanks Gerald :D, I failed lol (mob x,y and summon x,y)
        for (int x = 0; x < numAttacked; x++) {
            int monsterOid = slea.readInt(); // attacked oid
            slea.skip(17);
            boolean magic = slea.readByte() != 0;
            int damage = slea.readInt();
            allDamage.add(new SummonAttackEntry(monsterOid, damage, magic));
        }
        player.getMap().broadcastMessage(player, MaplePacketCreator.summonAttack(player.getId(), summon.getObjectId(), direction, allDamage), summon.getPosition());
        if (player.getMap().isOwnershipRestricted(player)) {
            return;
        }
        
        Map<Integer, Integer> maxDmgEntries = new HashMap<>();
        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster target = player.getMap().getMonsterByOid(attackEntry.getMonsterOid());
            if (target != null) {
                Integer maxDmg = maxDmgEntries.get(attackEntry.getMonsterOid());
                if (maxDmg == null) {
                    maxDmg = calcMaxDamage(summonEffect, player, attackEntry.isMagic());    // thanks Darter (YungMoozi) for reporting unchecked max dmg
                    maxDmgEntries.put(attackEntry.getMonsterOid(), maxDmg);
                }
                
                if (damage > maxDmg) {
                    AutobanFactory.DAMAGE_HACK.alert(c.getPlayer(), "Possible packet editing summon damage exploit.");

                    FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " used a summon of skillid " + summon.getSkill() + " to attack " + MapleMonsterInformationProvider.getInstance().getMobNameFromId(target.getId()) + " with damage " + damage + " (max: " + maxDmg + ")");
                    damage = maxDmg;
                }
                
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                    if (summonEffect.makeChanceResult()) {
                        target.applyStatus(player, new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, null, false), summonEffect.isPoison(), 4000);
                    }
                }
                player.getMap().damageMonster(player, target, damage);
            }
        }
    }
    
    private static int calcMaxDamage(MapleStatEffect summonEffect, MapleCharacter player, boolean magic) {
        double maxDamage;
        
        if (magic) {
            maxDamage = player.calculateMaxBaseMagicDamage() * (0.05 * summonEffect.getMatk());
        } else {
            maxDamage = player.calculateMaxBaseDamage(player.getTotalWatk()) * (0.021 * summonEffect.getWatk());
        }
        
        return (int) maxDamage;
    }
}

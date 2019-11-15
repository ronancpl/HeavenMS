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

import java.util.Map;
import client.MapleClient;
import client.MapleCharacter;
import client.autoban.AutobanFactory;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.maps.MapleMap;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 * @author Ronan
 */
public final class MobDamageMobHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int from = slea.readInt();
        slea.readInt();
        int to = slea.readInt();
        boolean magic = slea.readByte() == 0;
        int dmg = slea.readInt();
        MapleCharacter chr = c.getPlayer();
        
        MapleMap map = chr.getMap();
        MapleMonster attacker = map.getMonsterByOid(from);
        MapleMonster damaged = map.getMonsterByOid(to);
        
        if (attacker != null && damaged != null) {
            int maxDmg = calcMaxDamage(attacker, damaged, magic);     // thanks Darter (YungMoozi) for reporting unchecked dmg
            
            if (dmg > maxDmg) {
                AutobanFactory.DAMAGE_HACK.alert(c.getPlayer(), "Possible packet editing hypnotize damage exploit.");   // thanks Rien dev team
                
                FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " had hypnotized " + MapleMonsterInformationProvider.getInstance().getMobNameFromId(attacker.getId()) + " to attack " + MapleMonsterInformationProvider.getInstance().getMobNameFromId(damaged.getId()) + " with damage " + dmg + " (max: " + maxDmg + ")");
                dmg = maxDmg;
            }
            
            map.damageMonster(chr, damaged, dmg);
            map.broadcastMessage(chr, MaplePacketCreator.damageMonster(to, dmg), false);
        }
    }
    
    private static int calcMaxDamage(MapleMonster attacker, MapleMonster damaged, boolean magic) {
        int attackerAtk, damagedDef, attackerLevel = attacker.getLevel();
        double maxDamage;
        if (magic) {
            int atkRate = calcModifier(attacker, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MATK);
            attackerAtk = (attacker.getStats().getMADamage() * atkRate) / 100;
            
            int defRate = calcModifier(damaged, MonsterStatus.MAGIC_DEFENSE_UP, MonsterStatus.MDEF);
            damagedDef = (damaged.getStats().getMDDamage() * defRate) / 100;
            
            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(12));
        } else {
            int atkRate = calcModifier(attacker, MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WATK);
            attackerAtk = (attacker.getStats().getPADamage() * atkRate) / 100;
            
            int defRate = calcModifier(damaged, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.WDEF);
            damagedDef = (damaged.getStats().getPDDamage() * defRate) / 100;
            
            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(17));
        }
        
        return (int) maxDamage;
    }
    
    private static int calcModifier(MapleMonster monster, MonsterStatus buff, MonsterStatus nerf) {
        int atkModifier;
        final Map<MonsterStatus, MonsterStatusEffect> monsterStati = monster.getStati();
        
        MonsterStatusEffect atkBuff = monsterStati.get(buff);
        if (atkBuff != null) {
            atkModifier = atkBuff.getStati().get(buff);
        } else {
            atkModifier = 100;
        }

        MonsterStatusEffect atkNerf = monsterStati.get(nerf);
        if (atkNerf != null) {
            atkModifier -= atkNerf.getStati().get(nerf);
        }
        
        return atkModifier;
    }
}

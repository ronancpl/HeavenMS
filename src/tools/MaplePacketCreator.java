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
package tools;

import java.awt.Point;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import client.BuddylistEntry;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCharacter.SkillEntry;
import client.MapleClient;
import client.MapleDisease;
import client.MapleFamilyEntitlement;
import client.MapleFamilyEntry;
import client.keybind.MapleKeyBinding;
import client.keybind.MapleQuickslotBinding;
import client.MapleMount;
import client.MapleQuestStatus;
import client.MapleRing;
import client.MapleStat;
import client.MonsterBook;
import client.Skill;
import client.SkillMacro;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.ModifyInventory;
import client.newyear.NewYearCardRecord;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import config.YamlConfig;
import constants.game.ExpTable;
import constants.game.GameConstants;
import constants.inventory.ItemConstants;
import constants.net.ServerConstants;
import constants.skills.Buccaneer;
import constants.skills.Corsair;
import constants.skills.ThunderBreaker;
import net.opcodes.SendOpcode;
import net.server.PlayerCoolDownValueHolder;
import net.server.Server;
import net.server.channel.Channel;
import net.server.channel.handlers.PlayerInteractionHandler;
import net.server.channel.handlers.SummonDamageHandler.SummonAttackEntry;
import net.server.guild.MapleAlliance;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleGuildSummary;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import net.server.world.PartyOperation;
import net.server.world.World;
import server.CashShop.CashItem;
import server.CashShop.CashItemFactory;
import server.CashShop.SpecialCashItem;
import server.DueyPackage;
import server.MTSItemInfo;
import server.MapleItemInformationProvider;
import server.MapleShopItem;
import server.MapleTrade;
import server.events.gm.MapleSnowball;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.life.MaplePlayerNPC;
import server.life.MobSkill;
import server.maps.AbstractMapleMapObject;
import server.maps.MapleDoor;
import server.maps.MapleDoorObject;
import server.maps.MapleDragon;
import server.maps.MapleHiredMerchant;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMiniGame;
import server.maps.MapleMiniGame.MiniGameResult;
import server.maps.MapleMist;
import server.maps.MaplePlayerShop;
import server.maps.MaplePlayerShopItem;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.movement.LifeMovementFragment;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import java.util.TimeZone;

/**
 *
 * @author Frz
 */
public class MaplePacketCreator {

        public static final List<Pair<MapleStat, Integer>> EMPTY_STATUPDATE = Collections.emptyList();
        private final static long FT_UT_OFFSET = 116444736010800000L + (10000L * TimeZone.getDefault().getOffset(System.currentTimeMillis())); // normalize with timezone offset suggested by Ari
        private final static long DEFAULT_TIME = 150842304000000000L;//00 80 05 BB 46 E6 17 02
        public final static long ZERO_TIME = 94354848000000000L;//00 40 E0 FD 3B 37 4F 01
        private final static long PERMANENT = 150841440000000000L; // 00 C0 9B 90 7D E5 17 02

        private static long getTime(long utcTimestamp) {
                if (utcTimestamp < 0 && utcTimestamp >= -3) {
                        if (utcTimestamp == -1) {
                                return DEFAULT_TIME;    //high number ll
                        } else if (utcTimestamp == -2) {
                                return ZERO_TIME;
                        } else {
                                return PERMANENT;
                        }
                }
                
                return utcTimestamp * 10000 + FT_UT_OFFSET;
        }

        public static byte[] showHpHealed(int cid, int amount) { 
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(); 
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue()); 
                mplew.writeInt(cid); 
                mplew.write(0x0A); //Type 
                mplew.write(amount); 
                return mplew.getPacket(); 
        }  

        private static void addRemainingSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                int remainingSp[] = chr.getRemainingSps();
                int effectiveLength = 0;
                for (int i = 0; i < remainingSp.length; i++) {
                        if (remainingSp[i] > 0) {
                                effectiveLength++;
                        }
                }

                mplew.write(effectiveLength);
                for (int i = 0; i < remainingSp.length; i++) {
                        if (remainingSp[i] > 0) {
                                mplew.write(i + 1);
                                mplew.write(remainingSp[i]);
                        }
                }
        }
        
        private static void addCharStats(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeInt(chr.getId()); // character id
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getName(), '\0', 13));
                mplew.write(chr.getGender()); // gender (0 = male, 1 = female)
                mplew.write(chr.getSkinColor().getId()); // skin color
                mplew.writeInt(chr.getFace()); // face
                mplew.writeInt(chr.getHair()); // hair

                for (int i = 0; i < 3; i++) {
                        MaplePet pet = chr.getPet(i);
                        if (pet != null) //Checked GMS.. and your pets stay when going into the cash shop.
                        {
                                mplew.writeLong(pet.getUniqueId());
                        } else {
                                mplew.writeLong(0);
                        }
                }

                mplew.write(chr.getLevel()); // level
                mplew.writeShort(chr.getJob().getId()); // job
                mplew.writeShort(chr.getStr()); // str
                mplew.writeShort(chr.getDex()); // dex
                mplew.writeShort(chr.getInt()); // int
                mplew.writeShort(chr.getLuk()); // luk
                mplew.writeShort(chr.getHp()); // hp (?)
                mplew.writeShort(chr.getClientMaxHp()); // maxhp
                mplew.writeShort(chr.getMp()); // mp (?)
                mplew.writeShort(chr.getClientMaxMp()); // maxmp
                mplew.writeShort(chr.getRemainingAp()); // remaining ap
                if (GameConstants.hasSPTable(chr.getJob())) {
                        addRemainingSkillInfo(mplew, chr);
                } else {
                        mplew.writeShort(chr.getRemainingSp()); // remaining sp
                }
                mplew.writeInt(chr.getExp()); // current exp
                mplew.writeShort(chr.getFame()); // fame
                mplew.writeInt(chr.getGachaExp()); //Gacha Exp
                mplew.writeInt(chr.getMapId()); // current map id
                mplew.write(chr.getInitialSpawnpoint()); // spawnpoint
                mplew.writeInt(0);
        }

        protected static void addCharLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
                mplew.write(chr.getGender());
                mplew.write(chr.getSkinColor().getId()); // skin color
                mplew.writeInt(chr.getFace()); // face
                mplew.write(mega ? 0 : 1);
                mplew.writeInt(chr.getHair()); // hair
                addCharEquips(mplew, chr);
        }

        private static void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeLong(-1);
                mplew.write(0);
                addCharStats(mplew, chr);
                mplew.write(chr.getBuddylist().getCapacity());

                if (chr.getLinkedName() == null) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                        mplew.writeMapleAsciiString(chr.getLinkedName());
                }

                mplew.writeInt(chr.getMeso());
                addInventoryInfo(mplew, chr);
                addSkillInfo(mplew, chr);
                addQuestInfo(mplew, chr);
                addMiniGameInfo(mplew, chr);
                addRingInfo(mplew, chr);
                addTeleportInfo(mplew, chr);
                addMonsterBookInfo(mplew, chr);
                addNewYearInfo(mplew, chr);
                addAreaInfo(mplew, chr);//assuming it stayed here xd
                mplew.writeShort(0);
        }

        private static void addNewYearInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                Set<NewYearCardRecord> received = chr.getReceivedNewYearRecords();
            
                mplew.writeShort(received.size());
                for(NewYearCardRecord nyc : received) {
                        encodeNewYearCard(nyc, mplew);
                }
        }

        private static void addTeleportInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                final List<Integer> tele = chr.getTrockMaps();
                final List<Integer> viptele = chr.getVipTrockMaps();
                for (int i = 0; i < 5; i++) {
                        mplew.writeInt(tele.get(i));
                }
                for (int i = 0; i < 10; i++) {
                        mplew.writeInt(viptele.get(i));
                }
        }

        private static void addMiniGameInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeShort(0);
                /*for (int m = size; m > 0; m--) {//nexon does this :P
                 mplew.writeInt(0);
                 mplew.writeInt(0);
                 mplew.writeInt(0);
                 mplew.writeInt(0);
                 mplew.writeInt(0);
                 }*/
        }

        private static void addAreaInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                Map<Short, String> areaInfos = chr.getAreaInfos();
                mplew.writeShort(areaInfos.size());
                for (Short area : areaInfos.keySet()) {
                        mplew.writeShort(area);
                        mplew.writeMapleAsciiString(areaInfos.get(area));
                }
        }

        private static void addCharEquips(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
                Collection<Item> ii = MapleItemInformationProvider.getInstance().canWearEquipment(chr, equip.list());
                Map<Short, Integer> myEquip = new LinkedHashMap<>();
                Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
                for (Item item : ii) {
                        short pos = (byte) (item.getPosition() * -1);
                        if (pos < 100 && myEquip.get(pos) == null) {
                                myEquip.put(pos, item.getItemId());
                        } else if (pos > 100 && pos != 111) { // don't ask. o.o
                                pos -= 100;
                                if (myEquip.get(pos) != null) {
                                        maskedEquip.put(pos, myEquip.get(pos));
                                }
                                myEquip.put(pos, item.getItemId());
                        } else if (myEquip.get(pos) != null) {
                                maskedEquip.put(pos, item.getItemId());
                        }
                }
                for (Entry<Short, Integer> entry : myEquip.entrySet()) {
                        mplew.write(entry.getKey());
                        mplew.writeInt(entry.getValue());
                }
                mplew.write(0xFF);
                for (Entry<Short, Integer> entry : maskedEquip.entrySet()) {
                        mplew.write(entry.getKey());
                        mplew.writeInt(entry.getValue());
                }
                mplew.write(0xFF);
                Item cWeapon = equip.getItem((short) -111);
                mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
                for (int i = 0; i < 3; i++) {
                        if (chr.getPet(i) != null) {
                                mplew.writeInt(chr.getPet(i).getItemId());
                        } else {
                                mplew.writeInt(0);
                        }
                }
        }
        
        public static byte[] setExtraPendantSlot(boolean toggleExtraSlot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_EXTRA_PENDANT_SLOT.getValue());
                mplew.writeBool(toggleExtraSlot);
                return mplew.getPacket();
        }

        private static void addCharEntry(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean viewall) {
                addCharStats(mplew, chr);
                addCharLook(mplew, chr, false);
                if (!viewall) {
                        mplew.write(0);
                }
                if (chr.isGM() || chr.isGmJob()) {  // thanks Daddy Egg (Ubaware), resinate for noticing GM jobs crashing on non-GM players account
                        mplew.write(0);
                        return;
                }
                mplew.write(1); // world rank enabled (next 4 ints are not sent if disabled) Short??
                mplew.writeInt(chr.getRank()); // world rank
                mplew.writeInt(chr.getRankMove()); // move (negative is downwards)
                mplew.writeInt(chr.getJobRank()); // job rank
                mplew.writeInt(chr.getJobRankMove()); // move (negative is downwards)
        }
        
        private static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                List<MapleQuestStatus> started = chr.getStartedQuests();
                int startedSize = 0;
                for (MapleQuestStatus qs : started) {
                        if (qs.getInfoNumber() > 0) {
                                startedSize++;
                        }
                        startedSize++;
                }
                mplew.writeShort(startedSize);
                for (MapleQuestStatus qs : started) {
                        mplew.writeShort(qs.getQuest().getId());
                        mplew.writeMapleAsciiString(qs.getProgressData());
                        
                        short infoNumber = qs.getInfoNumber();
                        if (infoNumber > 0) {
                                MapleQuestStatus iqs = chr.getQuest(infoNumber);
                                mplew.writeShort(infoNumber);
                                mplew.writeMapleAsciiString(iqs.getProgressData());
                        }
                }
                List<MapleQuestStatus> completed = chr.getCompletedQuests();
                mplew.writeShort(completed.size());
                for (MapleQuestStatus qs : completed) {
                        mplew.writeShort(qs.getQuest().getId());
                        mplew.writeLong(getTime(qs.getCompletionTime()));
                }
        }
        
        private static void addExpirationTime(final MaplePacketLittleEndianWriter mplew, long time) {
                mplew.writeLong(getTime(time)); // offset expiration time issue found thanks to Thora
        }

        private static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item) {
                addItemInfo(mplew, item, false);
        }
        
        protected static void addItemInfo(final MaplePacketLittleEndianWriter mplew, Item item, boolean zeroPosition) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                boolean isCash = ii.isCash(item.getItemId());
                boolean isPet = item.getPetId() > -1;
                boolean isRing = false;
                Equip equip = null;
                short pos = item.getPosition();
                byte itemType = item.getItemType();
                if (itemType == 1) {
                        equip = (Equip) item;
                        isRing = equip.getRingId() > -1;
                }
                if (!zeroPosition) {
                        if (equip != null) {
                                if (pos < 0) {
                                        pos *= -1;
                                }
                                mplew.writeShort(pos > 100 ? pos - 100 : pos);
                        } else {
                                mplew.write(pos);
                        }
                }
                mplew.write(itemType);
                mplew.writeInt(item.getItemId());
                mplew.writeBool(isCash);
                if (isCash) {
                        mplew.writeLong(isPet ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
                }
                addExpirationTime(mplew, item.getExpiration());
                if (isPet) {
                        MaplePet pet = item.getPet();
                        mplew.writeAsciiString(StringUtil.getRightPaddedStr(pet.getName(), '\0', 13));
                        mplew.write(pet.getLevel());
                        mplew.writeShort(pet.getCloseness());
                        mplew.write(pet.getFullness());
                        addExpirationTime(mplew, item.getExpiration());
                        mplew.writeInt(pet.getPetFlag());  /* pet flags noticed by lrenex & Spoon */
                        
                        mplew.write(new byte[]{(byte) 0x50, (byte) 0x46}); //wonder what this is
                        mplew.writeInt(0);
                        return;
                }
                if (equip == null) {
                        mplew.writeShort(item.getQuantity());
                        mplew.writeMapleAsciiString(item.getOwner());
                        mplew.writeShort(item.getFlag()); // flag

                        if (ItemConstants.isRechargeable(item.getItemId())) {
                                mplew.writeInt(2);
                                mplew.write(new byte[]{(byte) 0x54, 0, 0, (byte) 0x34});
                        }
                        return;
                }
                mplew.write(equip.getUpgradeSlots()); // upgrade slots
                mplew.write(equip.getLevel()); // level
                mplew.writeShort(equip.getStr()); // str
                mplew.writeShort(equip.getDex()); // dex
                mplew.writeShort(equip.getInt()); // int
                mplew.writeShort(equip.getLuk()); // luk
                mplew.writeShort(equip.getHp()); // hp
                mplew.writeShort(equip.getMp()); // mp
                mplew.writeShort(equip.getWatk()); // watk
                mplew.writeShort(equip.getMatk()); // matk
                mplew.writeShort(equip.getWdef()); // wdef
                mplew.writeShort(equip.getMdef()); // mdef
                mplew.writeShort(equip.getAcc()); // accuracy
                mplew.writeShort(equip.getAvoid()); // avoid
                mplew.writeShort(equip.getHands()); // hands
                mplew.writeShort(equip.getSpeed()); // speed
                mplew.writeShort(equip.getJump()); // jump
                mplew.writeMapleAsciiString(equip.getOwner()); // owner name
                mplew.writeShort(equip.getFlag()); //Item Flags

                if (isCash) {
                        for (int i = 0; i < 10; i++) {
                                mplew.write(0x40);
                        }
                } else {
                        int itemLevel = equip.getItemLevel();
                        
                        long expNibble = (ExpTable.getExpNeededForLevel(ii.getEquipLevelReq(item.getItemId())) * equip.getItemExp());
                        expNibble /= ExpTable.getEquipExpNeededForLevel(itemLevel);
                        
                        mplew.write(0);
                        mplew.write(itemLevel); //Item Level
                        mplew.writeInt((int) expNibble);
                        mplew.writeInt(equip.getVicious()); //WTF NEXON ARE YOU SERIOUS?
                        mplew.writeLong(0);
                }
                mplew.writeLong(getTime(-2));
                mplew.writeInt(-1);

        }

        private static void addInventoryInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                for (byte i = 1; i <= 5; i++) {
                        mplew.write(chr.getInventory(MapleInventoryType.getByType(i)).getSlotLimit());
                }
                mplew.writeLong(getTime(-2));
                MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
                Collection<Item> equippedC = iv.list();
                List<Item> equipped = new ArrayList<>(equippedC.size());
                List<Item> equippedCash = new ArrayList<>(equippedC.size());
                for (Item item : equippedC) {
                        if (item.getPosition() <= -100) {
                                equippedCash.add((Item) item);
                        } else {
                                equipped.add((Item) item);
                        }
                }
                for (Item item : equipped) {    // equipped doesn't actually need sorting, thanks Pllsz
                        addItemInfo(mplew, item);
                }
                mplew.writeShort(0); // start of equip cash
                for (Item item : equippedCash) {
                        addItemInfo(mplew, item);
                }
                mplew.writeShort(0); // start of equip inventory
                for (Item item : chr.getInventory(MapleInventoryType.EQUIP).list()) {
                        addItemInfo(mplew, item);
                }
                mplew.writeInt(0);
                for (Item item : chr.getInventory(MapleInventoryType.USE).list()) {
                        addItemInfo(mplew, item);
                }
                mplew.write(0);
                for (Item item : chr.getInventory(MapleInventoryType.SETUP).list()) {
                        addItemInfo(mplew, item);
                }
                mplew.write(0);
                for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
                        addItemInfo(mplew, item);
                }
                mplew.write(0);
                for (Item item : chr.getInventory(MapleInventoryType.CASH).list()) {
                        addItemInfo(mplew, item);
                }
        }

        private static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.write(0); // start of skills
                Map<Skill, MapleCharacter.SkillEntry> skills = chr.getSkills();
                int skillsSize = skills.size();
                // We don't want to include any hidden skill in this, so subtract them from the size list and ignore them.
                for (Iterator<Entry<Skill, SkillEntry>> it = skills.entrySet().iterator(); it.hasNext();) {
                        Entry<Skill, MapleCharacter.SkillEntry> skill = it.next();
                        if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                                skillsSize--;
                        }
                }
                mplew.writeShort(skillsSize);
                for (Iterator<Entry<Skill, SkillEntry>> it = skills.entrySet().iterator(); it.hasNext();) {
                        Entry<Skill, MapleCharacter.SkillEntry> skill = it.next();
                        if (GameConstants.isHiddenSkills(skill.getKey().getId())) {
                                continue;
                        }
                        mplew.writeInt(skill.getKey().getId());
                        mplew.writeInt(skill.getValue().skillevel);
                        addExpirationTime(mplew, skill.getValue().expiration);
                        if (skill.getKey().isFourthJob()) {
                                mplew.writeInt(skill.getValue().masterlevel);
                        }
                }
                mplew.writeShort(chr.getAllCooldowns().size());
                for (PlayerCoolDownValueHolder cooling : chr.getAllCooldowns()) {
                        mplew.writeInt(cooling.skillId);
                        int timeLeft = (int) (cooling.length + cooling.startTime - System.currentTimeMillis());
                        mplew.writeShort(timeLeft / 1000);
                }
        }

        private static void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeInt(chr.getMonsterBookCover()); // cover
                mplew.write(0);
                Map<Integer, Integer> cards = chr.getMonsterBook().getCards();
                mplew.writeShort(cards.size());
                for (Entry<Integer, Integer> all : cards.entrySet()) {
                        mplew.writeShort(all.getKey() % 10000); // Id
                        mplew.write(all.getValue()); // Level
                }
        }

        public static byte[] sendGuestTOS() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUEST_ID_LOGIN.getValue());
                mplew.writeShort(0x100);
                mplew.writeInt(Randomizer.nextInt(999999));
                mplew.writeLong(0);
                mplew.writeLong(getTime(-2));
                mplew.writeLong(getTime(System.currentTimeMillis()));
                mplew.writeInt(0);
                mplew.writeMapleAsciiString("http://maplesolaxia.com");
                return mplew.getPacket();
        }

        /**
         * Sends a hello packet.
         *
         * @param mapleVersion The maple client version.
         * @param sendIv the IV in use by the server for sending
         * @param recvIv the IV in use by the server for receiving
         * @return
         */
        public static byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
                mplew.writeShort(0x0E);
                mplew.writeShort(mapleVersion);
                mplew.writeShort(1);
                mplew.write(49);
                mplew.write(recvIv);
                mplew.write(sendIv);
                mplew.write(8);
                return mplew.getPacket();
        }

        /**
         * Sends a ping packet.
         *
         * @return The packet.
         */
        public static byte[] getPing() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
                mplew.writeShort(SendOpcode.PING.getValue());
                return mplew.getPacket();
        }

        /**
         * Gets a login failed packet.
         *
         * Possible values for <code>reason</code>:<br> 3: ID deleted or blocked<br>
         * 4: Incorrect password<br> 5: Not a registered id<br> 6: System error<br>
         * 7: Already logged in<br> 8: System error<br> 9: System error<br> 10:
         * Cannot process so many connections<br> 11: Only users older than 20 can
         * use this channel<br> 13: Unable to log on as master at this ip<br> 14:
         * Wrong gateway or personal info and weird korean button<br> 15: Processing
         * request with that korean button!<br> 16: Please verify your account
         * through email...<br> 17: Wrong gateway or personal info<br> 21: Please
         * verify your account through email...<br> 23: License agreement<br> 25:
         * Maple Europe notice =[ FUCK YOU NEXON<br> 27: Some weird full client
         * notice, probably for trial versions<br>
         *
         * @param reason The reason logging in failed.
         * @return The login failed packet.
         */
        public static byte[] getLoginFailed(int reason) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
                mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
                mplew.write(reason);
                mplew.write(0);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        /**
         * Gets a login failed packet.
         *
         * Possible values for <code>reason</code>:<br> 2: ID deleted or blocked<br>
         * 3: ID deleted or blocked<br> 4: Incorrect password<br> 5: Not a
         * registered id<br> 6: Trouble logging into the game?<br> 7: Already logged
         * in<br> 8: Trouble logging into the game?<br> 9: Trouble logging into the
         * game?<br> 10: Cannot process so many connections<br> 11: Only users older
         * than 20 can use this channel<br> 12: Trouble logging into the game?<br>
         * 13: Unable to log on as master at this ip<br> 14: Wrong gateway or
         * personal info and weird korean button<br> 15: Processing request with
         * that korean button!<br> 16: Please verify your account through
         * email...<br> 17: Wrong gateway or personal info<br> 21: Please verify
         * your account through email...<br> 23: Crashes<br> 25: Maple Europe notice
         * =[ FUCK YOU NEXON<br> 27: Some weird full client notice, probably for
         * trial versions<br>
         *
         * @param reason The reason logging in failed.
         * @return The login failed packet.
         */
        public static byte[] getAfterLoginError(int reason) {//same as above o.o
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
                mplew.writeShort(SendOpcode.SELECT_CHARACTER_BY_VAC.getValue());
                mplew.writeShort(reason);//using other types than stated above = CRASH
                return mplew.getPacket();
        }

        public static byte[] sendPolice() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAKE_GM_NOTICE.getValue());
                mplew.write(0);//doesn't even matter what value
                return mplew.getPacket();
        }

        public static byte[] sendPolice(String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DATA_CRC_CHECK_FAILED.getValue());
                mplew.writeMapleAsciiString(text);
                return mplew.getPacket();
        }

        public static byte[] getPermBan(byte reason) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
                mplew.write(2); // Account is banned
                mplew.write(0);
                mplew.writeInt(0);
                mplew.write(0);
                mplew.writeLong(getTime(-1));

                return mplew.getPacket();
        }

        public static byte[] getTempBan(long timestampTill, byte reason) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);
                mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
                mplew.write(2);
                mplew.write(0);
                mplew.writeInt(0);
                mplew.write(reason);
                mplew.writeLong(getTime(timestampTill)); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.

                return mplew.getPacket();
        }

        /**
         * Gets a successful authentication packet.
         *
         * @param c
         * @return the successful authentication packet
         */
        public static byte[] getAuthSuccess(MapleClient c) {
                Server.getInstance().loadAccountCharacters(c);    // locks the login session until data is recovered from the cache or the DB.
                Server.getInstance().loadAccountStorages(c);
                
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.LOGIN_STATUS.getValue());
                mplew.writeInt(0);
                mplew.writeShort(0);
                mplew.writeInt(c.getAccID());
                mplew.write(c.getGender());
                
                boolean canFly = Server.getInstance().canFly(c.getAccID());
                mplew.writeBool((YamlConfig.config.server.USE_ENFORCE_ADMIN_ACCOUNT || canFly) ? c.getGMLevel() > 1 : false);    // thanks Steve(kaito1410) for pointing the GM account boolean here
                mplew.write(((YamlConfig.config.server.USE_ENFORCE_ADMIN_ACCOUNT || canFly) && c.getGMLevel() > 1) ? 0x80 : 0);  // Admin Byte. 0x80,0x40,0x20.. Rubbish.
                mplew.write(0); // Country Code.
                
                mplew.writeMapleAsciiString(c.getAccountName());
                mplew.write(0);
                
                mplew.write(0); // IsQuietBan
                mplew.writeLong(0);//IsQuietBanTimeStamp
                mplew.writeLong(0); //CreationTimeStamp

                mplew.writeInt(1); // 1: Remove the "Select the world you want to play in"
                
                mplew.write(YamlConfig.config.server.ENABLE_PIN && !c.canBypassPin() ? 0 : 1); // 0 = Pin-System Enabled, 1 = Disabled
                mplew.write(YamlConfig.config.server.ENABLE_PIC && !c.canBypassPic() ? (c.getPic() == null || c.getPic().equals("") ? 0 : 1) : 2); // 0 = Register PIC, 1 = Ask for PIC, 2 = Disabled
                
                return mplew.getPacket();
        }

        /**
         * Gets a packet detailing a PIN operation.
         *
         * Possible values for <code>mode</code>:<br> 0 - PIN was accepted<br> 1 -
         * Register a new PIN<br> 2 - Invalid pin / Reenter<br> 3 - Connection
         * failed due to system error<br> 4 - Enter the pin
         *
         * @param mode The mode.
         * @return
         */
        private static byte[] pinOperation(byte mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.CHECK_PINCODE.getValue());
                mplew.write(mode);
                return mplew.getPacket();
        }

        public static byte[] pinRegistered() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.UPDATE_PINCODE.getValue());
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] requestPin() {
                return pinOperation((byte) 4);
        }

        public static byte[] requestPinAfterFailure() {
                return pinOperation((byte) 2);
        }

        public static byte[] registerPin() {
                return pinOperation((byte) 1);
        }

        public static byte[] pinAccepted() {
                return pinOperation((byte) 0);
        }

        public static byte[] wrongPic() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.CHECK_SPW_RESULT.getValue());
                mplew.write(0);
                return mplew.getPacket();
        }

        /**
         * Gets a packet detailing a server and its channels.
         *
         * @param serverId
         * @param serverName The name of the server.
         * @param flag
         * @param eventmsg
         * @param channelLoad Load of the channel - 1200 seems to be max.
         * @return The server info packet.
         */
        public static byte[] getServerList(int serverId, String serverName, int flag, String eventmsg, List<Channel> channelLoad) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVERLIST.getValue());
                mplew.write(serverId);
                mplew.writeMapleAsciiString(serverName);
                mplew.write(flag);
                mplew.writeMapleAsciiString(eventmsg);
                mplew.write(100); // rate modifier, don't ask O.O!
                mplew.write(0); // event xp * 2.6 O.O!
                mplew.write(100); // rate modifier, don't ask O.O!
                mplew.write(0); // drop rate * 2.6
                mplew.write(0);
                mplew.write(channelLoad.size());
                for (Channel ch : channelLoad) {
                        mplew.writeMapleAsciiString(serverName + "-" + ch.getId());
                        mplew.writeInt(ch.getChannelCapacity());
                        
                        // thanks GabrielSin for this channel packet structure part
                        mplew.write(1);// nWorldID
                        mplew.write(ch.getId() - 1);// nChannelID
                        mplew.writeBool(false);// bAdultChannel
                }
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        /**
         * Gets a packet saying that the server list is over.
         *
         * @return The end of server list packet.
         */
        public static byte[] getEndOfServerList() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.SERVERLIST.getValue());
                mplew.write(0xFF);
                return mplew.getPacket();
        }

        /**
         * Gets a packet detailing a server status message.
         *
         * Possible values for <code>status</code>:<br> 0 - Normal<br> 1 - Highly
         * populated<br> 2 - Full
         *
         * @param status The server status.
         * @return The server status packet.
         */
        public static byte[] getServerStatus(int status) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.SERVERSTATUS.getValue());
                mplew.writeShort(status);
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client the IP of the channel server.
         *
         * @param inetAddr The InetAddress of the requested channel server.
         * @param port The port the channel is on.
         * @param clientId The ID of the client.
         * @return The server IP packet.
         */
        public static byte[] getServerIP(InetAddress inetAddr, int port, int clientId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVER_IP.getValue());
                mplew.writeShort(0);
                byte[] addr = inetAddr.getAddress();
                mplew.write(addr);
                mplew.writeShort(port);
                mplew.writeInt(clientId);
                mplew.write(new byte[]{0, 0, 0, 0, 0});
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client the IP of the new channel.
         *
         * @param inetAddr The InetAddress of the requested channel server.
         * @param port The port the channel is on.
         * @return The server IP packet.
         */
        public static byte[] getChannelChange(InetAddress inetAddr, int port) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHANGE_CHANNEL.getValue());
                mplew.write(1);
                byte[] addr = inetAddr.getAddress();
                mplew.write(addr);
                mplew.writeShort(port);
                return mplew.getPacket();
        }

        /**
         * Gets a packet with a list of characters.
         *
         * @param c The MapleClient to load characters of.
         * @param serverId The ID of the server requested.
         * @param status The charlist request result.
         * @return The character list packet.
         * 
         * Possible values for <code>status</code>:
         * <br> 2: ID deleted or blocked<br>
         * <br> 3: ID deleted or blocked<br>
         * <br> 4: Incorrect password<br>
         * <br> 5: Not an registered ID<br> 
         * <br> 6: Trouble logging in?<br>
         * <br> 10: Server handling too many connections<br>
         * <br> 11: Only 20 years or older<br>
         * <br> 13: Unable to log as master at IP<br>
         * <br> 14: Wrong gateway or personal info<br>
         * <br> 15: Still processing request<br>
         * <br> 16: Verify account via email<br>
         * <br> 17: Wrong gateway or personal info<br>
         * <br> 21: Verify account via email<br>
         */
        public static byte[] getCharList(MapleClient c, int serverId, int status) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHARLIST.getValue());
                mplew.write(status);
                List<MapleCharacter> chars = c.loadCharacters(serverId);
                mplew.write((byte) chars.size());
                for (MapleCharacter chr : chars) {
                        addCharEntry(mplew, chr, false);
                }

                mplew.write(YamlConfig.config.server.ENABLE_PIC && !c.canBypassPic() ? (c.getPic() == null || c.getPic().equals("") ? 0 : 1) : 2);
                mplew.writeInt(YamlConfig.config.server.COLLECTIVE_CHARSLOT ? chars.size() + c.getAvailableCharacterSlots() : c.getCharacterSlots());
                return mplew.getPacket();
        }

        public static byte[] enableTV() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.ENABLE_TV.getValue());
                mplew.writeInt(0);
                mplew.write(0);
                return mplew.getPacket();
        }

        /**
         * Removes TV
         *
         * @return The Remove TV Packet
         */
        public static byte[] removeTV() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
                mplew.writeShort(SendOpcode.REMOVE_TV.getValue());
                return mplew.getPacket();
        }

        /**
         * Sends MapleTV
         *
         * @param chr The character shown in TV
         * @param messages The message sent with the TV
         * @param type The type of TV
         * @param partner The partner shown with chr
         * @return the SEND_TV packet
         */
        public static byte[] sendTV(MapleCharacter chr, List<String> messages, int type, MapleCharacter partner) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SEND_TV.getValue());
                mplew.write(partner != null ? 3 : 1);
                mplew.write(type); //Heart = 2  Star = 1  Normal = 0
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                if (partner != null) {
                        mplew.writeMapleAsciiString(partner.getName());
                } else {
                        mplew.writeShort(0);
                }
                for (int i = 0; i < messages.size(); i++) {
                        if (i == 4 && messages.get(4).length() > 15) {
                                mplew.writeMapleAsciiString(messages.get(4).substring(0, 15));
                        } else {
                                mplew.writeMapleAsciiString(messages.get(i));
                        }
                }
                mplew.writeInt(1337); // time limit shit lol 'Your thing still start in blah blah seconds'
                if (partner != null) {
                        addCharLook(mplew, partner, false);
                }
                return mplew.getPacket();
        }

        /**
         * Gets character info for a character.
         *
         * @param chr The character to get info about.
         * @return The character info packet.
         */
        public static byte[] getCharInfo(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_FIELD.getValue());
                mplew.writeInt(chr.getClient().getChannel() - 1);
                mplew.write(1);
                mplew.write(1);
                mplew.writeShort(0);
                for (int i = 0; i < 3; i++) {
                        mplew.writeInt(Randomizer.nextInt());
                }
                addCharacterInfo(mplew, chr);
                mplew.writeLong(getTime(System.currentTimeMillis()));
                return mplew.getPacket();
        }

        /**
         * Gets an empty stat update.
         *
         * @return The empty stat update packet.
         */
        public static byte[] enableActions() {
                return updatePlayerStats(EMPTY_STATUPDATE, true, null);
        }

        /**
         * Gets an update for specified stats.
         *
         * @param stats The list of stats to update.
         * @param enableActions Allows actions after the update.
         * @param chr The update target.
         * @return The stat update packet.
         */
        public static byte[] updatePlayerStats(List<Pair<MapleStat, Integer>> stats, boolean enableActions, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
                mplew.write(enableActions ? 1 : 0);
                int updateMask = 0;
                for (Pair<MapleStat, Integer> statupdate : stats) {
                        updateMask |= statupdate.getLeft().getValue();
                }
                List<Pair<MapleStat, Integer>> mystats = stats;
                if (mystats.size() > 1) {
                        Collections.sort(mystats, new Comparator<Pair<MapleStat, Integer>>() {
                                @Override
                                public int compare(Pair<MapleStat, Integer> o1, Pair<MapleStat, Integer> o2) {
                                        int val1 = o1.getLeft().getValue();
                                        int val2 = o2.getLeft().getValue();
                                        return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
                                }
                        });
                }
                mplew.writeInt(updateMask);
                for (Pair<MapleStat, Integer> statupdate : mystats) {
                        if (statupdate.getLeft().getValue() >= 1) {
                                if (statupdate.getLeft().getValue() == 0x1) {
                                        mplew.write(statupdate.getRight().byteValue());
                                } else if (statupdate.getLeft().getValue() <= 0x4) {
                                        mplew.writeInt(statupdate.getRight());
                                } else if (statupdate.getLeft().getValue() < 0x20) {
                                        mplew.write(statupdate.getRight().shortValue());
                                } else if (statupdate.getLeft().getValue() == 0x8000) {
                                        if (GameConstants.hasSPTable(chr.getJob())) {
                                                addRemainingSkillInfo(mplew, chr);
                                        } else {
                                                mplew.writeShort(statupdate.getRight().shortValue());
                                        }
                                } else if (statupdate.getLeft().getValue() < 0xFFFF) {
                                        mplew.writeShort(statupdate.getRight().shortValue());
                                } else if (statupdate.getLeft().getValue() == 0x20000) {
                                        mplew.writeShort(statupdate.getRight().shortValue());
                                } else {
                                        mplew.writeInt(statupdate.getRight().intValue());
                                }
                        }
                }
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client to change maps.
         *
         * @param to The <code>MapleMap</code> to warp to.
         * @param spawnPoint The spawn portal number to spawn at.
         * @param chr The character warping to <code>to</code>
         * @return The map change packet.
         */
        public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_FIELD.getValue());
                mplew.writeInt(chr.getClient().getChannel() - 1);
                mplew.writeInt(0);//updated
                mplew.write(0);//updated
                mplew.writeInt(to.getId());
                mplew.write(spawnPoint);
                mplew.writeShort(chr.getHp());
                mplew.writeBool(false);
                mplew.writeLong(getTime(Server.getInstance().getCurrentTime()));
                return mplew.getPacket();
        }
        
        public static byte[] getWarpToMap(MapleMap to, int spawnPoint, Point spawnPosition, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_FIELD.getValue());
                mplew.writeInt(chr.getClient().getChannel() - 1);
                mplew.writeInt(0);//updated
                mplew.write(0);//updated
                mplew.writeInt(to.getId());
                mplew.write(spawnPoint);
                mplew.writeShort(chr.getHp());
                mplew.writeBool(true);
                mplew.writeInt(spawnPosition.x);    // spawn position placement thanks to Arnah (Vertisy)
                mplew.writeInt(spawnPosition.y);
                mplew.writeLong(getTime(Server.getInstance().getCurrentTime()));
                return mplew.getPacket();
        }
        
        /**
         * Gets a packet to spawn a portal.
         *
         * @param townId The ID of the town the portal goes to.
         * @param targetId The ID of the target.
         * @param pos Where to put the portal.
         * @return The portal spawn packet.
         */
        public static byte[] spawnPortal(int townId, int targetId, Point pos) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);
                mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
                mplew.writeInt(townId);
                mplew.writeInt(targetId);
                mplew.writePos(pos);
                return mplew.getPacket();
        }

        /**
         * Gets a packet to spawn a door.
         *
         * @param ownerid The door's owner ID.
         * @param pos The position of the door.
         * @param launched Already deployed the door. 
         * @return The remove door packet.
         */
        public static byte[] spawnDoor(int ownerid, Point pos, boolean launched) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
                mplew.writeShort(SendOpcode.SPAWN_DOOR.getValue());
                mplew.writeBool(launched);
                mplew.writeInt(ownerid);
                mplew.writePos(pos);
                return mplew.getPacket();
        }
        
        /**
         * Gets a packet to remove a door.
         *
         * @param ownerid The door's owner ID.
         * @param town
         * @return The remove door packet.
         */
        public static byte[] removeDoor(int ownerid, boolean town) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
                if (town) {
                        mplew.writeShort(SendOpcode.SPAWN_PORTAL.getValue());
                        mplew.writeInt(999999999);
                        mplew.writeInt(999999999);
                } else {
                        mplew.writeShort(SendOpcode.REMOVE_DOOR.getValue());
                        mplew.write(0);
                        mplew.writeInt(ownerid);
                }
                return mplew.getPacket();
        }

        /**
         * Gets a packet to spawn a special map object.
         *
         * @param summon
         * @param skillLevel The level of the skill used.
         * @param animated Animated spawn?
         * @return The spawn packet for the map object.
         */
        public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
                mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.getValue());
                mplew.writeInt(summon.getOwner().getId());
                mplew.writeInt(summon.getObjectId());
                mplew.writeInt(summon.getSkill());
                mplew.write(0x0A); //v83
                mplew.write(summon.getSkillLevel());
                mplew.writePos(summon.getPosition());
                mplew.write(summon.getStance());    //bMoveAction & foothold, found thanks to Rien dev team
                mplew.writeShort(0);
                mplew.write(summon.getMovementType().getValue()); // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
                mplew.write(summon.isPuppet() ? 0 : 1); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
                mplew.write(animated ? 0 : 1);
                return mplew.getPacket();
        }

        /**
         * Gets a packet to remove a special map object.
         *
         * @param summon
         * @param animated Animated removal?
         * @return The packet removing the object.
         */
        public static byte[] removeSummon(MapleSummon summon, boolean animated) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
                mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.getValue());
                mplew.writeInt(summon.getOwner().getId());
                mplew.writeInt(summon.getObjectId());
                mplew.write(animated ? 4 : 1); // ?
                return mplew.getPacket();
        }
        
        public static byte[] spawnKite(int oid, int itemid, String name, String msg, Point pos, int ft) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_KITE.getValue());
                mplew.writeInt(oid);
                mplew.writeInt(itemid);
                mplew.writeMapleAsciiString(msg);
                mplew.writeMapleAsciiString(name);
                mplew.writeShort(pos.x);
                mplew.writeShort(ft);
                return mplew.getPacket();
        }

	public static byte[] removeKite(int objectid, int animationType) {    // thanks to Arnah (Vertisy)
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.REMOVE_KITE.getValue());
		mplew.write(animationType); // 0 is 10/10, 1 just vanishes
		mplew.writeInt(objectid);
		return mplew.getPacket();
	}
        
        public static byte[] sendCannotSpawnKite() {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANNOT_SPAWN_KITE.getValue());
                return mplew.getPacket();
        }

        /**
         * Gets the response to a relog request.
         *
         * @return The relog response packet.
         */
        public static byte[] getRelogResponse() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.RELOG_RESPONSE.getValue());
                mplew.write(1);//1 O.O Must be more types ):
                return mplew.getPacket();
        }

        /**
         * Gets a server message packet.
         *
         * @param message The message to convey.
         * @return The server message packet.
         */
        public static byte[] serverMessage(String message) {
                return serverMessage(4, (byte) 0, message, true, false, 0);
        }

        /**
         * Gets a server notice packet.
         *
         * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
         * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
         * 5: Pink Text<br> 6: Lightblue Text
         *
         * @param type The type of the notice.
         * @param message The message to convey.
         * @return The server notice packet.
         */
        public static byte[] serverNotice(int type, String message) {
                return serverMessage(type, (byte) 0, message, false, false, 0);
        }

        /**
         * Gets a server notice packet.
         *
         * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
         * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
         * 5: Pink Text<br> 6: Lightblue Text
         *
         * @param type The type of the notice.
         * @param channel The channel this notice was sent on.
         * @param message The message to convey.
         * @return The server notice packet.
         */
        public static byte[] serverNotice(int type, String message, int npc) {
                return serverMessage(type, 0, message, false, false, npc);
        }

        public static byte[] serverNotice(int type, int channel, String message) {
                return serverMessage(type, channel, message, false, false, 0);
        }

        public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
                return serverMessage(type, channel, message, false, smegaEar, 0);
        }

        /**
         * Gets a server message packet.
         *
         * Possible values for <code>type</code>:<br> 0: [Notice]<br> 1: Popup<br>
         * 2: Megaphone<br> 3: Super Megaphone<br> 4: Scrolling message at top<br>
         * 5: Pink Text<br> 6: Lightblue Text<br> 7: BroadCasting NPC
         *
         * @param type The type of the notice.
         * @param channel The channel this notice was sent on.
         * @param message The message to convey.
         * @param servermessage Is this a scrolling ticker?
         * @return The server notice packet.
         */
        private static byte[] serverMessage(int type, int channel, String message, boolean servermessage, boolean megaEar, int npc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
                mplew.write(type);
                if (servermessage) {
                        mplew.write(1);
                }
                mplew.writeMapleAsciiString(message);
                if (type == 3) {
                        mplew.write(channel - 1); // channel
                        mplew.writeBool(megaEar);
                } else if (type == 6) {
                        mplew.writeInt(0);
                } else if (type == 7) { // npc 
                        mplew.writeInt(npc);
                }
                return mplew.getPacket();
        }

        /**
         * Sends a Avatar Super Megaphone packet.
         *
         * @param chr The character name.
         * @param medal The medal text.
         * @param channel Which channel.
         * @param itemId Which item used.
         * @param message The message sent.
         * @param ear Whether or not the ear is shown for whisper.
         * @return
         */
        public static byte[] getAvatarMega(MapleCharacter chr, String medal, int channel, int itemId, List<String> message, boolean ear) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_AVATAR_MEGAPHONE.getValue());
                mplew.writeInt(itemId);
                mplew.writeMapleAsciiString(medal + chr.getName());
                for (String s : message) {
                        mplew.writeMapleAsciiString(s);
                }
                mplew.writeInt(channel - 1); // channel
                mplew.writeBool(ear);
                addCharLook(mplew, chr, true);
                return mplew.getPacket();
        }

        /*
         * Sends a packet to remove the tiger megaphone
         * @return
         */
        public static byte[] byeAvatarMega() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CLEAR_AVATAR_MEGAPHONE.getValue());
                mplew.write(1);
                return mplew.getPacket();
        }

        /**
         * Sends the Gachapon green message when a user uses a gachapon ticket.
         *
         * @param item
         * @param town
         * @param player
         * @return
         */
        public static byte[] gachaponMessage(Item item, String town, MapleCharacter player) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
                mplew.write(0x0B);
                mplew.writeMapleAsciiString(player.getName() + " : got a(n)");
                mplew.writeInt(0); //random?
                mplew.writeMapleAsciiString(town);
                addItemInfo(mplew, item, true);
                return mplew.getPacket();
        }

        public static byte[] spawnNPC(MapleNPC life) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(24);
                mplew.writeShort(SendOpcode.SPAWN_NPC.getValue());
                mplew.writeInt(life.getObjectId());
                mplew.writeInt(life.getId());
                mplew.writeShort(life.getPosition().x);
                mplew.writeShort(life.getCy());
                if (life.getF() == 1) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                }
                mplew.writeShort(life.getFh());
                mplew.writeShort(life.getRx0());
                mplew.writeShort(life.getRx1());
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(23);
                mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
                mplew.write(1);
                mplew.writeInt(life.getObjectId());
                mplew.writeInt(life.getId());
                mplew.writeShort(life.getPosition().x);
                mplew.writeShort(life.getCy());
                if (life.getF() == 1) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                }
                mplew.writeShort(life.getFh());
                mplew.writeShort(life.getRx0());
                mplew.writeShort(life.getRx1());
                mplew.writeBool(MiniMap);
                return mplew.getPacket();
        }
        
        /**
         * Gets a spawn monster packet.
         *
         * @param life The monster to spawn.
         * @param newSpawn Is it a new spawn?
         * @return The spawn monster packet.
         */
        public static byte[] spawnMonster(MapleMonster life, boolean newSpawn) {
                return spawnMonsterInternal(life, false, newSpawn, false, 0, false);
        }

        /**
         * Gets a spawn monster packet.
         *
         * @param life The monster to spawn.
         * @param newSpawn Is it a new spawn?
         * @param effect The spawn effect.
         * @return The spawn monster packet.
         */
        public static byte[] spawnMonster(MapleMonster life, boolean newSpawn, int effect) {
                return spawnMonsterInternal(life, false, newSpawn, false, effect, false);
        }

        /**
         * Gets a control monster packet.
         *
         * @param life The monster to give control to.
         * @param newSpawn Is it a new spawn?
         * @param aggro Aggressive monster?
         * @return The monster control packet.
         */
        public static byte[] controlMonster(MapleMonster life, boolean newSpawn, boolean aggro) {
                return spawnMonsterInternal(life, true, newSpawn, aggro, 0, false);
        }

        /**
         * Removes a monster invisibility.
         *
         * @param life
         * @return
         */
        public static byte[] removeMonsterInvisibility(MapleMonster life) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
                mplew.write(1);
                mplew.writeInt(life.getObjectId());
                return mplew.getPacket();
        }

        /**
         * Makes a monster invisible for Ariant PQ.
         *
         * @param life
         * @return
         */
        public static byte[] makeMonsterInvisible(MapleMonster life) {
                return spawnMonsterInternal(life, true, false, false, 0, true);
        }

        private static void encodeParentlessMobSpawnEffect(MaplePacketLittleEndianWriter mplew, boolean newSpawn, int effect) {
                if (effect > 0) {
                        mplew.write(effect);
                        mplew.write(0);
                        mplew.writeShort(0);
                        if (effect == 15) {
                                mplew.write(0);
                        }
                }
                mplew.write(newSpawn ? -2 : -1);
        }
        
        private static void encodeTemporary(MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, MonsterStatusEffect> stati) {
                int pCounter = -1, mCounter = -1;
            
                writeLongEncodeTemporaryMask(mplew, stati.keySet());    // packet structure mapped thanks to Eric
                
                for (Entry<MonsterStatus, MonsterStatusEffect> s : stati.entrySet()) {
                        MonsterStatusEffect mse = s.getValue();
                        mplew.writeShort(mse.getStati().get(s.getKey()));
                        
                        MobSkill mobSkill = mse.getMobSkill();
                        if (mobSkill != null) {
                                mplew.writeShort(mobSkill.getSkillId());
                                mplew.writeShort(mobSkill.getSkillLevel());

                                switch(s.getKey()) {
                                    case WEAPON_REFLECT:
                                            pCounter = mobSkill.getX();
                                            break;
                                            
                                    case MAGIC_REFLECT:
                                            mCounter = mobSkill.getY();
                                            break;
                                }
                        } else {
                            Skill skill = mse.getSkill();
                            mplew.writeInt(skill != null ? skill.getId() : 0);
                        }
                        
                        mplew.writeShort(-1);    // duration
                }
                
                // reflect packet structure found thanks to Arnah (Vertisy)
		if(pCounter != -1) mplew.writeInt(pCounter);// wPCounter_
		if(mCounter != -1) mplew.writeInt(mCounter);// wMCounter_
		if(pCounter != -1 || mCounter != -1) mplew.writeInt(100);// nCounterProb_
        }
        
        /**
         * Internal function to handler monster spawning and controlling.
         *
         * @param life The mob to perform operations with.
         * @param requestController Requesting control of mob?
         * @param newSpawn New spawn (fade in?)
         * @param aggro Aggressive mob?
         * @param effect The spawn effect to use.
         * @return The spawn/control packet.
         */
        private static byte[] spawnMonsterInternal(MapleMonster life, boolean requestController, boolean newSpawn, boolean aggro, int effect, boolean makeInvis) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                if (makeInvis) {
                        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
                        mplew.write(0);
                        mplew.writeInt(life.getObjectId());
                        return mplew.getPacket();
                }
                if (requestController) {
                        mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
                        mplew.write(aggro ? 2 : 1);
                } else {
                        mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
                }
                mplew.writeInt(life.getObjectId());
                mplew.write(life.getController() == null ? 5 : 1);
                mplew.writeInt(life.getId());
                
                if (requestController) {
                    encodeTemporary(mplew, life.getStati());    // thanks shot for noticing encode temporary buffs missing
                } else {
                    mplew.skip(16);
                }
                
                mplew.writePos(life.getPosition());
                mplew.write(life.getStance());
                mplew.writeShort(0); //Origin FH //life.getStartFh()
                mplew.writeShort(life.getFh());
                
                
                /**
                 * -4: Fake -3: Appear after linked mob is dead -2: Fade in 1: Smoke 3:
                 * King Slime spawn 4: Summoning rock thing, used for 3rd job? 6:
                 * Magical shit 7: Smoke shit 8: 'The Boss' 9/10: Grim phantom shit?
                 * 11/12: Nothing? 13: Frankenstein 14: Angry ^ 15: Orb animation thing,
                 * ?? 16: ?? 19: Mushroom castle boss thing
                 */
                
                if (life.getParentMobOid() != 0) {
                        MapleMonster parentMob = life.getMap().getMonsterByOid(life.getParentMobOid());
                        if(parentMob != null && parentMob.isAlive()) {
                                mplew.write(effect != 0 ? effect : -3);
                                mplew.writeInt(life.getParentMobOid());
                        } else {
                                encodeParentlessMobSpawnEffect(mplew, newSpawn, effect);
                        }
                } else {
                	encodeParentlessMobSpawnEffect(mplew, newSpawn, effect);
                }
                
                mplew.write(life.getTeam());
                mplew.writeInt(0); // getItemEffect
                return mplew.getPacket();
        }

        /**
         * Handles monsters not being targettable, such as Zakum's first body.
         *
         * @param life The mob to spawn as non-targettable.
         * @param effect The effect to show when spawning.
         * @return The packet to spawn the mob as non-targettable.
         */
        public static byte[] spawnFakeMonster(MapleMonster life, int effect) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
                mplew.write(1);
                mplew.writeInt(life.getObjectId());
                mplew.write(5);
                mplew.writeInt(life.getId());
                encodeTemporary(mplew, life.getStati());
                mplew.writePos(life.getPosition());
                mplew.write(life.getStance());
                mplew.writeShort(0);//life.getStartFh()
                mplew.writeShort(life.getFh());
                if (effect > 0) {
                        mplew.write(effect);
                        mplew.write(0);
                        mplew.writeShort(0);
                }
                mplew.writeShort(-2);
                mplew.write(life.getTeam());
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        /**
         * Makes a monster previously spawned as non-targettable, targettable.
         *
         * @param life The mob to make targettable.
         * @return The packet to make the mob targettable.
         */
        public static byte[] makeMonsterReal(MapleMonster life) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_MONSTER.getValue());
                mplew.writeInt(life.getObjectId());
                mplew.write(5);
                mplew.writeInt(life.getId());
                encodeTemporary(mplew, life.getStati());
                mplew.writePos(life.getPosition());
                mplew.write(life.getStance());
                mplew.writeShort(0);//life.getStartFh()
                mplew.writeShort(life.getFh());
                mplew.writeShort(-1);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        /**
         * Gets a stop control monster packet.
         *
         * @param oid The ObjectID of the monster to stop controlling.
         * @return The stop control monster packet.
         */
        public static byte[] stopControllingMonster(int oid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.SPAWN_MONSTER_CONTROL.getValue());
                mplew.write(0);
                mplew.writeInt(oid);
                return mplew.getPacket();
        }

        /**
         * Gets a response to a move monster packet.
         *
         * @param objectid The ObjectID of the monster being moved.
         * @param moveid The movement ID.
         * @param currentMp The current MP of the monster.
         * @param useSkills Can the monster use skills?
         * @return The move response packet.
         */
        public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills) {
                return moveMonsterResponse(objectid, moveid, currentMp, useSkills, 0, 0);
        }

        /**
         * Gets a response to a move monster packet.
         *
         * @param objectid The ObjectID of the monster being moved.
         * @param moveid The movement ID.
         * @param currentMp The current MP of the monster.
         * @param useSkills Can the monster use skills?
         * @param skillId The skill ID for the monster to use.
         * @param skillLevel The level of the skill to use.
         * @return The move response packet.
         */
        
        public static byte[] moveMonsterResponse(int objectid, short moveid, int currentMp, boolean useSkills, int skillId, int skillLevel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(13);
                mplew.writeShort(SendOpcode.MOVE_MONSTER_RESPONSE.getValue());
                mplew.writeInt(objectid);
                mplew.writeShort(moveid);
                mplew.writeBool(useSkills);
                mplew.writeShort(currentMp);
                mplew.write(skillId);
                mplew.write(skillLevel);
                return mplew.getPacket();
        }
        
        /**
         * Gets a general chat packet.
         *
         * @param cidfrom The character ID who sent the chat.
         * @param text The text of the chat.
         * @param whiteBG
         * @param show
         * @return The general chat packet.
         */
        public static byte[] getChatText(int cidfrom, String text, boolean gm, int show) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHATTEXT.getValue());
                mplew.writeInt(cidfrom);
                mplew.writeBool(gm);
                mplew.writeMapleAsciiString(text);
                mplew.write(show);
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client to show an EXP increase.
         *
         * @param gain The amount of EXP gained.
         * @param inChat In the chat box?
         * @param white White text or yellow?
         * @return The exp gained packet.
         */
        public static byte[] getShowExpGain(int gain, int equip, int party, boolean inChat, boolean white) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(3); // 3 = exp, 4 = fame, 5 = mesos, 6 = guildpoints
                mplew.writeBool(white);
                mplew.writeInt(gain);
                mplew.writeBool(inChat);
                mplew.writeInt(0); // bonus event exp
                mplew.write(0); // third monster kill event
                mplew.write(0); // RIP byte, this is always a 0
                mplew.writeInt(0); //wedding bonus
                if (inChat) { // quest bonus rate stuff
                        mplew.write(0);
                }

                mplew.write(0); //0 = party bonus, 100 = 1x Bonus EXP, 200 = 2x Bonus EXP
                mplew.writeInt(party); // party bonus 
                mplew.writeInt(equip); //equip bonus
                mplew.writeInt(0); //Internet Cafe Bonus
                mplew.writeInt(0); //Rainbow Week Bonus
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client to show a fame gain.
         *
         * @param gain How many fame gained.
         * @return The meso gain packet.
         */
        public static byte[] getShowFameGain(int gain) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(4);
                mplew.writeInt(gain);
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client to show a meso gain.
         *
         * @param gain How many mesos gained.
         * @return The meso gain packet.
         */
        public static byte[] getShowMesoGain(int gain) {
                return getShowMesoGain(gain, false);
        }

        /**
         * Gets a packet telling the client to show a meso gain.
         *
         * @param gain How many mesos gained.
         * @param inChat Show in the chat window?
         * @return The meso gain packet.
         */
        public static byte[] getShowMesoGain(int gain, boolean inChat) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                if (!inChat) {
                        mplew.write(0);
                        mplew.writeShort(1); //v83
                } else {
                        mplew.write(5);
                }
                mplew.writeInt(gain);
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        /**
         * Gets a packet telling the client to show a item gain.
         *
         * @param itemId The ID of the item gained.
         * @param quantity How many items gained.
         * @return The item gain packet.
         */
        public static byte[] getShowItemGain(int itemId, short quantity) {
                return getShowItemGain(itemId, quantity, false);
        }

        /**
         * Gets a packet telling the client to show an item gain.
         *
         * @param itemId The ID of the item gained.
         * @param quantity The number of items gained.
         * @param inChat Show in the chat window?
         * @return The item gain packet.
         */
        public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                if (inChat) {
                        mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                        mplew.write(3);
                        mplew.write(1);
                        mplew.writeInt(itemId);
                        mplew.writeInt(quantity);
                } else {
                        mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                        mplew.writeShort(0);
                        mplew.writeInt(itemId);
                        mplew.writeInt(quantity);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                }
                return mplew.getPacket();
        }

        public static byte[] killMonster(int oid, boolean animation) {
                return killMonster(oid, animation ? 1 : 0);
        }

        /**
         * Gets a packet telling the client that a monster was killed.
         *
         * @param oid The objectID of the killed monster.
         * @param animation 0 = dissapear, 1 = fade out, 2+ = special
         * @return The kill monster packet.
         */
        public static byte[] killMonster(int oid, int animation) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.KILL_MONSTER.getValue());
                mplew.writeInt(oid);
                mplew.write(animation);
                mplew.write(animation);
                return mplew.getPacket();
        }
        
        public static byte[] updateMapItemObject(MapleMapItem drop, boolean giveOwnership) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
                mplew.write(2);
                mplew.writeInt(drop.getObjectId());
                mplew.writeBool(drop.getMeso() > 0);
                mplew.writeInt(drop.getItemId());
                mplew.writeInt(giveOwnership ? 0 : -1);
                mplew.write(drop.hasExpiredOwnershipTime() ? 2 : drop.getDropType());
                mplew.writePos(drop.getPosition());
                mplew.writeInt(giveOwnership ? 0 : -1);

                if (drop.getMeso() == 0) {
                        addExpirationTime(mplew, drop.getItem().getExpiration());
                }
                mplew.write(drop.isPlayerDrop() ? 0 : 1);
                return mplew.getPacket();
        }

        public static byte[] dropItemFromMapObject(MapleCharacter player, MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
                int dropType = drop.getDropType();
                if (drop.hasClientsideOwnership(player) && dropType < 3) {
                    dropType = 2;
                }
            
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
                mplew.write(mod);
                mplew.writeInt(drop.getObjectId());
                mplew.writeBool(drop.getMeso() > 0); // 1 mesos, 0 item, 2 and above all item meso bag,
                mplew.writeInt(drop.getItemId()); // drop object ID
                mplew.writeInt(drop.getClientsideOwnerId()); // owner charid/partyid :)
                mplew.write(dropType); // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
                mplew.writePos(dropto);
                mplew.writeInt(drop.getDropper().getObjectId()); // dropper oid, found thanks to Li Jixue

                if (mod != 2) {
                        mplew.writePos(dropfrom);
                        mplew.writeShort(0);//Fh?
                }
                if (drop.getMeso() == 0) {
                        addExpirationTime(mplew, drop.getItem().getExpiration());
                }
                mplew.write(drop.isPlayerDrop() ? 0 : 1); //pet EQP pickup
                return mplew.getPacket();
        }
        
        /**
         * Guild Name & Mark update packet, thanks to Arnah (Vertisy)
         * 
	 * @param guildName The Guild name, blank for nothing.
	 */
	public static byte[] guildNameChanged(int chrid, String guildName){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_NAME_CHANGED.getValue());
		mplew.writeInt(chrid);
		mplew.writeMapleAsciiString(guildName);
		return mplew.getPacket();
	}
        
	public static byte[] guildMarkChanged(int chrid, MapleGuild guild){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.GUILD_MARK_CHANGED.getValue());
		mplew.writeInt(chrid);
		mplew.writeShort(guild.getLogoBG());
		mplew.write(guild.getLogoBGColor());
		mplew.writeShort(guild.getLogo());
		mplew.write(guild.getLogoColor());
		return mplew.getPacket();
	}
        
        private static void writeForeignBuffs(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeInt(0);
                mplew.writeShort(0); //v83
                mplew.write(0xFC);
                mplew.write(1);
                if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                        mplew.writeInt(2);
                } else {
                        mplew.writeInt(0);
                }
                long buffmask = 0;
                Integer buffvalue = null;
                if ((chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null || chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) && !chr.isHidden()) {
                        buffmask |= MapleBuffStat.DARKSIGHT.getValue();
                }
                if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
                        buffmask |= MapleBuffStat.COMBO.getValue();
                        buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue());
                }
                if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
                        buffmask |= MapleBuffStat.SHADOWPARTNER.getValue();
                }
                if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
                        buffmask |= MapleBuffStat.SOULARROW.getValue();
                }
                if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
                        buffvalue = Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MORPH).intValue());
                }
                mplew.writeInt((int) ((buffmask >> 32) & 0xffffffffL));
                if (buffvalue != null) {
                        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) { //TEST
                                mplew.writeShort(buffvalue);
                        } else {
                                mplew.write(buffvalue.byteValue());
                        }
                }
                mplew.writeInt((int) (buffmask & 0xffffffffL));
                
                // Energy Charge
                mplew.writeInt(chr.getEnergyBar() == 15000 ? 1 : 0);
                mplew.writeShort(0);
                mplew.skip(4);
                
                boolean dashBuff = chr.getBuffedValue(MapleBuffStat.DASH) != null;
                // Dash Speed
                mplew.writeInt(dashBuff ? 1 << 24 : 0);
                mplew.skip(11);
                mplew.writeShort(0);
                // Dash Jump
                mplew.skip(9);
                mplew.writeInt(dashBuff ? 1 << 24 : 0);
                mplew.writeShort(0);
                mplew.write(0);
                
                // Monster Riding
                Integer bv = chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
                if (bv != null) {
                        MapleMount mount = chr.getMount();
                        if (mount != null) {
                                mplew.writeInt(mount.getItemId());
                                mplew.writeInt(mount.getSkillId());
                        } else {
                                mplew.writeLong(0);
                        }
                } else {
                        mplew.writeLong(0);
                }
                
                int CHAR_MAGIC_SPAWN = Randomizer.nextInt();    // skill references found thanks to Rien dev team
                mplew.writeInt(CHAR_MAGIC_SPAWN);
                // Speed Infusion
                mplew.skip(8);
                mplew.writeInt(CHAR_MAGIC_SPAWN);
                mplew.write(0);
                mplew.writeInt(CHAR_MAGIC_SPAWN);
                mplew.writeShort(0);
                // Homing Beacon
                mplew.skip(9);
                mplew.writeInt(CHAR_MAGIC_SPAWN);
                mplew.writeInt(0);
                // Zombify
                mplew.skip(9);
                mplew.writeInt(CHAR_MAGIC_SPAWN);
                mplew.writeShort(0);
                mplew.writeShort(0);
        }
        
        /**
         * Gets a packet spawning a player as a mapobject to other clients.
         *
         * @param target The client receiving this packet.
         * @param chr The character to spawn to other clients.
         * @param enteringField Whether the character to spawn is not yet present in the map or already is.
         * @return The spawn player packet.
         */
        public static byte[] spawnPlayerMapObject(MapleClient target, MapleCharacter chr, boolean enteringField) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_PLAYER.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(chr.getLevel()); //v83
                mplew.writeMapleAsciiString(chr.getName());
                if (chr.getGuildId() < 1) {
                        mplew.writeMapleAsciiString("");
                        mplew.write(new byte[6]);
                } else {
                        MapleGuildSummary gs = chr.getClient().getWorldServer().getGuildSummary(chr.getGuildId(), chr.getWorld());
                        if (gs != null) {
                                mplew.writeMapleAsciiString(gs.getName());
                                mplew.writeShort(gs.getLogoBG());
                                mplew.write(gs.getLogoBGColor());
                                mplew.writeShort(gs.getLogo());
                                mplew.write(gs.getLogoColor());
                        } else {
                                mplew.writeMapleAsciiString("");
                                mplew.write(new byte[6]);
                        }
                }
                
                writeForeignBuffs(mplew, chr);
                
                mplew.writeShort(chr.getJob().getId());
                
                /* replace "mplew.writeShort(chr.getJob().getId())" with this snippet for 3rd person FJ animation on all classes
                if (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.DAWNWARRIOR2) || chr.getJob().isA(MapleJob.NIGHTWALKER2)) {
			mplew.writeShort(chr.getJob().getId());
                } else {
			mplew.writeShort(412);
                }*/
                
                addCharLook(mplew, chr, false);
                mplew.writeInt(chr.getInventory(MapleInventoryType.CASH).countById(5110000));
                mplew.writeInt(chr.getItemEffect());
                mplew.writeInt(ItemConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
                
                if(enteringField) {
                    Point spawnPos = new Point(chr.getPosition());
                    spawnPos.y -= 42;
                    mplew.writePos(spawnPos);
                    mplew.write(6);
                } else {
                    mplew.writePos(chr.getPosition());
                    mplew.write(chr.getStance());
                }
                
                mplew.writeShort(0);//chr.getFh()
                mplew.write(0);
                MaplePet[] pet = chr.getPets();
                for (int i = 0; i < 3; i++) {
                        if (pet[i] != null) {
                                addPetInfo(mplew, pet[i], false);
                        }
                }
                mplew.write(0); //end of pets
                if (chr.getMount() == null) {
                        mplew.writeInt(1); // mob level
                        mplew.writeLong(0); // mob exp + tiredness
                } else {
                        mplew.writeInt(chr.getMount().getLevel());
                        mplew.writeInt(chr.getMount().getExp());
                        mplew.writeInt(chr.getMount().getTiredness());
                }
                
                MaplePlayerShop mps = chr.getPlayerShop();
                if (mps != null && mps.isOwner(chr)) {
                        if (mps.hasFreeSlot()) {
                                addAnnounceBox(mplew, mps, mps.getVisitors().length);
                        } else {
                                addAnnounceBox(mplew, mps, 1);
                        }
                } else {
                        MapleMiniGame miniGame = chr.getMiniGame();
                        if (miniGame != null && miniGame.isOwner(chr)) {
                                if (miniGame.hasFreeSlot()) {
                                        addAnnounceBox(mplew, miniGame, 1, 0);
                                } else {
                                        addAnnounceBox(mplew, miniGame, 2, miniGame.isMatchInProgress() ? 1 : 0);
                                }
                        } else {
                                mplew.write(0);
                        }
                }
                
                if (chr.getChalkboard() != null) {
                        mplew.write(1);
                        mplew.writeMapleAsciiString(chr.getChalkboard());
                } else {
                        mplew.write(0);
                }
                addRingLook(mplew, chr, true);  // crush
                addRingLook(mplew, chr, false); // friendship
                addMarriageRingLook(target, mplew, chr);
                encodeNewYearCardInfo(mplew, chr);  // new year seems to crash sometimes...
                mplew.write(0);
                mplew.write(0);
                mplew.write(chr.getTeam());//only needed in specific fields
                return mplew.getPacket();
        }

        private static void encodeNewYearCardInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                Set<NewYearCardRecord> newyears = chr.getReceivedNewYearRecords();
                if(!newyears.isEmpty()) {
                        mplew.write(1); 

                        mplew.writeInt(newyears.size());
                        for(NewYearCardRecord nyc : newyears) {
                                mplew.writeInt(nyc.getId());
                        }
                } else {
                        mplew.write(0); 
                }
        }
        
        public static byte[] onNewYearCardRes(MapleCharacter user, int cardId, int mode, int msg) {
                NewYearCardRecord newyear = user.getNewYearRecord(cardId);
                return onNewYearCardRes(user, newyear, mode, msg);
        }
        
        public static byte[] onNewYearCardRes(MapleCharacter user, NewYearCardRecord newyear, int mode, int msg) { 
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NEW_YEAR_CARD_RES.getValue()); 
                mplew.write(mode); 
                switch (mode) { 
                        case 4: // Successfully sent a New Year Card\r\n to %s. 
                        case 6: // Successfully received a New Year Card. 
                                encodeNewYearCard(newyear, mplew);
                                break;
                            
                        case 8: // Successfully deleted a New Year Card. 
                                mplew.writeInt(newyear.getId());
                                break;
                            
                        case 5: // Nexon's stupid and makes 4 modes do the same operation.. 
                        case 7: 
                        case 9: 
                        case 0xB: 
                                // 0x10: You have no free slot to store card.\r\ntry later on please. 
                                // 0x11: You have no card to send. 
                                // 0x12: Wrong inventory information ! 
                                // 0x13: Cannot find such character ! 
                                // 0x14: Incoherent Data ! 
                                // 0x15: An error occured during DB operation. 
                                // 0x16: An unknown error occured ! 
                                // 0xF: You cannot send a card to yourself ! 
                                mplew.write(msg);
                                break; 
                            
                        case 0xA:   // GetUnreceivedList_Done
                                int nSN = 1; 
                                mplew.writeInt(nSN); 
                                if ((nSN - 1) <= 98 && nSN > 0) {//lol nexon are you kidding 
                                        for (int i = 0; i < nSN; i++) { 
                                                mplew.writeInt(newyear.getId()); 
                                                mplew.writeInt(newyear.getSenderId()); 
                                                mplew.writeMapleAsciiString(newyear.getSenderName()); 
                                        } 
                                } 
                                break;
                            
                        case 0xC:   // NotiArrived
                                mplew.writeInt(newyear.getId());
                                mplew.writeMapleAsciiString(newyear.getSenderName());
                                break;
                            
                        case 0xD:   // BroadCast_AddCardInfo
                                mplew.writeInt(newyear.getId()); 
                                mplew.writeInt(user.getId()); 
                                break; 
                            
                        case 0xE:   // BroadCast_RemoveCardInfo
                                mplew.writeInt(newyear.getId());
                                break; 
                } 
                return mplew.getPacket(); 
        } 
        
        private static void encodeNewYearCard(NewYearCardRecord newyear, MaplePacketLittleEndianWriter mplew) { 
                mplew.writeInt(newyear.getId());
                mplew.writeInt(newyear.getSenderId()); 
                mplew.writeMapleAsciiString(newyear.getSenderName()); 
                mplew.writeBool(newyear.isSenderCardDiscarded()); 
                mplew.writeLong(newyear.getDateSent()); 
                mplew.writeInt(newyear.getReceiverId()); 
                mplew.writeMapleAsciiString(newyear.getReceiverName()); 
                mplew.writeBool(newyear.isReceiverCardDiscarded()); 
                mplew.writeBool(newyear.isReceiverCardReceived()); 
                mplew.writeLong(newyear.getDateReceived()); 
                mplew.writeMapleAsciiString(newyear.getMessage()); 
        } 
        
        private static void addRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean crush) {
                List<MapleRing> rings;
                if (crush) {
                        rings = chr.getCrushRings();
                } else {
                        rings = chr.getFriendshipRings();
                }
                boolean yes = false;
                for (MapleRing ring : rings) {
                        if (ring.equipped()) {
                                if (yes == false) {
                                        yes = true;
                                        mplew.write(1);
                                }
                                mplew.writeInt(ring.getRingId());
                                mplew.writeInt(0);
                                mplew.writeInt(ring.getPartnerRingId());
                                mplew.writeInt(0);
                                mplew.writeInt(ring.getItemId());
                        }
                }
                if (yes == false) {
                        mplew.write(0);
                }
        }

        private static void addMarriageRingLook(MapleClient target, final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                MapleRing ring = chr.getMarriageRing();
            
                if (ring == null || !ring.equipped()) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                        
                        MapleCharacter targetChr = target.getPlayer();
                        if (targetChr != null && targetChr.getPartnerId() == chr.getId()) {
                            mplew.writeInt(0);
                            mplew.writeInt(0);
                        } else {
                            mplew.writeInt(chr.getId());
                            mplew.writeInt(ring.getPartnerChrId());
                        }
                        
                        mplew.writeInt(ring.getItemId());
                }
        }

        /**
         * Adds a announcement box to an existing MaplePacketLittleEndianWriter.
         *
         * @param mplew The MaplePacketLittleEndianWriter to add an announcement box
         * to.
         * @param shop The shop to announce.
         */
        private static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop, int availability) {
                mplew.write(4);
                mplew.writeInt(shop.getObjectId());
                mplew.writeMapleAsciiString(shop.getDescription());
                mplew.write(0);
                mplew.write(0);
                mplew.write(1);
                mplew.write(availability);
                mplew.write(0);
        }

        private static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MapleMiniGame game, int ammount, int joinable) {
                mplew.write(game.getGameType().getValue());
                mplew.writeInt(game.getObjectId()); // gameid/shopid
                mplew.writeMapleAsciiString(game.getDescription()); // desc
                mplew.writeBool(!game.getPassword().isEmpty());    // password here, thanks GabrielSin
                mplew.write(game.getPieceType());
                mplew.write(ammount);
                mplew.write(2);         //player capacity
                mplew.write(joinable);
        }
        
        private static void updateHiredMerchantBoxInfo(MaplePacketLittleEndianWriter mplew, MapleHiredMerchant hm) {
                byte[] roomInfo = hm.getShopRoomInfo();
                
                mplew.write(5);
                mplew.writeInt(hm.getObjectId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.write(hm.getItemId() % 100);
                mplew.write(roomInfo);    // visitor capacity here, thanks GabrielSin
        }
        
        public static byte[] updateHiredMerchantBox(MapleHiredMerchant hm) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_HIRED_MERCHANT.getValue());
                mplew.writeInt(hm.getOwnerId());

                updateHiredMerchantBoxInfo(mplew, hm);
                return mplew.getPacket();
        }
        
        private static void updatePlayerShopBoxInfo(final MaplePacketLittleEndianWriter mplew, MaplePlayerShop shop) {
                byte[] roomInfo = shop.getShopRoomInfo();    
            
                mplew.write(4);
                mplew.writeInt(shop.getObjectId());
                mplew.writeMapleAsciiString(shop.getDescription());
                mplew.write(0);                 // pw
                mplew.write(shop.getItemId() % 100);
                mplew.write(roomInfo[0]);       // curPlayers
                mplew.write(roomInfo[1]);       // maxPlayers
                mplew.write(0);
        }
        
        public static byte[] updatePlayerShopBox(MaplePlayerShop shop) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
                mplew.writeInt(shop.getOwner().getId());
                
                updatePlayerShopBoxInfo(mplew, shop);
                return mplew.getPacket();
        }
        
        public static byte[] removePlayerShopBox(MaplePlayerShop shop) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
                mplew.writeInt(shop.getOwner().getId());
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static byte[] facialExpression(MapleCharacter from, int expression) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
                mplew.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
                mplew.writeInt(from.getId());
                mplew.writeInt(expression);
                return mplew.getPacket();
        }

        private static void rebroadcastMovementList(LittleEndianWriter lew, SeekableLittleEndianAccessor slea, long movementDataLength) {
        	//movement command length is sent by client, probably not a big issue? (could be calculated on server)
        	//if multiple write/reads are slow, could use (and cache?) a byte[] buffer
        	for(long i = 0; i < movementDataLength; i++) {
        		lew.write(slea.readByte());
        	}
        }
        
        private static void serializeMovementList(LittleEndianWriter lew, List<LifeMovementFragment> moves) {
        	lew.write(moves.size());
        	for(LifeMovementFragment move : moves) {
        		move.serialize(lew);
        	}
        }

        public static byte[] movePlayer(int cid, SeekableLittleEndianAccessor movementSlea, long movementDataLength) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MOVE_PLAYER.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(0);
                rebroadcastMovementList(mplew, movementSlea, movementDataLength);
                return mplew.getPacket();
        }

        public static byte[] moveSummon(int cid, int oid, Point startPos, SeekableLittleEndianAccessor movementSlea, long movementDataLength) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MOVE_SUMMON.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(oid);
                mplew.writePos(startPos);
                rebroadcastMovementList(mplew, movementSlea, movementDataLength);
                return mplew.getPacket();
        }
        
        public static byte[] moveMonster(int oid, boolean skillPossible, int skill, int skillId, int skillLevel, int pOption, Point startPos, SeekableLittleEndianAccessor movementSlea, long movementDataLength) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MOVE_MONSTER.getValue());
                mplew.writeInt(oid);
                mplew.write(0);
                mplew.writeBool(skillPossible);
                mplew.write(skill);
                mplew.write(skillId);
                mplew.write(skillLevel);
                mplew.writeShort(pOption);
                mplew.writePos(startPos);
                rebroadcastMovementList(mplew, movementSlea, movementDataLength);
                return mplew.getPacket();
        }
        
        public static byte[] summonAttack(int cid, int summonOid, byte direction, List<SummonAttackEntry> allDamage) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
                mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(summonOid);
                mplew.write(0);     // char level
                mplew.write(direction);
                mplew.write(allDamage.size());
                for (SummonAttackEntry attackEntry : allDamage) {
                        mplew.writeInt(attackEntry.getMonsterOid()); // oid
                        mplew.write(6); // who knows
                        mplew.writeInt(attackEntry.getDamage()); // damage
                }
                
                return mplew.getPacket();
        }
        
        /*
        public static byte[] summonAttack(int cid, int summonSkillId, byte direction, List<SummonAttackEntry> allDamage) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                //b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
                mplew.writeShort(SendOpcode.SUMMON_ATTACK.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(summonSkillId);
                mplew.write(direction);
                mplew.write(4);
                mplew.write(allDamage.size());
                for (SummonAttackEntry attackEntry : allDamage) {
                        mplew.writeInt(attackEntry.getMonsterOid()); // oid
                        mplew.write(6); // who knows
                        mplew.writeInt(attackEntry.getDamage()); // damage
                }
                return mplew.getPacket();
        }
        */

        public static byte[] closeRangeAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
                addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
                return mplew.getPacket();
        }

        public static byte[] rangedAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RANGED_ATTACK.getValue());
                addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, projectile, damage, speed, direction, display);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] magicAttack(MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, Map<Integer, List<Integer>> damage, int charge, int speed, int direction, int display) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
                addAttackBody(mplew, chr, skill, skilllevel, stance, numAttackedAndDamage, 0, damage, speed, direction, display);
                if (charge != -1) {
                        mplew.writeInt(charge);
                }
                return mplew.getPacket();
        }

        private static void addAttackBody(LittleEndianWriter lew, MapleCharacter chr, int skill, int skilllevel, int stance, int numAttackedAndDamage, int projectile, Map<Integer, List<Integer>> damage, int speed, int direction, int display) {
                lew.writeInt(chr.getId());
                lew.write(numAttackedAndDamage);
                lew.write(0x5B);//?
                lew.write(skilllevel);
                if (skilllevel > 0) {
                        lew.writeInt(skill);
                }
                lew.write(display);
                lew.write(direction);
                lew.write(stance);
                lew.write(speed);
                lew.write(0x0A);
                lew.writeInt(projectile);
                for (Integer oned : damage.keySet()) {
                        List<Integer> onedList = damage.get(oned);
                        if (onedList != null) {
                                lew.writeInt(oned.intValue());
                                lew.write(0x0);
                                if (skill == 4211006) {
                                        lew.write(onedList.size());
                                }
                                for (Integer eachd : onedList) {
                                        lew.writeInt(eachd.intValue());
                                }
                        }
                }
        }
        
        public static byte[] throwGrenade(int cid, Point p, int keyDown, int skillId, int skillLevel) { // packets found thanks to GabrielSin
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.THROW_GRENADE.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(p.x);
                mplew.writeInt(p.y);
                mplew.writeInt(keyDown);
                mplew.writeInt(skillId);
                mplew.writeInt(skillLevel);
                return mplew.getPacket();
        }

        // someone thought it was a good idea to handle floating point representation through packets ROFL
        private static int doubleToShortBits(double d) {
                return (int) (Double.doubleToLongBits(d) >> 48);
        }
        
        public static byte[] getNPCShop(MapleClient c, int sid, List<MapleShopItem> items) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.OPEN_NPC_SHOP.getValue());
                mplew.writeInt(sid);
                mplew.writeShort(items.size()); // item count
                for (MapleShopItem item : items) {
                        mplew.writeInt(item.getItemId());
                        mplew.writeInt(item.getPrice());
                        mplew.writeInt(item.getPrice() == 0 ? item.getPitch() : 0); //Perfect Pitch
                        mplew.writeInt(0); //Can be used x minutes after purchase
                        mplew.writeInt(0); //Hmm
                        if (!ItemConstants.isRechargeable(item.getItemId())) {
                                mplew.writeShort(1); // stacksize o.o
                                mplew.writeShort(item.getBuyable());
                        } else {
                                mplew.writeShort(0);
                                mplew.writeInt(0);
                                mplew.writeShort(doubleToShortBits(ii.getUnitPrice(item.getItemId())));
                                mplew.writeShort(ii.getSlotMax(c, item.getItemId()));
                        }
                }
                return mplew.getPacket();
        }

        /* 00 = /
         * 01 = You don't have enough in stock
         * 02 = You do not have enough mesos
         * 03 = Please check if your inventory is full or not
         * 05 = You don't have enough in stock
         * 06 = Due to an error, the trade did not happen
         * 07 = Due to an error, the trade did not happen
         * 08 = /
         * 0D = You need more items
         * 0E = CRASH; LENGTH NEEDS TO BE LONGER :O
         */
        public static byte[] shopTransaction(byte code) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
                mplew.write(code);
                return mplew.getPacket();
        }

        public static byte[] updateInventorySlotLimit(int type, int newLimit) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.INVENTORY_GROW.getValue());
                mplew.write(type);
                mplew.write(newLimit);
                return mplew.getPacket();
        }

        public static byte[] modifyInventory(boolean updateTick, final List<ModifyInventory> mods) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.INVENTORY_OPERATION.getValue());
                mplew.writeBool(updateTick);
                mplew.write(mods.size());
                //mplew.write(0); v104 :)
                int addMovement = -1;
                for (ModifyInventory mod : mods) {
                        mplew.write(mod.getMode());
                        mplew.write(mod.getInventoryType());
                        mplew.writeShort(mod.getMode() == 2 ? mod.getOldPosition() : mod.getPosition());
                        switch (mod.getMode()) {
                                case 0: {//add item
                                        addItemInfo(mplew, mod.getItem(), true);
                                        break;
                                }
                                case 1: {//update quantity
                                        mplew.writeShort(mod.getQuantity());
                                        break;
                                }
                                case 2: {//move                  
                                        mplew.writeShort(mod.getPosition());
                                        if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                                                addMovement = mod.getOldPosition() < 0 ? 1 : 2;
                                        }
                                        break;
                                }
                                case 3: {//remove
                                        if (mod.getPosition() < 0) {
                                                addMovement = 2;
                                        }
                                        break;
                                }
                        }
                        mod.clear();
                }
                if (addMovement > -1) {
                        mplew.write(addMovement);
                }
                return mplew.getPacket();
        }

        public static byte[] getScrollEffect(int chr, ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll) {   // thanks to Rien dev team
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
                mplew.writeInt(chr);
                mplew.writeBool(scrollSuccess == ScrollResult.SUCCESS);
                mplew.writeBool(scrollSuccess == ScrollResult.CURSE);
                mplew.writeBool(legendarySpirit);
                mplew.writeBool(whiteScroll);
                return mplew.getPacket();
        }

        public static byte[] removePlayerFromMap(int cid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
                mplew.writeInt(cid);
                return mplew.getPacket();
        }
        
        public static byte[] catchMessage(int message) { // not done, I guess
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BRIDLE_MOB_CATCH_FAIL.getValue());
                mplew.write(message); // 1 = too strong, 2 = Elemental Rock
                mplew.writeInt(0);//Maybe itemid?
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] showAllCharacter(int chars, int unk) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
                mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
                mplew.write(chars > 0 ? 1 : 5); // 2: already connected to server, 3 : unk error (view-all-characters), 5 : cannot find any
                mplew.writeInt(chars);
                mplew.writeInt(unk);
                return mplew.getPacket();
        }
        
        public static byte[] showAriantScoreBoard() {   // thanks lrenex for pointing match's end scoreboard packet
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ARIANT_ARENA_SHOW_RESULT.getValue());
                return mplew.getPacket();
        }
        
        public static byte[] updateAriantPQRanking(final MapleCharacter chr, final int score) {
                return updateAriantPQRanking(new LinkedHashMap<MapleCharacter, Integer>(){{put(chr, score);}});
        }
        
        public static byte[] updateAriantPQRanking(Map<MapleCharacter, Integer> playerScore) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ARIANT_ARENA_USER_SCORE.getValue());
                mplew.write(playerScore.size());
                for (Entry<MapleCharacter, Integer> e : playerScore.entrySet()) {
                        mplew.writeMapleAsciiString(e.getKey().getName());
                        mplew.writeInt(e.getValue());
                }
                return mplew.getPacket();
        }
        
        public static byte[] updateWitchTowerScore(int score) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.WITCH_TOWER_SCORE_UPDATE.getValue());
                mplew.write(score);
                return mplew.getPacket();
        }
        
        public static byte[] silentRemoveItemFromMap(int oid) {
                return removeItemFromMap(oid, 1, 0);
        }
        
        /**
         * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/> 4 -
         * explode<br/> cid is ignored for 0 and 1
         *
         * @param oid
         * @param animation
         * @param cid
         * @return
         */
        public static byte[] removeItemFromMap(int oid, int animation, int cid) {
                return removeItemFromMap(oid, animation, cid, false, 0);
        }

        /**
         * animation: 0 - expire<br/> 1 - without animation<br/> 2 - pickup<br/> 4 -
         * explode<br/> cid is ignored for 0 and 1.<br /><br />Flagging pet as true
         * will make a pet pick up the item.
         *
         * @param oid
         * @param animation
         * @param cid
         * @param pet
         * @param slot
         * @return
         */
        public static byte[] removeItemFromMap(int oid, int animation, int cid, boolean pet, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.REMOVE_ITEM_FROM_MAP.getValue());
                mplew.write(animation); // expire
                mplew.writeInt(oid);
                if (animation >= 2) {
                        mplew.writeInt(cid);
                        if (pet) {
                                mplew.write(slot);
                        }
                }
                return mplew.getPacket();
        }

        public static byte[] updateCharLook(MapleClient target, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(1);
                addCharLook(mplew, chr, false);
                addRingLook(mplew, chr, true);
                addRingLook(mplew, chr, false);
                addMarriageRingLook(target, mplew, chr);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] damagePlayer(int skill, int monsteridfrom, int cid, int damage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
                mplew.writeInt(cid);
                mplew.write(skill);
                mplew.writeInt(damage);
                if(skill != -4) {
                        mplew.writeInt(monsteridfrom);
                        mplew.write(direction);
                        if (pgmr) {
                                mplew.write(pgmr_1);
                                mplew.write(is_pg ? 1 : 0);
                                mplew.writeInt(oid);
                                mplew.write(6);
                                mplew.writeShort(pos_x);
                                mplew.writeShort(pos_y);
                                mplew.write(0);
                        } else {
                                mplew.writeShort(0);
                        }
                        mplew.writeInt(damage);
                        if (fake > 0) {
                                mplew.writeInt(fake);
                        }
                } else {
                        mplew.writeInt(damage);
                }
                
                return mplew.getPacket();
        }
        
        public static byte[] sendMapleLifeCharacterInfo() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAPLELIFE_RESULT.getValue());
                mplew.writeInt(0);
                return mplew.getPacket();
        }
        
        public static byte[] sendMapleLifeNameError() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAPLELIFE_RESULT.getValue());
                mplew.writeInt(2);
                mplew.writeInt(3);
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static byte[] sendMapleLifeError(int code) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAPLELIFE_ERROR.getValue());
                mplew.write(0);
                mplew.writeInt(code);
                return mplew.getPacket();
        }
        
        public static byte[] charNameResponse(String charname, boolean nameUsed) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
                mplew.writeMapleAsciiString(charname);
                mplew.write(nameUsed ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] addNewCharEntry(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
                mplew.write(0);
                addCharEntry(mplew, chr, false);
                return mplew.getPacket();
        }

        /**
         * State :
         * 0x00 = success
         * 0x06 = Trouble logging into the game?
         * 0x09 = Unknown error
         * 0x0A = Could not be processed due to too many connection requests to the server.
         * 0x12 = invalid bday
         * 0x14 = incorrect pic
         * 0x16 = Cannot delete a guild master.
         * 0x18 = Cannot delete a character with a pending wedding.
         * 0x1A = Cannot delete a character with a pending world transfer.
         * 0x1D = Cannot delete a character that has a family.
         *
         * @param cid
         * @param state
         * @return
         */
        public static byte[] deleteCharResponse(int cid, int state) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
                mplew.writeInt(cid);
                mplew.write(state);
                return mplew.getPacket();
        }

        public static byte[] selectWorld(int world) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.LAST_CONNECTED_WORLD.getValue());
                mplew.writeInt(world);//According to GMS, it should be the world that contains the most characters (most active)
                return mplew.getPacket();
        }

        public static byte[] sendRecommended(List<Pair<Integer, String>> worlds) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RECOMMENDED_WORLD_MESSAGE.getValue());
                mplew.write(worlds.size());//size
                for (Iterator<Pair<Integer, String>> it = worlds.iterator(); it.hasNext();) {
                        Pair<Integer, String> world = it.next();
                        mplew.writeInt(world.getLeft());
                        mplew.writeMapleAsciiString(world.getRight());
                }
                return mplew.getPacket();
        }

        /**
         *
         * @param chr
         * @param isSelf
         * @return
         */
        public static byte[] charInfo(MapleCharacter chr) {
                //3D 00 0A 43 01 00 02 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHAR_INFO.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(chr.getLevel());
                mplew.writeShort(chr.getJob().getId());
                mplew.writeShort(chr.getFame());
                mplew.write(chr.getMarriageRing() != null ? 1 : 0);
                String guildName = "";
                String allianceName = "";
                if (chr.getGuildId() > 0) {
                        MapleGuild mg = Server.getInstance().getGuild(chr.getGuildId());
                        guildName = mg.getName();
                        
                        MapleAlliance alliance = Server.getInstance().getAlliance(chr.getGuild().getAllianceId());
                        if (alliance != null) {
                                allianceName = alliance.getName();
                        }
                }
                mplew.writeMapleAsciiString(guildName);
                mplew.writeMapleAsciiString(allianceName);  // does not seem to work
                mplew.write(0); // pMedalInfo, thanks to Arnah (Vertisy)
                
                MaplePet[] pets = chr.getPets();
                Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -114);
                for (int i = 0; i < 3; i++) {
                        if (pets[i] != null) {
                                mplew.write(pets[i].getUniqueId());
                                mplew.writeInt(pets[i].getItemId()); // petid
                                mplew.writeMapleAsciiString(pets[i].getName());
                                mplew.write(pets[i].getLevel()); // pet level
                                mplew.writeShort(pets[i].getCloseness()); // pet closeness
                                mplew.write(pets[i].getFullness()); // pet fullness
                                mplew.writeShort(0);
                                mplew.writeInt(inv != null ? inv.getItemId() : 0);
                        }
                }
                mplew.write(0); //end of pets
                
                Item mount;     //mounts can potentially crash the client if the player's level is not properly checked
                if (chr.getMount() != null && (mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18)) != null && MapleItemInformationProvider.getInstance().getEquipLevelReq(mount.getItemId()) <= chr.getLevel()) {
                        MapleMount mmount = chr.getMount();
                        mplew.write(mmount.getId()); //mount
                        mplew.writeInt(mmount.getLevel()); //level
                        mplew.writeInt(mmount.getExp()); //exp
                        mplew.writeInt(mmount.getTiredness()); //tiredness
                } else {
                        mplew.write(0);
                }
                mplew.write(chr.getCashShop().getWishList().size());
                for (int sn : chr.getCashShop().getWishList()) {
                        mplew.writeInt(sn);
                }
                
                MonsterBook book = chr.getMonsterBook();
                mplew.writeInt(book.getBookLevel());
                mplew.writeInt(book.getNormalCard());
                mplew.writeInt(book.getSpecialCard());
                mplew.writeInt(book.getTotalCards());
                mplew.writeInt(chr.getMonsterBookCover() > 0 ? MapleItemInformationProvider.getInstance().getCardMobId(chr.getMonsterBookCover()) : 0);
                Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
                if (medal != null) {
                        mplew.writeInt(medal.getItemId());
                } else {
                        mplew.writeInt(0);
                }
                ArrayList<Short> medalQuests = new ArrayList<>();
                List<MapleQuestStatus> completed = chr.getCompletedQuests();
                for (MapleQuestStatus qs : completed) {
                        if (qs.getQuest().getId() >= 29000) { // && q.getQuest().getId() <= 29923
                                medalQuests.add(qs.getQuest().getId());
                        }
                }

                Collections.sort(medalQuests);
                mplew.writeShort(medalQuests.size());
                for (Short s : medalQuests) {
                        mplew.writeShort(s);
                }
                return mplew.getPacket();
        }

        /**
         * It is important that statups is in the correct order (see declaration
         * order in MapleBuffStat) since this method doesn't do automagical
         * reordering.
         *
         * @param buffid
         * @param bufflength
         * @param statups
         * @return
         */
        //1F 00 00 00 00 00 03 00 00 40 00 00 00 E0 00 00 00 00 00 00 00 00 E0 01 8E AA 4F 00 00 C2 EB 0B E0 01 8E AA 4F 00 00 C2 EB 0B 0C 00 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 44 02 8E AA 4F 00 00 C2 EB 0B 00 00 E0 7A 1D 00 8E AA 4F 00 00 00 00 00 00 00 00 03
        public static byte[] giveBuff(int buffid, int bufflength, List<Pair<MapleBuffStat, Integer>> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
                boolean special = false;
                writeLongMask(mplew, statups);
                for (Pair<MapleBuffStat, Integer> statup : statups) {
                        if (statup.getLeft().equals(MapleBuffStat.MONSTER_RIDING) || statup.getLeft().equals(MapleBuffStat.HOMING_BEACON)) {
                                special = true;
                        }
                        mplew.writeShort(statup.getRight().shortValue());
                        mplew.writeInt(buffid);
                        mplew.writeInt(bufflength);
                }
                mplew.writeInt(0);
                mplew.write(0);
                mplew.writeInt(statups.get(0).getRight()); //Homing beacon ...

                if (special) {
                        mplew.skip(3);
                }
                return mplew.getPacket();
        }

        /**
         *
         * @param cid
         * @param statups
         * @param mount
         * @return
         */
        public static byte[] showMonsterRiding(int cid, MapleMount mount) { //Gtfo with this, this is just giveForeignBuff
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                mplew.writeLong(MapleBuffStat.MONSTER_RIDING.getValue());
                mplew.writeLong(0);
                mplew.writeShort(0);
                mplew.writeInt(mount.getItemId());
                mplew.writeInt(mount.getSkillId());
                mplew.writeInt(0); //Server Tick value.
                mplew.writeShort(0);
                mplew.write(0); //Times you have been buffed
                return mplew.getPacket();
        }
        /*        mplew.writeInt(cid);
             writeLongMask(mplew, statups);
             for (Pair<MapleBuffStat, Integer> statup : statups) {
             if (morph) {
             mplew.writeInt(statup.getRight().intValue());
             } else {
             mplew.writeShort(statup.getRight().shortValue());
             }
             }
             mplew.writeShort(0);
             mplew.write(0);*/

        /**
         *
         * @param c
         * @param quest
         * @return
         */
        public static byte[] forfeitQuest(short quest) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(1);
                mplew.writeShort(quest);
                mplew.write(0);
                return mplew.getPacket();
        }

        /**
         *
         * @param c
         * @param quest
         * @return
         */
        public static byte[] completeQuest(short quest, long time) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(1);
                mplew.writeShort(quest);
                mplew.write(2);
                mplew.writeLong(getTime(time));
                return mplew.getPacket();
        }

        /**
         *
         * @param c
         * @param quest
         * @param npc
         * @param progress
         * @return
         */
        
        public static byte[] updateQuestInfo(short quest, int npc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(8); //0x0A in v95
                mplew.writeShort(quest);
                mplew.writeInt(npc);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] addQuestTimeLimit(final short quest, final int time) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(6);
                mplew.writeShort(1);//Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
                mplew.writeShort(quest);
                mplew.writeInt(time);
                return mplew.getPacket();
        }

        public static byte[] removeQuestTimeLimit(final short quest) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(7);
                mplew.writeShort(1);//Position
                mplew.writeShort(quest);
                return mplew.getPacket();
        }

        public static byte[] updateQuest(MapleCharacter chr, MapleQuestStatus qs, boolean infoUpdate) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(1);
                if (infoUpdate) {
                        MapleQuestStatus iqs = chr.getQuest(qs.getInfoNumber());
                        mplew.writeShort(iqs.getQuestID());
                        mplew.write(1);
                        mplew.writeMapleAsciiString(iqs.getProgressData());
                } else {
                        mplew.writeShort(qs.getQuest().getId());
                        mplew.write(qs.getStatus().getId());
                        mplew.writeMapleAsciiString(qs.getProgressData());
                }
                mplew.skip(5);
                return mplew.getPacket();
        }

        private static void writeLongMaskD(final MaplePacketLittleEndianWriter mplew, List<Pair<MapleDisease, Integer>> statups) {
                long firstmask = 0;
                long secondmask = 0;
                for (Pair<MapleDisease, Integer> statup : statups) {
                        if (statup.getLeft().isFirst()) {
                                firstmask |= statup.getLeft().getValue();
                        } else {
                                secondmask |= statup.getLeft().getValue();
                        }
                }
                mplew.writeLong(firstmask);
                mplew.writeLong(secondmask);
        }
        
        public static byte[] giveDebuff(List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
                writeLongMaskD(mplew, statups);
                for (Pair<MapleDisease, Integer> statup : statups) {
                        mplew.writeShort(statup.getRight().shortValue());
                        mplew.writeShort(skill.getSkillId());
                        mplew.writeShort(skill.getSkillLevel());
                        mplew.writeInt((int) skill.getDuration());
                }
                mplew.writeShort(0); // ??? wk charges have 600 here o.o
                mplew.writeShort(900);//Delay
                mplew.write(1);
                return mplew.getPacket();
        }
        
        public static byte[] giveForeignDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
                // Poison damage visibility and missing diseases status visibility, extended through map transitions thanks to Ronan
                
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskD(mplew, statups);
                for (Pair<MapleDisease, Integer> statup : statups) {
                        if (statup.getLeft() == MapleDisease.POISON) {
                                mplew.writeShort(statup.getRight().shortValue());
                        }
                        mplew.writeShort(skill.getSkillId());
                        mplew.writeShort(skill.getSkillLevel());
                }
                mplew.writeShort(0); // same as give_buff
                mplew.writeShort(900);//Delay
                return mplew.getPacket();
        }

        public static byte[] cancelForeignFirstDebuff(int cid, long mask) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                mplew.writeLong(mask);
                mplew.writeLong(0);
                return mplew.getPacket();
        }
        
        public static byte[] cancelForeignDebuff(int cid, long mask) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                mplew.writeLong(0);
                mplew.writeLong(mask);
                return mplew.getPacket();
        }

        public static byte[] giveForeignBuff(int cid, List<Pair<MapleBuffStat, Integer>> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMask(mplew, statups);
                for (Pair<MapleBuffStat, Integer> statup : statups) {
                        mplew.writeShort(statup.getRight().shortValue());
                }
                mplew.writeInt(0);
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskFromList(mplew, statups);
                return mplew.getPacket();
        }

        public static byte[] cancelBuff(List<MapleBuffStat> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
                writeLongMaskFromList(mplew, statups);
                mplew.write(1);//?
                return mplew.getPacket();
        }

        private static void writeLongMask(final MaplePacketLittleEndianWriter mplew, List<Pair<MapleBuffStat, Integer>> statups) {
                long firstmask = 0;
                long secondmask = 0;
                for (Pair<MapleBuffStat, Integer> statup : statups) {
                        if (statup.getLeft().isFirst()) {
                                firstmask |= statup.getLeft().getValue();
                        } else {
                                secondmask |= statup.getLeft().getValue();
                        }
                }
                mplew.writeLong(firstmask);
                mplew.writeLong(secondmask);
        }

        private static void writeLongMaskFromList(final MaplePacketLittleEndianWriter mplew, List<MapleBuffStat> statups) {
                long firstmask = 0;
                long secondmask = 0;
                for (MapleBuffStat statup : statups) {
                        if (statup.isFirst()) {
                                firstmask |= statup.getValue();
                        } else {
                                secondmask |= statup.getValue();
                        }
                }
                mplew.writeLong(firstmask);
                mplew.writeLong(secondmask);
        }
        
        private static void writeLongEncodeTemporaryMask(final MaplePacketLittleEndianWriter mplew, Collection<MonsterStatus> stati) {
                int masks[] = new int[4];
                
                for (MonsterStatus statup : stati) {
                        int pos = statup.isFirst() ? 0 : 2;
                        for (int i = 0; i < 2; i++) {
                                masks[pos + i] |= statup.getValue() >> 32 * i;
                        }
                }
                
                for (int i = 0; i < masks.length; i++) {
                        mplew.writeInt(masks[i]);
                }
        }

        public static byte[] cancelDebuff(long mask) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
                mplew.writeShort(SendOpcode.CANCEL_BUFF.getValue());
                mplew.writeLong(0);
                mplew.writeLong(mask);
                mplew.write(0);
                return mplew.getPacket();
        }

        private static void writeLongMaskSlowD(final MaplePacketLittleEndianWriter mplew) {
                mplew.writeInt(0);
                mplew.writeInt(2048);
                mplew.writeLong(0);
        }
        
        public static byte[] giveForeignSlowDebuff(int cid, List<Pair<MapleDisease, Integer>> statups, MobSkill skill) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskSlowD(mplew);
                for (Pair<MapleDisease, Integer> statup : statups) {
                        if (statup.getLeft() == MapleDisease.POISON) {
                                mplew.writeShort(statup.getRight().shortValue());
                        }
                        mplew.writeShort(skill.getSkillId());
                        mplew.writeShort(skill.getSkillLevel());
                }
                mplew.writeShort(0); // same as give_buff
                mplew.writeShort(900);//Delay
                return mplew.getPacket();
        }
        
        public static byte[] cancelForeignSlowDebuff(int cid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskSlowD(mplew);
                return mplew.getPacket();
        }
        
        private static void writeLongMaskChair(final MaplePacketLittleEndianWriter mplew) {
                mplew.writeInt(0);
                mplew.writeInt(262144);
                mplew.writeLong(0);
        }
        
        public static byte[] giveForeignChairSkillEffect(int cid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskChair(mplew);
                
                mplew.writeShort(0);
                mplew.writeShort(0);
                mplew.writeShort(100);
                mplew.writeShort(1);
                
                mplew.writeShort(0);
                mplew.writeShort(900);
                
                mplew.skip(7);
                
                return mplew.getPacket();
        }
        
        // packet found thanks to Ronan
        public static byte[] giveForeignWKChargeEffect(int cid, int buffid, List<Pair<MapleBuffStat, Integer>> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMask(mplew, statups);
                mplew.writeInt(buffid);
                mplew.writeShort(600);
                mplew.writeShort(1000);//Delay
                mplew.write(1);
                
                return mplew.getPacket();
        }
        
        public static byte[] cancelForeignChairSkillEffect(int cid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(19);
                mplew.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMaskChair(mplew);
                
                return mplew.getPacket();
        }
        
        public static byte[] getPlayerShopChat(MapleCharacter chr, String chat, boolean owner) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
                mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
                mplew.write(owner ? 0 : 1);
                mplew.writeMapleAsciiString(chr.getName() + " : " + chat);
                return mplew.getPacket();
        }

        public static byte[] getPlayerShopNewVisitor(MapleCharacter chr, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
                mplew.write(slot);
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                return mplew.getPacket();
        }

        public static byte[] getPlayerShopRemoveVisitor(int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                if (slot != 0) {
                        mplew.writeShort(slot);
                }
                return mplew.getPacket();
        }

        public static byte[] getTradePartnerAdd(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
                mplew.write(1);
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                return mplew.getPacket();
        }

        public static byte[] tradeInvite(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.INVITE.getCode());
                mplew.write(3);
                mplew.writeMapleAsciiString(chr.getName());
                mplew.write(new byte[]{(byte) 0xB7, (byte) 0x50, 0, 0});
                return mplew.getPacket();
        }

        public static byte[] getTradeMesoSet(byte number, int meso) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.SET_MESO.getCode());
                mplew.write(number);
                mplew.writeInt(meso);
                return mplew.getPacket();
        }

        public static byte[] getTradeItemAdd(byte number, Item item) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.SET_ITEMS.getCode());
                mplew.write(number);
                mplew.write(item.getPosition());
                addItemInfo(mplew, item, true);
                return mplew.getPacket();
        }

        public static byte[] getPlayerShopItemUpdate(MaplePlayerShop shop) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
                mplew.write(shop.getItems().size());
                for (MaplePlayerShopItem item : shop.getItems()) {
                        mplew.writeShort(item.getBundles());
                        mplew.writeShort(item.getItem().getQuantity());
                        mplew.writeInt(item.getPrice());
                        addItemInfo(mplew, item.getItem(), true);
                }
                return mplew.getPacket();
        }
        
        public static byte[] getPlayerShopOwnerUpdate(MaplePlayerShop.SoldItem item, int position) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.UPDATE_PLAYERSHOP.getCode());
                mplew.write(position);
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getBuyer());
                
                return mplew.getPacket();
        }
        
        /**
         *
         * @param c
         * @param shop
         * @param owner
         * @return
         */
        public static byte[] getPlayerShop(MaplePlayerShop shop, boolean owner) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(4);
                mplew.write(4);
                mplew.write(owner ? 0 : 1);
                
                if (owner) {
                        List<MaplePlayerShop.SoldItem> sold = shop.getSold();
                        mplew.write(sold.size());
                        for (MaplePlayerShop.SoldItem s : sold) {
                                mplew.writeInt(s.getItemId());
                                mplew.writeShort(s.getQuantity());
                                mplew.writeInt(s.getMesos());
                                mplew.writeMapleAsciiString(s.getBuyer());
                        }
                } else {
                        mplew.write(0);
                }
                
                addCharLook(mplew, shop.getOwner(), false);
                mplew.writeMapleAsciiString(shop.getOwner().getName());
                
                MapleCharacter visitors[] = shop.getVisitors();
                for(int i = 0; i < 3; i++) {
                    if(visitors[i] != null) {
                        mplew.write(i + 1);
                        addCharLook(mplew, visitors[i], false);
                        mplew.writeMapleAsciiString(visitors[i].getName());
                    }
                }
                
                mplew.write(0xFF);
                mplew.writeMapleAsciiString(shop.getDescription());
                List<MaplePlayerShopItem> items = shop.getItems();
                mplew.write(0x10);  //TODO SLOTS, which is 16 for most stores...slotMax
                mplew.write(items.size());
                for (MaplePlayerShopItem item : items) {
                        mplew.writeShort(item.getBundles());
                        mplew.writeShort(item.getItem().getQuantity());
                        mplew.writeInt(item.getPrice());
                        addItemInfo(mplew, item.getItem(), true);
                }
                return mplew.getPacket();
        }

        public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(3);
                mplew.write(2);
                mplew.write(number);
                if (number == 1) {
                        mplew.write(0);
                        addCharLook(mplew, trade.getPartner().getChr(), false);
                        mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
                }
                mplew.write(number);
                addCharLook(mplew, c.getPlayer(), false);
                mplew.writeMapleAsciiString(c.getPlayer().getName());
                mplew.write(0xFF);
                return mplew.getPacket();
        }

        public static byte[] getTradeConfirmation() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.CONFIRM.getCode());
                return mplew.getPacket();
        }

        /**
         * Possible values for <code>operation</code>:<br> 2: Trade cancelled, by the
         * other character<br> 7: Trade successful<br> 8: Trade unsuccessful<br> 
         * 9: Cannot carry more one-of-a-kind items<br> 12: Cannot trade on different maps<br>
         * 13: Cannot trade, game files damaged<br> 
         *
         * @param number
         * @param operation
         * @return
         */
        public static byte[] getTradeResult(byte number, byte operation) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                mplew.write(number);
                mplew.write(operation);
                return mplew.getPacket();
        }
        
        /**
         * Possible values for <code>speaker</code>:<br> 0: Npc talking (left)<br>
         * 1: Npc talking (right)<br> 2: Player talking (left)<br> 3: Player talking
         * (left)<br>
         *
         * @param npc Npcid
         * @param msgType
         * @param talk
         * @param endBytes
         * @param speaker
         * @return
         */
        public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte speaker) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NPC_TALK.getValue());
                mplew.write(4); // ?
                mplew.writeInt(npc);
                mplew.write(msgType);
                mplew.write(speaker);
                mplew.writeMapleAsciiString(talk);
                mplew.write(HexTool.getByteArrayFromHexString(endBytes));
                return mplew.getPacket();
        }

        public static byte[] getDimensionalMirror(String talk) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NPC_TALK.getValue());
                mplew.write(4); // ?
                mplew.writeInt(9010022);
                mplew.write(0x0E);
                mplew.write(0);
                mplew.writeInt(0);
                mplew.writeMapleAsciiString(talk);
                return mplew.getPacket();
        }

        public static byte[] getNPCTalkStyle(int npc, String talk, int styles[]) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NPC_TALK.getValue());
                mplew.write(4); // ?
                mplew.writeInt(npc);
                mplew.write(7);
                mplew.write(0); //speaker
                mplew.writeMapleAsciiString(talk);
                mplew.write(styles.length);
                for (int i = 0; i < styles.length; i++) {
                        mplew.writeInt(styles[i]);
                }
                return mplew.getPacket();
        }

        public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NPC_TALK.getValue());
                mplew.write(4); // ?
                mplew.writeInt(npc);
                mplew.write(3);
                mplew.write(0); //speaker
                mplew.writeMapleAsciiString(talk);
                mplew.writeInt(def);
                mplew.writeInt(min);
                mplew.writeInt(max);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] getNPCTalkText(int npc, String talk, String def) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NPC_TALK.getValue());
                mplew.write(4); // Doesn't matter
                mplew.writeInt(npc);
                mplew.write(2);
                mplew.write(0); //speaker
                mplew.writeMapleAsciiString(talk);
                mplew.writeMapleAsciiString(def);//:D
                mplew.writeInt(0);
                return mplew.getPacket();
        }
        
        // NPC Quiz packets thanks to Eric
        public static byte[] OnAskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz) { 
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(); 
                mplew.writeShort(SendOpcode.NPC_TALK.getValue()); 
                mplew.write(nSpeakerTypeID); 
                mplew.writeInt(nSpeakerTemplateID); 
                mplew.write(0x6); 
                mplew.write(0); 
                mplew.write(nResCode); 
                if (nResCode == 0x0) {//fail has no bytes <3 
                        mplew.writeMapleAsciiString(sTitle); 
                        mplew.writeMapleAsciiString(sProblemText); 
                        mplew.writeMapleAsciiString(sHintText); 
                        mplew.writeShort(nMinInput); 
                        mplew.writeShort(nMaxInput); 
                        mplew.writeInt(tRemainInitialQuiz); 
                } 
                return mplew.getPacket(); 
        } 

        public static byte[] OnAskSpeedQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz) { 
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(); 
                mplew.writeShort(SendOpcode.NPC_TALK.getValue()); 
                mplew.write(nSpeakerTypeID); 
                mplew.writeInt(nSpeakerTemplateID); 
                mplew.write(0x7); 
                mplew.write(0); 
                mplew.write(nResCode); 
                if (nResCode == 0x0) {//fail has no bytes <3 
                        mplew.writeInt(nType); 
                        mplew.writeInt(dwAnswer); 
                        mplew.writeInt(nCorrect); 
                        mplew.writeInt(nRemain); 
                        mplew.writeInt(tRemainInitialQuiz); 
                } 
                return mplew.getPacket(); 
        }

        public static byte[] showBuffeffect(int cid, int skillid, int effectid) {
                return showBuffeffect(cid, skillid, effectid, (byte) 3);
        }
        
        public static byte[] showBuffeffect(int cid, int skillid, int effectid, byte direction) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(effectid); //buff level
                mplew.writeInt(skillid);
                mplew.write(direction);
                mplew.write(1);
                mplew.writeLong(0);
                return mplew.getPacket();
        }
        
        public static byte[] showBuffeffect(int cid, int skillid, int skilllv, int effectid, byte direction) {   // updated packet structure found thanks to Rien dev team
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(effectid);
                mplew.writeInt(skillid);
                mplew.write(0);
                mplew.write(skilllv);
                mplew.write(direction);
        
                return mplew.getPacket();
        }

        public static byte[] showOwnBuffEffect(int skillid, int effectid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(effectid);
                mplew.writeInt(skillid);
                mplew.write(0xA9);
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] showOwnBerserk(int skilllevel, boolean Berserk) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(1);
                mplew.writeInt(1320006);
                mplew.write(0xA9);
                mplew.write(skilllevel);
                mplew.write(Berserk ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] showBerserk(int cid, int skilllevel, boolean Berserk) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(1);
                mplew.writeInt(1320006);
                mplew.write(0xA9);
                mplew.write(skilllevel);
                mplew.write(Berserk ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] updateSkill(int skillid, int level, int masterlevel, long expiration) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_SKILLS.getValue());
                mplew.write(1);
                mplew.writeShort(1);
                mplew.writeInt(skillid);
                mplew.writeInt(level);
                mplew.writeInt(masterlevel);
                addExpirationTime(mplew, expiration);
                mplew.write(4);
                return mplew.getPacket();
        }

        public static byte[] getShowQuestCompletion(int id) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.QUEST_CLEAR.getValue());
                mplew.writeShort(id);
                return mplew.getPacket();
        }

        public static byte[] getKeymap(Map<Integer, MapleKeyBinding> keybindings) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.KEYMAP.getValue());
                mplew.write(0);
                for (int x = 0; x < 90; x++) {
                        MapleKeyBinding binding = keybindings.get(Integer.valueOf(x));
                        if (binding != null) {
                                mplew.write(binding.getType());
                                mplew.writeInt(binding.getAction());
                        } else {
                                mplew.write(0);
                                mplew.writeInt(0);
                        }
                }
                return mplew.getPacket();
        }
        
        public static byte[] QuickslotMappedInit(MapleQuickslotBinding pQuickslot)
        {
                final MaplePacketLittleEndianWriter pOutPacket = new MaplePacketLittleEndianWriter();

                pOutPacket.writeShort(SendOpcode.QUICKSLOT_INIT.getValue());
                pQuickslot.Encode(pOutPacket);

                return pOutPacket.getPacket();
        }

        public static byte[] getWhisper(String sender, int channel, String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.WHISPER.getValue());
                mplew.write(0x12);
                mplew.writeMapleAsciiString(sender);
                mplew.writeShort(channel - 1); // I guess this is the channel
                mplew.writeMapleAsciiString(text);
                return mplew.getPacket();
        }

        /**
         *
         * @param target name of the target character
         * @param reply error code: 0x0 = cannot find char, 0x1 = success
         * @return the MaplePacket
         */
        public static byte[] getWhisperReply(String target, byte reply) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.WHISPER.getValue());
                mplew.write(0x0A); // whisper?
                mplew.writeMapleAsciiString(target);
                mplew.write(reply);
                return mplew.getPacket();
        }

        public static byte[] getInventoryFull() {
                return modifyInventory(true, Collections.<ModifyInventory>emptyList());
        }

        public static byte[] getShowInventoryFull() {
                return getShowInventoryStatus(0xff);
        }

        public static byte[] showItemUnavailable() {
                return getShowInventoryStatus(0xfe);
        }

        public static byte[] getShowInventoryStatus(int mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(0);
                mplew.write(mode);
                mplew.writeInt(0);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, int meso) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(0x16);
                mplew.writeInt(npcId);
                mplew.write(slots);
                mplew.writeShort(0x7E);
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeInt(meso);
                mplew.writeShort(0);
                mplew.write((byte) items.size());
                for (Item item : items) {
                        addItemInfo(mplew, item, true);
                }
                mplew.writeShort(0);
                mplew.write(0);
                return mplew.getPacket();
        }

        /*
         * 0x0A = Inv full
         * 0x0B = You do not have enough mesos
         * 0x0C = One-Of-A-Kind error
         */
        public static byte[] getStorageError(byte i) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(i);
                return mplew.getPacket();
        }

        public static byte[] mesoStorage(byte slots, int meso) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(0x13);
                mplew.write(slots);
                mplew.writeShort(2);
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.writeInt(meso);
                return mplew.getPacket();
        }

        public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(0xD);
                mplew.write(slots);
                mplew.writeShort(type.getBitfieldEncoding());
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.write(items.size());
                for (Item item : items) {
                        addItemInfo(mplew, item, true);
                }
                return mplew.getPacket();
        }

        public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(0x9);
                mplew.write(slots);
                mplew.writeShort(type.getBitfieldEncoding());
                mplew.writeShort(0);
                mplew.writeInt(0);
                mplew.write(items.size());
                for (Item item : items) {
                        addItemInfo(mplew, item, true);
                }
                return mplew.getPacket();
        }
        
        public static byte[] arrangeStorage(byte slots, Collection<Item> items) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

                mplew.writeShort(SendOpcode.STORAGE.getValue());
                mplew.write(0xF);
                mplew.write(slots);
                mplew.write(124);
                mplew.skip(10);
                mplew.write(items.size());
                for (Item item : items) {
                        addItemInfo(mplew, item, true);
                }
                mplew.write(0);
                return mplew.getPacket();
        }
        
        /**
         *
         * @param oid
         * @param remhppercentage
         * @return
         */
        public static byte[] showMonsterHP(int oid, int remhppercentage) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_MONSTER_HP.getValue());
                mplew.writeInt(oid);
                mplew.write(remhppercentage);
                return mplew.getPacket();
        }

        public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(5);
                mplew.writeInt(oid);
                mplew.writeInt(currHP);
                mplew.writeInt(maxHP);
                mplew.write(tagColor);
                mplew.write(tagBgColor);
                return mplew.getPacket();
        }
        
        private static Pair<Integer, Integer> normalizedCustomMaxHP(long currHP, long maxHP) {
                int sendHP, sendMaxHP;
            
                if(maxHP <= Integer.MAX_VALUE) {
                    sendHP = (int) currHP;
                    sendMaxHP = (int) maxHP;
                } else {
                    float f = ((float) currHP) / maxHP;
                    
                    sendHP = (int) (Integer.MAX_VALUE * f);
                    sendMaxHP = Integer.MAX_VALUE;
                }
                
                return new Pair<>(sendHP, sendMaxHP);
        }
        
        public static byte[] customShowBossHP(byte call, int oid, long currHP, long maxHP, byte tagColor, byte tagBgColor) {
                Pair<Integer, Integer> customHP = normalizedCustomMaxHP(currHP, maxHP);
            
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(call);
                mplew.writeInt(oid);
                mplew.writeInt(customHP.left);
                mplew.writeInt(customHP.right);
                mplew.write(tagColor);
                mplew.write(tagBgColor);
                return mplew.getPacket();
        }

        public static byte[] giveFameResponse(int mode, String charname, int newfame) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
                mplew.write(0);
                mplew.writeMapleAsciiString(charname);
                mplew.write(mode);
                mplew.writeShort(newfame);
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        /**
         * status can be: <br> 0: ok, use giveFameResponse<br> 1: the username is
         * incorrectly entered<br> 2: users under level 15 are unable to toggle with
         * fame.<br> 3: can't raise or drop fame anymore today.<br> 4: can't raise
         * or drop fame for this character for this month anymore.<br> 5: received
         * fame, use receiveFame()<br> 6: level of fame neither has been raised nor
         * dropped due to an unexpected error
         *
         * @param status
         * @return
         */
        public static byte[] giveFameErrorResponse(int status) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
                mplew.write(status);
                return mplew.getPacket();
        }

        public static byte[] receiveFame(int mode, String charnameFrom) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAME_RESPONSE.getValue());
                mplew.write(5);
                mplew.writeMapleAsciiString(charnameFrom);
                mplew.write(mode);
                return mplew.getPacket();
        }

        public static byte[] partyCreated(MapleParty party, int partycharid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.write(8);
                mplew.writeInt(party.getId());
                
                Map<Integer, MapleDoor> partyDoors = party.getDoors();
                if (partyDoors.size() > 0) {
                        MapleDoor door = partyDoors.get(partycharid);
                        
                        if(door != null) {
                                MapleDoorObject mdo = door.getAreaDoor();
                                mplew.writeInt(mdo.getTo().getId());
                                mplew.writeInt(mdo.getFrom().getId());
                                mplew.writeInt(mdo.getPosition().x);
                                mplew.writeInt(mdo.getPosition().y);
                        } else {
                                mplew.writeInt(999999999);
                                mplew.writeInt(999999999);
                                mplew.writeInt(0);
                                mplew.writeInt(0);
                        }
                } else {
                        mplew.writeInt(999999999);
                        mplew.writeInt(999999999);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                }
                return mplew.getPacket();
        }

        public static byte[] partyInvite(MapleCharacter from) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.write(4);
                mplew.writeInt(from.getParty().getId());
                mplew.writeMapleAsciiString(from.getName());
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static byte[] partySearchInvite(MapleCharacter from) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.write(4);
                mplew.writeInt(from.getParty().getId());
                mplew.writeMapleAsciiString("PS: " + from.getName());
                mplew.write(0);
                return mplew.getPacket();
        }

        /**
         * 10: A beginner can't create a party. 1/5/6/11/14/19: Your request for a
         * party didn't work due to an unexpected error. 12: Quit as leader of the
         * party. 13: You have yet to join a party.
         * 16: Already have joined a party. 17: The party you're trying to join is
         * already in full capacity. 19: Unable to find the requested character in
         * this channel. 25: Cannot kick another user in this map. 28/29: Leadership
         * can only be given to a party member in the vicinity. 30: Change leadership
         * only on same channel.
         *
         * @param message
         * @return
         */
        public static byte[] partyStatusMessage(int message) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.write(message);
                return mplew.getPacket();
        }

        /**
         * 21: Player is blocking any party invitations, 22: Player is taking care of
         * another invitation, 23: Player have denied request to the party.
         *
         * @param message
         * @param charname
         * @return
         */
        public static byte[] partyStatusMessage(int message, String charname) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.write(message);
                mplew.writeMapleAsciiString(charname);
                return mplew.getPacket();
        }

        private static void addPartyStatus(int forchannel, MapleParty party, LittleEndianWriter lew, boolean leaving) {
                List<MaplePartyCharacter> partymembers = new ArrayList<>(party.getMembers());
                while (partymembers.size() < 6) {
                        partymembers.add(new MaplePartyCharacter());
                }
                for (MaplePartyCharacter partychar : partymembers) {
                        lew.writeInt(partychar.getId());
                }
                for (MaplePartyCharacter partychar : partymembers) {
                        lew.writeAsciiString(getRightPaddedStr(partychar.getName(), '\0', 13));
                }
                for (MaplePartyCharacter partychar : partymembers) {
                        lew.writeInt(partychar.getJobId());
                }
                for (MaplePartyCharacter partychar : partymembers) {
                        lew.writeInt(partychar.getLevel());
                }
                for (MaplePartyCharacter partychar : partymembers) {
                        if (partychar.isOnline()) {
                                lew.writeInt(partychar.getChannel() - 1);
                        } else {
                                lew.writeInt(-2);
                        }
                }
                lew.writeInt(party.getLeader().getId());
                for (MaplePartyCharacter partychar : partymembers) {
                        if (partychar.getChannel() == forchannel) {
                                lew.writeInt(partychar.getMapId());
                        } else {
                                lew.writeInt(0);
                        }
                }
                
                Map<Integer, MapleDoor> partyDoors = party.getDoors();
                for (MaplePartyCharacter partychar : partymembers) {
                        if (partychar.getChannel() == forchannel && !leaving) {
                                if (partyDoors.size() > 0) {
                                        MapleDoor door = partyDoors.get(partychar.getId());
                                        if(door != null) {
                                                MapleDoorObject mdo = door.getTownDoor();
                                                lew.writeInt(mdo.getTown().getId());
                                                lew.writeInt(mdo.getArea().getId());
                                                lew.writeInt(mdo.getPosition().x);
                                                lew.writeInt(mdo.getPosition().y);
                                        } else {
                                                lew.writeInt(999999999);
                                                lew.writeInt(999999999);
                                                lew.writeInt(0);
                                                lew.writeInt(0);
                                        }
                                } else {
                                        lew.writeInt(999999999);
                                        lew.writeInt(999999999);
                                        lew.writeInt(0);
                                        lew.writeInt(0);
                                }
                        } else {
                                lew.writeInt(999999999);
                                lew.writeInt(999999999);
                                lew.writeInt(0);
                                lew.writeInt(0);
                        }
                }
        }

        public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                switch (op) {
                case DISBAND:
                case EXPEL:
                case LEAVE:
                        mplew.write(0x0C);
                        mplew.writeInt(party.getId());
                        mplew.writeInt(target.getId());
                        if (op == PartyOperation.DISBAND) {
                                mplew.write(0);
                                mplew.writeInt(party.getId());
                        } else {
                                mplew.write(1);
                                if (op == PartyOperation.EXPEL) {
                                        mplew.write(1);
                                } else {
                                        mplew.write(0);
                                }
                                mplew.writeMapleAsciiString(target.getName());
                                addPartyStatus(forChannel, party, mplew, false);
                        }
                        break;
                case JOIN:
                        mplew.write(0xF);
                        mplew.writeInt(party.getId());
                        mplew.writeMapleAsciiString(target.getName());
                        addPartyStatus(forChannel, party, mplew, false);
                        break;
                case SILENT_UPDATE:
                case LOG_ONOFF:
                        mplew.write(0x7);
                        mplew.writeInt(party.getId());
                        addPartyStatus(forChannel, party, mplew, false);
                        break;
                case CHANGE_LEADER:
                        mplew.write(0x1B);
                        mplew.writeInt(target.getId());
                        mplew.write(0);
                        break;
                }
                return mplew.getPacket();
        }

        public static byte[] partyPortal(int townId, int targetId, Point position) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARTY_OPERATION.getValue());
                mplew.writeShort(0x23);
                mplew.writeInt(townId);
                mplew.writeInt(targetId);
                mplew.writePos(position);
                return mplew.getPacket();
        }

        public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(curhp);
                mplew.writeInt(maxhp);
                return mplew.getPacket();
        }

        /**
         * mode: 0 buddychat; 1 partychat; 2 guildchat
         *
         * @param name
         * @param chattext
         * @param mode
         * @return
         */
        public static byte[] multiChat(String name, String chattext, int mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MULTICHAT.getValue());
                mplew.write(mode);
                mplew.writeMapleAsciiString(name);
                mplew.writeMapleAsciiString(chattext);
                return mplew.getPacket();
        }

        private static void writeIntMask(final MaplePacketLittleEndianWriter mplew, Map<MonsterStatus, Integer> stats) {
                int firstmask = 0;
                int secondmask = 0;
                for (MonsterStatus stat : stats.keySet()) {
                        if (stat.isFirst()) {
                                firstmask |= stat.getValue();
                        } else {
                                secondmask |= stat.getValue();
                        }
                }
                mplew.writeInt(firstmask);
                mplew.writeInt(secondmask);
        }
        
        public static byte[] applyMonsterStatus(final int oid, final MonsterStatusEffect mse, final List<Integer> reflection) {
                Map<MonsterStatus, Integer> stati = mse.getStati();
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.APPLY_MONSTER_STATUS.getValue());
                mplew.writeInt(oid);
                mplew.writeLong(0);
                writeIntMask(mplew, stati);
                for (Map.Entry<MonsterStatus, Integer> stat : stati.entrySet()) {
                        mplew.writeShort(stat.getValue());
                        if (mse.isMonsterSkill()) {
                                mplew.writeShort(mse.getMobSkill().getSkillId());
                                mplew.writeShort(mse.getMobSkill().getSkillLevel());
                        } else {
                                mplew.writeInt(mse.getSkill().getId());
                        }
                        mplew.writeShort(-1); // might actually be the buffTime but it's not displayed anywhere
                }
                int size = stati.size(); // size
                if (reflection != null) {
                        for (Integer ref : reflection) {
                                mplew.writeInt(ref);
                        }
                        if (reflection.size() > 0) {
                                size /= 2; // This gives 2 buffs per reflection but it's really one buff
                        }
                }
                mplew.write(size); // size
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] cancelMonsterStatus(int oid, Map<MonsterStatus, Integer> stats) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_MONSTER_STATUS.getValue());
                mplew.writeInt(oid);
                mplew.writeLong(0);
                writeIntMask(mplew, stats);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] getClock(int time) { // time in seconds
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CLOCK.getValue());
                mplew.write(2); // clock type. if you send 3 here you have to send another byte (which does not matter at all) before the timestamp
                mplew.writeInt(time);
                return mplew.getPacket();
        }

        public static byte[] getClockTime(int hour, int min, int sec) { // Current Time
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CLOCK.getValue());
                mplew.write(1); //Clock-Type
                mplew.write(hour);
                mplew.write(min);
                mplew.write(sec);
                return mplew.getPacket();
        }

        public static byte[] removeClock() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STOP_CLOCK.getValue());
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] spawnMist(int oid, int ownerCid, int skill, int level, MapleMist mist) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_MIST.getValue());
                mplew.writeInt(oid);
                mplew.writeInt(mist.isMobMist() ? 0 : mist.isPoisonMist() ? 1 : mist.isRecoveryMist() ? 4 : 2); // mob mist = 0, player poison = 1, smokescreen = 2, unknown = 3, recovery = 4
                mplew.writeInt(ownerCid);
                mplew.writeInt(skill);
                mplew.write(level);
                mplew.writeShort(mist.getSkillDelay()); // Skill delay
                mplew.writeInt(mist.getBox().x);
                mplew.writeInt(mist.getBox().y);
                mplew.writeInt(mist.getBox().x + mist.getBox().width);
                mplew.writeInt(mist.getBox().y + mist.getBox().height);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] removeMist(int oid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.REMOVE_MIST.getValue());
                mplew.writeInt(oid);
                return mplew.getPacket();
        }
        
        public static byte[] damageSummon(int cid, int oid, int damage, int monsterIdFrom) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DAMAGE_SUMMON.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(oid);
                mplew.write(12);
                mplew.writeInt(damage);         // damage display doesn't seem to work...
                mplew.writeInt(monsterIdFrom);
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] damageMonster(int oid, int damage) {
                return damageMonster(oid, damage, 0, 0);
        }
        
        public static byte[] healMonster(int oid, int heal, int curhp, int maxhp) {
                return damageMonster(oid, -heal, curhp, maxhp);
        }
        
        private static byte[] damageMonster(int oid, int damage, int curhp, int maxhp) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
                mplew.writeInt(oid);
                mplew.write(0);
                mplew.writeInt(damage);
                mplew.writeInt(curhp);
                mplew.writeInt(maxhp);
                return mplew.getPacket();
        }
        
        public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
                mplew.write(7);
                mplew.write(buddylist.size());
                for (BuddylistEntry buddy : buddylist) {
                        if (buddy.isVisible()) {
                                mplew.writeInt(buddy.getCharacterId()); // cid
                                mplew.writeAsciiString(getRightPaddedStr(buddy.getName(), '\0', 13));
                                mplew.write(0); // opposite status
                                mplew.writeInt(buddy.getChannel() - 1);
                                mplew.writeAsciiString(getRightPaddedStr(buddy.getGroup(), '\0', 13));
                                mplew.writeInt(0);//mapid?
                        }
                }
                for (int x = 0; x < buddylist.size(); x++) {
                        mplew.writeInt(0);//mapid?
                }
                return mplew.getPacket();
        }

        public static byte[] buddylistMessage(byte message) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
                mplew.write(message);
                return mplew.getPacket();
        }

        public static byte[] requestBuddylistAdd(int cidFrom, int cid, String nameFrom) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
                mplew.write(9);
                mplew.writeInt(cidFrom);
                mplew.writeMapleAsciiString(nameFrom);
                mplew.writeInt(cidFrom);
                mplew.writeAsciiString(getRightPaddedStr(nameFrom, '\0', 11));
                mplew.write(0x09);
                mplew.write(0xf0);
                mplew.write(0x01);
                mplew.writeInt(0x0f);
                mplew.writeNullTerminatedAsciiString("Default Group");
                mplew.writeInt(cid);
                return mplew.getPacket();
        }

        public static byte[] updateBuddyChannel(int characterid, int channel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
                mplew.write(0x14);
                mplew.writeInt(characterid);
                mplew.write(0);
                mplew.writeInt(channel);
                return mplew.getPacket();
        }

        public static byte[] itemEffect(int characterid, int itemid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
                mplew.writeInt(characterid);
                mplew.writeInt(itemid);
                return mplew.getPacket();
        }

        public static byte[] updateBuddyCapacity(int capacity) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BUDDYLIST.getValue());
                mplew.write(0x15);
                mplew.write(capacity);
                return mplew.getPacket();
        }

        public static byte[] showChair(int characterid, int itemid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_CHAIR.getValue());
                mplew.writeInt(characterid);
                mplew.writeInt(itemid);
                return mplew.getPacket();
        }

        public static byte[] cancelChair(int id) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_CHAIR.getValue());
                if (id < 0) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                        mplew.writeShort(id);
                }
                return mplew.getPacket();
        }

        // is there a way to spawn reactors non-animated?
        public static byte[] spawnReactor(MapleReactor reactor) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                Point pos = reactor.getPosition();
                mplew.writeShort(SendOpcode.REACTOR_SPAWN.getValue());
                mplew.writeInt(reactor.getObjectId());
                mplew.writeInt(reactor.getId());
                mplew.write(reactor.getState());
                mplew.writePos(pos);
                mplew.write(0);
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        // is there a way to trigger reactors without performing the hit animation?
        public static byte[] triggerReactor(MapleReactor reactor, int stance) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                Point pos = reactor.getPosition();
                mplew.writeShort(SendOpcode.REACTOR_HIT.getValue());
                mplew.writeInt(reactor.getObjectId());
                mplew.write(reactor.getState());
                mplew.writePos(pos);
                mplew.write(stance);
                mplew.writeShort(0);
                mplew.write(5); // frame delay, set to 5 since there doesn't appear to be a fixed formula for it
                return mplew.getPacket();
        }

        public static byte[] destroyReactor(MapleReactor reactor) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                Point pos = reactor.getPosition();
                mplew.writeShort(SendOpcode.REACTOR_DESTROY.getValue());
                mplew.writeInt(reactor.getObjectId());
                mplew.write(reactor.getState());
                mplew.writePos(pos);
                return mplew.getPacket();
        }

        public static byte[] musicChange(String song) {
                return environmentChange(song, 6);
        }

        public static byte[] showEffect(String effect) {
                return environmentChange(effect, 3);
        }

        public static byte[] playSound(String sound) {
                return environmentChange(sound, 4);
        }

        public static byte[] environmentChange(String env, int mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(mode);
                mplew.writeMapleAsciiString(env);
                return mplew.getPacket();
        }
        
        public static byte[] environmentMove(String env, int mode) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

                mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF.getValue());
                mplew.writeMapleAsciiString(env);
                mplew.writeInt(mode);   // 0: stop and back to start, 1: move
                
                return mplew.getPacket();
        }
        
        public static byte[] environmentMoveList(Set<Entry<String, Integer>> envList) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF_LIST.getValue());
                mplew.writeInt(envList.size());
                
                for(Entry<String, Integer> envMove : envList) {
                        mplew.writeMapleAsciiString(envMove.getKey());
                        mplew.writeInt(envMove.getValue());
                }
                
                return mplew.getPacket();
        }
        
        public static byte[] environmentMoveReset() {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_OBSTACLE_ALL_RESET.getValue());
                return mplew.getPacket();
        }
        
        public static byte[] startMapEffect(String msg, int itemid, boolean active) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
                mplew.write(active ? 0 : 1);
                mplew.writeInt(itemid);
                if (active) {
                        mplew.writeMapleAsciiString(msg);
                }
                return mplew.getPacket();
        }

        public static byte[] removeMapEffect() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue());
                mplew.write(0);
                mplew.writeInt(0);
                return mplew.getPacket();
        }
        
        public static byte[] mapEffect(String path) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(3);
                mplew.writeMapleAsciiString(path);
                return mplew.getPacket();
        }

        public static byte[] mapSound(String path) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(4);
                mplew.writeMapleAsciiString(path);
                return mplew.getPacket();
        }

        public static byte[] showGuildInfo(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x1A); //signature for showing guild info
                if (chr == null) { //show empty guild (used for leaving, expelled)
                        mplew.write(0);
                        return mplew.getPacket();
                }
                MapleGuild g = chr.getClient().getWorldServer().getGuild(chr.getMGC());
                if (g == null) { //failed to read from DB - don't show a guild
                        mplew.write(0);
                        return mplew.getPacket();
                }
                mplew.write(1); //bInGuild
                mplew.writeInt(g.getId());
                mplew.writeMapleAsciiString(g.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(g.getRankTitle(i));
                }
                Collection<MapleGuildCharacter> members = g.getMembers();
                mplew.write(members.size()); //then it is the size of all the members
                for (MapleGuildCharacter mgc : members) {//and each of their character ids o_O
                        mplew.writeInt(mgc.getId());
                }
                for (MapleGuildCharacter mgc : members) {
                        mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
                        mplew.writeInt(mgc.getJobId());
                        mplew.writeInt(mgc.getLevel());
                        mplew.writeInt(mgc.getGuildRank());
                        mplew.writeInt(mgc.isOnline() ? 1 : 0);
                        mplew.writeInt(g.getSignature());
                        mplew.writeInt(mgc.getAllianceRank());
                }
                mplew.writeInt(g.getCapacity());
                mplew.writeShort(g.getLogoBG());
                mplew.write(g.getLogoBGColor());
                mplew.writeShort(g.getLogo());
                mplew.write(g.getLogoColor());
                mplew.writeMapleAsciiString(g.getNotice());
                mplew.writeInt(g.getGP());
                mplew.writeInt(g.getAllianceId());
                return mplew.getPacket();
        }

        public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x3d);
                mplew.writeInt(gid);
                mplew.writeInt(cid);
                mplew.write(bOnline ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] guildInvite(int gid, String charName) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x05);
                mplew.writeInt(gid);
                mplew.writeMapleAsciiString(charName);
                return mplew.getPacket();
        }
        
        public static byte[] createGuildMessage(String masterName, String guildName) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x3);
                mplew.writeInt(0);
                mplew.writeMapleAsciiString(masterName);
                mplew.writeMapleAsciiString(guildName);
                return mplew.getPacket();
        }
        
        /**
         * Gets a Heracle/guild message packet.
         *
         * Possible values for <code>code</code>:<br> 28: guild name already in use<br>
         * 31: problem in locating players during agreement<br> 33/40: already joined a guild<br>
         * 35: Cannot make guild<br> 36: problem in player agreement<br> 38: problem during forming guild<br> 
         * 41: max number of players in joining guild<br> 42: character can't be found this channel<br> 
         * 45/48: character not in guild<br> 52: problem in disbanding guild<br> 56: admin cannot make guild<br>
         * 57: problem in increasing guild size<br> 
         * 
         *
         * @param code The response code.
         * @return The guild message packet.
         */
        public static byte[] genericGuildMessage(byte code) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(code);
                return mplew.getPacket();
        }
        
        /**
         * Gets a guild message packet appended with target name.
         * 
         * 53: player not accepting guild invites<br>
         * 54: player already managing an invite<br> 55: player denied an invite<br> 
         * 
         * @param code The response code.
         * @param targetName The initial player target of the invitation.
         * @return The guild message packet.
         */
        public static byte[] responseGuildMessage(byte code, String targetName) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(code);
                mplew.writeMapleAsciiString(targetName);
                return mplew.getPacket();
        }

        public static byte[] newGuildMember(MapleGuildCharacter mgc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x27);
                mplew.writeInt(mgc.getGuildId());
                mplew.writeInt(mgc.getId());
                mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
                mplew.writeInt(mgc.getJobId());
                mplew.writeInt(mgc.getLevel());
                mplew.writeInt(mgc.getGuildRank()); //should be always 5 but whatevs
                mplew.writeInt(mgc.isOnline() ? 1 : 0); //should always be 1 too
                mplew.writeInt(1); //? could be guild signature, but doesn't seem to matter
                mplew.writeInt(3);
                return mplew.getPacket();
        }

        //someone leaving, mode == 0x2c for leaving, 0x2f for expelled
        public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(bExpelled ? 0x2f : 0x2c);
                mplew.writeInt(mgc.getGuildId());
                mplew.writeInt(mgc.getId());
                mplew.writeMapleAsciiString(mgc.getName());
                return mplew.getPacket();
        }

        //rank change
        public static byte[] changeRank(MapleGuildCharacter mgc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x40);
                mplew.writeInt(mgc.getGuildId());
                mplew.writeInt(mgc.getId());
                mplew.write(mgc.getGuildRank());
                return mplew.getPacket();
        }

        public static byte[] guildNotice(int gid, String notice) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x44);
                mplew.writeInt(gid);
                mplew.writeMapleAsciiString(notice);
                return mplew.getPacket();
        }

        public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x3C);
                mplew.writeInt(mgc.getGuildId());
                mplew.writeInt(mgc.getId());
                mplew.writeInt(mgc.getLevel());
                mplew.writeInt(mgc.getJobId());
                return mplew.getPacket();
        }

        public static byte[] rankTitleChange(int gid, String[] ranks) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x3E);
                mplew.writeInt(gid);
                for (int i = 0; i < 5; i++) {
                        mplew.writeMapleAsciiString(ranks[i]);
                }
                return mplew.getPacket();
        }

        public static byte[] guildDisband(int gid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x32);
                mplew.writeInt(gid);
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] guildQuestWaitingNotice(byte channel, int waitingPos) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x4C);
                mplew.write(channel - 1);
                mplew.write(waitingPos);
                return mplew.getPacket();
        }

        public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x42);
                mplew.writeInt(gid);
                mplew.writeShort(bg);
                mplew.write(bgcolor);
                mplew.writeShort(logo);
                mplew.write(logocolor);
                return mplew.getPacket();
        }

        public static byte[] guildCapacityChange(int gid, int capacity) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x3A);
                mplew.writeInt(gid);
                mplew.write(capacity);
                return mplew.getPacket();
        }

        public static void addThread(final MaplePacketLittleEndianWriter mplew, ResultSet rs) throws SQLException {
                mplew.writeInt(rs.getInt("localthreadid"));
                mplew.writeInt(rs.getInt("postercid"));
                mplew.writeMapleAsciiString(rs.getString("name"));
                mplew.writeLong(getTime(rs.getLong("timestamp")));
                mplew.writeInt(rs.getInt("icon"));
                mplew.writeInt(rs.getInt("replycount"));
        }

        public static byte[] BBSThreadList(ResultSet rs, int start) throws SQLException {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
                mplew.write(0x06);
                if (!rs.last()) {
                        mplew.write(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        return mplew.getPacket();
                }
                int threadCount = rs.getRow();
                if (rs.getInt("localthreadid") == 0) { //has a notice
                        mplew.write(1);
                        addThread(mplew, rs);
                        threadCount--; //one thread didn't count (because it's a notice)
                } else {
                        mplew.write(0);
                }
                if (!rs.absolute(start + 1)) { //seek to the thread before where we start
                        rs.first(); //uh, we're trying to start at a place past possible
                        start = 0;
                }
                mplew.writeInt(threadCount);
                mplew.writeInt(Math.min(10, threadCount - start));
                for (int i = 0; i < Math.min(10, threadCount - start); i++) {
                        addThread(mplew, rs);
                        rs.next();
                }
                return mplew.getPacket();
        }

        public static byte[] showThread(int localthreadid, ResultSet threadRS, ResultSet repliesRS) throws SQLException, RuntimeException {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.getValue());
                mplew.write(0x07);
                mplew.writeInt(localthreadid);
                mplew.writeInt(threadRS.getInt("postercid"));
                mplew.writeLong(getTime(threadRS.getLong("timestamp")));
                mplew.writeMapleAsciiString(threadRS.getString("name"));
                mplew.writeMapleAsciiString(threadRS.getString("startpost"));
                mplew.writeInt(threadRS.getInt("icon"));
                if (repliesRS != null) {
                        int replyCount = threadRS.getInt("replycount");
                        mplew.writeInt(replyCount);
                        int i;
                        for (i = 0; i < replyCount && repliesRS.next(); i++) {
                                mplew.writeInt(repliesRS.getInt("replyid"));
                                mplew.writeInt(repliesRS.getInt("postercid"));
                                mplew.writeLong(getTime(repliesRS.getLong("timestamp")));
                                mplew.writeMapleAsciiString(repliesRS.getString("content"));
                        }
                        if (i != replyCount || repliesRS.next()) {
                                throw new RuntimeException(String.valueOf(threadRS.getInt("threadid")));
                        }
                } else {
                        mplew.writeInt(0);
                }
                return mplew.getPacket();
        }

        public static byte[] showGuildRanks(int npcid, ResultSet rs) throws SQLException {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x49);
                mplew.writeInt(npcid);
                if (!rs.last()) { //no guilds o.o
                        mplew.writeInt(0);
                        return mplew.getPacket();
                }
                mplew.writeInt(rs.getRow()); //number of entries
                rs.beforeFirst();
                while (rs.next()) {
                        mplew.writeMapleAsciiString(rs.getString("name"));
                        mplew.writeInt(rs.getInt("GP"));
                        mplew.writeInt(rs.getInt("logo"));
                        mplew.writeInt(rs.getInt("logoColor"));
                        mplew.writeInt(rs.getInt("logoBG"));
                        mplew.writeInt(rs.getInt("logoBGColor"));
                }
                return mplew.getPacket();
        }

        public static byte[] showPlayerRanks(int npcid, List<Pair<String, Integer>> worldRanking) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x49);
                mplew.writeInt(npcid);
                if (worldRanking.isEmpty()) { 
                        mplew.writeInt(0);
                        return mplew.getPacket();
                }
                mplew.writeInt(worldRanking.size());
                for (Pair<String, Integer> wr : worldRanking) {
                        mplew.writeMapleAsciiString(wr.getLeft());
                        mplew.writeInt(wr.getRight());
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                        mplew.writeInt(0);
                }
                return mplew.getPacket();
        }

        public static byte[] updateGP(int gid, int GP) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_OPERATION.getValue());
                mplew.write(0x48);
                mplew.writeInt(gid);
                mplew.writeInt(GP);
                return mplew.getPacket();
        }

        public static byte[] skillEffect(MapleCharacter from, int skillId, int level, byte flags, int speed, byte direction) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SKILL_EFFECT.getValue());
                mplew.writeInt(from.getId());
                mplew.writeInt(skillId);
                mplew.write(level);
                mplew.write(flags);
                mplew.write(speed);
                mplew.write(direction); //Mmmk
                return mplew.getPacket();
        }

        public static byte[] skillCancel(MapleCharacter from, int skillId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
                mplew.writeInt(from.getId());
                mplew.writeInt(skillId);
                return mplew.getPacket();
        }
        
        public static byte[] catchMonster(int mobOid, byte success) {   // updated packet structure found thanks to Rien dev team
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CATCH_MONSTER.getValue());
                mplew.writeInt(mobOid);
                mplew.write(success);
                return mplew.getPacket();
        }
        
        public static byte[] catchMonster(int mobOid, int itemid, byte success) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CATCH_MONSTER_WITH_ITEM.getValue());
                mplew.writeInt(mobOid);
                mplew.writeInt(itemid);
                mplew.write(success);
                return mplew.getPacket();
        }

        /**
         * Sends a player hint.
         *
         * @param hint The hint it's going to send.
         * @param width How tall the box is going to be.
         * @param height How long the box is going to be.
         * @return The player hint packet.
         */
        public static byte[] sendHint(String hint, int width, int height) {
                if (width < 1) {
                        width = hint.length() * 10;
                        if (width < 40) {
                                width = 40;
                        }
                }
                if (height < 5) {
                        height = 5;
                }
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_HINT.getValue());
                mplew.writeMapleAsciiString(hint);
                mplew.writeShort(width);
                mplew.writeShort(height);
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] messengerInvite(String from, int messengerid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x03);
                mplew.writeMapleAsciiString(from);
                mplew.write(0);
                mplew.writeInt(messengerid);
                mplew.write(0);
                return mplew.getPacket();
        }

        /*
        public static byte[] sendSpouseChat(MapleCharacter partner, String msg) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
                mplew.writeMapleAsciiString(partner.getName());
                mplew.writeMapleAsciiString(msg);
                return mplew.getPacket();
        }
        */
        
        public static byte[] OnCoupleMessage(String fiance, String text, boolean spouse) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
            mplew.write(spouse ? 5 : 4); // v2 = CInPacket::Decode1(a1) - 4;
            if (spouse) { // if ( v2 ) {
                mplew.writeMapleAsciiString(fiance);
            }
            mplew.write(spouse ? 5 : 1);
            mplew.writeMapleAsciiString(text);
            return mplew.getPacket();
        }  

        public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x00);
                mplew.write(position);
                addCharLook(mplew, chr, true);
                mplew.writeMapleAsciiString(from);
                mplew.write(channel);
                mplew.write(0x00);
                return mplew.getPacket();
        }

        public static byte[] removeMessengerPlayer(int position) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x02);
                mplew.write(position);
                return mplew.getPacket();
        }

        public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x07);
                mplew.write(position);
                addCharLook(mplew, chr, true);
                mplew.writeMapleAsciiString(from);
                mplew.write(channel);
                mplew.write(0x00);
                return mplew.getPacket();
        }

        public static byte[] joinMessenger(int position) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x01);
                mplew.write(position);
                return mplew.getPacket();
        }

        public static byte[] messengerChat(String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(0x06);
                mplew.writeMapleAsciiString(text);
                return mplew.getPacket();
        }

        public static byte[] messengerNote(String text, int mode, int mode2) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MESSENGER.getValue());
                mplew.write(mode);
                mplew.writeMapleAsciiString(text);
                mplew.write(mode2);
                return mplew.getPacket();
        }

        private static void addPetInfo(final MaplePacketLittleEndianWriter mplew, MaplePet pet, boolean showpet) {
                mplew.write(1);
                if (showpet) {
                        mplew.write(0);
                }

                mplew.writeInt(pet.getItemId());
                mplew.writeMapleAsciiString(pet.getName());
                mplew.writeLong(pet.getUniqueId());
                mplew.writePos(pet.getPos());
                mplew.write(pet.getStance());
                mplew.writeInt(pet.getFh());
        }

        public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_PET.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(chr.getPetIndex(pet));
                if (remove) {
                        mplew.write(0);
                        mplew.write(hunger ? 1 : 0);
                } else {
                        addPetInfo(mplew, pet, true);
                }
                return mplew.getPacket();
        }

        public static byte[] movePet(int cid, int pid, byte slot, List<LifeMovementFragment> moves) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MOVE_PET.getValue());
                mplew.writeInt(cid);
                mplew.write(slot);
                mplew.writeInt(pid);
                serializeMovementList(mplew, moves);
                return mplew.getPacket();
        }

        public static byte[] petChat(int cid, byte index, int act, String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PET_CHAT.getValue());
                mplew.writeInt(cid);
                mplew.write(index);
                mplew.write(0);
                mplew.write(act);
                mplew.writeMapleAsciiString(text);
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] petFoodResponse(int cid, byte index, boolean success, boolean balloonType) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
                mplew.writeInt(cid);
                mplew.write(index);
                mplew.write(1);
                mplew.writeBool(success);
                mplew.writeBool(balloonType);
                return mplew.getPacket();
        }
        
        public static byte[] commandResponse(int cid, byte index, boolean talk, int animation, boolean balloonType) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PET_COMMAND.getValue());
                mplew.writeInt(cid);
                mplew.write(index);
                mplew.write(0);
                mplew.write(animation);
                mplew.writeBool(!talk);
                mplew.writeBool(balloonType);
                return mplew.getPacket();
        }

        public static byte[] showOwnPetLevelUp(byte index) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(4);
                mplew.write(0);
                mplew.write(index); // Pet Index
                return mplew.getPacket();
        }

        public static byte[] showPetLevelUp(MapleCharacter chr, byte index) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(4);
                mplew.write(0);
                mplew.write(index);
                return mplew.getPacket();
        }

        public static byte[] changePetName(MapleCharacter chr, String newname, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PET_NAMECHANGE.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(0);
                mplew.writeMapleAsciiString(newname);
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static final byte[] loadExceptionList(final int cid, final int petId, final byte petIdx, final List<Integer> data) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PET_EXCEPTION_LIST.getValue());
                mplew.writeInt(cid);
                mplew.write(petIdx);
                mplew.writeLong(petId);
                mplew.write(data.size());
                for (final Integer ids : data) {
                        mplew.writeInt(ids);
                }
                return mplew.getPacket();
        }

        public static byte[] petStatUpdate(MapleCharacter chr) {
                // this actually does nothing... packet structure and stats needs to be uncovered
            
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.STAT_CHANGED.getValue());
                int mask = 0;
                mask |= MapleStat.PET.getValue();
                mplew.write(0);
                mplew.writeInt(mask);
                MaplePet[] pets = chr.getPets();
                for (int i = 0; i < 3; i++) {
                        if (pets[i] != null) {
                                mplew.writeLong(pets[i].getUniqueId());
                        } else {
                                mplew.writeLong(0);
                        }
                }
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] showForcedEquip(int team) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FORCED_MAP_EQUIP.getValue());
                if (team > -1) {
                        mplew.write(team);   // 00 = red, 01 = blue
                }
                return mplew.getPacket();
        }

        public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SUMMON_SKILL.getValue());
                mplew.writeInt(cid);
                mplew.writeInt(summonSkillId);
                mplew.write(newStance);
                return mplew.getPacket();
        }

        public static byte[] skillCooldown(int sid, int time) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.COOLDOWN.getValue());
                mplew.writeInt(sid);
                mplew.writeShort(time);//Int in v97
                return mplew.getPacket();
        }

        public static byte[] skillBookResult(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SKILL_LEARN_ITEM_RESULT.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(1);
                mplew.writeInt(skillid);
                mplew.writeInt(maxlevel);
                mplew.write(canuse ? 1 : 0);
                mplew.write(success ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] getMacros(SkillMacro[] macros) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MACRO_SYS_DATA_INIT.getValue());
                int count = 0;
                for (int i = 0; i < 5; i++) {
                        if (macros[i] != null) {
                                count++;
                        }
                }
                mplew.write(count);
                for (int i = 0; i < 5; i++) {
                        SkillMacro macro = macros[i];
                        if (macro != null) {
                                mplew.writeMapleAsciiString(macro.getName());
                                mplew.write(macro.getShout());
                                mplew.writeInt(macro.getSkill1());
                                mplew.writeInt(macro.getSkill2());
                                mplew.writeInt(macro.getSkill3());
                        }
                }
                return mplew.getPacket();
        }

        public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars, boolean usePic) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
                mplew.write(0);
                mplew.write(worldid);
                mplew.write(chars.size());
                for (MapleCharacter chr : chars) {
                        addCharEntry(mplew, chr, true);
                }
                mplew.write(usePic ? 1 : 2);
                return mplew.getPacket();
        }

        public static byte[] updateMount(int charid, MapleMount mount, boolean levelup) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_TAMING_MOB_INFO.getValue());
                mplew.writeInt(charid);
                mplew.writeInt(mount.getLevel());
                mplew.writeInt(mount.getExp());
                mplew.writeInt(mount.getTiredness());
                mplew.write(levelup ? (byte) 1 : (byte) 0);
                return mplew.getPacket();
        }

        public static byte[] crogBoatPacket(boolean type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CONTI_MOVE.getValue());
                mplew.write(10);
                mplew.write(type ? 4 : 5);
                return mplew.getPacket();
        }

        public static byte[] boatPacket(boolean type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CONTI_STATE.getValue());
                mplew.write(type ? 1 : 2);
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(1);
                mplew.write(0);
                mplew.write(owner ? 0 : 1);
                mplew.write(0);
                addCharLook(mplew, minigame.getOwner(), false);
                mplew.writeMapleAsciiString(minigame.getOwner().getName());
                if (minigame.getVisitor() != null) {
                        MapleCharacter visitor = minigame.getVisitor();
                        mplew.write(1);
                        addCharLook(mplew, visitor, false);
                        mplew.writeMapleAsciiString(visitor.getName());
                }
                mplew.write(0xFF);
                mplew.write(0);
                mplew.writeInt(1);
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, true));
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, true));
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, true));
                mplew.writeInt(minigame.getOwnerScore());
                if (minigame.getVisitor() != null) {
                        MapleCharacter visitor = minigame.getVisitor();
                        mplew.write(1);
                        mplew.writeInt(1);
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, true));
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, true));
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, true));
                        mplew.writeInt(minigame.getVisitorScore());
                }
                mplew.write(0xFF);
                mplew.writeMapleAsciiString(minigame.getDescription());
                mplew.write(piece);
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] getMiniGameReady(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.READY.getCode());
                return mplew.getPacket();
        }

        public static byte[] getMiniGameUnReady(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.UN_READY.getCode());
                return mplew.getPacket();
        }

        public static byte[] getMiniGameStart(MapleMiniGame game, int loser) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.START.getCode());
                mplew.write(loser);
                return mplew.getPacket();
        }

        public static byte[] getMiniGameSkipOwner(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.SKIP.getCode());
                mplew.write(0x01);
                return mplew.getPacket();
        }

        public static byte[] getMiniGameRequestTie(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.REQUEST_TIE.getCode());
                return mplew.getPacket();
        }

        public static byte[] getMiniGameDenyTie(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ANSWER_TIE.getCode());
                return mplew.getPacket();
        }

        /**
         * 1 = Room already closed  2 = Can't enter due full cappacity 3 = Other requests at this minute
         * 4 = Can't do while dead 5 = Can't do while middle event 6 = This character unable to do it
         * 7, 20 = Not allowed to trade anymore 9 = Can only trade on same map 10 = May not open store near portal
         * 11, 14 = Can't start game here 12 = Can't open store at this channel 13 = Can't estabilish miniroom
         * 15 = Stores only an the free market 16 = Lists the rooms at FM (?) 17 = You may not enter this store
         * 18 = Owner undergoing store maintenance 19 = Unable to enter tournament room 21 = Not enough mesos to enter
         * 22 = Incorrect password
         *
         * @param status
         * @return
         */
        public static byte[] getMiniRoomError(int status) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(0);
                mplew.write(status);
                return mplew.getPacket();
        }
        
        public static byte[] getMiniGameSkipVisitor(MapleMiniGame game) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.writeShort(PlayerInteractionHandler.Action.SKIP.getCode());
                return mplew.getPacket();
        }

        public static byte[] getMiniGameMoveOmok(MapleMiniGame game, int move1, int move2, int move3) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(12);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.MOVE_OMOK.getCode());
                mplew.writeInt(move1);
                mplew.writeInt(move2);
                mplew.write(move3);
                return mplew.getPacket();
        }

        public static byte[] getMiniGameNewVisitor(MapleMiniGame minigame, MapleCharacter chr, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
                mplew.write(slot);
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                mplew.writeInt(1);
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.WIN, true));
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.TIE, true));
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.LOSS, true));
                mplew.writeInt(minigame.getVisitorScore());
                return mplew.getPacket();
        }

        public static byte[] getMiniGameRemoveVisitor() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                mplew.write(1);
                return mplew.getPacket();
        }
        
        private static byte[] getMiniGameResult(MapleMiniGame game, int tie, int result, int forfeit) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.GET_RESULT.getCode());
                
                int matchResultType;
                if (tie == 0 && forfeit != 1) {
                        matchResultType = 0;
                } else if (tie != 0) {
                        matchResultType = 1;
                } else {
                        matchResultType = 2;
                }
                
                mplew.write(matchResultType);
                mplew.writeBool(result == 2); // host/visitor wins
                
                boolean omok = game.isOmok();
                if (matchResultType == 1) {
                        mplew.write(0);
                        mplew.writeShort(0);
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
                        mplew.writeInt(game.getOwnerScore()); // points

                        mplew.writeInt(0); // unknown
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
                        mplew.writeInt(game.getVisitorScore()); // points
                        mplew.write(0);
                } else {
                        mplew.writeInt(0);
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
                        mplew.writeInt(game.getOwner().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
                        mplew.writeInt(game.getOwnerScore()); // points
                        mplew.writeInt(0);
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.WIN, omok)); // wins
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.TIE, omok)); // ties
                        mplew.writeInt(game.getVisitor().getMiniGamePoints(MiniGameResult.LOSS, omok)); // losses
                        mplew.writeInt(game.getVisitorScore()); // points
                }
                
                return mplew.getPacket();
        }

        public static byte[] getMiniGameOwnerWin(MapleMiniGame game, boolean forfeit) {
                return getMiniGameResult(game, 0, 1, forfeit ? 1 : 0);
        }

        public static byte[] getMiniGameVisitorWin(MapleMiniGame game, boolean forfeit) {
                return getMiniGameResult(game, 0, 2, forfeit ? 1 : 0);
        }

        public static byte[] getMiniGameTie(MapleMiniGame game) {
                return getMiniGameResult(game, 1, 3, 0);
        }
        
        public static byte[] getMiniGameClose(boolean visitor, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                mplew.writeBool(visitor);
                mplew.write(type); /* 2 : CRASH 3 : The room has been closed 4 : You have left the room 5 : You have been expelled  */
                return mplew.getPacket();
        }

        public static byte[] getMatchCard(MapleClient c, MapleMiniGame minigame, boolean owner, int piece) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(2);
                mplew.write(2);
                mplew.write(owner ? 0 : 1);
                mplew.write(0);
                addCharLook(mplew, minigame.getOwner(), false);
                mplew.writeMapleAsciiString(minigame.getOwner().getName());
                if (minigame.getVisitor() != null) {
                        MapleCharacter visitor = minigame.getVisitor();
                        mplew.write(1);
                        addCharLook(mplew, visitor, false);
                        mplew.writeMapleAsciiString(visitor.getName());
                }
                mplew.write(0xFF);
                mplew.write(0);
                mplew.writeInt(2);
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.WIN, false));
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.TIE, false));
                mplew.writeInt(minigame.getOwner().getMiniGamePoints(MiniGameResult.LOSS, false));
                
                //set vs
                mplew.writeInt(minigame.getOwnerScore());
                if (minigame.getVisitor() != null) {
                        MapleCharacter visitor = minigame.getVisitor();
                        mplew.write(1);
                        mplew.writeInt(2);
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.WIN, false));
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.TIE, false));
                        mplew.writeInt(visitor.getMiniGamePoints(MiniGameResult.LOSS, false));
                        mplew.writeInt(minigame.getVisitorScore());
                }
                mplew.write(0xFF);
                mplew.writeMapleAsciiString(minigame.getDescription());
                mplew.write(piece);
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.START.getCode());
                mplew.write(loser);
                
                int last;
                if (game.getMatchesToWin() > 10) {
                        last = 30;
                } else if (game.getMatchesToWin() > 6) {
                        last = 20;
                } else {
                        last = 12;
                }
                
                mplew.write(last);
                for (int i = 0; i < last; i++) {
                        mplew.writeInt(game.getCardId(i));
                }
                return mplew.getPacket();
        }

        public static byte[] getMatchCardNewVisitor(MapleMiniGame minigame, MapleCharacter chr, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
                mplew.write(slot);
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                mplew.writeInt(1);
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.WIN, false));
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.TIE, false));
                mplew.writeInt(chr.getMiniGamePoints(MiniGameResult.LOSS, false));
                mplew.writeInt(minigame.getVisitorScore());
                return mplew.getPacket();
        }

        public static byte[] getMatchCardSelect(MapleMiniGame game, int turn, int slot, int firstslot, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.SELECT_CARD.getCode());
                mplew.write(turn);
                if (turn == 1) {
                        mplew.write(slot);
                } else if (turn == 0) {
                        mplew.write(slot);
                        mplew.write(firstslot);
                        mplew.write(type);
                }
                return mplew.getPacket();
        }
        
        // RPS_GAME packets thanks to Arnah (Vertisy)
        public static byte[] openRPSNPC() {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RPS_GAME.getValue());
                mplew.write(8);// open npc
                mplew.writeInt(9000019);
                return mplew.getPacket();
        }

        public static byte[] rpsMesoError(int mesos) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RPS_GAME.getValue());
                mplew.write(0x06);
                if (mesos != -1) {
                        mplew.writeInt(mesos);
                }
                return mplew.getPacket();
        }

        public static byte[] rpsSelection(byte selection, byte answer) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RPS_GAME.getValue());
                mplew.write(0x0B);// 11l
                mplew.write(selection);
                mplew.write(answer);
                return mplew.getPacket();
        }

        public static byte[] rpsMode(byte mode) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.RPS_GAME.getValue());
                mplew.write(mode);
                return mplew.getPacket();
        }

        public static byte[] fredrickMessage(byte operation) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.getValue());
                mplew.write(operation);
                return mplew.getPacket();
        }

        public static byte[] getFredrick(byte op) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FREDRICK.getValue());
                mplew.write(op);

                switch (op) {
                case 0x24:
                        mplew.skip(8);
                        break;
                default:
                        mplew.write(0);
                        break;
                }

                return mplew.getPacket();
        }

        public static byte[] getFredrick(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FREDRICK.getValue());
                mplew.write(0x23);
                mplew.writeInt(9030000); // Fredrick
                mplew.writeInt(32272); //id
                mplew.skip(5);
                mplew.writeInt(chr.getMerchantNetMeso());
                mplew.write(0);
                try {
                        List<Pair<Item, MapleInventoryType>> items = ItemFactory.MERCHANT.loadItems(chr.getId(), false);
                        mplew.write(items.size());

                        for (int i = 0; i < items.size(); i++) {
                                addItemInfo(mplew, items.get(i).getLeft(), true);
                        }
                } catch (SQLException e) {
                        e.printStackTrace();
                }
                mplew.skip(3);
                return mplew.getPacket();
        }
        
        public static byte[] addOmokBox(MapleCharacter chr, int ammount, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
                mplew.writeInt(chr.getId());
                addAnnounceBox(mplew, chr.getMiniGame(), ammount, type);
                return mplew.getPacket();
        }
        
        public static byte[] addMatchCardBox(MapleCharacter chr, int ammount, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
                mplew.writeInt(chr.getId());
                addAnnounceBox(mplew, chr.getMiniGame(), ammount, type);
                return mplew.getPacket();
        }
        
        public static byte[] removeMinigameBox(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
                mplew.writeInt(chr.getId());
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] getPlayerShopChat(MapleCharacter chr, String chat, byte slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
                mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
                mplew.write(slot);
                mplew.writeMapleAsciiString(chr.getName() + " : " + chat);
                return mplew.getPacket();
        }

        public static byte[] getTradeChat(MapleCharacter chr, String chat, boolean owner) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
                mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
                mplew.write(owner ? 0 : 1);
                mplew.writeMapleAsciiString(chr.getName() + " : " + chat);
                return mplew.getPacket();
        }

        public static byte[] hiredMerchantBox() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
                mplew.write(0x07);
                return mplew.getPacket();
        }

        // 0: Success
        // 1: The room is already closed.
        // 2: You can't enter the room due to full capacity.
        // 3: Other requests are being fulfilled this minute.
        // 4: You can't do it while you're dead.
        // 7: You are not allowed to trade other items at this point.
        // 17: You may not enter this store.
        // 18: The owner of the store is currently undergoing store maintenance. Please try again in a bit.
        // 23: This can only be used inside the Free Market.
        // default: This character is unable to do it.	
        public static byte[] getOwlMessage(int msg) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

                mplew.writeShort(SendOpcode.SHOP_LINK_RESULT.getValue());
                mplew.write(msg); // depending on the byte sent, a different message is sent.

                return mplew.getPacket();
        }
        
        public static byte[] owlOfMinerva(MapleClient c, int itemid, List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable) {
                byte itemType = ItemConstants.getInventoryType(itemid).getType();
                
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue()); // header.
                mplew.write(6);
                mplew.writeInt(0);
                mplew.writeInt(itemid);
                mplew.writeInt(hmsAvailable.size());
                for (Pair<MaplePlayerShopItem, AbstractMapleMapObject> hme : hmsAvailable) {
                        MaplePlayerShopItem item = hme.getLeft();
                        AbstractMapleMapObject mo = hme.getRight();
                        
                        if(mo instanceof MaplePlayerShop) {
                            MaplePlayerShop ps = (MaplePlayerShop) mo;
                            MapleCharacter owner = ps.getOwner();
                            
                            mplew.writeMapleAsciiString(owner.getName());
                            mplew.writeInt(owner.getMapId());
                            mplew.writeMapleAsciiString(ps.getDescription());
                            mplew.writeInt(item.getBundles());
                            mplew.writeInt(item.getItem().getQuantity());
                            mplew.writeInt(item.getPrice());
                            mplew.writeInt(owner.getId());
                            mplew.write(owner.getClient().getChannel() - 1);
                        } else {
                            MapleHiredMerchant hm = (MapleHiredMerchant) mo;
                        
                            mplew.writeMapleAsciiString(hm.getOwner());
                            mplew.writeInt(hm.getMapId());
                            mplew.writeMapleAsciiString(hm.getDescription());
                            mplew.writeInt(item.getBundles());
                            mplew.writeInt(item.getItem().getQuantity());
                            mplew.writeInt(item.getPrice());
                            mplew.writeInt(hm.getOwnerId());
                            mplew.write(hm.getChannel() - 1);
                        }
                        
                        mplew.write(itemType);
                        if (itemType == MapleInventoryType.EQUIP.getType()) {
                                addItemInfo(mplew, item.getItem(), true);
                        }
                }
                return mplew.getPacket();
        }

        public static byte[] getOwlOpen(List<Integer> owlLeaderboards) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

                mplew.writeShort(SendOpcode.SHOP_SCANNER_RESULT.getValue());
                mplew.write(7);
                mplew.write(owlLeaderboards.size());
                for (Integer i : owlLeaderboards) {
                        mplew.writeInt(i);
                }

                return mplew.getPacket();
        }
        
        public static byte[] retrieveFirstMessage() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
                mplew.write(0x09);
                return mplew.getPacket();
        }

        public static byte[] remoteChannelChange(byte ch) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue()); // header.
                mplew.write(0x10);
                mplew.writeInt(0);//No idea yet
                mplew.write(ch);
                return mplew.getPacket();
        }
        /*
         * Possible things for ENTRUSTED_SHOP_CHECK_RESULT
         * 0x0E = 00 = Renaming Failed - Can't find the merchant, 01 = Renaming successful
         * 0x10 = Changes channel to the store (Store is open at Channel 1, do you want to change channels?)
         * 0x11 = You cannot sell any items when managing.. blabla
         * 0x12 = FKING POPUP LOL
         */

        public static byte[] getHiredMerchant(MapleCharacter chr, MapleHiredMerchant hm, boolean firstTime) {//Thanks Dustin
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(0x05);
                mplew.write(0x04);
                mplew.writeShort(hm.getVisitorSlotThreadsafe(chr) + 1);
                mplew.writeInt(hm.getItemId());
                mplew.writeMapleAsciiString("Hired Merchant");
                
                MapleCharacter visitors[] = hm.getVisitors();
                for (int i = 0; i < 3; i++) {
                        if (visitors[i] != null) {
                                mplew.write(i + 1);
                                addCharLook(mplew, visitors[i], false);
                                mplew.writeMapleAsciiString(visitors[i].getName());
                        }
                }
                mplew.write(-1);
                if (hm.isOwner(chr)) {
                        List<Pair<String, Byte>> msgList = hm.getMessages();
                    
                        mplew.writeShort(msgList.size());
                        for (int i = 0; i < msgList.size(); i++) {
                                mplew.writeMapleAsciiString(msgList.get(i).getLeft());
                                mplew.write(msgList.get(i).getRight());
                        }
                } else {
                        mplew.writeShort(0);
                }
                mplew.writeMapleAsciiString(hm.getOwner());
                if (hm.isOwner(chr)) {
                        mplew.writeShort(0);
                        mplew.writeShort(hm.getTimeOpen());
                        mplew.write(firstTime ? 1 : 0);
                        List<MapleHiredMerchant.SoldItem> sold = hm.getSold();
                        mplew.write(sold.size());
                        for (MapleHiredMerchant.SoldItem s : sold) {
                                mplew.writeInt(s.getItemId());
                                mplew.writeShort(s.getQuantity());
                                mplew.writeInt(s.getMesos());
                                mplew.writeMapleAsciiString(s.getBuyer());
                        }
                        mplew.writeInt(chr.getMerchantMeso());//:D?
                }
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.write(0x10); //TODO SLOTS, which is 16 for most stores...slotMax
                mplew.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
                mplew.write(hm.getItems().size());
                if (hm.getItems().isEmpty()) {
                        mplew.write(0);//Hmm??
                } else {
                        for (MaplePlayerShopItem item : hm.getItems()) {
                                mplew.writeShort(item.getBundles());
                                mplew.writeShort(item.getItem().getQuantity());
                                mplew.writeInt(item.getPrice());
                                addItemInfo(mplew, item.getItem(), true);
                        }
                }
                return mplew.getPacket();
        }

        public static byte[] updateHiredMerchant(MapleHiredMerchant hm, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.UPDATE_MERCHANT.getCode());
                mplew.writeInt(hm.isOwner(chr) ? chr.getMerchantMeso() : chr.getMeso());
                mplew.write(hm.getItems().size());
                for (MaplePlayerShopItem item : hm.getItems()) {
                        mplew.writeShort(item.getBundles());
                        mplew.writeShort(item.getItem().getQuantity());
                        mplew.writeInt(item.getPrice());
                        addItemInfo(mplew, item.getItem(), true);
                }
                return mplew.getPacket();
        }

        public static byte[] hiredMerchantChat(String message, byte slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.CHAT.getCode());
                mplew.write(PlayerInteractionHandler.Action.CHAT_THING.getCode());
                mplew.write(slot);
                mplew.writeMapleAsciiString(message);
                return mplew.getPacket();
        }

        public static byte[] hiredMerchantVisitorLeave(int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                if (slot != 0) {
                        mplew.write(slot);
                }
                return mplew.getPacket();
        }
        
        public static byte[] hiredMerchantOwnerLeave() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
                mplew.write(0);
                return mplew.getPacket();
        }
        
        public static byte[] hiredMerchantOwnerMaintenanceLeave() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.REAL_CLOSE_MERCHANT.getCode());
                mplew.write(5);
                return mplew.getPacket();
        }

        public static byte[] hiredMerchantMaintenanceMessage() {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.ROOM.getCode());
                mplew.write(0x00);
                mplew.write(0x12);
                return mplew.getPacket();
        }
        
        public static byte[] leaveHiredMerchant(int slot, int status2) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.EXIT.getCode());
                mplew.write(slot);
                mplew.write(status2);
                return mplew.getPacket();
        }

        public static byte[] hiredMerchantVisitorAdd(MapleCharacter chr, int slot) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(PlayerInteractionHandler.Action.VISIT.getCode());
                mplew.write(slot);
                addCharLook(mplew, chr, false);
                mplew.writeMapleAsciiString(chr.getName());
                return mplew.getPacket();
        }

        public static byte[] spawnHiredMerchantBox(MapleHiredMerchant hm) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.getValue());
                mplew.writeInt(hm.getOwnerId());
                mplew.writeInt(hm.getItemId());
                mplew.writeShort((short) hm.getPosition().getX());
                mplew.writeShort((short) hm.getPosition().getY());
                mplew.writeShort(0);
                mplew.writeMapleAsciiString(hm.getOwner());
                mplew.write(0x05);
                mplew.writeInt(hm.getObjectId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.write(hm.getItemId() % 100);
                mplew.write(new byte[]{1, 4});
                return mplew.getPacket();
        }

        public static byte[] removeHiredMerchantBox(int id) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.getValue());
                mplew.writeInt(id);
                return mplew.getPacket();
        }

        public static byte[] spawnPlayerNPC(MaplePlayerNPC npc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
                mplew.write(1);
                mplew.writeInt(npc.getObjectId());
                mplew.writeInt(npc.getScriptId());
                mplew.writeShort(npc.getPosition().x);
                mplew.writeShort(npc.getCY());
                mplew.write(npc.getDirection());
                mplew.writeShort(npc.getFH());
                mplew.writeShort(npc.getRX0());
                mplew.writeShort(npc.getRX1());
                mplew.write(1);
                return mplew.getPacket();
        }
        
        public static byte[] getPlayerNPC(MaplePlayerNPC npc) {     // thanks to Arnah
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
                mplew.write(0x01);
                mplew.writeInt(npc.getScriptId());
                mplew.writeMapleAsciiString(npc.getName());
                mplew.write(npc.getGender());
                mplew.write(npc.getSkin());
                mplew.writeInt(npc.getFace());
                mplew.write(0);
                mplew.writeInt(npc.getHair());
                Map<Short, Integer> equip = npc.getEquips();
		Map<Short, Integer> myEquip = new LinkedHashMap<>();
		Map<Short, Integer> maskedEquip = new LinkedHashMap<>();
		for(short position : equip.keySet()) {
			short pos = (byte) (position * -1);
			if(pos < 100 && myEquip.get(pos) == null) {
				myEquip.put(pos, equip.get(position));
			} else if((pos > 100 && pos != 111) || pos == -128) { // don't ask. o.o
				pos -= 100;
				if(myEquip.get(pos) != null) {
					maskedEquip.put(pos, myEquip.get(pos));
				}
				myEquip.put(pos, equip.get(position));
			} else if(myEquip.get(pos) != null) {
				maskedEquip.put(pos, equip.get(position));
			}
		}
		for(Entry<Short, Integer> entry : myEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		for(Entry<Short, Integer> entry : maskedEquip.entrySet()) {
			mplew.write(entry.getKey());
			mplew.writeInt(entry.getValue());
		}
		mplew.write(0xFF);
		Integer cWeapon = equip.get((byte) -111);
		if(cWeapon != null) {
			mplew.writeInt(cWeapon);
		} else {
			mplew.writeInt(0);
		}
		for(int i = 0; i < 3; i++) {
			mplew.writeInt(0);
		}
                return mplew.getPacket();
        }

        public static byte[] removePlayerNPC(int oid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.IMITATED_NPC_DATA.getValue());
                mplew.write(0x00);
                mplew.writeInt(oid);
                
                return mplew.getPacket();
        }
        
        public static byte[] sendYellowTip(String tip) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
                mplew.write(0xFF);
                mplew.writeMapleAsciiString(tip);
                mplew.writeShort(0);
                return mplew.getPacket();
        }

        public static byte[] givePirateBuff(List<Pair<MapleBuffStat, Integer>> statups, int buffid, int duration) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
                mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
                writeLongMask(mplew, statups);
                mplew.writeShort(0);
                for (Pair<MapleBuffStat, Integer> stat : statups) {
                        mplew.writeInt(stat.getRight().shortValue());
                        mplew.writeInt(buffid);
                        mplew.skip(infusion ? 10 : 5);
                        mplew.writeShort(duration);
                }
                mplew.skip(3);
                return mplew.getPacket();
        }

        public static byte[] giveForeignPirateBuff(int cid, int buffid, int time, List<Pair<MapleBuffStat, Integer>> statups) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
                mplew.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
                writeLongMask(mplew, statups);
                mplew.writeShort(0);
                for (Pair<MapleBuffStat, Integer> statup : statups) {
                        mplew.writeInt(statup.getRight().shortValue());
                        mplew.writeInt(buffid);
                        mplew.skip(infusion ? 10 : 5);
                        mplew.writeShort(time);
                }
                mplew.writeShort(0);
                mplew.write(2);
                return mplew.getPacket();
        }

        public static byte[] sendMTS(List<MTSItemInfo> items, int tab, int type, int page, int pages) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x15); //operation
                mplew.writeInt(pages * 16); //testing, change to 10 if fails
                mplew.writeInt(items.size()); //number of items
                mplew.writeInt(tab);
                mplew.writeInt(type);
                mplew.writeInt(page);
                mplew.write(1);
                mplew.write(1);
                for (int i = 0; i < items.size(); i++) {
                        MTSItemInfo item = items.get(i);
                        addItemInfo(mplew, item.getItem(), true);
                        mplew.writeInt(item.getID()); //id
                        mplew.writeInt(item.getTaxes()); //this + below = price
                        mplew.writeInt(item.getPrice()); //price
                        mplew.writeInt(0);
                        mplew.writeLong(getTime(item.getEndingDate()));
                        mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                        mplew.writeMapleAsciiString(item.getSeller()); //char name
                        for (int j = 0; j < 28; j++) {
                                mplew.write(0);
                        }
                }
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] noteSendMsg() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
                mplew.write(4);
                return mplew.getPacket();
        }

        /*
         *  0 = Player online, use whisper
         *  1 = Check player's name
         *  2 = Receiver inbox full
         */
        public static byte[] noteError(byte error) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
                mplew.write(5);
                mplew.write(error);
                return mplew.getPacket();
        }

        public static byte[] showNotes(ResultSet notes, int count) throws SQLException {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MEMO_RESULT.getValue());
                mplew.write(3);
                mplew.write(count);
                for (int i = 0; i < count; i++) {
                        mplew.writeInt(notes.getInt("id"));
                        mplew.writeMapleAsciiString(notes.getString("from") + " ");//Stupid nexon forgot space lol
                        mplew.writeMapleAsciiString(notes.getString("message"));
                        mplew.writeLong(getTime(notes.getLong("timestamp")));
                        mplew.write(notes.getByte("fame"));//FAME :D
                        notes.next();
                }
                return mplew.getPacket();
        }

        public static byte[] useChalkboard(MapleCharacter chr, boolean close) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CHALKBOARD.getValue());
                mplew.writeInt(chr.getId());
                if (close) {
                        mplew.write(0);
                } else {
                        mplew.write(1);
                        mplew.writeMapleAsciiString(chr.getChalkboard());
                }
                return mplew.getPacket();
        }

        public static byte[] trockRefreshMapList(MapleCharacter chr, boolean delete, boolean vip) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAP_TRANSFER_RESULT.getValue());
                mplew.write(delete ? 2 : 3);
                if (vip) {
                        mplew.write(1);
                        List<Integer> map = chr.getVipTrockMaps();
                        for (int i = 0; i < 10; i++) {
                                mplew.writeInt(map.get(i));
                        }
                } else {
                        mplew.write(0);
                        List<Integer> map = chr.getTrockMaps();
                        for (int i = 0; i < 5; i++) {
                                mplew.writeInt(map.get(i));
                        }
                }
                return mplew.getPacket();
        }
        
        /*  1: cannot find char info,
            2: cannot transfer under 20,
            3: cannot send banned,
            4: cannot send married,
            5: cannot send guild leader,
            6: cannot send if account already requested transfer,
            7: cannot transfer within 30days,
            8: must quit family,
            9: unknown error
        */
        public static byte[] sendWorldTransferRules(int error, MapleClient c) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_CHECK_TRANSFER_WORLD_POSSIBLE_RESULT.getValue());
                mplew.writeInt(0); //ignored
                mplew.write(error);
                mplew.writeInt(0);
                mplew.writeBool(error == 0); //0 = ?, otherwise list servers
                if(error == 0) {
                    List<World> worlds = Server.getInstance().getWorlds();
                    mplew.writeInt(worlds.size());
                    for(World world : worlds) {
                        mplew.writeMapleAsciiString(GameConstants.WORLD_NAMES[world.getId()]);
                    }
                }
                return mplew.getPacket();
        }
        
        public static byte[] showWorldTransferSuccess(Item item, int accountId) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            mplew.write(0xA0);
            addCashItemInformation(mplew, item, accountId);
            return mplew.getPacket();
    }
        
        /*  0: no error, send rules
            1: name change already submitted
            2: name change within a month
            3: recently banned
            4: unknown error
        */
        public static byte[] sendNameTransferRules(int error) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE_POSSIBLE_RESULT.getValue());
                mplew.writeInt(0);
                mplew.write(error);
                mplew.writeInt(0);
                
                return mplew.getPacket();
        }
        
        /*  0: Name available
         * >0: Name is in use
         * <0: Unknown error
         */
        
        public static byte[] sendNameTransferCheck(String availableName, boolean canUseName) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_CHECK_NAME_CHANGE.getValue());
                //Send provided name back to client to add to temporary cache of checked & accepted names
                mplew.writeMapleAsciiString(availableName);
                mplew.writeBool(!canUseName);
                return mplew.getPacket();
        }
        
        public static byte[] showNameChangeSuccess(Item item, int accountId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
                mplew.write(0x9E);
                addCashItemInformation(mplew, item, accountId);
                return mplew.getPacket();
        }
        
        public static byte[] showNameChangeCancel(boolean success) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CANCEL_NAME_CHANGE_RESULT.getValue());
            mplew.writeBool(success);
            if(!success) mplew.write(0);
            //mplew.writeMapleAsciiString("Custom message."); //only if ^ != 0
            return mplew.getPacket();
        }
        
        public static byte[] showWorldTransferCancel(boolean success) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CANCEL_TRANSFER_WORLD_RESULT.getValue());
            mplew.writeBool(success);
            if(!success) mplew.write(0);
            //mplew.writeMapleAsciiString("Custom message."); //only if ^ != 0
            return mplew.getPacket();
        }
        
        public static byte[] showMTSCash(MapleCharacter p) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION2.getValue());
                mplew.writeInt(p.getCashShop().getCash(4));
                mplew.writeInt(p.getCashShop().getCash(2));
                return mplew.getPacket();
        }

        public static byte[] MTSWantedListingOver(int nx, int items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x3D);
                mplew.writeInt(nx);
                mplew.writeInt(items);
                return mplew.getPacket();
        }

        public static byte[] MTSConfirmSell() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x1D);
                return mplew.getPacket();
        }

        public static byte[] MTSConfirmBuy() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x33);
                return mplew.getPacket();
        }

        public static byte[] MTSFailBuy() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x34);
                mplew.write(0x42);
                return mplew.getPacket();
        }

        public static byte[] MTSConfirmTransfer(int quantity, int pos) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x27);
                mplew.writeInt(quantity);
                mplew.writeInt(pos);
                return mplew.getPacket();
        }

        public static byte[] notYetSoldInv(List<MTSItemInfo> items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x23);
                mplew.writeInt(items.size());
                if (!items.isEmpty()) {
                        for (MTSItemInfo item : items) {
                                addItemInfo(mplew, item.getItem(), true);
                                mplew.writeInt(item.getID()); //id
                                mplew.writeInt(item.getTaxes()); //this + below = price
                                mplew.writeInt(item.getPrice()); //price
                                mplew.writeInt(0);
                                mplew.writeLong(getTime(item.getEndingDate()));
                                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                                mplew.writeMapleAsciiString(item.getSeller()); //char name
                                for (int i = 0; i < 28; i++) {
                                        mplew.write(0);
                                }
                        }
                } else {
                        mplew.writeInt(0);
                }
                return mplew.getPacket();
        }

        public static byte[] transferInventory(List<MTSItemInfo> items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MTS_OPERATION.getValue());
                mplew.write(0x21);
                mplew.writeInt(items.size());
                if (!items.isEmpty()) {
                        for (MTSItemInfo item : items) {
                                addItemInfo(mplew, item.getItem(), true);
                                mplew.writeInt(item.getID()); //id
                                mplew.writeInt(item.getTaxes()); //taxes
                                mplew.writeInt(item.getPrice()); //price
                                mplew.writeInt(0);
                                mplew.writeLong(getTime(item.getEndingDate()));
                                mplew.writeMapleAsciiString(item.getSeller()); //account name (what was nexon thinking?)
                                mplew.writeMapleAsciiString(item.getSeller()); //char name
                                for (int i = 0; i < 28; i++) {
                                        mplew.write(0);
                                }
                        }
                }
                mplew.write(0xD0 + items.size());
                mplew.write(new byte[]{-1, -1, -1, 0});
                return mplew.getPacket();
        }
        
        public static byte[] showCouponRedeemedItems(int accountId, int maplePoints, int mesos, List<Item> cashItems, List<Pair<Integer, Integer>> items) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
                mplew.write(0x59);
                mplew.write((byte)cashItems.size());
                for(Item item : cashItems) {
                    addCashItemInformation(mplew, item, accountId);
                }
                mplew.writeInt(maplePoints);
                mplew.writeInt(items.size());
                for(Pair<Integer, Integer> itemPair : items) {
                    int quantity = itemPair.getLeft();
                    mplew.writeShort((short) quantity); //quantity (0 = 1 for cash items)
                    mplew.writeShort(0x1F); //0 = ?, >=0x20 = ?, <0x20 = ? (does nothing?)
                    mplew.writeInt(itemPair.getRight());
                }
                mplew.writeInt(mesos);
                return mplew.getPacket();
        }

        public static byte[] showCash(MapleCharacter mc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.QUERY_CASH_RESULT.getValue());

                mplew.writeInt(mc.getCashShop().getCash(1));
                mplew.writeInt(mc.getCashShop().getCash(2));
                mplew.writeInt(mc.getCashShop().getCash(4));

                return mplew.getPacket();
        }

        public static byte[] enableCSUse(MapleCharacter mc) {
                return showCash(mc);
        }

        /**
         *
         * @param target
         * @param mapid
         * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
         * @return
         */
        public static byte[] getFindReply(String target, int mapid, int MTSmapCSchannel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.WHISPER.getValue());
                mplew.write(9);
                mplew.writeMapleAsciiString(target);
                mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
                mplew.writeInt(mapid); // -1 if mts, cs
                if (MTSmapCSchannel == 1) {
                        mplew.write(new byte[8]);
                }
                return mplew.getPacket();
        }

        /**
         *
         * @param target
         * @param mapid
         * @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel
         * @return
         */
        public static byte[] getBuddyFindReply(String target, int mapid, int MTSmapCSchannel) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.WHISPER.getValue());
                mplew.write(72);
                mplew.writeMapleAsciiString(target);
                mplew.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
                mplew.writeInt(mapid); // -1 if mts, cs
                if (MTSmapCSchannel == 1) {
                        mplew.write(new byte[8]);
                }
                return mplew.getPacket();
        }

        public static byte[] sendAutoHpPot(int itemId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.AUTO_HP_POT.getValue());
                mplew.writeInt(itemId);
                return mplew.getPacket();
        }

        public static byte[] sendAutoMpPot(int itemId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.AUTO_MP_POT.getValue());
                mplew.writeInt(itemId);
                return mplew.getPacket();
        }

        public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.OX_QUIZ.getValue());
                mplew.write(askQuestion ? 1 : 0);
                mplew.write(questionSet);
                mplew.writeShort(questionId);
                return mplew.getPacket();
        }

        public static byte[] updateGender(MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.SET_GENDER.getValue());
                mplew.write(chr.getGender());
                return mplew.getPacket();
        }

        public static byte[] enableReport() { // thanks to snow
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.CLAIM_STATUS_CHANGED.getValue());
                mplew.write(1);
                return mplew.getPacket();
        }

        public static byte[] giveFinalAttack(int skillid, int time) { // packets found thanks to lailainoob
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GIVE_BUFF.getValue());
                mplew.writeLong(0);
                mplew.writeShort(0);
                mplew.write(0);//some 80 and 0 bs DIRECTION
                mplew.write(0x80);//let's just do 80, then 0
                mplew.writeInt(0);
                mplew.writeShort(1);
                mplew.writeInt(skillid);
                mplew.writeInt(time);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] loadFamily(MapleCharacter player) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_PRIVILEGE_LIST.getValue());
                mplew.writeInt(MapleFamilyEntitlement.values().length);
                for (int i = 0; i < MapleFamilyEntitlement.values().length; i++) {
                        MapleFamilyEntitlement entitlement = MapleFamilyEntitlement.values()[i];
                        mplew.write(i <= 1 ? 1 : 2); //type
                        mplew.writeInt(entitlement.getRepCost());
                        mplew.writeInt(entitlement.getUsageLimit());
                        mplew.writeMapleAsciiString(entitlement.getName());
                        mplew.writeMapleAsciiString(entitlement.getDescription());
                }
                return mplew.getPacket();
        }

        /**
         * Family Result Message
         *
         * Possible values for <code>type</code>:<br>
         * 64: You cannot add this character as a junior.
         * 65: The name could not be found or is not online.
         * 66: You belong to the same family.
         * 67: You do not belong to the same family.<br>
         * 69: The character you wish to add as\r\na Junior must be in the same
         * map.<br>
         * 70: This character is already a Junior of another character.<br>
         * 71: The Junior you wish to add\r\nmust be at a lower rank.<br>
         * 72: The gap between you and your\r\njunior must be within 20 levels.<br>
         * 73: Another character has requested to add this character.\r\nPlease try
         * again later.<br>
         * 74: Another character has requested a summon.\r\nPlease try again
         * later.<br>
         * 75: The summons has failed. Your current location or state does not allow
         * a summons.<br>
         * 76: The family cannot extend more than 1000 generations from above and
         * below.<br>
         * 77: The Junior you wish to add\r\nmust be over Level 10.<br>
         * 78: You cannot add a Junior \r\nthat has requested to change worlds.<br>
         * 79: You cannot add a Junior \r\nsince you've requested to change
         * worlds.<br>
         * 80: Separation is not possible due to insufficient Mesos.\r\nYou will
         * need %d Mesos to\r\nseparate with a Senior.<br>
         * 81: Separation is not possible due to insufficient Mesos.\r\nYou will
         * need %d Mesos to\r\nseparate with a Junior.<br>
         * 82: The Entitlement does not apply because your level does not match the
         * corresponding area.<br>
         *
         * @param type The type
         * @return Family Result packet
         */
        public static byte[] sendFamilyMessage(int type, int mesos) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.FAMILY_RESULT.getValue());
                mplew.writeInt(type);
                mplew.writeInt(mesos);
                return mplew.getPacket();
        }

        public static byte[] getFamilyInfo(MapleFamilyEntry f) {
                if(f == null) return getEmptyFamilyInfo();
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_INFO_RESULT.getValue());
                mplew.writeInt(f.getReputation()); // cur rep left
                mplew.writeInt(f.getTotalReputation()); // tot rep left
                mplew.writeInt(f.getTodaysRep()); // todays rep
                mplew.writeShort(f.getJuniorCount()); // juniors added
                mplew.writeShort(2); // juniors allowed
                mplew.writeShort(0); //Unknown
                mplew.writeInt(f.getFamily().getLeader().getChrId()); // Leader ID (Allows setting message)
                mplew.writeMapleAsciiString(f.getFamily().getName());
                mplew.writeMapleAsciiString(f.getFamily().getMessage()); //family message
                mplew.writeInt(MapleFamilyEntitlement.values().length); //Entitlement info count
                for(MapleFamilyEntitlement entitlement : MapleFamilyEntitlement.values()) {
                    mplew.writeInt(entitlement.ordinal()); //ID
                    mplew.writeInt(f.isEntitlementUsed(entitlement) ? 1 : 0); //Used count
                }
                return mplew.getPacket();
        }
        
        private static byte[] getEmptyFamilyInfo() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_INFO_RESULT.getValue());
                mplew.writeInt(0); // cur rep left
                mplew.writeInt(0); // tot rep left
                mplew.writeInt(0); // todays rep
                mplew.writeShort(0); // juniors added
                mplew.writeShort(2); // juniors allowed
                mplew.writeShort(0); //Unknown
                mplew.writeInt(0); // Leader ID (Allows setting message)
                mplew.writeMapleAsciiString("");
                mplew.writeMapleAsciiString(""); //family message
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] showPedigree(MapleFamilyEntry entry) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_CHART_RESULT.getValue());
                mplew.writeInt(entry.getChrId()); //ID of viewed player's pedigree, can't be leader?
                List<MapleFamilyEntry> superJuniors = new ArrayList<MapleFamilyEntry>(4);
                boolean hasOtherJunior = false;
                int entryCount = 2; //2 guaranteed, leader and self
                entryCount += Math.min(2, entry.getTotalSeniors());
                //needed since MaplePacketLittleEndianWriter doesn't have any seek functionality
                if(entry.getSenior() != null) {
                    if(entry.getSenior().getJuniorCount() == 2) {
                        entryCount++;
                        hasOtherJunior = true;
                    }
                }
                for(MapleFamilyEntry junior : entry.getJuniors()) {
                    if(junior == null) continue;
                    entryCount++;
                    for(MapleFamilyEntry superJunior : junior.getJuniors()) {
                        if(superJunior == null) continue;
                        entryCount++;
                        superJuniors.add(superJunior);
                    }
                }
                //write entries
                boolean missingEntries = entryCount == 2; //pedigree requires at least 3 entries to show leader, might only have 2 if leader's juniors leave
                if(missingEntries) entryCount++;
                mplew.writeInt(entryCount); //player count
                addPedigreeEntry(mplew, entry.getFamily().getLeader());
                if(entry.getSenior() != null) {
                    if(entry.getSenior().getSenior() != null) addPedigreeEntry(mplew, entry.getSenior().getSenior());
                    addPedigreeEntry(mplew, entry.getSenior());
                }
                addPedigreeEntry(mplew, entry);
                if(hasOtherJunior) { //must be sent after own entry
                    MapleFamilyEntry otherJunior = entry.getSenior().getOtherJunior(entry);
                    if(otherJunior != null) addPedigreeEntry(mplew, otherJunior);
                }
                if(missingEntries) addPedigreeEntry(mplew, entry);
                for(MapleFamilyEntry junior : entry.getJuniors()) {
                    if(junior == null) continue;
                    addPedigreeEntry(mplew, junior);
                    for(MapleFamilyEntry superJunior : junior.getJuniors()) {
                        if(superJunior != null) addPedigreeEntry(mplew, superJunior);
                    }
                }
                mplew.writeInt(2 + superJuniors.size()); //member info count
                // 0 = total seniors, -1 = total members, otherwise junior count of ID
                mplew.writeInt(-1); 
                mplew.writeInt(entry.getFamily().getTotalMembers());
                mplew.writeInt(0);
                mplew.writeInt(entry.getTotalSeniors()); //client subtracts provided seniors
                for(MapleFamilyEntry superJunior : superJuniors) {
                    mplew.writeInt(superJunior.getChrId());
                    mplew.writeInt(superJunior.getTotalJuniors());
                }
                mplew.writeInt(0); //another loop count (entitlements used)
                //mplew.writeInt(1); //entitlement index
                //mplew.writeInt(2); //times used
                mplew.writeShort(entry.getJuniorCount() >= 2 ? 0 : 2); //0 disables Add button (only if viewing own pedigree)
                return mplew.getPacket();
        }
        
        private static void addPedigreeEntry(MaplePacketLittleEndianWriter mplew, MapleFamilyEntry entry) {
                MapleCharacter chr = entry.getChr();
                boolean isOnline = chr != null;
                mplew.writeInt(entry.getChrId()); //ID
                mplew.writeInt(entry.getSenior() != null ? entry.getSenior().getChrId() : 0); //parent ID
                mplew.writeShort(entry.getJob().getId()); //job id
                mplew.write(entry.getLevel()); //level
                mplew.writeBool(isOnline); //isOnline
                mplew.writeInt(entry.getReputation()); //current rep
                mplew.writeInt(entry.getTotalReputation()); //total rep
                mplew.writeInt(entry.getRepsToSenior()); //reps recorded to senior
                mplew.writeInt(entry.getTodaysRep());
                mplew.writeInt(isOnline ? ((chr.isAwayFromWorld() || chr.getCashShop().isOpened()) ? -1 : chr.getClient().getChannel() - 1) : 0);
                mplew.writeInt(isOnline ? (int) (chr.getLoggedInTime() / 60000) : 0); //time online in minutes
                mplew.writeMapleAsciiString(entry.getName()); //name
        }

        public static byte[] updateAreaInfo(int area, String info) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(0x0A); //0x0B in v95
                mplew.writeShort(area);//infoNumber
                mplew.writeMapleAsciiString(info);
                return mplew.getPacket();
        }

        public static byte[] getGPMessage(int gpChange) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(6);
                mplew.writeInt(gpChange);
                return mplew.getPacket();
        }

        public static byte[] getItemMessage(int itemid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(7);
                mplew.writeInt(itemid);
                return mplew.getPacket();
        }

        public static byte[] addCard(boolean full, int cardid, int level) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
                mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_CARD.getValue());
                mplew.write(full ? 0 : 1);
                mplew.writeInt(cardid);
                mplew.writeInt(level);
                return mplew.getPacket();
        }

        public static byte[] showGainCard() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(0x0D);
                return mplew.getPacket();
        }

        public static byte[] showForeignCardEffect(int id) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(id);
                mplew.write(0x0D);
                return mplew.getPacket();
        }

        public static byte[] changeCover(int cardid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.MONSTER_BOOK_SET_COVER.getValue());
                mplew.writeInt(cardid);
                return mplew.getPacket();
        }

        public static byte[] aranGodlyStats() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FORCED_STAT_SET.getValue());
                mplew.write(new byte[]{(byte) 0x1F, (byte) 0x0F, 0, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0xFF, 0, (byte) 0xE7, 3, (byte) 0xE7, 3, (byte) 0x78, (byte) 0x8C});
                return mplew.getPacket();
        }

        public static byte[] showIntro(String path) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(0x12);
                mplew.writeMapleAsciiString(path);
                return mplew.getPacket();
        }

        public static byte[] showInfo(String path) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(0x17);
                mplew.writeMapleAsciiString(path);
                mplew.writeInt(1);
                return mplew.getPacket();
        }
        
        public static byte[] showForeignInfo(int cid, String path) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(0x17);
                mplew.writeMapleAsciiString(path);
                mplew.writeInt(1);
                return mplew.getPacket();
        }

        /**
         * Sends a UI utility. 0x01 - Equipment Inventory. 0x02 - Stat Window. 0x03
         * - Skill Window. 0x05 - Keyboard Settings. 0x06 - Quest window. 0x09 -
         * Monsterbook Window. 0x0A - Char Info 0x0B - Guild BBS 0x12 - Monster
         * Carnival Window 0x16 - Party Search. 0x17 - Item Creation Window. 0x1A -
         * My Ranking O.O 0x1B - Family Window 0x1C - Family Pedigree 0x1D - GM
         * Story Board /funny shet 0x1E - Envelop saying you got mail from an admin.
         * lmfao 0x1F - Medal Window 0x20 - Maple Event (???) 0x21 - Invalid Pointer
         * Crash
         *
         * @param ui
         * @return
         */
        public static byte[] openUI(byte ui) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.OPEN_UI.getValue());
                mplew.write(ui);
                return mplew.getPacket();
        }

        public static byte[] lockUI(boolean enable) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.LOCK_UI.getValue());
                mplew.write(enable ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] disableUI(boolean enable) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DISABLE_UI.getValue());
                mplew.write(enable ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
                mplew.write(8);
                mplew.writeMapleAsciiString(msg);
                mplew.write(channel - 1);
                mplew.write(whisper ? 1 : 0);
                if (item == null) {
                        mplew.write(0);
                } else {
                        mplew.write(item.getPosition());
                        addItemInfo(mplew, item, true);
                }
                return mplew.getPacket();
        }

        public static byte[] removeNPC(int oid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.REMOVE_NPC.getValue());
                mplew.writeInt(oid);
                
                return mplew.getPacket();
        }
        
        public static byte[] removeNPCController(int objectid) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

                mplew.writeShort(SendOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
                mplew.write(0);
                mplew.writeInt(objectid);

                return mplew.getPacket();
        }

        /**
         * Sends a report response
         *
         * Possible values for <code>mode</code>:<br> 0: You have succesfully
         * reported the user.<br> 1: Unable to locate the user.<br> 2: You may only
         * report users 10 times a day.<br> 3: You have been reported to the GM's by
         * a user.<br> 4: Your request did not go through for unknown reasons.
         * Please try again later.<br>
         *
         * @param mode The mode
         * @return Report Reponse packet
         */
        public static byte[] reportResponse(byte mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SUE_CHARACTER_RESULT.getValue());
                mplew.write(mode);
                return mplew.getPacket();
        }

        public static byte[] sendHammerData(int hammerUsed) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
                mplew.write(0x39);
                mplew.writeInt(0);
                mplew.writeInt(hammerUsed);
                return mplew.getPacket();
        }

        public static byte[] sendHammerMessage() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.VICIOUS_HAMMER.getValue());
                mplew.write(0x3D);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] playPortalSound() {
                return showSpecialEffect(7);
        }

        public static byte[] showMonsterBookPickup() {
                return showSpecialEffect(14);
        }

        public static byte[] showEquipmentLevelUp() {
                return showSpecialEffect(15);
        }

        public static byte[] showItemLevelup() {
                return showSpecialEffect(15);
        }
        
        public static byte[] showBuybackEffect() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(11);
                mplew.writeInt(0);
                
                return mplew.getPacket();
        }
        
        public static byte[] showForeignBuybackEffect(int cid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(11);
                mplew.writeInt(0);
                
                return mplew.getPacket();
        }
        
        /**
         * 0 = Levelup 6 = Exp did not drop (Safety Charms) 7 = Enter portal sound 
         * 8 = Job change 9 = Quest complete 10 = Recovery 11 = Buff effect 
         * 14 = Monster book pickup 15 = Equipment levelup 16 = Maker Skill Success
         * 17 = Buff effect w/ sfx 19 = Exp card [500, 200, 50] 21 = Wheel of destiny
         * 26 = Spirit Stone
         *
         * @param effect
         * @return
         */
        public static byte[] showSpecialEffect(int effect) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(effect);
                return mplew.getPacket();
        }
        
        public static byte[] showMakerEffect(boolean makerSucceeded) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(16);
                mplew.writeInt(makerSucceeded ? 0 : 1);
                return mplew.getPacket();
        }
        
        public static byte[] showForeignMakerEffect(int cid, boolean makerSucceeded) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(16);
                mplew.writeInt(makerSucceeded ? 0 : 1);
                return mplew.getPacket();
        }
        
        public static byte[] showForeignEffect(int effect) {
                return showForeignEffect(-1, effect);
        }

        public static byte[] showForeignEffect(int cid, int effect) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(effect);
                return mplew.getPacket();
        }

        public static byte[] showOwnRecovery(byte heal) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(0x0A);
                mplew.write(heal);
                return mplew.getPacket();
        }

        public static byte[] showRecovery(int cid, byte amount) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
                mplew.write(0x0A);
                mplew.write(amount);
                return mplew.getPacket();
        }
        
        public static byte[] showWheelsLeft(int left) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(0x15);
                mplew.write(left);
                return mplew.getPacket();
        }

        public static byte[] updateQuestFinish(short quest, int npc, short nextquest) { //Check
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue()); //0xF2 in v95
                mplew.write(8);//0x0A in v95
                mplew.writeShort(quest);
                mplew.writeInt(npc);
                mplew.writeShort(nextquest);
                return mplew.getPacket();
        }

        public static byte[] showInfoText(String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(9);
                mplew.writeMapleAsciiString(text);
                return mplew.getPacket();
        }

        public static byte[] questError(short quest) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(0x0A);
                mplew.writeShort(quest);
                return mplew.getPacket();
        }

        public static byte[] questFailure(byte type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(type);//0x0B = No meso, 0x0D = Worn by character, 0x0E = Not having the item ?
                return mplew.getPacket();
        }

        public static byte[] questExpire(short quest) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.UPDATE_QUEST_INFO.getValue());
                mplew.write(0x0F);
                mplew.writeShort(quest);
                return mplew.getPacket();
        }
        
        // MAKER_RESULT packets thanks to Arnah (Vertisy)
        public static byte[] makerResult(boolean success, int itemMade, int itemCount, int mesos, List<Pair<Integer, Integer>> itemsLost, int catalystID, List<Integer> INCBuffGems) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
                mplew.writeInt(success ? 0 : 1); // 0 = success, 1 = fail
                mplew.writeInt(1); // 1 or 2 doesn't matter, same methods
                mplew.writeBool(!success);
                if (success) {
                        mplew.writeInt(itemMade);
                        mplew.writeInt(itemCount);
                }
                mplew.writeInt(itemsLost.size()); // Loop
                for (Pair<Integer, Integer> item : itemsLost) {
                        mplew.writeInt(item.getLeft());
                        mplew.writeInt(item.getRight());
                }
                mplew.writeInt(INCBuffGems.size());
                for (Integer gem : INCBuffGems) {
                        mplew.writeInt(gem);
                }
                if (catalystID != -1) {
                    mplew.write(1); // stimulator
                    mplew.writeInt(catalystID);
                } else {
                    mplew.write(0);
                }
                
                mplew.writeInt(mesos);
                return mplew.getPacket();
        }

        public static byte[] makerResultCrystal(int itemIdGained, int itemIdLost) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
                mplew.writeInt(0); // Always successful!
                mplew.writeInt(3); // Monster Crystal
                mplew.writeInt(itemIdGained);
                mplew.writeInt(itemIdLost);
                return mplew.getPacket();
        }

        public static byte[] makerResultDesynth(int itemId, int mesos, List<Pair<Integer, Integer>> itemsGained) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
                mplew.writeInt(0); // Always successful!
                mplew.writeInt(4); // Mode Desynth
                mplew.writeInt(itemId); // Item desynthed
                mplew.writeInt(itemsGained.size()); // Loop of items gained, (int, int)
                for (Pair<Integer, Integer> item : itemsGained) {
                        mplew.writeInt(item.getLeft());
                        mplew.writeInt(item.getRight());
                }
                mplew.writeInt(mesos); // Mesos spent.
                return mplew.getPacket();
        }
        
        public static byte[] makerEnableActions() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MAKER_RESULT.getValue());
                mplew.writeInt(0); // Always successful!
                mplew.writeInt(0); // Monster Crystal
                mplew.writeInt(0);
                mplew.writeInt(0);
                return mplew.getPacket();
        }

        public static byte[] getMultiMegaphone(String[] messages, int channel, boolean showEar) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SERVERMESSAGE.getValue());
                mplew.write(0x0A);
                if (messages[0] != null) {
                        mplew.writeMapleAsciiString(messages[0]);
                }
                mplew.write(messages.length);
                for (int i = 1; i < messages.length; i++) {
                        if (messages[i] != null) {
                                mplew.writeMapleAsciiString(messages[i]);
                        }
                }
                for (int i = 0; i < 10; i++) {
                        mplew.write(channel - 1);
                }
                mplew.write(showEar ? 1 : 0);
                mplew.write(1);
                return mplew.getPacket();
        }

        /**
         * Gets a gm effect packet (ie. hide, banned, etc.)
         *
         * Possible values for <code>type</code>:<br> 0x04: You have successfully
         * blocked access.<br>
         * 0x05: The unblocking has been successful.<br> 0x06 with Mode 0: You have
         * successfully removed the name from the ranks.<br> 0x06 with Mode 1: You
         * have entered an invalid character name.<br> 0x10: GM Hide, mode
         * determines whether or not it is on.<br> 0x1E: Mode 0: Failed to send
         * warning Mode 1: Sent warning<br> 0x13 with Mode 0: + mapid 0x13 with Mode
         * 1: + ch (FF = Unable to find merchant)
         *
         * @param type The type
         * @param mode The mode
         * @return The gm effect packet
         */
        public static byte[] getGMEffect(int type, byte mode) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
                mplew.write(type);
                mplew.write(mode);
                return mplew.getPacket();
        }

        public static byte[] findMerchantResponse(boolean map, int extra) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
                mplew.write(0x13);
                mplew.write(map ? 0 : 1); //00 = mapid, 01 = ch
                if (map) {
                        mplew.writeInt(extra);
                } else {
                        mplew.write(extra); //-1 = unable to find
                }
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] disableMinimap() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ADMIN_RESULT.getValue());
                mplew.writeShort(0x1C);
                return mplew.getPacket();
        }

        public static byte[] sendFamilyInvite(int playerId, String inviter) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST.getValue());
                mplew.writeInt(playerId);
                mplew.writeMapleAsciiString(inviter);
                return mplew.getPacket();
        }
        
        public static byte[] sendFamilySummonRequest(String familyName, String from) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_SUMMON_REQUEST.getValue());
                mplew.writeMapleAsciiString(from);
                mplew.writeMapleAsciiString(familyName);
                return mplew.getPacket();
        }
        
        public static byte[] sendFamilyLoginNotice(String name, boolean loggedIn) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_NOTIFY_LOGIN_OR_LOGOUT.getValue());
                mplew.writeBool(loggedIn);
                mplew.writeMapleAsciiString(name);
                return mplew.getPacket();
        }
        
        public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_JOIN_REQUEST_RESULT.getValue());
                mplew.write(accepted ? 1 : 0);
                mplew.writeMapleAsciiString(added);
                return mplew.getPacket();
        }
    
        public static byte[] getSeniorMessage(String name) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_JOIN_ACCEPTED.getValue());
                mplew.writeMapleAsciiString(name);
                mplew.writeInt(0);
                return mplew.getPacket();
        }
    
        public static byte[] sendGainRep(int gain, String from) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FAMILY_REP_GAIN.getValue());
                mplew.writeInt(gain);
                mplew.writeMapleAsciiString(from);
                return mplew.getPacket();
        }

        public static byte[] showBoughtCashPackage(List<Item> cashPackage, int accountId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x89);
                mplew.write(cashPackage.size());

                for (Item item : cashPackage) {
                        addCashItemInformation(mplew, item, accountId);
                }

                mplew.writeShort(0);

                return mplew.getPacket();
        }

        public static byte[] showBoughtQuestItem(int itemId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x8D);
                mplew.writeInt(1);
                mplew.writeShort(1);
                mplew.write(0x0B);
                mplew.write(0);
                mplew.writeInt(itemId);

                return mplew.getPacket();
        }
        
        // Cash Shop Surprise packets found thanks to Arnah (Vertisy)
        public static byte[] onCashItemGachaponOpenFailed(){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
		mplew.write(0xE4);
		return mplew.getPacket();
	}

	public static byte[] onCashGachaponOpenSuccess(int accountid, long sn, int remainingBoxes, Item item, int itemid, int nSelectedItemCount, boolean bJackpot){
		MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
		mplew.writeShort(SendOpcode.CASHSHOP_CASH_ITEM_GACHAPON_RESULT.getValue());
		mplew.write(0xE5);   // subopcode thanks to Ubaware
		mplew.writeLong(sn);// sn of the box used
		mplew.writeInt(remainingBoxes);
		addCashItemInformation(mplew, item, accountid);
		mplew.writeInt(itemid);// the itemid of the liSN?
		mplew.write(nSelectedItemCount);// the total count now? o.O
		mplew.writeBool(bJackpot);// "CashGachaponJackpot"
		return mplew.getPacket();
	}
        
        private static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
                mplew.writeInt(guild.getId());
                mplew.writeMapleAsciiString(guild.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(guild.getRankTitle(i));
                }
                Collection<MapleGuildCharacter> members = guild.getMembers();
                mplew.write(members.size());
                for (MapleGuildCharacter mgc : members) {
                        mplew.writeInt(mgc.getId());
                }
                for (MapleGuildCharacter mgc : members) {
                        mplew.writeAsciiString(getRightPaddedStr(mgc.getName(), '\0', 13));
                        mplew.writeInt(mgc.getJobId());
                        mplew.writeInt(mgc.getLevel());
                        mplew.writeInt(mgc.getGuildRank());
                        mplew.writeInt(mgc.isOnline() ? 1 : 0);
                        mplew.writeInt(guild.getSignature());
                        mplew.writeInt(mgc.getAllianceRank());
                }
                mplew.writeInt(guild.getCapacity());
                mplew.writeShort(guild.getLogoBG());
                mplew.write(guild.getLogoBGColor());
                mplew.writeShort(guild.getLogo());
                mplew.write(guild.getLogoColor());
                mplew.writeMapleAsciiString(guild.getNotice());
                mplew.writeInt(guild.getGP());
                mplew.writeInt(guild.getAllianceId());
        }

        public static byte[] getAllianceInfo(MapleAlliance alliance) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x0C);
                mplew.write(1);
                mplew.writeInt(alliance.getId());
                mplew.writeMapleAsciiString(alliance.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i));
                }
                mplew.write(alliance.getGuilds().size());
                mplew.writeInt(alliance.getCapacity()); // probably capacity
                for (Integer guild : alliance.getGuilds()) {
                        mplew.writeInt(guild);
                }
                mplew.writeMapleAsciiString(alliance.getNotice());
                return mplew.getPacket();
        }

        public static byte[] updateAllianceInfo(MapleAlliance alliance, int world) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x0F);
                mplew.writeInt(alliance.getId());
                mplew.writeMapleAsciiString(alliance.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i));
                }
                mplew.write(alliance.getGuilds().size());
                for (Integer guild : alliance.getGuilds()) {
                        mplew.writeInt(guild);
                }
                mplew.writeInt(alliance.getCapacity()); // probably capacity
                mplew.writeShort(0);
                for (Integer guildid : alliance.getGuilds()) {
                        getGuildInfo(mplew, Server.getInstance().getGuild(guildid, world));
                }
                return mplew.getPacket();
        }

        public static byte[] getGuildAlliances(MapleAlliance alliance, int worldId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x0D);
                mplew.writeInt(alliance.getGuilds().size());
                for (Integer guild : alliance.getGuilds()) {
                        getGuildInfo(mplew, Server.getInstance().getGuild(guild, worldId));
                }
                return mplew.getPacket();
        }

        public static byte[] addGuildToAlliance(MapleAlliance alliance, int newGuild, MapleClient c) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x12);
                mplew.writeInt(alliance.getId());
                mplew.writeMapleAsciiString(alliance.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i));
                }
                mplew.write(alliance.getGuilds().size());
                for (Integer guild : alliance.getGuilds()) {
                        mplew.writeInt(guild);
                }
                mplew.writeInt(alliance.getCapacity());
                mplew.writeMapleAsciiString(alliance.getNotice());
                mplew.writeInt(newGuild);
                getGuildInfo(mplew, Server.getInstance().getGuild(newGuild, c.getWorld(), null));
                return mplew.getPacket();
        }

        public static byte[] allianceMemberOnline(MapleCharacter mc, boolean online) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x0E);
                mplew.writeInt(mc.getGuild().getAllianceId());
                mplew.writeInt(mc.getGuildId());
                mplew.writeInt(mc.getId());
                mplew.write(online ? 1 : 0);
                return mplew.getPacket();
        }

        public static byte[] allianceNotice(int id, String notice) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x1C);
                mplew.writeInt(id);
                mplew.writeMapleAsciiString(notice);
                return mplew.getPacket();
        }

        public static byte[] changeAllianceRankTitle(int alliance, String[] ranks) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x1A);
                mplew.writeInt(alliance);
                for (int i = 0; i < 5; i++) {
                        mplew.writeMapleAsciiString(ranks[i]);
                }
                return mplew.getPacket();
        }

        public static byte[] updateAllianceJobLevel(MapleCharacter mc) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x18);
                mplew.writeInt(mc.getGuild().getAllianceId());
                mplew.writeInt(mc.getGuildId());
                mplew.writeInt(mc.getId());
                mplew.writeInt(mc.getLevel());
                mplew.writeInt(mc.getJob().getId());
                return mplew.getPacket();
        }

        public static byte[] removeGuildFromAlliance(MapleAlliance alliance, int expelledGuild, int worldId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x10);
                mplew.writeInt(alliance.getId());
                mplew.writeMapleAsciiString(alliance.getName());
                for (int i = 1; i <= 5; i++) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i));
                }
                mplew.write(alliance.getGuilds().size());
                for (Integer guild : alliance.getGuilds()) {
                        mplew.writeInt(guild);
                }
                mplew.writeInt(alliance.getCapacity());
                mplew.writeMapleAsciiString(alliance.getNotice());
                mplew.writeInt(expelledGuild);
                getGuildInfo(mplew, Server.getInstance().getGuild(expelledGuild, worldId, null));
                mplew.write(0x01);
                return mplew.getPacket();
        }

        public static byte[] disbandAlliance(int alliance) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x1D);
                mplew.writeInt(alliance);
                return mplew.getPacket();
        }
        
        public static byte[] allianceInvite(int allianceid, MapleCharacter chr) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.ALLIANCE_OPERATION.getValue());
                mplew.write(0x03);
                mplew.writeInt(allianceid);
                mplew.writeMapleAsciiString(chr.getName());
                mplew.writeShort(0);
                return mplew.getPacket();
        }
        
        public static byte[] sendMesoLimit() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TRADE_MONEY_LIMIT.getValue()); //Players under level 15 can only trade 1m per day
                return mplew.getPacket();
        }

        public static byte[] removeItemFromDuey(boolean remove, int Package) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARCEL.getValue());
                mplew.write(0x17);
                mplew.writeInt(Package);
                mplew.write(remove ? 3 : 4);
                return mplew.getPacket();
        }
        
        public static byte[] sendDueyParcelReceived(String from, boolean quick) {    // thanks inhyuk
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARCEL.getValue());
                mplew.write(0x19);
                mplew.writeMapleAsciiString(from);
                mplew.writeBool(quick);
                return mplew.getPacket();
        }

        public static byte[] sendDueyParcelNotification(boolean quick) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARCEL.getValue());
                mplew.write(0x1B);
                mplew.writeBool(quick);  // 0 : package received, 1 : quick delivery package
                return mplew.getPacket();
        }
        
        public static byte[] sendDueyMSG(byte operation) {
                return sendDuey(operation, null);
        }

        public static byte[] sendDuey(int operation, List<DueyPackage> packages) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PARCEL.getValue());
                mplew.write(operation);
                if (operation == 8) {
                        mplew.write(0);
                        mplew.write(packages.size());
                        for (DueyPackage dp : packages) {
                                mplew.writeInt(dp.getPackageId());
                                mplew.writeAsciiString(dp.getSender());
                                for (int i = dp.getSender().length(); i < 13; i++) {
                                        mplew.write(0);
                                }
                                
                                mplew.writeInt(dp.getMesos());
                                mplew.writeLong(getTime(dp.sentTimeInMilliseconds()));
                                
                                String msg = dp.getMessage();
                                if (msg != null) {
                                        mplew.writeInt(1);
                                        mplew.writeAsciiString(msg);
                                        for (int i = msg.length(); i < 200; i++) {
                                                mplew.write(0);
                                        }
                                } else {
                                        mplew.writeInt(0);
                                        mplew.skip(200);
                                }
                                
                                mplew.write(0);
                                if (dp.getItem() != null) {
                                        mplew.write(1);
                                        addItemInfo(mplew, dp.getItem(), true);
                                } else {
                                        mplew.write(0);
                                }
                        }
                        mplew.write(0);
                }

                return mplew.getPacket();
        }

        public static byte[] sendDojoAnimation(byte firstByte, String animation) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(firstByte);
                mplew.writeMapleAsciiString(animation);
                return mplew.getPacket();
        }

        public static byte[] getDojoInfo(String info) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(10);
                mplew.write(new byte[]{(byte) 0xB7, 4});//QUEST ID f5
                mplew.writeMapleAsciiString(info);
                return mplew.getPacket();
        }

        public static byte[] getDojoInfoMessage(String message) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(9);
                mplew.writeMapleAsciiString(message);
                return mplew.getPacket();
        }

        /**
         * Gets a "block" packet (ie. the cash shop is unavailable, etc)
         *
         * Possible values for <code>type</code>:<br> 1: The portal is closed for
         * now.<br> 2: You cannot go to that place.<br> 3: Unable to approach due to
         * the force of the ground.<br> 4: You cannot teleport to or on this
         * map.<br> 5: Unable to approach due to the force of the ground.<br> 6:
         * Only party members can enter this map.<br> 7: The Cash Shop is
         * currently not available. Stay tuned...<br>
         *
         * @param type The type
         * @return The "block" packet.
         */
        public static byte[] blockedMessage(int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BLOCKED_MAP.getValue());
                mplew.write(type);
                return mplew.getPacket();
        }

        /**
         * Gets a "block" packet (ie. the cash shop is unavailable, etc)
         *
         * Possible values for <code>type</code>:<br> 1: You cannot move that
         * channel. Please try again later.<br> 2: You cannot go into the cash shop.
         * Please try again later.<br> 3: The Item-Trading Shop is currently
         * unavailable. Please try again later.<br> 4: You cannot go into the trade
         * shop, due to limitation of user count.<br> 5: You do not meet the minimum
         * level requirement to access the Trade Shop.<br>
         *
         * @param type The type
         * @return The "block" packet.
         */
        public static byte[] blockedMessage2(int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BLOCKED_SERVER.getValue());
                mplew.write(type);
                return mplew.getPacket();
        }

        public static byte[] updateDojoStats(MapleCharacter chr, int belt) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(10);
                mplew.write(new byte[]{(byte) 0xB7, 4}); //?
                mplew.writeMapleAsciiString("pt=" + chr.getDojoPoints() + ";belt=" + belt + ";tuto=" + (chr.getFinishedDojoTutorial() ? "1" : "0"));
                return mplew.getPacket();
        }

        /**
         * Sends a "levelup" packet to the guild or family.
         *
         * Possible values for <code>type</code>:<br> 0: <Family> ? has reached Lv.
         * ?.<br> - The Reps you have received from ? will be reduced in half. 1:
         * <Family> ? has reached Lv. ?.<br> 2: <Guild> ? has reached Lv. ?.<br>
         *
         * @param type The type
         * @return The "levelup" packet.
         */
        public static byte[] levelUpMessage(int type, int level, String charname) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NOTIFY_LEVELUP.getValue());
                mplew.write(type);
                mplew.writeInt(level);
                mplew.writeMapleAsciiString(charname);

                return mplew.getPacket();
        }

        /**
         * Sends a "married" packet to the guild or family.
         *
         * Possible values for <code>type</code>:<br> 0: <Guild ? is now married.
         * Please congratulate them.<br> 1: <Family ? is now married. Please
         * congratulate them.<br>
         *
         * @param type The type
         * @return The "married" packet.
         */
        public static byte[] marriageMessage(int type, String charname) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NOTIFY_MARRIAGE.getValue());
                mplew.write(type);  // 0: guild, 1: family
                mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

                return mplew.getPacket();
        }

        /**
         * Sends a "job advance" packet to the guild or family.
         *
         * Possible values for <code>type</code>:<br> 0: <Guild ? has advanced to
         * a(an) ?.<br> 1: <Family ? has advanced to a(an) ?.<br>
         *
         * @param type The type
         * @return The "job advance" packet.
         */
        public static byte[] jobMessage(int type, int job, String charname) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.NOTIFY_JOB_CHANGE.getValue());
                mplew.write(type);
                mplew.writeInt(job); //Why fking int?
                mplew.writeMapleAsciiString("> " + charname); //To fix the stupid packet lol

                return mplew.getPacket();
        }

        /**
         *
         * @param type - (0:Light&Long 1:Heavy&Short)
         * @param delay - seconds
         * @return
         */
        public static byte[] trembleEffect(int type, int delay) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.FIELD_EFFECT.getValue());
                mplew.write(1);
                mplew.write(type);
                mplew.writeInt(delay);
                return mplew.getPacket();
        }

        public static byte[] getEnergy(String info, int amount) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SESSION_VALUE.getValue());
                mplew.writeMapleAsciiString(info);
                mplew.writeMapleAsciiString(Integer.toString(amount));
                return mplew.getPacket();
        }

        public static byte[] dojoWarpUp() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DOJO_WARP_UP.getValue());
                mplew.write(0);
                mplew.write(6);
                return mplew.getPacket();
        }

        public static byte[] itemExpired(int itemid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(2);
                mplew.writeInt(itemid);
                return mplew.getPacket();
        }

        private static String getRightPaddedStr(String in, char padchar, int length) {
                StringBuilder builder = new StringBuilder(in);
                for (int x = in.length(); x < length; x++) {
                        builder.append(padchar);
                }
                return builder.toString();
        }

        public static byte[] MobDamageMobFriendly(MapleMonster mob, int damage, int remainingHp) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.DAMAGE_MONSTER.getValue());
                mplew.writeInt(mob.getObjectId());
                mplew.write(1); // direction ?
                mplew.writeInt(damage);
                mplew.writeInt(remainingHp);
                mplew.writeInt(mob.getMaxHp());
                return mplew.getPacket();
        }

        public static byte[] shopErrorMessage(int error, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PLAYER_INTERACTION.getValue());
                mplew.write(0x0A);
                mplew.write(type);
                mplew.write(error);
                return mplew.getPacket();
        }

        private static void addRingInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
                mplew.writeShort(chr.getCrushRings().size());
                for (MapleRing ring : chr.getCrushRings()) {
                        mplew.writeInt(ring.getPartnerChrId());
                        mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
                        mplew.writeInt(ring.getRingId());
                        mplew.writeInt(0);
                        mplew.writeInt(ring.getPartnerRingId());
                        mplew.writeInt(0);
                }
                mplew.writeShort(chr.getFriendshipRings().size());
                for (MapleRing ring : chr.getFriendshipRings()) {
                        mplew.writeInt(ring.getPartnerChrId());
                        mplew.writeAsciiString(getRightPaddedStr(ring.getPartnerName(), '\0', 13));
                        mplew.writeInt(ring.getRingId());
                        mplew.writeInt(0);
                        mplew.writeInt(ring.getPartnerRingId());
                        mplew.writeInt(0);
                        mplew.writeInt(ring.getItemId());
                }
                
                if(chr.getPartnerId() > 0) {
                        MapleRing marriageRing = chr.getMarriageRing();
                    
                        mplew.writeShort(1);
                        mplew.writeInt(chr.getRelationshipId());
                        mplew.writeInt(chr.getGender() == 0 ? chr.getId() : chr.getPartnerId());
                        mplew.writeInt(chr.getGender() == 0 ? chr.getPartnerId() : chr.getId());
                        mplew.writeShort((marriageRing != null) ? 3 : 1);
                        if (marriageRing != null) {
                                mplew.writeInt(marriageRing.getItemId());
                                mplew.writeInt(marriageRing.getItemId());
                        } else {
                                mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
                                mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
                        }
                        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? chr.getName() : MapleCharacter.getNameById(chr.getPartnerId()), '\0', 13));
                        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? MapleCharacter.getNameById(chr.getPartnerId()) : chr.getName(), '\0', 13));
                } else {
                        mplew.writeShort(0);
                }
        }

        public static byte[] finishedSort(int inv) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.GATHER_ITEM_RESULT.getValue());
                mplew.write(0);
                mplew.write(inv);
                return mplew.getPacket();
        }

        public static byte[] finishedSort2(int inv) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.SORT_ITEM_RESULT.getValue());
                mplew.write(0);
                mplew.write(inv);
                return mplew.getPacket();
        }

        public static byte[] bunnyPacket() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHOW_STATUS_INFO.getValue());
                mplew.write(9);
                mplew.writeAsciiString("Protect the Moon Bunny!!!");
                return mplew.getPacket();
        }

        public static byte[] hpqMessage(String text) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.BLOW_WEATHER.getValue()); // not 100% sure
                mplew.write(0);
                mplew.writeInt(5120016);
                mplew.writeAsciiString(text);
                return mplew.getPacket();
        }

        public static byte[] showEventInstructions() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
                mplew.write(0);
                return mplew.getPacket();
        }

        public static byte[] leftKnockBack() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
                mplew.writeShort(SendOpcode.LEFT_KNOCK_BACK.getValue());
                return mplew.getPacket();
        }

        public static byte[] rollSnowBall(boolean entermap, int state, MapleSnowball ball0, MapleSnowball ball1) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SNOWBALL_STATE.getValue());
                if (entermap) {
                        mplew.skip(21);
                } else {
                        mplew.write(state);// 0 = move, 1 = roll, 2 is down disappear, 3 is up disappear
                        mplew.writeInt(ball0.getSnowmanHP() / 75);
                        mplew.writeInt(ball1.getSnowmanHP() / 75);
                        mplew.writeShort(ball0.getPosition());//distance snowball down, 84 03 = max
                        mplew.write(-1);
                        mplew.writeShort(ball1.getPosition());//distance snowball up, 84 03 = max
                        mplew.write(-1);
                }
                return mplew.getPacket();
        }

        public static byte[] hitSnowBall(int what, int damage) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.HIT_SNOWBALL.getValue());
                mplew.write(what);
                mplew.writeInt(damage);
                return mplew.getPacket();
        }

        /**
         * Sends a Snowball Message<br>
         *
         * Possible values for <code>message</code>:<br> 1: ... Team's snowball has
         * passed the stage 1.<br> 2: ... Team's snowball has passed the stage
         * 2.<br> 3: ... Team's snowball has passed the stage 3.<br> 4: ... Team is
         * attacking the snowman, stopping the progress<br> 5: ... Team is moving
         * again<br>
         *
         * @param message
         *
         */
        public static byte[] snowballMessage(int team, int message) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.SNOWBALL_MESSAGE.getValue());
                mplew.write(team);// 0 is down, 1 is up
                mplew.writeInt(message);
                return mplew.getPacket();
        }

        public static byte[] coconutScore(int team1, int team2) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.COCONUT_SCORE.getValue());
                mplew.writeShort(team1);
                mplew.writeShort(team2);
                return mplew.getPacket();
        }

        public static byte[] hitCoconut(boolean spawn, int id, int type) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.COCONUT_HIT.getValue());
                if (spawn) {
                        mplew.writeShort(-1);
                        mplew.writeShort(5000);
                        mplew.write(0);
                } else {
                        mplew.writeShort(id);
                        mplew.writeShort(1000);//delay till you can attack again! 
                        mplew.write(type); // What action to do for the coconut.
                }
                return mplew.getPacket();
        }

        public static byte[] customPacket(String packet) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.write(HexTool.getByteArrayFromHexString(packet));
                return mplew.getPacket();
        }

        public static byte[] customPacket(byte[] packet) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(packet.length);
                mplew.write(packet);
                return mplew.getPacket();
        }

        public static byte[] spawnGuide(boolean spawn) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.SPAWN_GUIDE.getValue());
                if (spawn) {
                        mplew.write(1);
                } else {
                        mplew.write(0);
                }
                return mplew.getPacket();
        }

        public static byte[] talkGuide(String talk) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
                mplew.write(0);
                mplew.writeMapleAsciiString(talk);
                mplew.write(new byte[]{(byte) 0xC8, 0, 0, 0, (byte) 0xA0, (byte) 0x0F, 0, 0});
                return mplew.getPacket();
        }

        public static byte[] guideHint(int hint) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(11);
                mplew.writeShort(SendOpcode.TALK_GUIDE.getValue());
                mplew.write(1);
                mplew.writeInt(hint);
                mplew.writeInt(7000);
                return mplew.getPacket();
        }

        public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId) {
                addCashItemInformation(mplew, item, accountId, null);
        }

        public static void addCashItemInformation(final MaplePacketLittleEndianWriter mplew, Item item, int accountId, String giftMessage) {
                boolean isGift = giftMessage != null;
                boolean isRing = false;
                Equip equip = null;
                if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                        equip = (Equip) item;
                        isRing = equip.getRingId() > -1;
                }
                mplew.writeLong(item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId());
                if (!isGift) {
                        mplew.writeInt(accountId);
                        mplew.writeInt(0);
                }
                mplew.writeInt(item.getItemId());
                if (!isGift) {
                        mplew.writeInt(item.getSN());
                        mplew.writeShort(item.getQuantity());
                }
                mplew.writeAsciiString(StringUtil.getRightPaddedStr(item.getGiftFrom(), '\0', 13));
                if (isGift) {
                        mplew.writeAsciiString(StringUtil.getRightPaddedStr(giftMessage, '\0', 73));
                        return;
                }
                addExpirationTime(mplew, item.getExpiration());
                mplew.writeLong(0);
        }

        public static byte[] showWishList(MapleCharacter mc, boolean update) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                if (update) {
                        mplew.write(0x55);
                } else {
                        mplew.write(0x4F);
                }

                for (int sn : mc.getCashShop().getWishList()) {
                        mplew.writeInt(sn);
                }

                for (int i = mc.getCashShop().getWishList().size(); i < 10; i++) {
                        mplew.writeInt(0);
                }

                return mplew.getPacket();
        }

        public static byte[] showBoughtCashItem(Item item, int accountId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x57);
                addCashItemInformation(mplew, item, accountId);

                return mplew.getPacket();
        }
        
        public static byte[] showBoughtCashRing(Item ring, String recipient, int accountId) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            mplew.write(0x87);
            addCashItemInformation(mplew, ring, accountId);
            mplew.writeMapleAsciiString(recipient);
            mplew.writeInt(ring.getItemId());
            mplew.writeShort(1); //quantity
            return mplew.getPacket();
        }
        
        /*
         * 00 = Due to an unknown error, failed
         * A3 = Request timed out. Please try again.
         * A4 = Due to an unknown error, failed + warpout
         * A5 = You don't have enough cash.
         * A6 = long as shet msg
         * A7 = You have exceeded the allotted limit of price for gifts.
         * A8 = You cannot send a gift to your own account. Log in on the char and purchase
         * A9 = Please confirm whether the character's name is correct.
         * AA = Gender restriction!
         * AB = gift cannot be sent because recipient inv is full
         * AC = exceeded the number of cash items you can have
         * AD = check and see if the character name is wrong or there is gender restrictions
         * //Skipped a few
         * B0 = Wrong Coupon Code
         * B1 = Disconnect from CS because of 3 wrong coupon codes < lol
         * B2 = Expired Coupon
         * B3 = Coupon has been used already
         * B4 = Nexon internet cafes? lolfk
         * B8 = Due to gender restrictions, the coupon cannot be used.
         * BB = inv full
         * BC = long as shet "(not?) available to purchase by a use at the premium" msg
         * BD = invalid gift recipient
         * BE = invalid receiver name
         * BF = item unavailable to purchase at this hour
         * C0 = not enough items in stock, therefore not available
         * C1 = you have exceeded spending limit of NX
         * C2 = not enough mesos? Lol not even 1 mesos xD
         * C3 = cash shop unavailable during beta phase
         * C4 = check birthday code
         * C7 = only available to users buying cash item, whatever msg too long
         * C8 = already applied for this
         * CD = You have reached the daily purchase limit for the cash shop.
         * D0 = coupon account limit reached
         * D2 = coupon system currently unavailable
         * D3 = item can only be used 15 days after registration
         * D4 = not enough gift tokens
         * D6 = fresh people cannot gift items lul
         * D7 = bad people cannot gift items >:(
         * D8 = cannot gift due to limitations
         * D9 = cannot gift due to amount of gifted times
         * DA = cannot be gifted due to technical difficulties
         * DB = cannot transfer to char below level 20
         * DC = cannot transfer char to same world
         * DD = cannot transfer char to new server world
         * DE = cannot transfer char out of this world
         * DF = cannot transfer char due to no empty char slots
         * E0 = event or free test time ended
         * E6 = item cannot be purchased with MaplePoints
         * E7 = lol sorry for the inconvenience, eh?
         * E8 = cannot purchase by anyone under 7
         */
        public static byte[] showCashShopMessage(byte message) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x5C);
                mplew.write(message);

                return mplew.getPacket();
        }

        public static byte[] showCashInventory(MapleClient c) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x4B);
                mplew.writeShort(c.getPlayer().getCashShop().getInventory().size());

                for (Item item : c.getPlayer().getCashShop().getInventory()) {
                        addCashItemInformation(mplew, item, c.getAccID());
                }

                mplew.writeShort(c.getPlayer().getStorage().getSlots());
                mplew.writeShort(c.getCharacterSlots());

                return mplew.getPacket();
        }

        public static byte[] showGifts(List<Pair<Item, String>> gifts) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x4D);
                mplew.writeShort(gifts.size());

                for (Pair<Item, String> gift : gifts) {
                        addCashItemInformation(mplew, gift.getLeft(), 0, gift.getRight());
                }

                return mplew.getPacket();
        }

        public static byte[] showGiftSucceed(String to, CashItem item) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x5E); //0x5D, Couldn't be sent
                mplew.writeMapleAsciiString(to);
                mplew.writeInt(item.getItemId());
                mplew.writeShort(item.getCount());
                mplew.writeInt(item.getPrice());

                return mplew.getPacket();
        }

        public static byte[] showBoughtInventorySlots(int type, short slots) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x60);
                mplew.write(type);
                mplew.writeShort(slots);

                return mplew.getPacket();
        }

        public static byte[] showBoughtStorageSlots(short slots) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x62);
                mplew.writeShort(slots);

                return mplew.getPacket();
        }

        public static byte[] showBoughtCharacterSlot(short slots) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(5);
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x64);
                mplew.writeShort(slots);

                return mplew.getPacket();
        }

        public static byte[] takeFromCashInventory(Item item) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x68);
                mplew.writeShort(item.getPosition());
                addItemInfo(mplew, item, true);

                return mplew.getPacket();
        }
        
        public static byte[] deleteCashItem(Item item) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            mplew.write(0x6C);
            mplew.writeLong(item.getCashId());
            return mplew.getPacket();
        }
        
        public static byte[] refundCashItem(Item item, int maplePoints) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());
            mplew.write(0x85);
            mplew.writeLong(item.getCashId());
            mplew.writeInt(maplePoints);
            return mplew.getPacket();
        }

        public static byte[] putIntoCashInventory(Item item, int accountId) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.CASHSHOP_OPERATION.getValue());

                mplew.write(0x6A);
                addCashItemInformation(mplew, item, accountId);

                return mplew.getPacket();
        }

        public static byte[] openCashShop(MapleClient c, boolean mts) throws Exception {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(mts ? SendOpcode.SET_ITC.getValue() : SendOpcode.SET_CASH_SHOP.getValue());

                addCharacterInfo(mplew, c.getPlayer());

                if (!mts) {
                        mplew.write(1);
                }

                mplew.writeMapleAsciiString(c.getAccountName());
                if (mts) {
                        mplew.write(new byte[]{(byte) 0x88, 19, 0, 0, 7, 0, 0, 0, (byte) 0xF4, 1, 0, 0, (byte) 0x18, 0, 0, 0, (byte) 0xA8, 0, 0, 0, (byte) 0x70, (byte) 0xAA, (byte) 0xA7, (byte) 0xC5, (byte) 0x4E, (byte) 0xC1, (byte) 0xCA, 1});
                } else {
                        mplew.writeInt(0);
                        List<SpecialCashItem> lsci = CashItemFactory.getSpecialCashItems();
                        mplew.writeShort(lsci.size());//Guess what
                        for (SpecialCashItem sci : lsci) {
                                mplew.writeInt(sci.getSN());
                                mplew.writeInt(sci.getModifier());
                                mplew.write(sci.getInfo());
                        }
                        mplew.skip(121);

                        List<List<Integer>> mostSellers = c.getWorldServer().getMostSellerCashItems();
                        for (int i = 1; i <= 8; i++) {
                                List<Integer> mostSellersTab = mostSellers.get(i);
                                
                                for (int j = 0; j < 2; j++) {
                                        for (Integer snid : mostSellersTab) {
                                                mplew.writeInt(i);
                                                mplew.writeInt(j);
                                                mplew.writeInt(snid);
                                        }
                                }
                        }

                        mplew.writeInt(0);
                        mplew.writeShort(0);
                        mplew.write(0);
                        mplew.writeInt(75);
                }
                return mplew.getPacket();
        }

        public static byte[] sendVegaScroll(int op) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
                mplew.writeShort(SendOpcode.VEGA_SCROLL.getValue());
                mplew.write(op);
                return mplew.getPacket();
        }
        
        public static byte[] resetForcedStats() {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);
                mplew.writeShort(SendOpcode.FORCED_STAT_RESET.getValue());
                return mplew.getPacket();
        }

        public static byte[] showCombo(int count) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.SHOW_COMBO.getValue());
                mplew.writeInt(count);
                return mplew.getPacket();
        }

        public static byte[] earnTitleMessage(String msg) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SCRIPT_PROGRESS_MESSAGE.getValue());
                mplew.writeMapleAsciiString(msg);
                return mplew.getPacket();
        }

        public static byte[] CPUpdate(boolean party, int curCP, int totalCP, int team) { // CPQ
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                if (!party) {
                        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
                } else {
                        mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_PARTY_CP.getValue());
                        mplew.write(team); // team?
                }
                mplew.writeShort(curCP);
                mplew.writeShort(totalCP);
        return mplew.getPacket();
    }

    public static byte[] CPQMessage(byte message) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_MESSAGE.getValue());
            mplew.write(message); // Message
            return mplew.getPacket();
    }

    public static byte[] playerSummoned(String name, int tab, int number) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
            mplew.write(tab);
            mplew.write(number);
            mplew.writeMapleAsciiString(name);
            return mplew.getPacket();
    }

    public static byte[] playerDiedMessage(String name, int lostCP, int team) { // CPQ
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_DIED.getValue());
            mplew.write(team); // team
            mplew.writeMapleAsciiString(name);
            mplew.write(lostCP);
            return mplew.getPacket();
    }

    public static byte[] startMonsterCarnival(MapleCharacter chr, int team, int oposition) {
            final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(25);
            mplew.writeShort(SendOpcode.MONSTER_CARNIVAL_START.getValue());
            mplew.write(team); // team
            mplew.writeShort(chr.getCP()); // Obtained CP - Used CP
            mplew.writeShort(chr.getTotalCP()); // Total Obtained CP
            mplew.writeShort(chr.getMonsterCarnival().getCP(team)); // Obtained CP - Used CP of the team
            mplew.writeShort(chr.getMonsterCarnival().getTotalCP(team)); // Total Obtained CP of the team
            mplew.writeShort(chr.getMonsterCarnival().getCP(oposition)); // Obtained CP - Used CP of the team
            mplew.writeShort(chr.getMonsterCarnival().getTotalCP(oposition)); // Total Obtained CP of the team
            mplew.writeShort(0); // Probably useless nexon shit
            mplew.writeLong(0); // Probably useless nexon shit
            return mplew.getPacket();
    }

        public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHEEP_RANCH_INFO.getValue());
                mplew.write(wolf);
                mplew.write(sheep);
                return mplew.getPacket();
        }
        //Know what this is? ?? >=)

        public static byte[] sheepRanchClothes(int id, byte clothes) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SHEEP_RANCH_CLOTHES.getValue());
                mplew.writeInt(id); //Character id
                mplew.write(clothes); //0 = sheep, 1 = wolf, 2 = Spectator (wolf without wool)
                return mplew.getPacket();
        }

        public static byte[] incubatorResult() {//lol
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8);
                mplew.writeShort(SendOpcode.INCUBATOR_RESULT.getValue());
                mplew.skip(6);
                return mplew.getPacket();
        }

        public static byte[] pyramidGauge(int gauge) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(6);
                mplew.writeShort(SendOpcode.PYRAMID_GAUGE.getValue());
                mplew.writeInt(gauge);
                return mplew.getPacket();
        }
        // f2

        public static byte[] pyramidScore(byte score, int exp) {//Type cannot be higher than 4 (Rank D), otherwise you'll crash
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7);
                mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue());
                mplew.write(score);
                mplew.writeInt(exp);
                return mplew.getPacket();
        }
        
        public static byte[] spawnDragon(MapleDragon dragon) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SPAWN_DRAGON.getValue());
                mplew.writeInt(dragon.getOwner().getId());//objectid = owner id
                mplew.writeShort(dragon.getPosition().x);
                mplew.writeShort(0);
                mplew.writeShort(dragon.getPosition().y);
                mplew.writeShort(0);
                mplew.write(dragon.getStance());
                mplew.write(0);
                mplew.writeShort(dragon.getOwner().getJob().getId());
                return mplew.getPacket();
        }

        public static byte[] moveDragon(MapleDragon dragon, Point startPos, SeekableLittleEndianAccessor movementSlea, long movementDataLength) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.MOVE_DRAGON.getValue());
                mplew.writeInt(dragon.getOwner().getId());
                mplew.writePos(startPos);
                rebroadcastMovementList(mplew, movementSlea, movementDataLength);
                return mplew.getPacket();
        }

        /**
         * Sends a request to remove Mir<br>
         *
         * @param charid - Needs the specific Character ID
         * @return The packet
         *
         */
        public static byte[] removeDragon(int chrid) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.REMOVE_DRAGON.getValue());
                mplew.writeInt(chrid);
                return mplew.getPacket();
        }

        /**
         * Changes the current background effect to either being rendered or not.
         * Data is still missing, so this is pretty binary at the moment in how it
         * behaves.
         *
         * @param remove whether or not the remove or add the specified layer.
         * @param layer the targeted layer for removal or addition.
         * @param transition the time it takes to transition the effect.
         *
         * @return a packet to change the background effect of a specified layer.
         */
        public static byte[] changeBackgroundEffect(boolean remove, int layer, int transition) {
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_BACK_EFFECT.getValue());
                mplew.writeBool(remove);
                mplew.writeInt(0); // not sure what this int32 does yet
                mplew.write(layer);
                mplew.writeInt(transition);
                return mplew.getPacket();
        }
        
        public static byte[] setNPCScriptable(Set<Pair<Integer, String>> scriptNpcDescriptions) {  // thanks to GabrielSin
                MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.SET_NPC_SCRIPTABLE.getValue());
                mplew.write(scriptNpcDescriptions.size());
                for (Pair<Integer, String> p : scriptNpcDescriptions) {
                        mplew.writeInt(p.getLeft());
                        mplew.writeMapleAsciiString(p.getRight());
                        mplew.writeInt(0); // start time
                        mplew.writeInt(Integer.MAX_VALUE); // end time
                }
                return mplew.getPacket();
        }
        
        private static byte[] MassacreResult(byte nRank,int nIncExp) {
        	//CField_MassacreResult__OnMassacreResult @ 0x005617C5
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.PYRAMID_SCORE.getValue()); //MASSACRERESULT | 0x009E 
                mplew.write(nRank); //(0 - S) (1 - A) (2 - B) (3 - C) (4 - D) ( Else - Crash )
                mplew.writeInt(nIncExp);
                return mplew.getPacket();
        }

        private static byte[] GuildBoss_HealerMove(short nY) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_BOSS_HEALER_MOVE.getValue());
                mplew.writeShort(nY); //New Y Position
                return mplew.getPacket();
        }
		

        private static byte[] GuildBoss_PulleyStateChange(byte nState) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.GUILD_BOSS_PULLEY_STATE_CHANGE.getValue());
                mplew.write(nState);
                return mplew.getPacket();
        }
		
        private static byte[] Tournament__Tournament(byte nState, byte nSubState) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TOURNAMENT.getValue());
                mplew.write(nState);
		mplew.write(nSubState);
                return mplew.getPacket();
        }
		
        private static byte[] Tournament__MatchTable(byte nState, byte nSubState) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TOURNAMENT_MATCH_TABLE.getValue()); //Prompts CMatchTableDlg Modal
                return mplew.getPacket();
        }
		
        private static byte[] Tournament__SetPrize(byte bSetPrize, byte bHasPrize,int nItemID1,int nItemID2) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TOURNAMENT_SET_PRIZE.getValue());
				
		//0 = "You have failed the set the prize. Please check the item number again."
		//1 = "You have successfully set the prize."
		mplew.write(bSetPrize);
		
		mplew.write(bHasPrize);
		
		if(bHasPrize != 0)
		{
			mplew.writeInt(nItemID1);
			mplew.writeInt(nItemID2);
		}
		
                return mplew.getPacket();
        }
		
        private static byte[] Tournament__UEW(byte nState) {
                final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
                mplew.writeShort(SendOpcode.TOURNAMENT_UEW.getValue());
				
		//Is this a bitflag o.o ?
		//2 = "You have reached the finals by default."
		//4 = "You have reached the semifinals by default."
		//8 or 16 = "You have reached the round of %n by default." | Encodes nState as %n ?!
		mplew.write(nState);
			
                return mplew.getPacket();
        }
	
}

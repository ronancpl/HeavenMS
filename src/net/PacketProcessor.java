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
package net;

import java.util.LinkedHashMap;
import java.util.Map;

import net.opcodes.RecvOpcode;
import net.server.channel.handlers.*;
import net.server.handlers.CustomPacketHandler;
import net.server.handlers.KeepAliveHandler;
import net.server.handlers.LoginRequiringNoOpHandler;
import net.server.handlers.login.AcceptToSHandler;
import net.server.handlers.login.AfterLoginHandler;
import net.server.handlers.login.CharSelectedHandler;
import net.server.handlers.login.CharSelectedWithPicHandler;
import net.server.handlers.login.CharlistRequestHandler;
import net.server.handlers.login.CheckCharNameHandler;
import net.server.handlers.login.CreateCharHandler;
import net.server.handlers.login.DeleteCharHandler;
import net.server.handlers.login.GuestLoginHandler;
import net.server.handlers.login.LoginPasswordHandler;
import net.server.handlers.login.RegisterPicHandler;
import net.server.handlers.login.RegisterPinHandler;
import net.server.handlers.login.RelogRequestHandler;
import net.server.handlers.login.ServerStatusRequestHandler;
import net.server.handlers.login.ServerlistRequestHandler;
import net.server.handlers.login.SetGenderHandler;
import net.server.handlers.login.ViewAllCharHandler;
import net.server.handlers.login.ViewAllCharRegisterPicHandler;
import net.server.handlers.login.ViewAllCharSelectedHandler;
import net.server.handlers.login.ViewAllCharSelectedWithPicHandler;

public final class PacketProcessor {

    private final static Map<String, PacketProcessor> instances = new LinkedHashMap<>();
    private MaplePacketHandler[] handlers;

    private PacketProcessor() {
        int maxRecvOp = 0;
        for (RecvOpcode op : RecvOpcode.values()) {
            if (op.getValue() > maxRecvOp) {
                maxRecvOp = op.getValue();
            }
        }
        handlers = new MaplePacketHandler[maxRecvOp + 1];
    }

    public MaplePacketHandler getHandler(short packetId) {
        if (packetId > handlers.length) {
            return null;
        }
        MaplePacketHandler handler = handlers[packetId];
        if (handler != null) {
            return handler;
        }
        return null;
    }

    public void registerHandler(RecvOpcode code, MaplePacketHandler handler) {
        try {
            handlers[code.getValue()] = handler;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Error registering handler - " + code.name());
        }
    }

    public synchronized static PacketProcessor getProcessor(int world, int channel) {
        final String lolpair = world + " " + channel;
        PacketProcessor processor = instances.get(lolpair);
        if (processor == null) {
            processor = new PacketProcessor();
            processor.reset(channel);
            instances.put(lolpair, processor);
        }
        return processor;
    }

    public void reset(int channel) {
        handlers = new MaplePacketHandler[handlers.length];

        registerHandler(RecvOpcode.PONG, new KeepAliveHandler());
        registerHandler(RecvOpcode.CUSTOM_PACKET, new CustomPacketHandler());
        if (channel < 0) {
            //LOGIN HANDLERS
            registerHandler(RecvOpcode.ACCEPT_TOS, new AcceptToSHandler());
            registerHandler(RecvOpcode.AFTER_LOGIN, new AfterLoginHandler());
            registerHandler(RecvOpcode.SERVERLIST_REREQUEST, new ServerlistRequestHandler());
            registerHandler(RecvOpcode.CHARLIST_REQUEST, new CharlistRequestHandler());
            registerHandler(RecvOpcode.CHAR_SELECT, new CharSelectedHandler());
            registerHandler(RecvOpcode.LOGIN_PASSWORD, new LoginPasswordHandler());
            registerHandler(RecvOpcode.RELOG, new RelogRequestHandler());
            registerHandler(RecvOpcode.SERVERLIST_REQUEST, new ServerlistRequestHandler());
            registerHandler(RecvOpcode.SERVERSTATUS_REQUEST, new ServerStatusRequestHandler());
            registerHandler(RecvOpcode.CHECK_CHAR_NAME, new CheckCharNameHandler());
            registerHandler(RecvOpcode.CREATE_CHAR, new CreateCharHandler());
            registerHandler(RecvOpcode.DELETE_CHAR, new DeleteCharHandler());
            registerHandler(RecvOpcode.VIEW_ALL_CHAR, new ViewAllCharHandler());
            registerHandler(RecvOpcode.PICK_ALL_CHAR, new ViewAllCharSelectedHandler());
            registerHandler(RecvOpcode.REGISTER_PIN, new RegisterPinHandler());
            registerHandler(RecvOpcode.GUEST_LOGIN, new GuestLoginHandler());
            registerHandler(RecvOpcode.REGISTER_PIC, new RegisterPicHandler());
            registerHandler(RecvOpcode.CHAR_SELECT_WITH_PIC, new CharSelectedWithPicHandler());
            registerHandler(RecvOpcode.SET_GENDER, new SetGenderHandler());
            registerHandler(RecvOpcode.VIEW_ALL_WITH_PIC, new ViewAllCharSelectedWithPicHandler());
            registerHandler(RecvOpcode.VIEW_ALL_PIC_REGISTER, new ViewAllCharRegisterPicHandler());
        } else {
            //CHANNEL HANDLERS
            registerHandler(RecvOpcode.NAME_TRANSFER, new TransferNameHandler());
            registerHandler(RecvOpcode.CHECK_CHAR_NAME, new TransferNameResultHandler());
            registerHandler(RecvOpcode.WORLD_TRANSFER, new TransferWorldHandler());
            registerHandler(RecvOpcode.CHANGE_CHANNEL, new ChangeChannelHandler());
            registerHandler(RecvOpcode.STRANGE_DATA, LoginRequiringNoOpHandler.getInstance());
            registerHandler(RecvOpcode.GENERAL_CHAT, new GeneralChatHandler());
            registerHandler(RecvOpcode.WHISPER, new WhisperHandler());
            registerHandler(RecvOpcode.NPC_TALK, new NPCTalkHandler());
            registerHandler(RecvOpcode.NPC_TALK_MORE, new NPCMoreTalkHandler());
            registerHandler(RecvOpcode.QUEST_ACTION, new QuestActionHandler());
            registerHandler(RecvOpcode.NPC_SHOP, new NPCShopHandler());
            registerHandler(RecvOpcode.ITEM_SORT, new InventoryMergeHandler());
            registerHandler(RecvOpcode.ITEM_MOVE, new ItemMoveHandler());
            registerHandler(RecvOpcode.MESO_DROP, new MesoDropHandler());
            registerHandler(RecvOpcode.PLAYER_LOGGEDIN, new PlayerLoggedinHandler());
            registerHandler(RecvOpcode.CHANGE_MAP, new ChangeMapHandler());
            registerHandler(RecvOpcode.MOVE_LIFE, new MoveLifeHandler());
            registerHandler(RecvOpcode.CLOSE_RANGE_ATTACK, new CloseRangeDamageHandler());
            registerHandler(RecvOpcode.RANGED_ATTACK, new RangedAttackHandler());
            registerHandler(RecvOpcode.MAGIC_ATTACK, new MagicDamageHandler());
            registerHandler(RecvOpcode.TAKE_DAMAGE, new TakeDamageHandler());
            registerHandler(RecvOpcode.MOVE_PLAYER, new MovePlayerHandler());
            registerHandler(RecvOpcode.USE_CASH_ITEM, new UseCashItemHandler());
            registerHandler(RecvOpcode.USE_ITEM, new UseItemHandler());
            registerHandler(RecvOpcode.USE_RETURN_SCROLL, new UseItemHandler());
            registerHandler(RecvOpcode.USE_UPGRADE_SCROLL, new ScrollHandler());
            registerHandler(RecvOpcode.USE_SUMMON_BAG, new UseSummonBagHandler());
            registerHandler(RecvOpcode.FACE_EXPRESSION, new FaceExpressionHandler());
            registerHandler(RecvOpcode.HEAL_OVER_TIME, new HealOvertimeHandler());
            registerHandler(RecvOpcode.ITEM_PICKUP, new ItemPickupHandler());
            registerHandler(RecvOpcode.CHAR_INFO_REQUEST, new CharInfoRequestHandler());
            registerHandler(RecvOpcode.SPECIAL_MOVE, new SpecialMoveHandler());
            registerHandler(RecvOpcode.USE_INNER_PORTAL, new InnerPortalHandler());
            registerHandler(RecvOpcode.CANCEL_BUFF, new CancelBuffHandler());
            registerHandler(RecvOpcode.CANCEL_ITEM_EFFECT, new CancelItemEffectHandler());
            registerHandler(RecvOpcode.PLAYER_INTERACTION, new PlayerInteractionHandler());
            registerHandler(RecvOpcode.DISTRIBUTE_AP, new DistributeAPHandler());
            registerHandler(RecvOpcode.DISTRIBUTE_SP, new DistributeSPHandler());
            registerHandler(RecvOpcode.CHANGE_KEYMAP, new KeymapChangeHandler());
            registerHandler(RecvOpcode.CHANGE_MAP_SPECIAL, new ChangeMapSpecialHandler());
            registerHandler(RecvOpcode.STORAGE, new StorageHandler());
            registerHandler(RecvOpcode.GIVE_FAME, new GiveFameHandler());
            registerHandler(RecvOpcode.PARTY_OPERATION, new PartyOperationHandler());
            registerHandler(RecvOpcode.DENY_PARTY_REQUEST, new DenyPartyRequestHandler());
            registerHandler(RecvOpcode.MULTI_CHAT, new MultiChatHandler());
            registerHandler(RecvOpcode.USE_DOOR, new DoorHandler());
            registerHandler(RecvOpcode.ENTER_MTS, new EnterMTSHandler());
            registerHandler(RecvOpcode.ENTER_CASHSHOP, new EnterCashShopHandler());
            registerHandler(RecvOpcode.DAMAGE_SUMMON, new DamageSummonHandler());
            registerHandler(RecvOpcode.MOVE_SUMMON, new MoveSummonHandler());
            registerHandler(RecvOpcode.SUMMON_ATTACK, new SummonDamageHandler());
            registerHandler(RecvOpcode.BUDDYLIST_MODIFY, new BuddylistModifyHandler());
            registerHandler(RecvOpcode.USE_ITEMEFFECT, new UseItemEffectHandler());
            registerHandler(RecvOpcode.USE_CHAIR, new UseChairHandler());
            registerHandler(RecvOpcode.CANCEL_CHAIR, new CancelChairHandler());
            registerHandler(RecvOpcode.DAMAGE_REACTOR, new ReactorHitHandler());
            registerHandler(RecvOpcode.GUILD_OPERATION, new GuildOperationHandler());
            registerHandler(RecvOpcode.DENY_GUILD_REQUEST, new DenyGuildRequestHandler());
            registerHandler(RecvOpcode.BBS_OPERATION, new BBSOperationHandler());
            registerHandler(RecvOpcode.SKILL_EFFECT, new SkillEffectHandler());
            registerHandler(RecvOpcode.MESSENGER, new MessengerHandler());
            registerHandler(RecvOpcode.NPC_ACTION, new NPCAnimationHandler());
            registerHandler(RecvOpcode.CHECK_CASH, new TouchingCashShopHandler());
            registerHandler(RecvOpcode.CASHSHOP_OPERATION, new CashOperationHandler());
            registerHandler(RecvOpcode.COUPON_CODE, new CouponCodeHandler());
            registerHandler(RecvOpcode.SPAWN_PET, new SpawnPetHandler());
            registerHandler(RecvOpcode.MOVE_PET, new MovePetHandler());
            registerHandler(RecvOpcode.PET_CHAT, new PetChatHandler());
            registerHandler(RecvOpcode.PET_COMMAND, new PetCommandHandler());
            registerHandler(RecvOpcode.PET_FOOD, new PetFoodHandler());
            registerHandler(RecvOpcode.PET_LOOT, new PetLootHandler());
            registerHandler(RecvOpcode.AUTO_AGGRO, new AutoAggroHandler());
            registerHandler(RecvOpcode.MONSTER_BOMB, new MonsterBombHandler());
            registerHandler(RecvOpcode.CANCEL_DEBUFF, new CancelDebuffHandler());
            registerHandler(RecvOpcode.USE_SKILL_BOOK, new SkillBookHandler());
            registerHandler(RecvOpcode.SKILL_MACRO, new SkillMacroHandler());
            registerHandler(RecvOpcode.NOTE_ACTION, new NoteActionHandler());
            registerHandler(RecvOpcode.CLOSE_CHALKBOARD, new CloseChalkboardHandler());
            registerHandler(RecvOpcode.USE_MOUNT_FOOD, new UseMountFoodHandler());
            registerHandler(RecvOpcode.MTS_OPERATION, new MTSHandler());
            registerHandler(RecvOpcode.RING_ACTION, new RingActionHandler());
            registerHandler(RecvOpcode.SPOUSE_CHAT, new SpouseChatHandler());
            registerHandler(RecvOpcode.PET_AUTO_POT, new PetAutoPotHandler());
            registerHandler(RecvOpcode.PET_EXCLUDE_ITEMS, new PetExcludeItemsHandler());
            registerHandler(RecvOpcode.OWL_ACTION, new UseOwlOfMinervaHandler());
            registerHandler(RecvOpcode.OWL_WARP, new OwlWarpHandler());
            registerHandler(RecvOpcode.TOUCH_MONSTER_ATTACK, new TouchMonsterDamageHandler());
            registerHandler(RecvOpcode.TROCK_ADD_MAP, new TrockAddMapHandler());
            registerHandler(RecvOpcode.HIRED_MERCHANT_REQUEST, new HiredMerchantRequest());
            registerHandler(RecvOpcode.MOB_DAMAGE_MOB, new MobDamageMobHandler());
            registerHandler(RecvOpcode.REPORT, new ReportHandler());
            registerHandler(RecvOpcode.MONSTER_BOOK_COVER, new MonsterBookCoverHandler());
            registerHandler(RecvOpcode.AUTO_DISTRIBUTE_AP, new AutoAssignHandler());
            registerHandler(RecvOpcode.MAKER_SKILL, new MakerSkillHandler());
            registerHandler(RecvOpcode.ADD_FAMILY, new FamilyAddHandler());
            registerHandler(RecvOpcode.USE_FAMILY, new FamilyUseHandler());
            registerHandler(RecvOpcode.USE_HAMMER, new UseHammerHandler());
            registerHandler(RecvOpcode.SCRIPTED_ITEM, new ScriptedItemHandler());
            registerHandler(RecvOpcode.TOUCHING_REACTOR, new TouchReactorHandler());
            registerHandler(RecvOpcode.BEHOLDER, new BeholderHandler());
            registerHandler(RecvOpcode.ADMIN_COMMAND, new AdminCommandHandler());
            registerHandler(RecvOpcode.ADMIN_LOG, new AdminLogHandler());
            registerHandler(RecvOpcode.ALLIANCE_OPERATION, new AllianceOperationHandler());
            registerHandler(RecvOpcode.USE_SOLOMON_ITEM, new UseSolomonHandler());
            registerHandler(RecvOpcode.USE_GACHA_EXP, new UseGachaExpHandler());
            registerHandler(RecvOpcode.NEW_YEAR_CARD_REQUEST, new NewYearCardHandler());
            registerHandler(RecvOpcode.CASHSHOP_SURPRISE, new CashShopSurpriseHandler());
            registerHandler(RecvOpcode.USE_ITEM_REWARD, new ItemRewardHandler());
            registerHandler(RecvOpcode.USE_REMOTE, new RemoteGachaponHandler());
            registerHandler(RecvOpcode.ACCEPT_FAMILY, new AcceptFamilyHandler());
            registerHandler(RecvOpcode.DUEY_ACTION, new DueyHandler());
            registerHandler(RecvOpcode.USE_DEATHITEM, new UseDeathItemHandler());
            //registerHandler(RecvOpcode.PLAYER_UPDATE, new PlayerUpdateHandler()); unused
            registerHandler(RecvOpcode.PLAYER_MAP_TRANSFER, new PlayerMapTransitionHandler());
            registerHandler(RecvOpcode.USE_MAPLELIFE, new UseMapleLifeHandler());
            registerHandler(RecvOpcode.USE_CATCH_ITEM, new UseCatchItemHandler());
            registerHandler(RecvOpcode.MOB_DAMAGE_MOB_FRIENDLY, new MobDamageMobFriendlyHandler());
            registerHandler(RecvOpcode.PARTY_SEARCH_REGISTER, new PartySearchRegisterHandler());
            registerHandler(RecvOpcode.PARTY_SEARCH_START, new PartySearchStartHandler());
            registerHandler(RecvOpcode.ITEM_SORT2, new InventorySortHandler());
            registerHandler(RecvOpcode.LEFT_KNOCKBACK, new LeftKnockbackHandler());
            registerHandler(RecvOpcode.SNOWBALL, new SnowballHandler());
            registerHandler(RecvOpcode.COCONUT, new CoconutHandler());
            registerHandler(RecvOpcode.ARAN_COMBO_COUNTER, new AranComboHandler());
            registerHandler(RecvOpcode.CLICK_GUIDE, new ClickGuideHandler());
            registerHandler(RecvOpcode.FREDRICK_ACTION, new FredrickHandler());
            registerHandler(RecvOpcode.MONSTER_CARNIVAL, new MonsterCarnivalHandler());
            registerHandler(RecvOpcode.REMOTE_STORE, new RemoteStoreHandler());
            registerHandler(RecvOpcode.WEDDING_ACTION, new WeddingHandler());
            registerHandler(RecvOpcode.WEDDING_TALK, new WeddingTalkHandler());
            registerHandler(RecvOpcode.WEDDING_TALK_MORE, new WeddingTalkMoreHandler());
            registerHandler(RecvOpcode.WATER_OF_LIFE, new UseWaterOfLifeHandler());
            registerHandler(RecvOpcode.ADMIN_CHAT, new AdminChatHandler());
            registerHandler(RecvOpcode.MOVE_DRAGON, new MoveDragonHandler());
        }
    }
}
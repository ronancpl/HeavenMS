/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tools.packets;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.MapleCharacter;
import java.util.ArrayList;
import java.util.List;
import tools.MaplePacketCreator;
import tools.StringUtil;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 * CField_Wedding, CField_WeddingPhoto, CWeddingMan, OnMarriageResult, and all Wedding/Marriage enum/structs.
 * 
 * @author Eric
 */
public class Wedding extends MaplePacketCreator {
    private static final short MARRIAGE_REQUEST = 0x48;
    private static final short MARRIAGE_RESULT = 0x49;
    private static final short WEDDING_GIFT_RESULT = 0x4A;
    private static final short NOTIFY_MARRIED_PARTNER_MAP_TRANSFER = 0x4B;
    private static final short WEDDING_PHOTO = 0x2B;
    private static final short WEDDING_PROGRESS = 0x140;
    private static final short WEDDING_CEREMONY_END = 0x141;
    
    /*
        00000000 CWeddingMan     struc ; (sizeof=0x104)
        00000000 vfptr           dd ?                    ; offset
        00000004 ___u1           $01CBC6800BD386B8A8FD818EAD990BEC ?
        0000000C m_mCharIDToMarriageNo ZMap<unsigned long,unsigned long,unsigned long> ?
        00000024 m_mReservationPending ZMap<unsigned long,ZRef<GW_WeddingReservation>,unsigned long> ?
        0000003C m_mReservationPendingGroom ZMap<unsigned long,ZRef<CUser>,unsigned long> ?
        00000054 m_mReservationPendingBride ZMap<unsigned long,ZRef<CUser>,unsigned long> ?
        0000006C m_mReservationStartUser ZMap<unsigned long,unsigned long,unsigned long> ?
        00000084 m_mReservationCompleted ZMap<unsigned long,ZRef<GW_WeddingReservation>,unsigned long> ?
        0000009C m_mGroomWishList ZMap<unsigned long,ZRef<ZArray<ZXString<char> > >,unsigned long> ?
        000000B4 m_mBrideWishList ZMap<unsigned long,ZRef<ZArray<ZXString<char> > >,unsigned long> ?
        000000CC m_mEngagementPending ZMap<unsigned long,ZRef<GW_MarriageRecord>,unsigned long> ?
        000000E4 m_nCurrentWeddingState dd ?
        000000E8 m_dwCurrentWeddingNo dd ?
        000000EC m_dwCurrentWeddingMap dd ?
        000000F0 m_bIsReservationLoaded dd ?
        000000F4 m_dwNumGuestBless dd ?
        000000F8 m_bPhotoSuccess dd ?
        000000FC m_tLastUpdate   dd ?
        00000100 m_bStartWeddingCeremony dd ?
        00000104 CWeddingMan     ends
    */
    
    public class Field_Wedding {
        public int m_nNoticeCount;
        public int m_nCurrentStep;
        public int m_nBlessStartTime;
    }
    
    public class Field_WeddingPhoto {
        public boolean m_bPictureTook;
    }
    
    public class GW_WeddingReservation {
        public int dwReservationNo;
        public int dwGroom, dwBride;
        public String sGroomName, sBrideName;
        public int usWeddingType;
    }
    
    public class WeddingWishList {
        public MapleCharacter pUser;
        public int dwMarriageNo;
        public int nGender;
        public int nWLType;
        public int nSlotCount;
        public List<String> asWishList = new ArrayList<>();
        public int usModifiedFlag; // dword
        public boolean bLoaded;
    }
    
    public class GW_WeddingWishList {
        public final int WEDDINGWL_MAX = 0xA; // enum WEDDINGWL
        public int dwReservationNo;
        public byte nGender;
        public String sItemName;
    }
    
    public enum MarriageStatus {
        SINGLE(0x0),
        ENGAGED(0x1),
        RESERVED(0x2),
        MARRIED(0x3);
        private int ms;
        private MarriageStatus(int ms) {
            this.ms = ms;
        }
        
        public int getMarriageStatus() {
            return ms;
        }
    }
    
    public enum MarriageRequest {
        AddMarriageRecord(0x0),
        SetMarriageRecord(0x1),
        DeleteMarriageRecord(0x2),
        LoadReservation(0x3),
        AddReservation(0x4),
        DeleteReservation(0x5),
        GetReservation(0x6);
        private int req;
        private MarriageRequest(int req) {
            this.req = req;
        }
        
        public int getMarriageRequest() {
            return req;
        }
    }
    
    public enum WeddingType {
        CATHEDRAL(0x1),
        VEGAS(0x2),
        CATHEDRAL_PREMIUM(0xA),
        CATHEDRAL_NORMAL(0xB),
        VEGAS_PREMIUM(0x14),
        VEGAS_NORMAL(0x15);
        private int wt;
        private WeddingType(int wt) {
            this.wt = wt;
        }
        
        public int getType() {
            return wt;
        }
    }
    
    public enum WeddingMap {
        WEDDINGTOWN(680000000),
        CHAPEL_STARTMAP(680000110),
        CATHEDRAL_STARTMAP(680000210),
        PHOTOMAP(680000300),
        EXITMAP(680000500);
        private int wm;
        private WeddingMap(int wm) {
            this.wm = wm;
        }
        
        public int getMap() {
            return wm;
        }
    }
    
    public enum WeddingItem {
        WR_MOONSTONE(1112803), // Wedding Ring
        WR_STARGEM(1112806),
        WR_GOLDENHEART(1112807),
        WR_SILVERSWAN(1112809),
        ERB_MOONSTONE(2240000), // Engagement Ring Box
        ERB_STARGEM(2240001),
        ERB_GOLDENHEART(2240002),
        ERB_SILVERSWAN(2240003),
        ERBE_MOONSTONE(4031357), // Engagement Ring Box (Empty)
        ER_MOONSTONE(4031358), // Engagement Ring
        ERBE_STARGEM(4031359),
        ER_STARGEM(4031360),
        ERBE_GOLDENHEART(4031361),
        ER_GOLDENHEART(4031362),
        ERBE_SILVERSWAN(4031363),
        ER_SILVERSWAN(4031364),
        PARENTS_BLESSING(4031373), // Parents Blessing
        OFFICIATORS_PERMISSION(4031374), // Officiator's Permission
        WR_CATHEDRAL_PREMIUM(4031375), // Wedding Ring?
        WR_VEGAS_PREMIUM(4031376),
        IB_VEGAS(4031377),      // toSend invitation
        IB_CATHEDRAL(4031395),  // toSend invitation
        IG_VEGAS(4031406),      // rcvd invitation
        IG_CATHEDRAL(4031407),  // rcvd invitation
        OB_FORCOUPLE(4031424), // Onyx Box? For Couple
        WR_CATHEDRAL_NORMAL(4031480), // Wedding Ring?
        WR_VEGAS_NORMAL(4031481),
        WT_CATHEDRAL_NORMAL(5251000), // Wedding Ticket
        WT_VEGAS_NORMAL(5251001),
        WT_VEGAS_PREMIUM(5251002),
        WT_CATHEDRAL_PREMIUM(5251003);
        private int wi;
        private WeddingItem(int wi) {
            this.wi = wi;
        }
        
        public int getItem() {
            return wi;
        }
    }
    
    /**
     * <name> has requested engagement. Will you accept this proposal?
     * 
     *    @param name
     *    @param playerid
     *    @return mplew
     */
    public static byte[] OnMarriageRequest(String name, int playerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(MARRIAGE_REQUEST);
        mplew.write(0); //mode, 0 = engage, 1 = cancel, 2 = answer.. etc
        mplew.writeMapleAsciiString(name); // name
        mplew.writeInt(playerid); // playerid
        return mplew.getPacket();
    }
    
    /**
     * A quick rundown of how (I think based off of enough BMS searching) WeddingPhoto_OnTakePhoto works:
     * - We send this packet with (first) the Groom / Bride IGNs
     * - We then send a fieldId (unsure about this part at the moment, 90% sure it's the id of the map)
     * - After this, we write an integer of the amount of characters within the current map (which is the Cake Map -- exclude users within Exit Map)
     * - Once we've retrieved the size of the characters, we begin to write information about them (Encode their name, guild, etc info)
     * - Now that we've Encoded our character data, we begin to Encode the ScreenShotPacket which requires a TemplateID, IGN, and their positioning
     * - Finally, after encoding all of our data, we send this packet out to a MapGen application server
     * - The MapGen server will then retrieve the packet byte array and convert the bytes into a ImageIO 2D JPG output
     * - The result after converting into a JPG will then be remotely uploaded to /weddings/ with ReservedGroomName_ReservedBrideName to be displayed on the web server.
     * 
     * - Will no longer continue Wedding Photos, needs a WvsMapGen :(
     * 
     *    @param ReservedGroomName The groom IGN of the wedding
     *    @param ReservedBrideName The bride IGN of the wedding
     *    @param m_dwField The current field id (the id of the cake map, ex. 680000300)
     *    @param m_uCount The current user count (equal to m_dwUsers.size)
     *    @param m_dwUsers The List of all MapleCharacter guests within the current cake map to be encoded
     *    @return mplew (MaplePacket) Byte array to be converted and read for byte[]->ImageIO
     */
    public static byte[] OnTakePhoto(String ReservedGroomName, String ReservedBrideName, int m_dwField, List<MapleCharacter> m_dwUsers) { // OnIFailedAtWeddingPhotos
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(WEDDING_PHOTO); // v53 header, convert -> v83
        mplew.writeMapleAsciiString(ReservedGroomName);
        mplew.writeMapleAsciiString(ReservedBrideName);
        mplew.writeInt(m_dwField); // field id?
        mplew.writeInt(m_dwUsers.size());
        
        for (MapleCharacter guest : m_dwUsers) {
            // Begin Avatar Encoding
            addCharLook(mplew, guest, false); // CUser::EncodeAvatar
            mplew.writeInt(30000); // v20 = *(_DWORD *)(v13 + 2192) -- new groom marriage ID??
            mplew.writeInt(30000); // v20 = *(_DWORD *)(v13 + 2192) -- new bride marriage ID??
            mplew.writeMapleAsciiString(guest.getName());
            mplew.writeMapleAsciiString(guest.getGuildId() > 0 && guest.getGuild() != null ? guest.getGuild().getName() : "");
            mplew.writeShort(guest.getGuildId() > 0 && guest.getGuild() != null ? guest.getGuild().getLogoBG() : 0);
            mplew.write(guest.getGuildId() > 0 && guest.getGuild() != null ? guest.getGuild().getLogoBGColor() : 0);
            mplew.writeShort(guest.getGuildId() > 0 && guest.getGuild() != null ? guest.getGuild().getLogo() : 0);
            mplew.write(guest.getGuildId() > 0 && guest.getGuild() != null ? guest.getGuild().getLogoColor() : 0);
            mplew.writeShort(guest.getPosition().x); // v18 = *(_DWORD *)(v13 + 3204);
            mplew.writeShort(guest.getPosition().y); // v20 = *(_DWORD *)(v13 + 3208);
            // Begin Screenshot Encoding
            mplew.write(1); // // if ( *(_DWORD *)(v13 + 288) ) { COutPacket::Encode1(&thisa, v20);
            // CPet::EncodeScreenShotPacket(*(CPet **)(v13 + 288), &thisa);
            mplew.writeInt(1); // dwTemplateID
            mplew.writeMapleAsciiString(guest.getName()); // m_sName
            mplew.writeShort(guest.getPosition().x); // m_ptCurPos.x
            mplew.writeShort(guest.getPosition().y); // m_ptCurPos.y
            mplew.write(guest.getStance()); // guest.m_bMoveAction
        }
        
        return mplew.getPacket();
    }
    
    /**
     * Enable spouse chat and their engagement ring without @relog
     * 
     *    @param marriageId
     *    @param chr
     *    @param wedding
     *    @return mplew
     */
    public static byte[] OnMarriageResult(int marriageId, MapleCharacter chr, boolean wedding) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(MARRIAGE_RESULT);
        mplew.write(11);
        mplew.writeInt(marriageId);
        mplew.writeInt(chr.getGender() == 0 ? chr.getId() : chr.getPartnerId());
        mplew.writeInt(chr.getGender() == 0 ? chr.getPartnerId() : chr.getId());
        mplew.writeShort(wedding ? 3 : 1);
        if (wedding) {
            mplew.writeInt(chr.getMarriageItemId());
            mplew.writeInt(chr.getMarriageItemId());
        } else {
            mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
            mplew.writeInt(1112803); // Engagement Ring's Outcome (doesn't matter for engagement)
        }
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? chr.getName() : MapleCharacter.getNameById(chr.getPartnerId()), '\0', 13));
        mplew.writeAsciiString(StringUtil.getRightPaddedStr(chr.getGender() == 0 ? MapleCharacter.getNameById(chr.getPartnerId()) : chr.getName(), '\0', 13));
        
        return mplew.getPacket();
    }
    
    /**
     * To exit the Engagement Window (Waiting for her response...), we send a GMS-like pop-up.
     * 
     *    @param msg
     *    @return mplew
     */
    public static byte[] OnMarriageResult(final byte msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(MARRIAGE_RESULT);
        mplew.write(msg);
        if (msg == 36) {
            mplew.write(1);
            mplew.writeMapleAsciiString("You are now engaged.");
        }
        return mplew.getPacket();
    }
    
    /**
     * The World Map includes 'loverPos' in which this packet controls
     * 
     *    @param partner
     *    @param mapid
     *    @return mplew
     */
    public static byte[] OnNotifyWeddingPartnerTransfer(int partner, int mapid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(NOTIFY_MARRIED_PARTNER_MAP_TRANSFER);
        mplew.writeInt(mapid);
        mplew.writeInt(partner);
        
        return mplew.getPacket();
    }
    
    /**
     * The wedding packet to display Pelvis Bebop and enable the Wedding Ceremony Effect between two characters
     * CField_Wedding::OnWeddingProgress - Stages
     * CField_Wedding::OnWeddingCeremonyEnd - Wedding Ceremony Effect
     * 
     *    @param SetBlessEffect
     *    @param groom
     *    @param bride
     *    @param step
     *    @return mplew
     */
    public static byte[] OnWeddingProgress(boolean SetBlessEffect, int groom, int bride, byte step) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SetBlessEffect ? WEDDING_CEREMONY_END : WEDDING_PROGRESS);
        if (!SetBlessEffect) { // in order for ceremony packet to send, byte step = 2 must be sent first
            mplew.write(step);
        }
        mplew.writeInt(groom);
        mplew.writeInt(bride);
        return mplew.getPacket();
    }
    
    /**
     * When we open a Wedding Invitation, we display the Bride & Groom
     * 
     *    @param groom
     *    @param bride
     *    @return mplew
     */
    public static byte[] sendWeddingInvitation(String groom, String bride) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(MARRIAGE_RESULT);
        mplew.write(15);
        mplew.writeMapleAsciiString(groom);
        mplew.writeMapleAsciiString(bride);
        mplew.writeShort(1); // 0 = Cathedral Normal?, 1 = Cathedral Premium?, 2 = Chapel Normal?
        return mplew.getPacket();
    }
    
    public static byte[] sendWishList() { // fuck my life
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(MARRIAGE_REQUEST);
        mplew.write(9);
        return mplew.getPacket();
    }

    /**
     * Handles all of WeddingWishlist packets
     * 
     *    @param mode
     *    @param itemnames
     *    @param items
     *    @return mplew
     */
    public static byte[] OnWeddingGiftResult(byte mode, List<String> itemnames, List<Item> items) {
        // if (itemnames == null || itemnames.size() < 1) { // for now lol
        //     itemnames = new ArrayList<>();
        //     itemnames.add("mesos");
        //     itemnames.add("rare items");
        //     itemnames.add("more mesos");
        // }
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(WEDDING_GIFT_RESULT);
        mplew.write(mode);
        switch(mode) {
            case 0x09: { // Load Wedding Registry
                mplew.write(itemnames.size());
                for (String names : itemnames) {
                    mplew.writeMapleAsciiString(names);
                }
                mplew.write(itemnames.size());
                for (String names : itemnames) {
                    mplew.writeMapleAsciiString(names);
                }
                // need to load items somehow
                break;
            }
            case 0xA: // Load Bride's Wishlist 
            case 0xF: // 10, 15, 16 = CWishListRecvDlg::OnPacket
            case 0xB: { // Add Item to Wedding Registry 
                // 11 : You have sent a gift | 12 : You cannot give more than one present for each wishlist | 13 : Failed to send the gift. | 14 : Failed to send the gift.
                if (mode == 0xB) {
                    mplew.write(itemnames.size());
                    for (String names : itemnames) {
                        mplew.writeMapleAsciiString(names);
                    }
                }
                switch (items.get((items.size() - 1)).getInventoryType()) {
                    case EQUIP:
                        mplew.writeLong(4);
                        break;
                    case USE:
                        mplew.writeLong(8);
                        break;
                    case SETUP:
                        mplew.writeLong(16);
                        break;
                    case ETC:
                        mplew.writeLong(32);
                        break;
                    default: // impossible flag, cash item can't be sent
                        if (items.get((items.size() - 1)).getInventoryType() != MapleInventoryType.CASH) {
                            mplew.writeLong(0);
                        }
                }
                if (mode == 0xA) { // random unknown bytes involved within Bride's Wishlist
                    mplew.writeInt(0);
                }
                mplew.write(items.size());
                for (Item item : items) {
                    MaplePacketCreator.addItemInfo(mplew, item, true);
                }
                break;
            }
            default: {
                System.out.println("Unknown Wishlist Mode: " + mode);
                break;
            }
        }
        return mplew.getPacket();
    }
} 
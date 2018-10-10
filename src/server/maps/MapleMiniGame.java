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
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import net.server.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 * @author Ronan (HeavenMS)
 */
public class MapleMiniGame extends AbstractMapleMapObject {
    private MapleCharacter owner;
    private MapleCharacter visitor;
    private String password;
    private MiniGameType GameType = MiniGameType.UNDEFINED;
    private int piecetype;
    private int inprogress = 0;
    private int[] piece = new int[250];
    private List<Integer> list4x3 = new ArrayList<>();
    private List<Integer> list5x4 = new ArrayList<>();
    private List<Integer> list6x5 = new ArrayList<>();
    private String description;
    private int loser = 1;
    private int firstslot = 0;
    private int visitorpoints = 0, visitorscore = 0, visitorforfeits = 0, lastvisitor = -1;
    private int ownerpoints = 0, ownerscore = 0, ownerforfeits = 0;
    private long nextavailabletie = 0;
    private int matchestowin = 0;

    public static enum MiniGameType {
        UNDEFINED(0), OMOK(1), MATCH_CARD(2);
        private int value = 0;

        private MiniGameType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public static enum MiniGameResult {
        WIN, LOSS, TIE;
    }
    
    public MapleMiniGame(MapleCharacter owner, String description, String password) {
        this.owner = owner;
        this.description = description;
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }
    
    public boolean checkPassword(String sentPw) {
        return this.password.length() == 0 || sentPw.toLowerCase().contentEquals(this.password.toLowerCase());
    }
    
    public boolean hasFreeSlot() {
        return visitor == null;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner.equals(c);
    }
    
    public void addVisitor(MapleCharacter challenger) {
        visitor = challenger;
        if (lastvisitor != challenger.getId()) {
            ownerscore = 0;
            ownerforfeits = 0;
            
            visitorscore = 0;
            visitorforfeits = 0;
            lastvisitor = challenger.getId();
        }
        
        MapleCharacter owner = this.getOwner();
        if (GameType == MiniGameType.OMOK) {
            owner.announce(MaplePacketCreator.getMiniGameNewVisitor(this, challenger, 1));
            owner.getMap().broadcastMessage(MaplePacketCreator.addOmokBox(owner, 2, 0));
        } else if (GameType == MiniGameType.MATCH_CARD) {
            owner.announce(MaplePacketCreator.getMatchCardNewVisitor(this, challenger, 1));
            owner.getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(owner, 2, 0));
        }
    }

    public void removeVisitor(MapleCharacter challenger) {
        if (visitor == challenger) {
            visitor = null;
            this.getOwner().getClient().announce(MaplePacketCreator.getMiniGameRemoveVisitor());
            if (GameType == MiniGameType.OMOK) {
                this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(owner, 1, 0));
            } else if (GameType == MiniGameType.MATCH_CARD) {
                this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addMatchCardBox(owner, 1, 0));
            }
        }
    }

    public boolean isVisitor(MapleCharacter challenger) {
        return visitor == challenger;
    }

    public void broadcastToOwner(final byte[] packet) {
        MapleClient c = owner.getClient();
        if (c != null && c.getSession() != null) {
            c.announce(packet);
        }
    }
    
    public void broadcastToVisitor(final byte[] packet) {
        if (visitor != null) {
            visitor.getClient().announce(packet);
        }
    }

    public void setFirstSlot(int type) {
        firstslot = type;
    }

    public int getFirstSlot() {
        return firstslot;
    }

    private void updateMiniGameBox() {
        this.getOwner().getMap().broadcastMessage(MaplePacketCreator.addOmokBox(owner, visitor != null ? 2 : 1, inprogress));
    }
    
    private void minigameMatchFinished() {
        inprogress = 0;
        updateMiniGameBox();
    }
    
    public void minigameMatchStarted() {
        inprogress = 1;
    }
    
    public boolean isMatchInProgress() {
        return inprogress != 0;
    }
    
    public void denyTie(MapleCharacter chr) {
        if (this.isOwner(chr)) {
            inprogress |= (1 << 1);
        } else {
            inprogress |= (1 << 2);
        }
    }
    
    public boolean isTieDenied(MapleCharacter chr) {
        if (this.isOwner(chr)) {
            return ((inprogress >> 1) % 2) == 1;
        } else {
            return ((inprogress >> 2) % 2) == 1;
        }
    }
    
    public void minigameMatchOwnerWins(boolean forfeit) {
        owner.setMiniGamePoints(visitor, 1, this.isOmok());
        if (visitorforfeits < 4 || !forfeit) ownerscore += 50;
        visitorscore += (15 * (forfeit ? -1 : 1));
        if (forfeit) visitorforfeits++;
        
        this.broadcast(MaplePacketCreator.getMiniGameOwnerWin(this, forfeit));
        minigameMatchFinished();
    }
    
    public void minigameMatchVisitorWins(boolean forfeit) {
        owner.setMiniGamePoints(visitor, 2, this.isOmok());
        if (ownerforfeits < 4 || !forfeit) visitorscore += 50;
        ownerscore += (15 * (forfeit ? -1 : 1));
        if (forfeit) ownerforfeits++;
        
        this.broadcast(MaplePacketCreator.getMiniGameVisitorWin(this, forfeit));
        minigameMatchFinished();
    }
    
    public void minigameMatchDraw() {
        owner.setMiniGamePoints(visitor, 3, this.isOmok());
        
        long timeNow = Server.getInstance().getCurrentTime();
        if (nextavailabletie <= timeNow) {
            visitorscore += 10;
            ownerscore += 10;
            
            nextavailabletie = timeNow + 5 * 60 * 1000;
        }
        
        this.broadcast(MaplePacketCreator.getMiniGameTie(this));
        minigameMatchFinished();
    }
    
    public void setOwnerPoints() {
        ownerpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints == visitorpoints) {
                minigameMatchDraw();
            } else if (ownerpoints > visitorpoints) {
                minigameMatchOwnerWins(false);
            } else {
                minigameMatchVisitorWins(false);
            }
            ownerpoints = 0;
            visitorpoints = 0;
        }
    }

    public void setVisitorPoints() {
        visitorpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints > visitorpoints) {
                minigameMatchOwnerWins(false);
            } else if (visitorpoints > ownerpoints) {
                minigameMatchVisitorWins(false);
            } else {
                minigameMatchDraw();
            }
            ownerpoints = 0;
            visitorpoints = 0;
        }
    }

    public void setMatchesToWin(int type) {
        matchestowin = type;
    }

    public void setPieceType(int type) {
        piecetype = type;
    }

    public int getPieceType() {
        return piecetype;
    }

    public void setGameType(MiniGameType game) {
        GameType = game;
        if (GameType == MiniGameType.MATCH_CARD) {
            if (matchestowin == 6) {
                for (int i = 0; i < 6; i++) {
                    list4x3.add(i);
                    list4x3.add(i);
                }
            } else if (matchestowin == 10) {
                for (int i = 0; i < 10; i++) {
                    list5x4.add(i);
                    list5x4.add(i);
                }
            } else {
                for (int i = 0; i < 15; i++) {
                    list6x5.add(i);
                    list6x5.add(i);
                }
            }
        }
    }

    public MiniGameType getGameType() {
        return GameType;
    }
    
    public boolean isOmok() {
        return GameType.equals(MiniGameType.OMOK);
    }

    public void shuffleList() {
        if (matchestowin == 6) {
            Collections.shuffle(list4x3);
        } else if (matchestowin == 10) {
            Collections.shuffle(list5x4);
        } else {
            Collections.shuffle(list6x5);
        }
    }

    public int getCardId(int slot) {
        int cardid;
        if (matchestowin == 6) {
            cardid = list4x3.get(slot);
        } else if (matchestowin == 10) {
            cardid = list5x4.get(slot);
        } else {
            cardid = list6x5.get(slot);
        }
        return cardid;
    }

    public int getMatchesToWin() {
        return matchestowin;
    }

    public void setLoser(int type) {
        loser = type;
    }

    public int getLoser() {
        return loser;
    }

    public void broadcast(final byte[] packet) {
        broadcastToOwner(packet);
        broadcastToVisitor(packet);
    }

    public void chat(MapleClient c, String chat) {
        broadcast(MaplePacketCreator.getPlayerShopChat(c.getPlayer(), chat, isOwner(c.getPlayer())));
    }

    public void sendOmok(MapleClient c, int type) {
        c.announce(MaplePacketCreator.getMiniGame(c, this, isOwner(c.getPlayer()), type));
    }

    public void sendMatchCard(MapleClient c, int type) {
        c.announce(MaplePacketCreator.getMatchCard(c, this, isOwner(c.getPlayer()), type));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter getVisitor() {
        return visitor;
    }

    public void setPiece(int move1, int move2, int type, MapleCharacter chr) {
        int slot = move2 * 15 + move1 + 1;
        if (piece[slot] == 0) {
            piece[slot] = type;
            this.broadcast(MaplePacketCreator.getMiniGameMoveOmok(this, move1, move2, type));
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 11; x++) {
                    if (searchCombo(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.minigameMatchOwnerWins(false);
                            this.setLoser(0);
                        } else {
                            this.minigameMatchVisitorWins(false);
                            this.setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = (y2 * 15 + x2 + 1);
                                piece[slot2] = 0;
                            }
                        }
                    }
                }
            }
            for (int y = 0; y < 15; y++) {
                for (int x = 4; x < 15; x++) {
                    if (searchCombo2(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.minigameMatchOwnerWins(false);
                            this.setLoser(0);
                        } else {
                            this.minigameMatchVisitorWins(false);
                            this.setLoser(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = (y2 * 15 + x2 + 1);
                                piece[slot2] = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean searchCombo(int x, int y, int type) {
        int slot = y * 15 + x + 1;
        for (int i = 0; i < 5; i++) {
            if (piece[slot + i] == type) {
                if (i == 4) {
                    return true;
                }
            } else {
                break;
            }
        }
        for (int j = 15; j < 17; j++) {
            for (int i = 0; i < 5; i++) {
                if (piece[slot + i * j] == type) {
                    if (i == 4) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private boolean searchCombo2(int x, int y, int type) {
        int slot = y * 15 + x + 1;
        for (int j = 14; j < 15; j++) {
            for (int i = 0; i < 5; i++) {
                if (piece[slot + i * j] == type) {
                    if (i == 4) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }

    public String getDescription() {
        return description;
    }
    
    public int getOwnerScore() {
        return ownerscore;
    }
    
    public int getVisitorScore() {
        return visitorscore;
    }

    @Override
    public void sendDestroyData(MapleClient client) {}

    @Override
    public void sendSpawnData(MapleClient client) {}

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MINI_GAME;
    }
}

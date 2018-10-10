package server.maps;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import constants.ServerConstants;
import tools.MaplePacketCreator;

public class MapleKite extends AbstractMapleMapObject {

    private Point pos;
    private MapleCharacter owner;
    private String text;
    private int ft;
    private int itemid;

    public MapleKite(MapleCharacter owner, String text, int itemid) {
        this.owner = owner;
        this.pos = owner.getPosition();
        this.ft = owner.getFh();
        this.text = text;
        this.itemid = itemid;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.KITE;
    }

    @Override
    public Point getPosition() {
        return pos.getLocation();
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    public final byte[] makeSpawnData() {
        return MaplePacketCreator.spawnKite(getObjectId(), itemid, owner.getName(), text, pos, ft);
    }

    public final byte[] makeDestroyData() {
        return MaplePacketCreator.removeKite(getObjectId(), 0);
    }
}
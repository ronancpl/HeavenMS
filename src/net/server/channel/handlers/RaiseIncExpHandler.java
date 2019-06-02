package net.server.channel.handlers;

import java.util.Map;

import client.MapleClient;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleItemInformationProvider.QuestConsItem;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Xari
 * @author Ronan - added concurrency protection and quest progress limit
 */
public class RaiseIncExpHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte inventorytype = slea.readByte();//nItemIT
        short slot = slea.readShort();//nSlotPosition
        int itemid = slea.readInt();//nItemID    
        
        if (c.tryacquireClient()) {
            try {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                QuestConsItem consItem = ii.getQuestConsumablesInfo(itemid);
                if (consItem == null) {
                    return;
                }

                int questid = consItem.questid;
                Map<Integer, Integer> consumables = consItem.items;

                int consId;
                MapleInventory inv = c.getPlayer().getInventory(MapleInventoryType.getByType(inventorytype));
                inv.lockInventory();
                try {
                    consId = inv.getItem(slot).getItemId();
                    if (!consumables.containsKey(consId) || !c.getPlayer().haveItem(consId)) {
                        return;
                    }

                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(inventorytype), (short) slot, (short) 1, false, true);
                } finally {
                    inv.unlockInventory();
                }

                int nextValue = Math.min(consumables.get(consId) + Integer.parseInt(c.getPlayer().getQuestInfo(questid)), consItem.exp * consItem.grade);
                c.getPlayer().updateQuestInfo(questid, "" + nextValue);
            } finally {
                c.releaseClient();
            }
        }
    }
}

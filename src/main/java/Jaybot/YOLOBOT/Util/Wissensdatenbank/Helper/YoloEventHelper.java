package Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper;

import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import Jaybot.YOLOBOT.YoloState;
import core.game.Observation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Torsten on 28.05.2017.
 */
public class YoloEventHelper {

    private YoloEventHelper() {
        // Nothing to do here --> static helper class
    }

    public static void setSpawnedIType(YoloEvent event2Learn, YoloState currentState, YoloState previousState) {
        SimpleState previousSimpleState = previousState.getSimpleState();
        SimpleState currentSimpleState = currentState.getSimpleState();
        ArrayList<Observation>[][] obs = previousState.getObservationGrid();
        int gridXCount = obs.length;
        int gridYCount = obs[0].length;

        for (int x = 0; x < gridXCount; x++) {
            for (int y = 0; y < gridYCount; y++) {
                int maskPrevious = previousSimpleState.getMask(x, y);
                int maskCurrent = currentSimpleState.getMask(x, y);

                // xor the masks to find the difference between previous and current state
                int diffMask = (maskPrevious ^ maskCurrent);

                // if the difference is a 1-bit which exists in the current state as well
                // i.e. it was added/spawned and not removed/killed
                if ((diffMask & maskCurrent) != 0){
                    // set the spawned type
                    event2Learn.setSpawns(Integer.numberOfTrailingZeros(diffMask));
                    return;
                }
            }
        }
    }

    public static void setTeleportIType(YoloEvent event2Learn, YoloState currentState, YoloState previousState) {
        int avatarCurrentX = currentState.getAvatarX();
        int avatarCurrentY = currentState.getAvatarY();
        SimpleState previousSimpleState = previousState.getSimpleState();

        int distance = Math.abs(avatarCurrentX - previousState.getAvatarX()) + Math.abs(avatarCurrentY - previousState.getAvatarY());

        // only learn something if it really was a teleport i.e. the current position is more than one cell away from the previous position
        if (distance > 1) {
            // retrieve the iType of the sprite from the previous state which was at the position the avatar is in the current state
            int teleportToItypeAvatar = Integer.numberOfTrailingZeros(previousSimpleState.getMask(avatarCurrentX, avatarCurrentY));
            event2Learn.setTeleportTo(teleportToItypeAvatar);
        }
    }

    public static void setITypeChange(YoloEvent event, YoloState currentState, YoloState previousState) {
        event.setOldIType(previousState.getAvatar().itype);
        event.setNewIType(currentState.getAvatar().itype);
    }

    public static void setInventoryChange(YoloEvent event, YoloState currentState, YoloState previousState) {
        // calculate inventory changes
        Map<Integer, Integer> previousInventory = previousState.getAvatarResources();
        Map<Integer, Integer> currentInventory = currentState.getAvatarResources();

        // no inventory before and after --> no change
        if ((previousInventory == null || previousInventory.isEmpty()) &&
                (currentInventory == null || currentInventory.isEmpty())) {
            event.setAddInventorySlotItem(YoloEvent.UNDEFINED);
            event.setRemoveInventorySlotItem(YoloEvent.UNDEFINED);
        }
        // previous empty, now not empty --> add
        else if (previousInventory.isEmpty() && !currentInventory.isEmpty()) {
            // only one key can exist, as only one item can be collected in one step
            Integer key = currentInventory.keySet().iterator().next();
            event.setAddInventorySlotItem(key);
        }
        // previous not empty, now empty --> remove
        else if (!previousInventory.isEmpty() && currentInventory.isEmpty()) {
            // only one key can exist, as only one item can be removed in one step
            Integer key = previousInventory.keySet().iterator().next();
            event.setRemoveInventorySlotItem(key);
        }
        // previous not empty, now not empty --> we have to check
        else {
            boolean foundInventoryChange = false;
            List<Integer> keySetPrevious = new LinkedList<>(previousInventory.keySet());

            for (Integer currentKey : currentInventory.keySet()) {
                if (keySetPrevious.contains(currentKey)) {
                    keySetPrevious.remove(currentKey);

                    Integer currentAmount = currentInventory.get(currentKey);
                    Integer previousAmount = previousInventory.get(currentKey);
                    if (currentAmount == previousAmount) {
                        continue;
                    } else if (currentAmount > previousAmount) {
                        event.setAddInventorySlotItem(currentKey);
                        foundInventoryChange = true;
                        break;
                    } else {
                        event.setRemoveInventorySlotItem(currentKey);
                        foundInventoryChange = true;
                        break;
                    }
                }
                // key not found in previous inventory --> added
                else {
                    event.setAddInventorySlotItem(currentKey);
                    foundInventoryChange = true;
                    // only one inventory item can be added so break here
                    break;
                }
            }

            if (!foundInventoryChange && !keySetPrevious.isEmpty()) {
                event.setRemoveInventorySlotItem(keySetPrevious.get(0));
            }
        }
    }
}

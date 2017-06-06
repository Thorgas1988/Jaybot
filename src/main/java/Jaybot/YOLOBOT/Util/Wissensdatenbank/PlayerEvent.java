package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Util.RandomForest.InvolvedActors;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;

public class PlayerEvent implements YoloEventController {

    /**
     * Class description:
     * A controller for YoloEvent, i.e. a two-class classifier
     */

    private static final boolean DEBUG = true;
    private RandomForest randomForest;

    /**
     * Constructor:
     * Initialize all member variables. Int to 0, new class instance.
     */
    public PlayerEvent(int maxIndices) {
        randomForest = new RandomForest(maxIndices, 100);
    }

    /**
     * Learn other events from observation w.r.t inventory items and event
     * 1) Increase event counter
     * 2) IF move==true && specialEventRandomForest and defaultEvent never move once:
     * update event counter, cancel counter
     * reset cancel trigger and learn the event for cancel trigger with false
     * 3) Check if the "input" event is identical to defaultEvent, by calling the comparable function of YoloEvent
     * IF it is so: learn this input event for default event
     * ELSE: learn this input event for special event
     *
     * @param inventoryItems  An array which stores the number of each inventory type
     * @param newItype        chage of the avatar type
     * @param move            a nil action or a specific action
     * @param scoreDelta      score change
     * @param killed          terminal state
     * @param spawnedItype    new game object
     * @param teleportTo      new game object
     * @param winGame         terminal state
     * @param addInventory    change of the avatar inventory
     * @param removeInventory change of the avatar inventory
     */
    public void learnEventHappened(InvolvedActors actors, byte[] inventoryItems, byte newItype, boolean move, byte scoreDelta, boolean killed, byte spawnedItype, byte teleportTo, boolean winGame, byte addInventory, byte removeInventory) {
        YoloEvent event = new YoloEvent();
        event.setNewIType(newItype);
        event.setOldIType(actors.getPlayerIType());
        event.setAddInventorySlotItem(addInventory);
        event.setDefeat(killed);
        event.setBlocked(!move);
        event.setRemoveInventorySlotItem(removeInventory);
        event.setScoreDelta(scoreDelta);
        event.setSpawns(spawnedItype);
        event.setTeleportTo(teleportTo);
        event.setVictory(winGame);

        randomForest.train(actors, inventoryItems, event);
    }

    public void learnEventHappened(InvolvedActors actors, byte[] inventoryItems, YoloEvent event) {
        randomForest.train(actors, inventoryItems, event);
    }

    /**
     * @return boolean if this move is beeing blocked
     */
    public boolean willCancel(InvolvedActors actors, byte[] inventoryItems) {
        YoloEvent event = randomForest.getEvent(actors, inventoryItems);
        return event.isBlocked();
    }

    /**
     * @param inventoryItems An array which stores the number of each inventory type
     * @return The YoloEvent with the highest probability
     */
    public YoloEvent getEvent(InvolvedActors actors, byte[] inventoryItems) {
        return randomForest.getEvent(actors, inventoryItems);
    }

    public boolean hasEventForActors(InvolvedActors actors) {
        return randomForest.hasEventForActors(actors);
    }

    public int classLabelCount() {
        return randomForest.classLabelCount();
    }

    public int classLabelCount(YoloEvent event) {
        return randomForest.classLabelCount(event);
    }
}

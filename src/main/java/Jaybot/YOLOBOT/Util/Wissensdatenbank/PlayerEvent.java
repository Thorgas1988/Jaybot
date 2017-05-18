package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;

public class PlayerEvent implements YoloEventController {

    /**
     * Class description:
     * A controller for YoloEvent, i.e. a two-class classifier
     */

    private static final boolean DEBUG = true;
    private TriggerConditionWithInventory cancelTrigger;
    private RandomForest specialEventRandomForest;
    // 3 counters for easy decision between blocking move(class 1) and other events(class 2)
    private short observeCount;
    private short cancelCount;
    private short eventCount;


    /**
     * Constructor:
     * Initialize all member variables. Int to 0, new class instance.
     */
    public PlayerEvent() {
        observeCount = 0;
        cancelCount = 0;
        eventCount = 0;
        specialEventRandomForest = new RandomForest(10, 1000);
        cancelTrigger = new TriggerConditionWithInventory();
    }

    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder("############################\nCurrent Knowledge: ");
        retVal.append("\n\t Special Event Triggering = " + (eventCount == observeCount - cancelCount));
        retVal.append("###############################");

        return retVal.toString();
    }


// Following are 3 setters(update)

    /**
     * Learn blocking move from observation w.r.t. inventory items and canceled
     * 1) Increase observe counter
     * 2) IF canceled, increase cancel counter
     * 3) Learn blocking move for cancelTrigger
     *
     * @param inventoryItems An array which stores the number of each inventory type
     * @param canceled       If in this case w.r.t inventory items the move was canceled in observation
     */
    public void learnCancelEvent(byte[] inventoryItems, boolean canceled) {
        observeCount++;
        if (canceled)
            cancelCount++;
        cancelTrigger.update(inventoryItems, canceled);
        if (!Agent.UPLOAD_VERSION && DEBUG) {
            System.out.println("Cancel Event: " + canceled);
        }
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
    public void learnEventHappened(byte[] inventoryItems, byte newItype, boolean move, byte scoreDelta, boolean killed, byte spawnedItype, byte teleportTo, boolean winGame, byte addInventory, byte removeInventory) {
        YoloEvent event = new YoloEvent();
        event.setIType(newItype);
        event.setAddInventorySlotItem(addInventory);
        event.setKill(killed);
        event.setMove(move);
        event.setRemoveInventorySlotItem(removeInventory);
        event.setScoreDelta(scoreDelta);
        event.setSpawns(spawnedItype);
        event.setTeleportTo(teleportTo);
        event.setWinGame(winGame);

        specialEventRandomForest.train(inventoryItems, event);
    }

    /**
     * Learn kill event from observation w.r.t inventory items and killed state observation
     * 1) Increase observe counter
     * 2) Update cancel trigger with false(not canceled, but killed or other event)
     * 3) Call the getEvent() function to get special or default event
     * IF current event predicts wrongly, that means getKill()!=kill
     * update the corresponding event by calling:
     * learnKill(kill);
     * learnNotWin();
     *
     * @param inventory An array which stores the number of each inventory type
     * @param kill      If in this case w.r.t. inventory items the avatar was killed in observation
     */
    public void update(byte[] inventory, boolean kill) {
//        observeCount++;
//        cancelTrigger.update(inventory, false);
//        YoloEvent currentlyExpected = getEvent(inventory);
//        if (currentlyExpected.getKill() != kill) {
//            //Derzeitige annahme ist falsch!
//            boolean specialShouldTrigger = currentlyExpected == defaultEvent;
//            specialEventTrigger.update(inventory, specialShouldTrigger);
//            if (specialShouldTrigger) {
//                specialEventRandomForest.learnKill(kill);
//                specialEventRandomForest.learnNotWin();
//            } else {
//                defaultEvent.learnKill(kill);
//                defaultEvent.learnNotWin();
//            }
//        }
    }


// Following are 3 SPECIAL getters

    /**
     * IF all observed moves were canceled: return True
     * ELSE: let cancelTrigger to decide if the move would be canceled or not
     *
     * @param inventoryItems An array which stores the number of each inventory type
     * @return boolean if a this move is blocking
     */
    public boolean willCancel(byte[] inventoryItems) {
        if (observeCount == 0)
            return false;
        else {
            if (cancelCount == observeCount)
                return true;
            else
                return cancelTrigger.willTrigger(inventoryItems);
        }
    }

    /**
     * IF specialEventTrigger will Trigger: return special event
     * ESLE: return default event
     *
     * @param inventoryItems An array which stores the number of each inventory type
     * @return YoloEvent, either special or default event
     */
    public YoloEvent getEvent(byte[] inventoryItems) {
        return specialEventRandomForest.getEvent(inventoryItems);
    }

    // Following are 4 RAW getters
    public short getObserveCount() {
        return observeCount;
    }

    public short getCancelCount() {
        return cancelCount;
    }

}

package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;

import java.util.LinkedList;
import java.util.List;

public class PlayerEvent implements YoloEventController {

    /**
     * Class description:
     * A controller for YoloEvent, i.e. a two-class classifier
     */

    private static final boolean DEBUG = true;
    private TriggerConditionWithInventory cancelTrigger;
    private List<TriggerConditionWithInventory> specialEventTrigger;
    private List<YoloEvent> specialEvent;
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
        specialEvent = new LinkedList<>();
        cancelTrigger = new TriggerConditionWithInventory();
        specialEventTrigger = new LinkedList<>();
    }

    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder("############################\nCurrent Knowledge: ");
        retVal.append("\n\t Special Event Triggering = " + (eventCount == observeCount - cancelCount));
        retVal.append("\n\t Special Event Description: \n");
        for (YoloEvent event : specialEvent) {
            retVal.append(event.toString() + "\n");
        }
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
     * 2) IF move==true && specialEvent and defaultEvent never move once:
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
        eventCount++;

        // TODO ist das kleinkunst oder kann das weg?
//		if(move && !specialEvent.hasMovedOnce && !defaultEvent.hasMovedOnce){
//			//Das erste mal, dass dieses Event als push-Event gesehen wird!
//			//Daher muessen bisherige cancels als falsch angesehen werden und als nicht ausfuehrbare push-versuche interpretiert werden!
//			eventCount += cancelCount;
//			cancelCount = 0;
//			cancelTrigger.reset();
//			cancelTrigger.update(inventoryItems, false);
//		}

        int maxIndex = 0;
        int maxLikelyValue = -1;
        int likelyLimit = 200;

        for (int i = 0; i < specialEvent.size(); i++) {
            YoloEvent event = specialEvent.get(i);
            int likelyValue = event.likelyValue(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);
            if (likelyValue > maxLikelyValue) {
                maxLikelyValue = likelyValue;
                maxIndex = i;
            }
        }

        if (maxLikelyValue > likelyLimit) {
            specialEventTrigger.get(maxIndex).update(inventoryItems, true);
            specialEvent.get(maxIndex).update(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);
        } else {
            TriggerConditionWithInventory trigger = new TriggerConditionWithInventory();
            trigger.update(inventoryItems, true);
            YoloEvent event = new YoloEvent();
            event.update(newItype, move, scoreDelta, killed, spawnedItype, teleportTo, winGame, addInventory, removeInventory);

            // this has to be synchronized!!
            specialEventTrigger.add(trigger);
            specialEvent.add(event);
        }
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
//                specialEvent.learnKill(kill);
//                specialEvent.learnNotWin();
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
        YoloEvent event = new YoloEvent();
        double maxProbability = -1;

        for (int i=0; i<specialEvent.size(); i++) {
            TriggerConditionWithInventory trigger = specialEventTrigger.get(i);
            double probability = trigger.getTriggerPropability(inventoryItems);
            if (trigger.willTrigger(inventoryItems) &&  probability > maxProbability) {
                maxProbability = probability;
                event = specialEvent.get(i);
            }
        }

        System.out.println("Probably event: " + event.toString() + " with an probability of " + maxProbability);

        return event;
    }

    // Following are 4 RAW getters
    public short getObserveCount() {
        return observeCount;
    }

    public short getCancelCount() {
        return cancelCount;
    }

}

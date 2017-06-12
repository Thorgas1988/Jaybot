package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Util.RandomForest.InvolvedActors;

import java.util.HashMap;

public class PlayerEvent implements YoloEventController {

    /**
     * Class description:
     * A controller for YoloEvent, i.e. a two-class classifier
     */

    private static final boolean DEBUG = true;
    private RandWald randWald;
    private HashMap<InvolvedActors,Integer> cancelCount;
    private HashMap<InvolvedActors,Integer> observeCount;

    /**
     * Constructor:
     * Initialize all member variables. Int to 0, new class instance.
     */
    public PlayerEvent(int treenum, int treesize) {
        randWald = new RandWald(treenum, treesize);
        observeCount = new HashMap<>();
        cancelCount = new HashMap<>();
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
    public void learnEventHappened(InvolvedActors actors, byte[] inventoryItems,byte newItype, boolean move,
                                   byte scoreDelta, boolean killed, byte spawnedItype, byte teleportTo,
                                   boolean winGame, byte addInventory, byte removeInventory, byte pusher) {
//        YoloEvent event = new YoloEvent();
//        event.setNewIType(newItype);
//        event.setOldIType(actors.getPlayerIType());
//        event.setAddInventorySlotItem(addInventory);
//        event.setDefeat(killed);
//        event.setBlocked(!move);
//        event.setRemoveInventorySlotItem(removeInventory);
//        event.setScoreDelta(scoreDelta);
//        event.setSpawns(spawnedItype);
//        event.setTeleportTo(teleportTo);
//        event.setVictory(winGame);
        if(observeCount.containsKey(actors)) observeCount.replace(actors,observeCount.get(actors)+1);
        else observeCount.put(actors,1);

        if(!move){
            if(cancelCount.containsKey(actors)) cancelCount.replace(actors,cancelCount.get(actors)+1);
            else cancelCount.put(actors,1);
        }
        randWald.train(actors,inventoryItems,winGame,killed,!move,newItype,scoreDelta,0,spawnedItype,teleportTo,addInventory,removeInventory,pusher);

    }

    public void learnEventHappened(InvolvedActors actors, byte[] inventoryItems, YoloEvent event) {
        if(observeCount.containsKey(actors)) observeCount.replace(actors,observeCount.get(actors)+1);
        else observeCount.put(actors,1);

        if(event.isBlocked()){
            if(cancelCount.containsKey(actors)) cancelCount.replace(actors,cancelCount.get(actors)+1);
            else cancelCount.put(actors,1);
        }
        randWald.train(actors,inventoryItems,event.isVictory(),event.isDefeat(),event.isBlocked(),(byte)event.getNewIType(),
                (int)event.getScoreDelta(),event.getHpDelta(),(byte)event.getSpawnIType(),(byte)event.getTeleportTo(),
                (byte)event.getAddInventorySlotItem(),(byte)event.getRemoveInventorySlotItem(),(byte)event.getPusher());
    }

    public void trainCancle(InvolvedActors actors, byte[] inventoryItems){
        randWald.trainCancel(actors,inventoryItems);
        if(observeCount.containsKey(actors)) observeCount.replace(actors,observeCount.get(actors)+1);
        else observeCount.put(actors,1);
        if(cancelCount.containsKey(actors)) cancelCount.replace(actors,cancelCount.get(actors)+1);
        else cancelCount.put(actors,1);
    }

    public void trainVictory(InvolvedActors actors, byte[] inventoryItems){
        randWald.trainVictory(actors,inventoryItems);
        if(observeCount.containsKey(actors)) observeCount.replace(actors,observeCount.get(actors)+1);
        else observeCount.put(actors,1);
    }

    /**
     * @return boolean if this move is beeing blocked
     */
    public boolean willCancel(InvolvedActors actors, byte[] inventoryItems) {
        YoloEvent event = randWald.predict(actors, inventoryItems);
        return event.isBlocked();
    }

    /**
     * @param inventoryItems An array which stores the number of each inventory type
     * @return The YoloEvent with the highest probability
     */
    public YoloEvent getEvent(InvolvedActors actors, byte[] inventoryItems) {
        return randWald.predict(actors, inventoryItems);
    }

    public int getObserveCount(InvolvedActors actor){
        return observeCount.getOrDefault(actor,0);
    }

    public int getCancelCount(InvolvedActors actor){
        return cancelCount.getOrDefault(actor,0);
    }

}

package Jaybot.YOLOBOT.Util.Wissensdatenbank;


import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper.YoloEventHelper;
import Jaybot.YOLOBOT.YoloState;
import core.game.Observation;
import ontology.Types;

public class YoloEvent {

    public static final byte UNDEFINED = -1;
    public static final byte NO_CHANGE = 0;

    private boolean victory = false;
    private boolean defeat = false;
    private boolean blocked = false;

    private int newIType = UNDEFINED;
    private int oldIType = UNDEFINED;
    private int hpDelta = NO_CHANGE;
    private double scoreDelta = NO_CHANGE;
    private int spawnedIType = UNDEFINED;
    private int teleportTo = UNDEFINED;
    private int addInventorySlot = UNDEFINED;
    private int removeInventorySlot = UNDEFINED;


    public YoloEvent() {
    }

    // Only needed for the random forest unit tests
    public YoloEvent(boolean blocked, boolean defeat, boolean victory, int oldIType, int newItype, int hpDelta, double scoreDelta,
                     int spawnedItype, int teleportTo, int addInventory, int removeInventory) {
        setOldIType(oldIType);
        setNewIType(newItype);
        setScoreDelta(scoreDelta);
        setSpawns(spawnedItype);
        setTeleportTo(teleportTo);
        setAddInventorySlotItem(addInventory);
        setRemoveInventorySlotItem(removeInventory);
        setBlocked(blocked);
        setDefeat(defeat);
        setVictory(victory);
    }

    public static YoloEvent create(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone, byte[] inventory) {
        if(currentState == null || previousState == null)
            return null;

        if(currentState.getGameTick() != previousState.getGameTick()+1){
            if(!Agent.UPLOAD_VERSION)
                System.out.println("No sequential states given!");
            return null;
        }

        YoloEvent event2Learn = new YoloEvent();

        // player has lost
        if ( currentState.getAvatar() == null ||
                (currentState.isGameOver() &&
                        currentState.getStateObservation().getGameWinner() != Types.WINNER.PLAYER_WINS) ) {
            event2Learn.setDefeat(true);
            return event2Learn;
        }
        // player has won
        else if (currentState.isGameOver() &&
                currentState.getStateObservation().getGameWinner() == Types.WINNER.PLAYER_WINS) {
            event2Learn.setVictory(true);
            return event2Learn;
        }

        if(previousState == null || previousState.getAvatar() == null){
            if(!Agent.UPLOAD_VERSION)
                System.out.println("Did not find State or Avatar");
            return null;
        }

        Observation currentAvatar = currentState.getAvatar();
        Observation previousAvatar = previousState.getAvatar();

        // Action was a movement action, but position did not change and orientation dit not change as well
        // --> blocked
        if ( (actionDone == Types.ACTIONS.ACTION_UP || actionDone == Types.ACTIONS.ACTION_DOWN ||
                actionDone == Types.ACTIONS.ACTION_LEFT || actionDone == Types.ACTIONS.ACTION_RIGHT) &&
                (currentAvatar.position.equals(previousAvatar.position)) &&
                ( !currentState.getAvatarOrientation().equals(previousState.getAvatarOrientation()) ) ) {
            event2Learn.setBlocked(true);
            return event2Learn;
        }

        // everything from here is a move action
        // i.e. the other features like spawner, score delta, etc. are interesting
        SimpleState previousSimpleState = previousState.getSimpleState();
        SimpleState currentSimpleState = currentState.getSimpleState();

        // set the hp delta
        event2Learn.setHpDelta(currentState.getHP() - previousState.getHP());

        // set the score delta
        event2Learn.setScoreDelta(currentState.getGameScore() - previousState.getGameScore());

        // set old and new iTypes
        YoloEventHelper.setITypeChange(event2Learn, currentState, previousState);

        // set inventory change (add/remove)
        YoloEventHelper.setInventoryChange(event2Learn, currentState, previousState);

        // learn the teleport iType if a teleport happend
        YoloEventHelper.setTeleportIType(event2Learn, currentState, previousState);

        // learn if something has spawend anywhere on the gameboard
        YoloEventHelper.setSpawnedIType(event2Learn, currentState, previousState);

        return event2Learn;
    }

    public boolean isDefault() {
        return this.equals(new YoloEvent());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Event ist: ");
        if (victory)
            sb.append("Victory");
        else if (defeat)
            sb.append("Defeat");
        else if (blocked)
            sb.append("Blocked");
        else {
            sb.append("Moved");
            if (oldIType != UNDEFINED)
                sb.append(", ").append("old iType: ").append(oldIType);
            if (newIType != UNDEFINED)
                sb.append(", ").append("new iType: ").append(newIType);
            if (hpDelta != NO_CHANGE)
                sb.append(", ").append("hpDelta: ").append(hpDelta);
            if (scoreDelta != NO_CHANGE)
                sb.append(", ").append("scoreDelta: ").append(scoreDelta);
            if (spawnedIType != UNDEFINED)
                sb.append(", ").append("spawnedIType: ").append(spawnedIType);
            if (teleportTo != UNDEFINED)
                sb.append(", ").append("teleports to: ").append(teleportTo);
            if (addInventorySlot != UNDEFINED)
                sb.append(", ").append("adds inventory: ").append(addInventorySlot);
            if (removeInventorySlot != UNDEFINED)
                sb.append(", ").append("remove inventory: ").append(removeInventorySlot);
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YoloEvent yoloEvent = (YoloEvent) o;

        // both classify a defeating state
        if (defeat && yoloEvent.defeat) return true;
        // both classify a victory state
        if (victory && yoloEvent.victory) return true;
        // both classify a blocking state
        if (blocked && yoloEvent.blocked) return true;

        // a not finishing state (check for the different parts)
        if (blocked != yoloEvent.blocked) return false;
        if (oldIType != yoloEvent.oldIType) return false;
        if (newIType != yoloEvent.newIType) return false;
        if (hpDelta != yoloEvent.hpDelta) return false;
        if (scoreDelta != yoloEvent.scoreDelta) return false;
        if (spawnedIType != yoloEvent.spawnedIType) return false;
        if (teleportTo != yoloEvent.teleportTo) return false;
        if (addInventorySlot != yoloEvent.addInventorySlot) return false;
        return removeInventorySlot == yoloEvent.removeInventorySlot;

    }

    @Override
    public int hashCode() {
        // a victory state everything else is irrelevant
        if (victory)
            return 0;

        int result = 1;

        // a defeat state, everything else is irrelevant
        if (defeat)
            return result;

        result = 31 * result;
        // a blocking state, everything else is irrelevant
        if (blocked)
            return result;

        result += 1;
        result = 31 * result + (int) oldIType;
        result = 31 * result + (int) newIType;
        result = 31 * result + (int) hpDelta;
        result = 31 * result + (int) scoreDelta;
        result = 31 * result + (int) spawnedIType;
        result = 31 * result + (int) teleportTo;
        result = 31 * result + (int) addInventorySlot;
        result = 31 * result + (int) removeInventorySlot;
        return result;
    }

    public void setOldIType(int iType) {
        this.oldIType = iType;
    }

    public void setNewIType(int iType) {
        this.newIType = iType;
    }

    public void setHpDelta(int hpDelta) {
        this.hpDelta = hpDelta;
    }

    public void setScoreDelta(double scoreDelta) {
        this.scoreDelta = scoreDelta;
    }

    public void setSpawns(int spawns) {
        this.spawnedIType = spawns;
    }

    public void setTeleportTo(int teleportTo) {
        this.teleportTo = teleportTo;
    }

    public void setAddInventorySlotItem(int addInventorySlotItem) {
        this.addInventorySlot = addInventorySlotItem;
    }

    public void setRemoveInventorySlotItem(int removeInventorySlotItem) {
        this.removeInventorySlot = removeInventorySlotItem;
    }

    public void setDefeat(boolean defeat) {
        this.defeat = defeat;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }


    public int getOldIType() {
        return oldIType;
    }

    public int getNewIType() {
        return newIType;
    }

    public int getHpDelta() {
        return getHpDelta();
    }

    public double getScoreDelta() {
        return scoreDelta;
    }

    public int getSpawnIType() {
        return spawnedIType;
    }

    public int getTeleportTo() {
        return teleportTo;
    }

    public int getAddInventorySlotItem() {
        return addInventorySlot;
    }

    public int getRemoveInventorySlotItem() {
        return removeInventorySlot;
    }

    public boolean isDefeat() {
        return defeat;
    }

    public boolean isVictory() {
        return victory;
    }

    public boolean isMoved() {
        return !blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }
}

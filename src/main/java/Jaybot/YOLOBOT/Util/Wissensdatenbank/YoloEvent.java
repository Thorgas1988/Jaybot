package Jaybot.YOLOBOT.Util.Wissensdatenbank;


public class YoloEvent {

	public static final byte UNDEFINED = -1;
	public static final byte NO_SCORE_CHANGE = 0;

	private boolean victory = false;
	private boolean defeat = false;
	private boolean blocked = false;

	private byte newIType = UNDEFINED;
	private byte scoreDelta = NO_SCORE_CHANGE;
	private byte spawnedIType = UNDEFINED;
	private byte teleportTo = UNDEFINED;
	private byte addInventory = UNDEFINED;
	private byte removeInventory = UNDEFINED;


	public YoloEvent() {
	}

	public YoloEvent(boolean blocked, boolean defeat, boolean victory, byte newItype, byte scoreDelta, byte spawnedItype, byte teleportTo, byte addInventory,
					 byte removeInventory) {
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
			if (newIType != -1)
				sb.append(", ").append("new iType: ").append(newIType);
			if (scoreDelta != 0)
				sb.append(", ").append("scoreDelta: ").append(scoreDelta);
			if (spawnedIType != -1)
				sb.append(", ").append("spawnedIType: ").append(spawnedIType);
			if (teleportTo != -1)
				sb.append(", ").append("teleports to: ").append(teleportTo);
			if (addInventory != -1)
				sb.append(", ").append("adds inventory: ").append(addInventory);
			if (removeInventory != -1)
				sb.append(", ").append("remove inventory: ").append(removeInventory);
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
		if (blocked  && yoloEvent.blocked) return true;

		// a not finishing state (check for the different parts)
		if (blocked != yoloEvent.blocked) return false;
		if (newIType != yoloEvent.newIType) return false;
		if (scoreDelta != yoloEvent.scoreDelta) return false;
		if (spawnedIType != yoloEvent.spawnedIType) return false;
		if (teleportTo != yoloEvent.teleportTo) return false;
		if (addInventory != yoloEvent.addInventory) return false;
		return removeInventory == yoloEvent.removeInventory;

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
		result = 31 * result + (int) newIType;
		result = 31 * result + (int) scoreDelta;
		result = 31 * result + (int) spawnedIType;
		result = 31 * result + (int) teleportTo;
		result = 31 * result + (int) addInventory;
		result = 31 * result + (int) removeInventory;
		return result;
	}

	public void setNewIType(byte iType) {
		this.newIType = iType;
	}
	public void setScoreDelta(byte scoreDelta){
		this.scoreDelta = scoreDelta;
	}
	public void setSpawns(byte spawns){
		this.spawnedIType = spawns;
	}
	public void setTeleportTo(byte teleportTo){
		this.teleportTo = teleportTo;
	}
	public void setAddInventorySlotItem(byte addInventorySlotItem){
		this.addInventory = addInventorySlotItem;
	}
	public void setRemoveInventorySlotItem(byte removeInventorySlotItem){
		this.removeInventory = removeInventorySlotItem;
	}
	public void setDefeat(boolean defeat){
		this.defeat = defeat;
	}
	public void setVictory(boolean victory){
		this.victory = victory;
	}
	public void setBlocked(boolean blocked){
		this.blocked = blocked;
	}



	public byte getNewIType(){
		return newIType;
	}
	public byte getScoreDelta(){
		return scoreDelta;
	}
	public byte getSpawnIType(){
		return spawnedIType;
	}
	public byte getTeleportTo(){
		return teleportTo;
	}
	public byte getAddInventorySlotItem(){
		return addInventory;
	}
	public byte getRemoveInventorySlotItem(){
		return removeInventory;
	}
	public boolean isDefeat(){
		return defeat;
	}
	public boolean isVictory(){
		return victory;
	}
	public boolean isMoved(){
		return !blocked;
	}
	public boolean isBlocked() {
		return blocked;
	}
}

package Jaybot.YOLOBOT.Util.Wissensdatenbank;


public class YoloEvent {

	public static final byte UNDEFINED = -1;
	public static final byte NO_SCORE_CHANGE = 0;

	private boolean victory = false;
	private boolean defeat = false;
	private boolean blocked = false;

	private int newIType = UNDEFINED;
	private int oldIType = UNDEFINED;
	private double scoreDelta = NO_SCORE_CHANGE;
	private int spawnedIType = UNDEFINED;
	private int teleportTo = UNDEFINED;
	private int addInventorySlot = UNDEFINED;
	private int removeInventorySlot = UNDEFINED;


	public YoloEvent() {
	}

	public YoloEvent(boolean blocked, boolean defeat, boolean victory, int oldIType, int newItype, double scoreDelta,
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
			if (addInventorySlot != -1)
				sb.append(", ").append("adds inventory: ").append(addInventorySlot);
			if (removeInventorySlot != -1)
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
		if (blocked  && yoloEvent.blocked) return true;

		// a not finishing state (check for the different parts)
		if (blocked != yoloEvent.blocked) return false;
		if (newIType != yoloEvent.newIType) return false;
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
		result = 31 * result + (int) newIType;
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
	public void setScoreDelta(double scoreDelta){
		this.scoreDelta = scoreDelta;
	}
	public void setSpawns(int spawns){
		this.spawnedIType = spawns;
	}
	public void setTeleportTo(int teleportTo){
		this.teleportTo = teleportTo;
	}
	public void setAddInventorySlotItem(int addInventorySlotItem){
		this.addInventorySlot = addInventorySlotItem;
	}
	public void setRemoveInventorySlotItem(int removeInventorySlotItem){
		this.removeInventorySlot = removeInventorySlotItem;
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



	public int getOldIType(){
		return oldIType;
	}
	public int getNewIType(){
		return newIType;
	}
	public double getScoreDelta(){
		return scoreDelta;
	}
	public int getSpawnIType(){
		return spawnedIType;
	}
	public int getTeleportTo(){
		return teleportTo;
	}
	public int getAddInventorySlotItem(){
		return addInventorySlot;
	}
	public int getRemoveInventorySlotItem(){
		return removeInventorySlot;
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

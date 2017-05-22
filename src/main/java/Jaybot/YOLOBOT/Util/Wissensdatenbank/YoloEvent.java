package Jaybot.YOLOBOT.Util.Wissensdatenbank;


public class YoloEvent extends Event {

	/**
	 * Speichert Byte events.
	 * <br>Eintraege gehoeren zu:<br>
	 * <ul>
	 * <li> 0 = itype </li>
	 * <li> 1 = scoreDelta </li>
	 * <li> 2 = spawnedItype </li>
	 * <li> 3 = teleportTo </li>
	 * <li> 4 = addInventory </li>
	 * <li> 5 = removeInventory </li>
	 * </ul>
	 */

	/**
	 * Speichert Boolean events.
	 * <br>Eintraege gehoeren zu:<br>
	 * <ul>
	 * <li> 0 = killed </li>
	 * <li> 1 = winGame </li>
	 * <li> 2 = move </li>
	 * </ul>
	 */

    /**
	 * Push wird gesondert von den restlichen Events behandelt:<br>
	 * Wird einmal ein erfolgreicher Push bemerkt, so wird diesers Event immer als pushbar angesehen.<br>
	 * Ob der push tatsaechlich ausfuehrbar ist wird allerdings nicht direkt ermittelt.
	 * Dazu muss rekursiv weiter nachgeforscht werden (ob der zu pushende block da hin kann wo er hin muesste)
	 */
	boolean hasMovedOnce;


// Following is a constructor
    /**
     * Constructon: Initialization of YoloEvent:
     *     No IType change: byteEvents[0] = -1;
     *     No Teleport: byteEvents[3] = -1;
     *     No Inventory change: byteEvents[4] = -1;
     *     Assume its not a nil action: byteEvents[5] = -1;
     */
	public YoloEvent() {
		super(6,3);
		byteEvents[0] = -1;	//Standartmaessig keine IType aenderung!
		byteEvents[3] = -1;	//Standartmaessig kein Teleport!
		byteEvents[4] = -1;	//Standartmaessig keine Inventarerhoehungen
		byteEvents[5] = -1;	//Standartmaessig keine Inventarsenkungen
		boolEvents[2] = true; 	//Initial wird Bewegen auf ja geschaetzt
		
	}

    @Override
    public String toString() {
        String retVal = "Event ist:";
        if(byteEventsPropability[0] > MIN_VALUE && byteEvents[0] != -1)
            retVal += "\t iType change to: " + byteEvents[0];
        if(byteEventsPropability[1] > MIN_VALUE && byteEvents[1] != 0)
            retVal += "\n\t Score Aenderung: " + byteEvents[1];
        if(byteEventsPropability[2] > MIN_VALUE && byteEvents[2] != -1)
            retVal += "\n\t Spawn Object: " + byteEvents[2];
        if(byteEventsPropability[4] > MIN_VALUE && byteEvents[4] != -1)
            retVal += "\n\t Add Inventory: " + byteEvents[2];
        if(byteEventsPropability[5] > MIN_VALUE && byteEvents[5] != -1)
            retVal += "\n\t Remove Inventory: " + byteEvents[2];

        if(boolEventsPropability[0] > MIN_VALUE && boolEvents[0])
            retVal += "\n\t kill this object!";
        if(boolEventsPropability[1] > MIN_VALUE && boolEvents[1])
            retVal += "\n\t Win the game!";
        if(boolEventsPropability[2] > MIN_VALUE && boolEvents[2])
            retVal += "\n\t Move";

        return retVal;
    }


// Following are three update(setters) functions for YoloEvent
    /**
     * By calling super class setters and update member variable hasMovedOnce
     *      updateByteEvents(...)
     *      updateBoolEvents(...)
     *      hasMovedOnce |= move
     * @param newItype chage of the avatar type
     * @param move a nil action or a specific action
     * @param scoreDelta score change
     * @param killed terminal state
     * @param spawnedItype new game object
     * @param teleportToItype new game object
     * @param winGame terminal state
     * @param addInventory change of the avatar inventory
     * @param removeInventory change of the avatar inventory
     */
	public void update(byte newItype, boolean move, byte scoreDelta, boolean killed, byte spawnedItype, byte teleportToItype, boolean winGame, byte addInventory, byte removeInventory){
		updateByteEvents(newItype, scoreDelta, spawnedItype, teleportToItype, addInventory, removeInventory);
		updateBoolEvents(killed, winGame, move);
		this.hasMovedOnce |= move;
	}

    /**
     * Call super class setter updateBoolEvent for index 0(killed)
     * @param kill
     */
	public void learnKill(boolean kill) {
		updateBoolEvent(5,0, kill);
	}

    /**
     * Call super class setter updateBoolEvent for index 1(winGame)
     */
	public void learnNotWin() {
		updateBoolEvent(5,1, false);
	}



// Following are two "Comparable" functions between "this" Event and "input" Event

    /**
    * Check if "this" Event and "input" Event are "identical". E.g. if this has an element with its probability of MIN_VALUE,
    * it would be seen as identical.
    * @param newItype change of the avatar type
    * @param move a nil action or a specific action
    * @param scoreDelta score change
    * @param killed terminal state
    * @param spawnedItype new game object
    * @param teleportTo new game object
    * @param winGame terminal state
    * @param addInventory change of the avatar inventory
    * @param removeInventory change of the avatar inventory
    * @return boolean: If "this" Event and the input "Event" are identical
    */
	public boolean hasValues(byte newItype, boolean move, byte scoreDelta,
			boolean killed, byte spawnedItype, byte teleportTo,
			boolean winGame, byte addInventory, byte removeInventory) {

		if(addInventory != byteEvents[4] && byteEventsPropability[4] != MIN_VALUE)
			return false;
		
		if(removeInventory != byteEvents[5] && byteEventsPropability[5] != MIN_VALUE)
			return false;
		
		if(scoreDelta != byteEvents[1] && byteEventsPropability[1] != MIN_VALUE)
			return false;
		
		if(spawnedItype != byteEvents[2] && byteEventsPropability[2] != MIN_VALUE)
			return false;
		
		if(teleportTo != byteEvents[3] && byteEventsPropability[3] != MIN_VALUE)
			return false;
		
		if(newItype != byteEvents[0] && byteEventsPropability[0] != MIN_VALUE)
			return false;
		
		if(move != boolEvents[2] && boolEventsPropability[2] != MIN_VALUE)
			return false;
		
		if(killed != boolEvents[0] && boolEventsPropability[0] != MIN_VALUE)
			return false;
		
		if(winGame != boolEvents[1] && boolEventsPropability[1] != MIN_VALUE)
			return false;
		
		return true;
	}

    /**
     * How similar is "this" Event and "input" Event. Allocate for each attribute one bit in the order of importance
     * @param newItype change of the avatar type
     * @param push a nil action or a specific action
     * @param scoreDelta score change
     * @param killed terminal state
     * @param spawnedItype new game object
     * @param teleportTo new game object
     * @param win terminal statew
     * @param addInventory change of the avatar inventory
     * @param removeInventory change of the avatar inventory
     * @return int likely value from 0 (min) to 511 (max)
     */
    public int likelyValue(byte newItype, boolean push, byte scoreDelta,
                           boolean killed, byte spawnedItype, byte teleportTo, boolean win, byte addInventory, byte removeInventory) {
        int likely=0;
        int mask = 1;

        int index_byte[] = {4,5,1,2,3,0};
        byte input_byte[] = {addInventory,removeInventory,scoreDelta,spawnedItype,teleportTo,newItype};
        int index_bool[] = {2,0,1};
        boolean input_bool[] = {push,killed,win};
        for(int j=0;j<6;j++){
			if(input_byte[j]==byteEvents[index_byte[j]]) likely += mask;
			else likely += mask * Math.abs(byteEvents[index_byte[j]]-Math.abs(byteEvents[index_byte[j]]-input_byte[j]))
					* (MAX_VALUE-byteEventsPropability[index_byte[j]])/(double)(MAX_VALUE-MIN_VALUE);
			mask *= 10;
		}

        for(int j=0;j<3;j++){
			if(input_bool[j]==boolEvents[index_bool[j]]) likely += mask;
			else likely += mask * (MAX_VALUE-boolEventsPropability[index_bool[j]])/(double)(MAX_VALUE-MIN_VALUE);
			mask *= 10;
		}

        return likely;
    }

	/**
	 * Calculates the likelyValue between this and the given YoloEvent.
	 * @param e The other YoloEvent to compare this YoloEvent to.
	 * @return integer between 0 (min) and 511 (max)
	 */
	public int likelyValue(YoloEvent e) {
		return likelyValue(e.getIType(), e.getMove(), e.getScoreDelta(), e.getKill(), e.getSpawns(), e.getTeleportTo(), e.getWinGame(), e.getAddInventorySlotItem(), e.getRemoveInventorySlotItem());
	}



// Following are 9 getters in the order of their index in the Events Array

	public byte getIType(){
		return byteEvents[0];
	}
	public byte getScoreDelta(){
		return byteEvents[1];
	}
	public byte getSpawns(){
		return byteEvents[2];
	}
	public byte getTeleportTo(){
		return byteEvents[3];
	}
	public byte getAddInventorySlotItem(){
		return byteEvents[4];
	}
	public byte getRemoveInventorySlotItem(){
		return byteEvents[5];
	}
	public boolean getKill(){
		return boolEvents[0];
	}
	public boolean getWinGame(){
		return boolEvents[1];
	}
	public boolean getMove(){
		return boolEvents[2];
	}


}

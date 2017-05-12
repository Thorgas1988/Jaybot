package Jaybot.YOLOBOT.Util.Wissensdatenbank;


public class UseEvent extends Event {

	/**
	 * Class Description:
	 * 		A concrete Event class, which stores only one byte event "score change" and a boolean event "wall"
	 */


// Constructor
	public UseEvent() {
		super(1,1);
	}
	

	@Override
	public String toString() {
		String retVal = "Event ist:";
		if(byteEventsPropability[0] > Byte.MIN_VALUE && byteEvents[0] != -1)
			retVal += "\t Score change by: " + byteEvents[0];		
		return retVal;
	}


// Setter(update)
	public void update(byte scoreDelta, boolean wall){
		updateByteEvents(scoreDelta);
		updateBoolEvents(wall);
	}

// Getters
	public int getScoreDelta(){
		return byteEvents[0];
	}
	
	public boolean getWall(){
		return boolEvents[0];
	}
	
}

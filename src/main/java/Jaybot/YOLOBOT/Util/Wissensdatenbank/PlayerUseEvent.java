package Jaybot.YOLOBOT.Util.Wissensdatenbank;


public class PlayerUseEvent implements YoloEventController {

	/**
	 * Class description:
	 * 		A controller of UseEvent. Maintain a UseEvent instance. Doing learning(update) and prediction(get).
	 */
	private UseEvent triggerEvent;
	private boolean eventSeen;
	
	public PlayerUseEvent() {
	}

	public void learnTriggerEvent(byte scoreDelta, boolean wall){
		if(triggerEvent == null)
			triggerEvent = new UseEvent();
		eventSeen = true;
		triggerEvent.update(scoreDelta, wall);
	}
	
	public UseEvent getTriggerEvent(){
		return triggerEvent;
	}
	
	public boolean willTrigger(){
		return eventSeen;
	}
	
}

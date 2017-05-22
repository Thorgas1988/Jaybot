package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;


public abstract class Event {

	private final static boolean DEBUG = false;
	// Minimal and maximal number for byteEventsProbability and boolEventProbability
	public static final Byte MIN_VALUE = -120, MAX_VALUE = 120;
	// Arrays for storing events information
	byte[] byteEvents;
	boolean[] boolEvents;
	// Arrays for storing events counter
	byte[] byteEventsPropability;
	byte[] boolEventsPropability;


// Constructor
	/**
	 * Allocate space for Events Array
	 * Set probability of each event to MIN_VALUE
 	 * @param byteEventCount number of byte type Event
	 * @param booleanEventsCount number of boolean type Event
	 */
	public Event(int byteEventCount, int booleanEventsCount) {
		byteEvents = new byte[byteEventCount];
		byteEventsPropability = new byte[byteEventCount];
		for (int i = 0; i < byteEventsPropability.length; i++) {
			byteEventsPropability[i] = MIN_VALUE;
		}
		
		
		boolEvents = new boolean[booleanEventsCount];
		boolEventsPropability = new byte[booleanEventsCount];
		for (int i = 0; i < boolEventsPropability.length; i++) {
			boolEventsPropability[i] = MIN_VALUE;
		}
	}


// Following are 3 setters(update) for events
	/**
	 * Iterate over all events:
	 * 		IF new event != this event:
	 * 			IF Prob(this) > MIN_Prob: Decrease Prob of this event
	 * 			ELSE: replace this event with new event
	 * 		ESLE:
	 * 			Increse Prob of this event
	 * @param byteValues parameters representing the new byte events
	 */
	void updateByteEvents(Byte... byteValues){
		for (int i = 0; i < byteEvents.length; i++) {
			if(byteValues[i] != byteEvents[i]){
				//Gegensaetzliches Event!
				if(byteEventsPropability[i] > MIN_VALUE){
					//Urspruengliches Event ist wahrscheinlich genug um es nicht zu aendern!
					byteEventsPropability[i]--;	//Wahrscheinlichkeit, dass das alte event richtig war sinkt
				}else{
					// Urspruengliches Event ist sehr unwahrscheinlich, aendere in aktuell gesehenenes event!
					if(!Agent.UPLOAD_VERSION && DEBUG)
						System.out.println("Change Event: ByteEvent"+i+" was '"+byteEvents[i]+"' and will be '"+byteValues[i]+"'!" );
					byteEvents[i] = byteValues[i];
				}
			}else{
				//Das gleiche event wie geahnt trifft ein --> erhoehe wahrscheinlichkeit
				if(byteEventsPropability[i] < MAX_VALUE)
					byteEventsPropability[i]++;
			}
		}
	}

	/**
	 * Iterate over all events by calling the do_one_iteration function updateBoolEvent with a different factor of importance
	 * @param boolValues parameters representing the new boolean events
	 */
	void updateBoolEvents(boolean... boolValues){
		int learnStep;
		for (int i = 0; i < boolEvents.length; i++) {
			if(boolValues[i] && (i == 2 || i == 0)){
				learnStep = 5;	//Move und Kill lernt 'ja' schneller!
			}else{
				learnStep = 1;
			}
			updateBoolEvent(learnStep, i, boolValues[i]);
		}
	}

	/**
	 * Learn one bool event: analog as one iteration of updateByteEvents.
	 * @param learnStep      importance rate: how fast to increase prob of this event
	 * @param i	             which event to be updated
	 * @param toLearnValue   which value to be learned
	 */
	void updateBoolEvent(int learnStep, int i, boolean toLearnValue) {
		if(toLearnValue != boolEvents[i]){
			//Gegensaetzliches Event!
			if(boolEventsPropability[i] > MIN_VALUE){
				//Urspruengliches Event ist wahrscheinlich genug um es nicht zu aendern!
				boolEventsPropability[i]--;	//Wahrscheinlichkeit, dass das alte event richtig war sinkt
			}else{
				// Urspruengliches Event ist sehr unwahrscheinlich, aendere in aktuell gesehenenes event!
				if(!Agent.UPLOAD_VERSION && DEBUG)
					System.out.println("Change SpecialEvent: BoolEvent"+i+" was '"+boolEvents[i]+"' and will be '"+toLearnValue+"'!" );
				boolEvents[i] = toLearnValue;
			}
		}else{
			//Das gleiche event wie geahnt trifft ein --> erhoehe wahrscheinlichkeit
			if(boolEventsPropability[i] <= MAX_VALUE - learnStep)
				boolEventsPropability[i]+= learnStep;
		}
	}

}

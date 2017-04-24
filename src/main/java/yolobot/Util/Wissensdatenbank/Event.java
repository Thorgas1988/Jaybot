package yolobot.Util.Wissensdatenbank;

public abstract class Event {

	public static final Byte MIN_VALUE = -120;
	public static final Byte MAX_VALUE = 120;

	byte[] byteEvents;
	byte[] byteEventsPropability;

	boolean[] boolEvents;
	byte[] boolEventsPropability;

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

	void updateByteEvents(Byte... byteValues) {
		for (int i = 0; i < byteEvents.length; i++) {
			if (byteValues[i] != byteEvents[i]) {
				//Gegensaetzliches Event!
				if (byteEventsPropability[i] > MIN_VALUE) {
					//Urspruengliches Event ist wahrscheinlich genug um es nicht zu aendern!
					byteEventsPropability[i]--;    //Wahrscheinlichkeit, dass das alte event richtig war sinkt
				} else {
					// Urspruengliches Event ist sehr unwahrscheinlich, aendere in aktuell gesehenenes event!
					byteEvents[i] = byteValues[i];
				}
			} else {
				//Das gleiche event wie geahnt trifft ein --> erhoehe wahrscheinlichkeit
				if (byteEventsPropability[i] < MAX_VALUE)
					byteEventsPropability[i]++;
			}
		}
	}


	void updateBoolEvents(boolean... boolValues) {
		int learnStep;
		for (int i = 0; i < boolEvents.length; i++) {
			if (boolValues[i] && (i == 2 || i == 0)) {
				learnStep = 5;    //Move und Kill lernt 'ja' schneller!
			} else {
				learnStep = 1;
			}
			updateBoolEvent(learnStep, i, boolValues[i]);
		}
	}

	void updateBoolEvent(int learnStep, int i, boolean toLearnValue) {
		if (toLearnValue != boolEvents[i]) {
			//Gegensaetzliches Event!
			if (boolEventsPropability[i] > MIN_VALUE) {
				//Urspruengliches Event ist wahrscheinlich genug um es nicht zu aendern!
				boolEventsPropability[i]--;    //Wahrscheinlichkeit, dass das alte event richtig war sinkt
			} else {
				// Urspruengliches Event ist sehr unwahrscheinlich, aendere in aktuell gesehenenes event!
				boolEvents[i] = toLearnValue;
			}
		} else {
			//Das gleiche event wie geahnt trifft ein --> erhoehe wahrscheinlichkeit
			if (boolEventsPropability[i] <= MAX_VALUE - learnStep)
				boolEventsPropability[i] += learnStep;
		}
	}
}

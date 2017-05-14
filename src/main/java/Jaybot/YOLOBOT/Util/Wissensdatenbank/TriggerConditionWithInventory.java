package Jaybot.YOLOBOT.Util.Wissensdatenbank;

public class TriggerConditionWithInventory {

	private enum TriggerType{
		conditional,
		ever,
		never;
	}


// Following are 3 types of member variables, w.r.t their functionality
	/**
	 * Member variable for non-conditional trigger:
	 * irrelevantEverProbybility can be seen as an trust region, currently implemented as [-5,5]
	 * 	   IF the current value is int range of [-5,5]:
	 * 	       IF trigger state and input state are identical: increase it
	 * 	       ELSE: decrease
	 * 	   ELSE:
	 * 	   	   Update trigger state with input state
	 */
	private boolean occurredOnce, notOccurredOnce;
	private byte irrelevantEverProbybility;
	/**
	 * Member variables for conditional trigger (each element in the array represents one inventory type):
	 *     A trigger for one inventory type will be triggered if it is in the interval [minOccurred, maxOccurred]
	 *     A trigger will not be triggered if it is in the interval [minNotOccurred, maxNotOccurred]
	 *     If the two intervals overlap with each other:
	 *         We see this as a irrelevant attribute for the classification
	 */
	protected byte[] minOccurred;
	protected byte[] maxOccurred;
	protected byte[] minNotOccurred;
	protected byte[] maxNotOccurred;
	/**
	 * Record those indexes which are irrelevant with classifiction
	 * Additionally with a counter for those irrelevant inventory tpyes
	 */
	protected boolean[] isIrrelevant;
	private byte irrelevantCount;


	private TriggerType trigger;


// Constructor
	/**
	 * Initialization:
	 * 		Set non-conditional variables to false
	 * 		Set occured/notOccurred interval to [MAX_VALUE, +∞]
	 * 		Set all inventory type as relevant
	 */
	public TriggerConditionWithInventory() {
		reset();
	}
	public void reset() {
		// Resettet das gelernte Wissen.
		occurredOnce = false;
		notOccurredOnce = false;
		minOccurred = new byte[YoloKnowledge.INDEX_MAX];
		maxOccurred = new byte[YoloKnowledge.INDEX_MAX];
		minNotOccurred = new byte[YoloKnowledge.INDEX_MAX];
		maxNotOccurred = new byte[YoloKnowledge.INDEX_MAX];

		for (int i = 0; i < minOccurred.length; i++) {
			minOccurred[i] = Byte.MAX_VALUE;
			minNotOccurred[i] = Byte.MAX_VALUE;
		}

		isIrrelevant = new boolean[YoloKnowledge.INDEX_MAX];

		trigger = TriggerType.conditional;
		irrelevantCount = 0;
		irrelevantEverProbybility = 0;
	}


// Following is a setter(update) for the trigger
	/**
	 * Update the trigger condition with the observed state:
	 * 		(1) Case non-conditional:
	 * 				Update the variable irrelevantEverProbybility or set trigger to input state.
	 * 				More details in the comment of member variable
	 * 		(2) Case conditional:
	 * 				Iterate over all relevant inventory type:
	 * 					(a) Update the interval of occured/notOccured
	 * 					(b) Check if the intervals of occured and notOccured overlap with each other, if it is so
	 * 					set this inventory type as irrelevant. If all inventory types are irrelevant, set trigger
	 * 					state w.r.t input state.
	 * @param inventoryItems an array with elements: the number of each inventory type
	 * @param occurred observed event in this state(in this case only inventory items are considered)
	 */
	public void update(byte[] inventoryItems, boolean occurred){
		if(trigger != TriggerType.conditional){
			if((occurred && trigger == TriggerType.ever)||(!occurred && trigger == TriggerType.never)){
				if(irrelevantEverProbybility < 5)
					irrelevantEverProbybility++;
			}else if(irrelevantEverProbybility > -5){
				irrelevantEverProbybility--;
			}else{
				trigger = occurred? TriggerType.ever: TriggerType.never;
			}
		}
		if(irrelevantCount == inventoryItems.length)
			return;
		for (int itemIndex = 0; itemIndex < inventoryItems.length; itemIndex++) {
			byte inventoryCount = inventoryItems[itemIndex];

			if(isIrrelevant[itemIndex])
				continue;

			if(occurred){
				occurredOnce = true;
				minOccurred[itemIndex] = (byte) Math.min(minOccurred[itemIndex], inventoryCount);
				maxOccurred[itemIndex] = (byte) Math.max(maxOccurred[itemIndex], inventoryCount);
			}else{
				notOccurredOnce = true;
				minNotOccurred[itemIndex] = (byte) Math.min(minNotOccurred[itemIndex], inventoryCount);
				maxNotOccurred[itemIndex] = (byte) Math.max(maxNotOccurred[itemIndex], inventoryCount);
			}


			if(occurredOnce && notOccurredOnce && (minOccurred[itemIndex] <= minNotOccurred[itemIndex] && minNotOccurred[itemIndex] <= maxOccurred[itemIndex] || minOccurred[itemIndex] <= maxNotOccurred[itemIndex] && maxNotOccurred[itemIndex] <= maxOccurred[itemIndex])){
				isIrrelevant[itemIndex] = true;
				irrelevantCount++;
				if(irrelevantCount == inventoryItems.length){
					if(occurred)
						trigger = TriggerType.ever;
					else
						trigger = TriggerType.never;
				}
			}

		}
	}



// Getters (Do the class prediction):
	/**
	 * Iterate over all inventory type:
	 * 		Count how many of them lie in the occured interval
	 * Return the rate of this number over the number of total inventory types.
	 * @param inventoryItems an array with elements: the number of each inventory type
	 * @return how many of the inventory type would satisfies the occurred interval, return the rate over number of inventory types
	 */
	public double getTriggerPropability(byte[] inventoryItems){
		byte occur = 0;
		for (int index = 0; index < inventoryItems.length; index++) {
			if(minOccurred[index] <= inventoryItems[index] && inventoryItems[index] <= maxOccurred[index]){ //XOR
				occur++;
			}else{
				occur--;
			}
		}
		return (double)occur/inventoryItems.length;
	}
	/**
	 * For non-conditional and conditional case, return the corresponding result
	 * In conditional case, this function call getConditionalTrigger and return its result.
	 * @param inventoryItems an array with elements: the number of each inventory type
	 * @return boolean if an event will be triggered
	 */
	public boolean willTrigger(byte[] inventoryItems){
		switch (trigger) {
		case conditional:
			return getConditionalTrigger(inventoryItems);
		default:
			return trigger == TriggerType.ever;
		}
		
	}
	/**
	 * If any inventory type lies in the interval of notOccurred, return false. Otherwise return true.
	 * @param inventoryItems an array with elements: the number of each inventory type
	 * @return boolean if an event will be triggered
	 */
	private boolean getConditionalTrigger(byte[] inventoryItems) {
		for (int index = 0; index < inventoryItems.length; index++) {
			if(!isIrrelevant[index]){
				if(minNotOccurred[index] <= inventoryItems[index] && inventoryItems[index] <= maxNotOccurred[index]){
					return false;
				}
			}
		}
		return true;
	}




	
	
	
}

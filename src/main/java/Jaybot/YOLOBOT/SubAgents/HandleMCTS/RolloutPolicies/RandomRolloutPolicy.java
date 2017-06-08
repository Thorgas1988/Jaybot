package Jaybot.YOLOBOT.SubAgents.HandleMCTS.RolloutPolicies;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import Jaybot.YOLOBOT.YoloState;
import ontology.Types.ACTIONS;

import java.util.ArrayList;
import java.util.Iterator;

public class RandomRolloutPolicy extends RolloutPolicy {

	@Override
	public ArrayList<ACTIONS> possibleNextActions(YoloState state,
			ArrayList<ACTIONS> forbiddenAction, boolean forceNotEpsilon) {
		ArrayList<ACTIONS> validActions = new ArrayList<ACTIONS>(state.getAvailableActions(true));
		if(forbiddenAction != null && validActions.size() > forbiddenAction.size())
			validActions.removeAll(forbiddenAction);
		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator.hasNext();) {
			ACTIONS actions = (ACTIONS) iterator.next();

			if(YoloKnowledge.getInstance().actionLeadsOutOfBattlefield(state, actions))
				iterator.remove();
		}
		
		if(validActions.isEmpty())
			return new ArrayList<ACTIONS>(state.getAvailableActions(true));
			
		return validActions;
	}

}

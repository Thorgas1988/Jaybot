package YOLOBOT.SubAgents.debug;

import YOLOBOT.YoloState;
import YOLOBOT.SubAgents.SubAgent;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TestSubAgent extends SubAgent {


	private ACTIONS[] debug = new ACTIONS[]{ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT };
	private ACTIONS[] debug2 = new ACTIONS[]{ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP};
	private ACTIONS[] debug3 = new ACTIONS[]{ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_DOWN, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_NIL, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_USE, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_USE, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP, ACTIONS.ACTION_UP };
	
	
	@Override
	public ACTIONS act(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
		YoloState current = yoloState;
		current.setNewSeed(0);
		for (int i = 0; i < debug2.length; i++) {
			current = current.copyAdvanceLearn(debug2[i]);
		}

		if(current.getAvatarY() != 9)
			System.out.println("AHA!");
		else
			System.out.println("oho!");
		
		
//		if(yoloState.getGameTick() < debug2.length)
//			return debug2[yoloState.getGameTick()];
		
		return ACTIONS.ACTION_NIL;
		
	}

	@Override
	public double EvaluateWeight(YoloState yoloState) {
		// TODO Auto-generated method stub
		return Double.MAX_VALUE;
	}

	@Override
	public void preRun(YoloState yoloState, ElapsedCpuTimer elapsedTimer) {
		
		while(true){
			YoloState current = yoloState.copy();
			current.setNewSeed(0);
			for (int i = 0; i < debug2.length; i++) {
				current.advance(debug2[i]);
			}

			if(current.getAvatarY() != 9)
				System.out.println("AHA!");
			else
				System.out.println("oho!");
		}

	}

}

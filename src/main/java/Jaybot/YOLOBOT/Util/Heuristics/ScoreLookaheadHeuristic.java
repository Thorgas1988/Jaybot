package Jaybot.YOLOBOT.Util.Heuristics;

import core.game.Observation;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import Jaybot.YOLOBOT.YoloState;
import tools.Vector2d;

public class ScoreLookaheadHeuristic extends IModdableHeuristic {

	private double upperBound;
	private double lowerBound;
	private boolean isActive;
	private boolean[][] isWall;



	public ScoreLookaheadHeuristic() {

		upperBound = -Double.MAX_VALUE;
		lowerBound = Double.MAX_VALUE;
		isActive = false;
	}

	@Override
	public HeuristicType GetType() {
		return HeuristicType.ScoreLookaheadHeuristic;
	}

	@Override
	public double Evaluate(YoloState ys) {
		return EvaluateWithoutNormalisation(ys)/GetAbsoluteMax();
	}

	@Override
	public double EvaluateWithoutNormalisation(YoloState ys) {
		return getModdedHeuristic(ys, ys.getAvatarX(), ys.getAvatarY(), ys.getAvatarOrientation());
	}

	@Override
	public double getModdedHeuristic(YoloState state, int agentX, int agentY, Vector2d avatarOrientation) {
		

		double value = 0;
		if(!YoloKnowledge.getInstance().positionOnGrid(state, agentX, agentY)){
			value = -1;
		}else{
			int avatarItype = state.getAvatar().itype;
			byte[] inventory = state.getInventoryArray();
			for (Observation obs : state.getObservationGrid()[agentX][agentY]) {
				PlayerEvent pEvent = YoloKnowledge.getInstance().getPlayerEventController();
				if(pEvent.classLabelCount()>0)
					value += pEvent.getEvent(avatarItype, obs.itype, inventory).getScoreDelta();
			}
		}
			
		if(value < lowerBound)
			lowerBound = value;
		if(value > upperBound)
			upperBound = value;
		return value;
	}

	@Override
	public double GetAbsoluteMax() {
		return Math.abs(lowerBound);
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}
	
	public void refreshWalls(boolean[][] isWall){
		this.isWall = isWall;
		isActive = true;
	}

	@Override
	public boolean canStepOn(int x, int y) {
		return !isWall[x][y];
	}
}

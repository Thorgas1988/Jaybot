package Jaybot.YOLOBOT.Util.Heuristics;

import Jaybot.YOLOBOT.YoloState;
import tools.Vector2d;

public abstract class IModdableHeuristic extends IHeuristic {
	protected boolean targetToUse;
	
	public abstract double getModdedHeuristic(YoloState state, int trueX, int trueY, Vector2d avatarOrientation);

	public void setTargetToUse(boolean value) {
		targetToUse = value;
	}

	public boolean isTargetToUse() {
		return targetToUse;
	}

	public abstract boolean canStepOn(int myX, int myY);
}

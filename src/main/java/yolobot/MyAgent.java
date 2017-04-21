package yolobot;

import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public interface MyAgent {

	public void runFirstSecond(YoloState state, ElapsedCpuTimer elapseTimer);
	public ACTIONS act(YoloState state, ElapsedCpuTimer elapseTimer);
}

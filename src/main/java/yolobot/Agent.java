package yolobot;

import yolobot.Util.Wissensdatenbank.YoloKnowledge;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent{
	public final static boolean UPLOAD_VERSION = true;
	public static YoloState currentYoloState;
	
	private MyAgent agent;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer, MyAgent agent) {
		this.agent = agent;
		YoloState startYoloState = new YoloState(so);
		YoloKnowledge.instance = new YoloKnowledge(startYoloState);
		agent.runFirstSecond(startYoloState, elapsedTimer);
	}

	public ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		currentYoloState = new YoloState(so);
		YoloKnowledge.instance.learnStochasticEffekts(currentYoloState);
		YoloState.currentGameScore = currentYoloState.getGameScore();

		return agent.act(currentYoloState, elapsedTimer);
	}
}

package jaybot;

import YOLOBOT.YoloAgent;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import YOLOBOT.YoloMCTSAgent;
import YOLOBOT.YoloState;

/**
 * Created by Torsten on 22.04.17.
 */
public class Agent extends AbstractPlayer {
    private YoloState currentYoloState = null;
    //private YoloMCTSAgent yoloAgent = null;
//    private YoloMCTSAgent yoloAgent = null;
    private YoloAgent yoloAgent = null;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        YoloState startYoloState = new YoloState(so);
        YoloKnowledge.instance = new YoloKnowledge(startYoloState);
        //yoloAgent = new YoloMCTSAgent(startYoloState, elapsedTimer);
        //yoloAgent = new YoloMCTSAgent();
        yoloAgent = new YoloAgent(startYoloState, elapsedTimer);
        yoloAgent.runFirstSecond(startYoloState, elapsedTimer);
    }

    @Override
    public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        currentYoloState = new YoloState(so);
        YoloKnowledge.instance.learnStochasticEffekts(currentYoloState);
        YoloState.currentGameScore = currentYoloState.getGameScore();

        return yoloAgent.act(currentYoloState, elapsedTimer);
    }
}

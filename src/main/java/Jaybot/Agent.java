package Jaybot;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import Jaybot.YOLOBOT.YoloState;

/**
 * Created by Torsten on 22.04.17.
 */
public class Agent extends AbstractPlayer {
    private YoloState currentYoloState = null;
    private Jaybot.YOLOBOT.Agent yoloAgent;

    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        //YoloState startYoloState = new YoloState(so);
        //YoloKnowledge.instance = new YoloKnowledge(startYoloState);
        yoloAgent = new Jaybot.YOLOBOT.Agent(so, elapsedTimer);
    }

    @Override
    public Types.ACTIONS act(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        return yoloAgent.act(so, elapsedTimer);
    }
}

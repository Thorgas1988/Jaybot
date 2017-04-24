package yolobot;

import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * This has to be implemented! It is currently only an random agent.
 */
public class YoloAgent {
    public static final boolean UPLOAD_VERSION = false;
    private static final Random randomGenerator = new Random();

    public YoloAgent(YoloState startYoloState, ElapsedCpuTimer elapsedTimer) {
        runFirstSecond(startYoloState, elapsedTimer);
    }

    public void runFirstSecond(YoloState state, ElapsedCpuTimer elapseTimer) {

    }

    public ACTIONS act(YoloState state, ElapsedCpuTimer elapseTimer) {

        //Get the available actions in this game.
        ArrayList<ACTIONS> actions = state.getAvailableActions();

        //Determine an index randomly and get the action to return.
        int index = randomGenerator.nextInt(actions.size());
        Types.ACTIONS action = actions.get(index);

        //Return the action.
        return action;
    }
}

package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.YoloState;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Created by Torsten on 27.05.17.
 */
public class YoloKnowledge {

    private static final boolean DEBUG = false;

    private static double SQRT_2 = Math.sqrt(2.0);
    private static int MAX_ITYPES = 32;

    private static YoloKnowledge instance = null;

    public static final Vector2d ORIENTATION_NULL = new Vector2d(0, 0);
    public static final Vector2d ORIENTATION_UP = new Vector2d(0, -1);
    public static final Vector2d ORIENTATION_DOWN = new Vector2d(0, 1);
    public static final Vector2d ORIENTATION_LEFT = new Vector2d(-1, 0);
    public static final Vector2d ORIENTATION_RIGHT = new Vector2d(1, 0);

    private boolean[] isContinuousMovingEnemy;


    private YoloKnowledge() {
        clear();
    }

    public static YoloKnowledge getInstance() {
        if (instance == null) {
            instance = new YoloKnowledge();
        }
        return instance;
    }

    /**
     * Should be called in the first second, before the game starts to fill the knowledge base with initial knowledge.
     */
    public void initialize() {

    }

    public void clear() {
        isContinuousMovingEnemy = new boolean[MAX_ITYPES];
    }


    /**
     * Learns knowledge from the transition of the previous state to the current.
     *
     * @param currentState The current game state.
     * @param previousState The previous game state
     * @param actionDone The action which was done to transition from the previous to the current state.
     */
    public void learnFrom(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {

    }

    public void learnContinuousMovingEnemies(YoloState state)
    {
        // Simulate 2 states
        YoloState[] states = new YoloState[3];
        states[0] = state;
        states[1] = states[0].copyAdvanceLearn(Types.ACTIONS.ACTION_NIL);
        states[2] = states[1].copyAdvanceLearn(Types.ACTIONS.ACTION_NIL);

        Vector2d[][] positions = new Vector2d[isContinuousMovingEnemy.length][states.length];

        // store positions of every npc for every state
        for (int posIndex = 0; posIndex < states.length) {

            // get the npc Positions of the current state
            ArrayList<Observation>[] currentStateNpcPos = states[posIndex].getNpcPositions();
            if (currentStateNpcPos == null) {
                return;
            }

            // for every npc of the current state
            for (int npcNr = 0; npcNr < currentStateNpcPos.length; npcNr++) {
                if (currentStateNpcPos[npcNr] == null || currentStateNpcPos[npcNr].isEmpty()) {
                    continue;
                }

                Observation obs = currentStateNpcPos[npcNr].get(0);
                positions[obs.itype][posIndex] = obs.position;
            }
        }

        // calculate distances between the states of the diffrent itypes
        // and check if it is a continuousMovingEnemy
        for (int iType=0; iType<positions.length; iType++) {
            if (positions[iType][0] != null && positions[iType][1] != null && positions[iType][2] != null) {
                continue;
            }

            double zeroToFirstDistance = positions[iType][0].dist(positions[iType][1]);
            double firstToSecondDistance = positions[iType][1].dist(positions[iType][2]);

            if (zeroToFirstDistance == firstToSecondDistance && zeroToFirstDistance < state.getBlockSize())
            {
                isContinuousMovingEnemy[iType] = true;
                System.out.println("NPC moves continuously, maybe :-P");
            }
        }
    }
}

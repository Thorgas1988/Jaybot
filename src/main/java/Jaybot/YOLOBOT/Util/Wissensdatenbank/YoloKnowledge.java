package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;
import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper.YoloEventHelper;
import Jaybot.YOLOBOT.YoloState;
import core.game.*;
import core.game.Event;
import ontology.Types;
import tools.Vector2d;

import java.util.*;

/**
 * Created by Torsten on 27.05.17.
 */
public class YoloKnowledge {
    public static boolean DEACTIVATE_LEARNING = false;

    private static double SQRT_2 = Math.sqrt(2.0);
    private static int MAX_ITYPES = 32;
    private static int CONTINUOUS_MOVING_STATE_ADVANCES_COUNT = 2;
    private static int STOCHASTIC_ITERATIONS_COUNT = 10;
    private static int STOCHASTIC_ITERATIONS_MIN_TRIES = 1;

    private static YoloKnowledge instance = null;

    public static final Vector2d ORIENTATION_NULL = new Vector2d(0, 0);
    public static final Vector2d ORIENTATION_UP = new Vector2d(0, -1);
    public static final Vector2d ORIENTATION_DOWN = new Vector2d(0, 1);
    public static final Vector2d ORIENTATION_LEFT = new Vector2d(-1, 0);
    public static final Vector2d ORIENTATION_RIGHT = new Vector2d(1, 0);

    private boolean[] isContinuousMovingEnemy;
    private boolean[] isStochasticEnemy;

    private int playerITypeMask = 0;

    private boolean hasScoreWithoutWinning = false;

    private PlayerUseEvent[][] useEffects;

    private RandomForest randomForestClassifier;


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
        isStochasticEnemy = new boolean[MAX_ITYPES];
        randomForestClassifier = new RandomForest(MAX_ITYPES, 1000);
    }


    private void learnCollisionEvent(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {
        // get the inventory of the current state
        byte[] inventory = randomForestClassifier.getInventoryArray(currentState);
        // create the YoloEvent 2 learn
        YoloEvent event2Learn = YoloEvent.create(currentState, previousState, actionDone, inventory);
        // train the random forest with the YoloEvent
        randomForestClassifier.train(inventory, event2Learn);
    }

    private void learnUseEvent(YoloState currentState, YoloState lastState,
                               Event collider) {

        if(!Agent.UPLOAD_VERSION)
            System.out.println("Learn Use Event: " + collider.activeSpriteId + " -> " + collider.passiveSpriteId);

        int otherIType = collider.passiveTypeId;
        int playerObjIType = collider.activeTypeId;
        boolean wall = currentState.getSimpleState().getObservationWithIdentifier(collider.passiveSpriteId) != null;

        if(useEffects[playerObjIType][otherIType] == null){
            useEffects[playerObjIType][otherIType] = new PlayerUseEvent();
        }

        PlayerUseEvent uEvent = useEffects[playerObjIType][otherIType];
        byte deltaScore = (byte) (currentState.getGameScore()-lastState.getGameScore());

        if(!hasScoreWithoutWinning && deltaScore>0 && !currentState.isGameOver())
            hasScoreWithoutWinning = true;

        uEvent.learnTriggerEvent(deltaScore, wall);
    }


    /**
     * Learns knowledge from the transition of the previous state to the current.
     *
     * @param currentState  The current game state.
     * @param previousState The previous game state
     * @param actionDone    The action which was done to transition from the previous to the current state.
     */
    public void learnFrom(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {

        if(currentState == null || previousState == null || DEACTIVATE_LEARNING)
            return;

        if(currentState.getGameTick() != previousState.getGameTick()+1){
            if(!Agent.UPLOAD_VERSION)
                System.out.println("No sequential states given!");
            return;
        }

        TreeSet<core.game.Event> history = currentState.getEventsHistory();
        while (history.size() > 0) {
            Event collider = history.pollLast();

            if (collider.gameStep != previousState.getGameTick()) {
                break;
            }

            int passiveItype = collider.passiveTypeId;
            int activeItype = collider.activeTypeId;

            // if the active iType is no known player iType
            if (!isPlayerIType(activeItype)) {
                // add it as an player iType
                playerITypeMask = playerITypeMask | 1 << activeItype;
            }

            // was the collision provoked by a sprite created from the player
            if (!collider.fromAvatar) {
                // learn the use event
                learnUseEvent(currentState, previousState, collider);
            }
            // the player collided
            else {
                // learn the collision event
                learnCollisionEvent(currentState, previousState, actionDone);
            }
        }
    }

    /**
     * Learns continuous moving enemies
     *
     * @param state The initial state of the game.
     */
    public void learnContinuousMovingEnemies(YoloState state) {
        // Simulate CONTINUOUS_MOVIN_STATE_ADVANCES_COUNT many states (increment by 1 to store the source state)
        YoloState[] states = new YoloState[CONTINUOUS_MOVING_STATE_ADVANCES_COUNT + 1];
        states[0] = state;
        for (int i = 1; i < states.length) {
            states[i] = states[i - 1].copyAdvanceLearn(Types.ACTIONS.ACTION_NIL);
        }

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
        for (int iType = 0; iType < positions.length; iType++) {
            if (positions[iType][0] != null && positions[iType][1] != null && positions[iType][2] != null) {
                continue;
            }

            double zeroToFirstDistance = positions[iType][0].dist(positions[iType][1]);
            double firstToSecondDistance = positions[iType][1].dist(positions[iType][2]);

            if (zeroToFirstDistance == firstToSecondDistance && zeroToFirstDistance < state.getBlockSize()) {
                isContinuousMovingEnemy[iType] = true;
                System.out.println("NPC moves continuously, maybe :-P");
            }
        }
    }

    /**
     * Learns stochastic moving NPCs
     *
     * @param state The initial state of the game
     */
    public void learnStochasticEffekts(YoloState state) {
        Map<Integer, Vector2d> positions = new HashMap<Integer, Vector2d>();
        int stochasticNpcCount = 0;

        if (state.getNpcPositions() == null || state.getNpcPositions().length == 0)
            return;

        // run STOCHASTIC_ITERATIONS_COUNT many iterations to find stochastic NPCs
        for (int iteration = 0; iteration < STOCHASTIC_ITERATIONS_COUNT; iteration++) {
            boolean haveStochasticEnemy = false;
            state.setNewSeed((int) (Math.random() * 10000));
            YoloState nextState = state.copyAdvanceLearn(Types.ACTIONS.ACTION_NIL);
            ArrayList<Observation>[] nextStateNPCs = nextState.getNpcPositions();

            // try at least STOCHASTIC_ITERATIONS_MIN_TRIES many state advances, maybe the first few were just bad luck
            if (nextStateNPCs == null && iteration >= STOCHASTIC_ITERATIONS_MIN_TRIES) {
                break;
            }

            for (int npcNr = 0; npcNr < nextStateNPCs.length; npcNr++) {

                // do we even have something to do here?
                if (nextStateNPCs[npcNr] == null || nextStateNPCs[npcNr].isEmpty()) {
                    continue;
                }


                // Check if this NPC was not recognized as stochastic before
                int iType = nextStateNPCs[npcNr].get(0).itype;
                if (!isStochasticEnemy[iType]) {

                    // was not recognized as stochastic before, then check the position.
                    // if it is not stochastic all observation position of this iType should be the same position
                    // over all iterations
                    for (int i = 0; i < nextStateNPCs[npcNr].size(); i++) {
                        Observation obs = nextStateNPCs[npcNr].get(i);
                        Vector2d referencePos = positions.get(obs.obsID);

                        // store the reference position if it was not stored before
                        // most likely in the first iteration or if the iType was absent in the early iterations
                        // because of other stochastic effects which killed the iType
                        if (referencePos == null) {
                            positions.put(obs.obsID, obs.position);
                        } else {
                            if (!referencePos.equals(obs.position)) {
                                //NPC stochastic movement detected!
                                isStochasticEnemy[iType] = true;
                                stochasticNpcCount++;
                                break;
                            }
                        }
                    }
                }

                //Iteration fuer diesen Itype durchgelaufen
                if (isStochasticEnemy[iType])
                    haveStochasticEnemy = true;        //Merke, dass es einen stochastischen Gegner gab!
            }

            // did not find any stochastic enemies?
            // try at least STOCHASTIC_ITERATIONS_MIN_TRIES many state advances, maybe the first few were just bad luck
            if (!haveStochasticEnemy && iteration >= STOCHASTIC_ITERATIONS_MIN_TRIES) {
                break;
            }
        }

        if (!Agent.UPLOAD_VERSION)
            System.out.println("Stochastische NPCs: " + stochasticNpcCount);
    }

    public boolean isPlayerIType(int iType) {
        // return true if the bit at position iType is 1 in the playerITypeMask
        return ((playerITypeMask & (1 << iType)) > 0);
    }
}

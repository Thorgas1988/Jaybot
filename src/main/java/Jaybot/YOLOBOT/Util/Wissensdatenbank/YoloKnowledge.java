package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper.YoloEventHelper;
import Jaybot.YOLOBOT.YoloState;
import core.game.Observation;
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

        // get the inventory of the current state
        byte[] inventory = randomForestClassifier.getInventoryArray(currentState);

        YoloEvent event2Learn = new YoloEvent();

        // player has lost
        if ( currentState.getAvatar() == null ||
                (currentState.isGameOver() &&
                currentState.getStateObservation().getGameWinner() != Types.WINNER.PLAYER_WINS) ) {
            event2Learn.setDefeat(true);
            randomForestClassifier.train(inventory, event2Learn);
            return;
        }
        // player has won
        else if (currentState.isGameOver() &&
                   currentState.getStateObservation().getGameWinner() == Types.WINNER.PLAYER_WINS) {
            event2Learn.setVictory(true);
            randomForestClassifier.train(inventory, event2Learn);
            return;
        }

        if(previousState == null || previousState.getAvatar() == null){
            if(!Agent.UPLOAD_VERSION)
                System.out.println("Did not find State or Avatar");
            return;
        }

        Observation currentAvatar = currentState.getAvatar();
        Observation previousAvatar = previousState.getAvatar();

        // Action was a movement action, but position did not change and orientation dit not change as well
        // --> blocked
        if ( (actionDone == Types.ACTIONS.ACTION_UP || actionDone == Types.ACTIONS.ACTION_DOWN ||
                actionDone == Types.ACTIONS.ACTION_LEFT || actionDone == Types.ACTIONS.ACTION_RIGHT) &&
                (currentAvatar.position.equals(previousAvatar.position)) &&
                ( !currentState.getAvatarOrientation().equals(previousState.getAvatarOrientation()) ) ) {
            event2Learn.setBlocked(true);
            randomForestClassifier.train(inventory, event2Learn);
            return;
        }

        // everything from here is a move action
        // i.e. the other features like spawner, score delta, etc. are interesting

        // set the score delta
        event2Learn.setScoreDelta(currentState.getGameScore() - previousState.getGameScore();

        // set old and new iTypes
        YoloEventHelper.setITypeChange(event2Learn, currentState, previousState);

        // set inventory change (add/remove)
        YoloEventHelper.setInventoryChange(event2Learn, currentState, previousState);

        
        TreeSet<core.game.Event> collisionHistory = currentState.getEventsHistory();



        event2Learn.setTeleportTo();
        event2Learn.setSpawns();


        randomForestClassifier.train(inventory, event2Learn);
        return;


        learnNpcMovement(currentState, lastState);
        learnAlivePosition(currentState);
        learnSpawner(currentState, lastState);
        learnDynamicObjects(currentState, lastState);
        learnAgentMovement(currentState, lastState, actionDone);

        if(actionDone == Types.ACTIONS.ACTION_USE)
            learnUseActionResult(currentState, lastState);


        int lastAgentItype = lastState.getAvatar().itype;
        int lastGameTick = lastState.getGameTick();
        TreeSet<core.game.Event> history = currentState.getEventsHistory();
        while (history.size() > 0) {
            core.game.Event newEvent = history.pollLast();
            if(newEvent.gameStep != lastGameTick){
                break;
            }

            int passiveItype = newEvent.passiveTypeId;
            byte passiveIndex = itypeToIndex(passiveItype);
            int activeItype = newEvent.activeTypeId;
            byte activeIndex = itypeToIndex(activeItype);

            //Lerne PlayerIndex
            if(!isPlayerIndex[activeIndex]){
                //Dieser Index wurde bisher nicht mit Player in verbindung gebracht:
                isPlayerIndex[activeIndex] = true;
                playerIndexMask = playerIndexMask | 1 << activeIndex;
                playerITypes.add(activeItype);
            }

            if(!newEvent.fromAvatar){
                //Was the Avatar itself
                learnAgentEvent(currentState, lastState, passiveIndex, newEvent.passiveSpriteId, actionDone);
            }else{
                learnEvent(currentState, lastState, newEvent);
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
}

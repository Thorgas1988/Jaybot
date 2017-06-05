package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;
import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.YoloState;
import core.game.Event;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.util.*;

/**
 * Created by Torsten on 27.05.17.
 */
public class YoloKnowledge {
    public static boolean DEACTIVATE_LEARNING = false;

    public static int UNDEFINED = YoloEvent.UNDEFINED;

    private static final double SQRT_2 = Math.sqrt(2.0);
    private static final int MAX_INDICES = 32;
    private static final int CONTINUOUS_MOVING_STATE_ADVANCES_COUNT = 2;
    private static final int STOCHASTIC_ITERATIONS_COUNT = 10;
    private static final int STOCHASTIC_ITERATIONS_MIN_TRIES = 1;

    private static YoloKnowledge instance = null;

    public static final Vector2d ORIENTATION_NULL = new Vector2d(0, 0);
    public static final Vector2d ORIENTATION_UP = new Vector2d(0, -1);
    public static final Vector2d ORIENTATION_DOWN = new Vector2d(0, 1);
    public static final Vector2d ORIENTATION_LEFT = new Vector2d(-1, 0);
    public static final Vector2d ORIENTATION_RIGHT = new Vector2d(1, 0);

    private byte currentITypeIndex;
    private int[] iType2IndexMap;

    private boolean[] isContinuousMovingEnemy;
    private boolean[] isStochasticEnemy;
    private int[] spawnedNPCiTypes;
    private int[] spawnedRessourceiTypes;

    private int playerITypeMask;

    private int dynamicMask;

    private int[] iTypeCategories;

    private boolean hasScoreWithoutWinning;

    private PlayerUseEvent[][] useEffects;

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private int[][] maxMoveInPixelPerNpcIType;

    private RandomForest randomForestClassifier;


    private YoloKnowledge() {
        reset();
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
    public void initialize(YoloState initState) {
        learnObjectCategories(initState);
        learnContinuousMovingEnemies(initState);
        learnStochasticEffects(initState);
    }

    /**
     * Resets the YoloKnowledge base.
     */
    public void reset() {
        currentITypeIndex = 0;
        iType2IndexMap = new int[Byte.MAX_VALUE];
        for (int i = 0; i < iType2IndexMap.length; i++) {
            iType2IndexMap[i] = UNDEFINED;
        }

        isContinuousMovingEnemy = new boolean[MAX_INDICES];
        isStochasticEnemy = new boolean[MAX_INDICES];
        playerITypeMask = 0;
        dynamicMask = 0;
        spawnedNPCiTypes = new int[MAX_INDICES];
        spawnedRessourceiTypes = new int[MAX_INDICES];
        iTypeCategories = new int[MAX_INDICES];
        hasScoreWithoutWinning = false;

        useEffects = new PlayerUseEvent[MAX_INDICES][MAX_INDICES];
        // for every possible iType and for both axis (x and y)
        maxMoveInPixelPerNpcIType = new int[MAX_INDICES][2];

        randomForestClassifier = new RandomForest(MAX_INDICES, 1000);
    }

    private int iType2Index(int iType) {
        if (iType2IndexMap[iType] == UNDEFINED) {
            iType2IndexMap[iType] = currentITypeIndex;
            currentITypeIndex++;
        }
        return iType2IndexMap[iType];
    }

    /**
     * Returns if the iType belongs to the player
     */
    public boolean isPlayerIType(int iType) {
        int index = iType2Index(iType);
        // return true if the bit at position iType is 1 in the playerITypeMask
        return ((playerITypeMask & (1 << index)) > 0);
    }

    /**
     * Stores the given iType in the player mask. This allows to identify the iType as the player.
     */
    public void storePlayerIType(int iType) {
        int index = iType2Index(iType);
        playerITypeMask = playerITypeMask | (1 << index);
    }


    /**
     * Learns knowledge from the transition of the previous state to the current.
     *
     * @param currentState  The current game state.
     * @param previousState The previous game state
     * @param actionDone    The action which was done to transition from the previous to the current state.
     */
    public void learnFrom(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {

        if (currentState == null || previousState == null || DEACTIVATE_LEARNING)
            return;

        if (currentState.getGameTick() != previousState.getGameTick() + 1) {
            if (!Agent.UPLOAD_VERSION)
                System.out.println("No sequential states given!");
            return;
        }

        // learn general game information
        learnNpcMovementRange(currentState, previousState);
        learnSpawner(currentState, previousState);

        // learn collision events
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
                storePlayerIType(activeItype);
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

    private void setSpawnedIType(Observation spawnedIType, int portalIType) {
        int portalIndex = iType2Index(portalIType);

        if (spawnedIType.category == Types.TYPE_NPC) {
            spawnedNPCiTypes[portalIndex] = spawnedIType.itype;
            return;
        }

        if (spawnedIType.category == Types.TYPE_RESOURCE) {
            spawnedRessourceiTypes[portalIndex] = spawnedIType.itype;
        }
    }

    private List<Observation> getAdjacentObservations(YoloState state, int x, int y) {
        final List<Observation> result = new LinkedList<>();
        final ArrayList<Observation>[][] grid = state.getObservationGrid()[][];
        final int xMax = grid.length;
        final int yMax = grid[0].length;

        // step one to the left and one to the right or one up and one down
        final int[] steps = new int[]{-1, 1};

        // check the field "left" and "right" from the current field.
        for (int xStep : steps) {
            int tempX = x + xStep;

            if (tempX < 0 || tempX > xMax) {
                continue;
            }

            result.addAll(grid[tempX][y]);
        }

        // check the field "above" and "under" the current field.
        for (int yStep : steps) {
            int tempY = y + yStep;

            if (tempY < 0 || tempY > xMax) {
                continue;
            }

            result.addAll(grid[x][tempY]);
        }
    }

    private Observation findITypeSpawnedOnPosition(YoloState currentState, YoloState previousState, int x, int y) {
        ArrayList<Observation> spawnedCanidates = new ArrayList<>();
        ArrayList<Observation> obsCurrent = currentState.getObservationGrid()[x][y];

        // Add the observations of the previous state from the "searched" field
        List<Observation> obsPrevious = new LinkedList<>(previousState.getObservationGrid()[x][y]);

        for (Observation currentObservation : obsCurrent) {
            if (! (currentObservation.category == Types.TYPE_NPC || currentObservation.category == Types.TYPE_RESOURCE)) {
                continue;
            }

            // add all observations of adjacent fields
            obsPrevious.addAll(getAdjacentObservations(previousState, x, y));

            // check if the currentObservation did exist in the previous state either
            // on the searched field or on adjacent fields
            boolean spawned = true;
            for (Observation previousObservation : obsPrevious) {
                if (previousObservation.obsID == currentObservation.obsID) {
                    spawned = false;
                    break;
                }
            }

            // the observation did not exist on the field and the adjacent fields in the previous state.
            // i.e. it could only be spawned.
            if (spawned) {
                // we only return one observation, as one spawner should only spawn one object in one timestep
                return currentObservation;
            }
        }


        ArrayList<Observation>
    }

    /**
     * Checks every portal object if it has spawned something and what it has spawned.
     * Therefore it checks the field of the portal itself for any objects which did not exist before.
     * And it check the adjacent fields as well, as portals could spawn onto the adjacent fields.
     */
    private void learnSpawner(YoloState currentState, YoloState previousState) {
        final ArrayList<Observation>[] portalObservationsArray = currentState.getPortalsPositions();

        for (ArrayList<Observation> portalObs : portalObservationsArray) {
            for (Observation portal : portalObs) {

                int portalIndex = iType2Index(portal.itype);
                if (spawnedNPCiTypes[portalIndex] != UNDEFINED || spawnedRessourceiTypes[portalIndex] != UNDEFINED) {
                    // we assume that spawners do not change their spawned object type
                    continue;
                }

                Observation spawnedIType = null;
                Vector2d portalPos = portal.position;
                int x = (int) portalPos.x;
                int y = (int) portalPos.y;

                spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, x, y);
                if (spawnedIType != null) {
                    setSpawnedIType(spawnedIType, portal.itype);
                    continue;
                }


                // step one to the left and one to the right or one up and one down
                final int[] steps = new int[]{-1, 1};
                // check the field "left" and "right" from the current field.
                for (int xStep : steps) {
                    int tempX = x + xStep;

                    spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, tempX, y);
                    if (spawnedIType != null) {
                        setSpawnedIType(spawnedIType, portal.itype);
                        continue;
                    }
                }

                // check the field "left" and "right" from the current field.
                for (int yStep : steps) {
                    int tempY = y + yStep;

                    spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, x, yStep);
                    if (spawnedIType != null) {
                        setSpawnedIType(spawnedIType, portal.itype);
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Learns how far (maximum) a NPCs can walk/travel in one gametick.
     */
    private void learnNpcMovementRange(YoloState currentState, YoloState previousState) {
        ArrayList<Observation>[] previousNpcs = previousState.getNpcPositions();
        ArrayList<Observation>[] currentNpcs = currentState.getNpcPositions();
        Map<Integer, Observation> previousObservationMap = new HashMap<>();

        for (ArrayList<Observation> previousObservations : previousNpcs) {
            for (Observation o : previousObservations) {
                previousObservationMap.put(o.obsID, o);
            }
        }

        for (ArrayList<Observation> currentObservations : currentNpcs) {
            for (Observation currentObs : currentObservations) {
                Observation previousObs = previousObservationMap.get(currentObs.obsID);

                if (previousObs == null)
                    continue;

                int iTypeIndex = currentObs.itype;

                Vector2d currentPos = currentObs.position;
                Vector2d previousPos = previousObs.position;

                int moveX = (int) Math.abs(currentPos.x - previousPos.x);
                int moveY = (int) Math.abs(currentPos.y - previousPos.y);

                if (moveX > maxMoveInPixelPerNpcIType[iTypeIndex][AXIS_X]) {
                    maxMoveInPixelPerNpcIType[iTypeIndex][AXIS_X] = moveX;
                }

                if (moveY > maxMoveInPixelPerNpcIType[iTypeIndex][AXIS_Y]) {
                    maxMoveInPixelPerNpcIType[iTypeIndex][AXIS_Y] = moveY;
                }
            }
        }
    }

    /**
     * Learns collision events (YoloEvent)
     */
    private void learnCollisionEvent(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {
        // get the inventory of the current state
        byte[] inventory = randomForestClassifier.getInventoryArray(currentState);
        // create the YoloEvent 2 learn
        YoloEvent event2Learn = YoloEvent.create(currentState, previousState, actionDone, inventory);
        // train the random forest with the YoloEvent
        randomForestClassifier.train(inventory, event2Learn);
    }

    /**
     * Learns use events occured because of a collision from the player created projectile with another object.
     */
    private void learnUseEvent(YoloState currentState, YoloState lastState,
                               Event collider) {

        if (!Agent.UPLOAD_VERSION)
            System.out.println("Learn Use Event: " + collider.activeSpriteId + " -> " + collider.passiveSpriteId);

        int otherITypeIndex = iType2Index(collider.passiveTypeId);
        int playerObjITypeIndex = iType2Index(collider.activeTypeId);
        boolean wall = currentState.getSimpleState().getObservationWithIdentifier(collider.passiveSpriteId) != null;

        if (useEffects[playerObjITypeIndex][otherITypeIndex] == null) {
            useEffects[playerObjITypeIndex][otherITypeIndex] = new PlayerUseEvent();
        }

        PlayerUseEvent uEvent = useEffects[playerObjITypeIndex][otherITypeIndex];
        byte deltaScore = (byte) (currentState.getGameScore() - lastState.getGameScore());

        if (!hasScoreWithoutWinning && deltaScore > 0 && !currentState.isGameOver())
            hasScoreWithoutWinning = true;

        uEvent.learnTriggerEvent(deltaScore, wall);
    }

    /**
     * Learns the categories of iTypes
     */
    public void learnObjectCategories(YoloState state) {
        learnObjectCategories(state.getImmovablePositions());
        learnObjectCategories(state.getFromAvatarSpritesPositions());
        learnObjectCategories(state.getMovablePositions());
        learnObjectCategories(state.getNpcPositions());
        learnObjectCategories(state.getPortalsPositions());
        learnObjectCategories(state.getResourcesPositions());
    }

    private void learnObjectCategories(ArrayList<Observation>[] list) {
        if (list == null)
            return;
        for (ArrayList<Observation> observationList : list) {
            if (observationList != null && !observationList.isEmpty()) {
                Observation obs = observationList.get(0);
                int iTypeIndex = iType2Index(obs.itype);
                iTypeCategories[iTypeIndex] = obs.category;

                if (obs.category == Types.TYPE_NPC) {
                    dynamicMask = dynamicMask | (1 << iTypeIndex);
                }

            }
        }
    }

    /**
     * Learns continuous moving enemies
     */
    private void learnContinuousMovingEnemies(YoloState state) {
        // Simulate CONTINUOUS_MOVIN_STATE_ADVANCES_COUNT many states (increment by 1 to store the source state)
        YoloState[] states = new YoloState[CONTINUOUS_MOVING_STATE_ADVANCES_COUNT + 1];
        states[0] = state;
        for (int i = 1; i < states.length; i++) {
            states[i] = states[i - 1].copyAdvanceLearn(Types.ACTIONS.ACTION_NIL);
        }

        Vector2d[][] positions = new Vector2d[isContinuousMovingEnemy.length][states.length];

        // store positions of every npc for every state
        for (int posIndex = 0; posIndex < states.length; posIndex++) {

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
                int iTypeIndex = iType2Index(obs.itype);
                positions[iTypeIndex][posIndex] = obs.position;
            }
        }

        // calculate distances between the states of the diffrent itypes
        // and check if it is a continuousMovingEnemy
        for (int iTypeIndex = 0; iTypeIndex < positions.length; iTypeIndex++) {
            if (positions[iTypeIndex][0] != null && positions[iTypeIndex][1] != null && positions[iTypeIndex][2] != null) {
                continue;
            }

            double zeroToFirstDistance = positions[iTypeIndex][0].dist(positions[iTypeIndex][1]);
            double firstToSecondDistance = positions[iTypeIndex][1].dist(positions[iTypeIndex][2]);

            if (zeroToFirstDistance == firstToSecondDistance && zeroToFirstDistance < state.getBlockSize()) {
                isContinuousMovingEnemy[iTypeIndex] = true;
                System.out.println("NPC moves continuously, maybe :-P");
            }
        }
    }

    /**
     * Learns stochastic moving NPCs
     */
    private void learnStochasticEffects(YoloState state) {
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
                int iTypeIndex = iType2Index(nextStateNPCs[npcNr].get(0).itype);
                if (!isStochasticEnemy[iTypeIndex]) {

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
                                isStochasticEnemy[iTypeIndex] = true;
                                stochasticNpcCount++;
                                break;
                            }
                        }
                    }
                }

                //Iteration fuer diesen Itype durchgelaufen
                if (isStochasticEnemy[iTypeIndex])
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

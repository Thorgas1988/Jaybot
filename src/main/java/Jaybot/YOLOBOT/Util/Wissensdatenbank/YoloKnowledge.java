package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper.Learner;
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
    public static final int MAX_INDICES = 32;
    private static final int CONTINUOUS_MOVING_STATE_ADVANCES_COUNT = 2;
    private static final int STOCHASTIC_ITERATIONS_COUNT = 10;
    private static final int STOCHASTIC_ITERATIONS_MIN_TRIES = 1;
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;

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

    private boolean scoreWithoutWinning;

    private PlayerUseEvent[][] useEffects;

    private int[][] maxMoveInPixelPerNpcIType;

    private PlayerEvent playerEventController;


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
    private void reset() {
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
        for (int i=0; i<MAX_INDICES; i++) {
            spawnedNPCiTypes[i] = UNDEFINED;
            spawnedRessourceiTypes[i] = UNDEFINED;
        }


        iTypeCategories = new int[MAX_INDICES];
        scoreWithoutWinning = false;

        useEffects = new PlayerUseEvent[MAX_INDICES][MAX_INDICES];
        // for every possible iType and for both axis (x and y)
        maxMoveInPixelPerNpcIType = new int[MAX_INDICES][2];

        playerEventController = new PlayerEvent(MAX_INDICES);
    }

    public int iType2Index(int iType) {
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
    private void storePlayerIType(int iType) {
        int index = iType2Index(iType);
        playerITypeMask = playerITypeMask | (1 << index);
    }

    public boolean isDynamic(int iType) {
        int index = iType2Index(iType);
        return (dynamicMask & (1 << index)) > 0;
    }

    public void storeDynamicIType(int iType) {
        int index = iType2Index(iType);
        dynamicMask = dynamicMask | (1 << index);
    }

    public void storeSpawnedIType(Observation spawnedIType, int portalIType) {
        int portalIndex = iType2Index(portalIType);

        if (spawnedIType.category == Types.TYPE_NPC) {
            spawnedNPCiTypes[portalIndex] = spawnedIType.itype;
            return;
        }

        if (spawnedIType.category == Types.TYPE_RESOURCE) {
            spawnedRessourceiTypes[portalIndex] = spawnedIType.itype;
        }
    }

    public int getSpawnedNpcIType(int portalIType) {
        int index = iType2Index(portalIType);
        return spawnedNPCiTypes[index];
    }

    public int getSpawnedRessourceIType(int portalIType) {
        int index = iType2Index(portalIType);
        return spawnedRessourceiTypes[index];
    }

    public void storeMaxMoveInPixelPerNpcIType(int iType, int axis, int maxMove) {
        int index = iType2Index(iType);
        maxMoveInPixelPerNpcIType[index][axis] = maxMove;
    }

    public int[] getMaxMoveInPixelPerNpcIType(int iType) {
        int index = iType2Index(iType);
        return maxMoveInPixelPerNpcIType[index];
    }

    public int getMaxMoveInPixelPerNpcIType(int iType, int axis) {
        int index = iType2Index(iType);
        return maxMoveInPixelPerNpcIType[index][axis];
    }

    public PlayerUseEvent getUseEffect(int playerIType, int otherIType) {
        int playerIndex = iType2Index(playerIType);
        int otherIndex = iType2Index(otherIType);

        return useEffects[playerIndex][otherIndex];
    }

    public void storeUseEffect(int playerIType, int otherIType, PlayerUseEvent useEffect) {
        int playerIndex = iType2Index(playerIType);
        int otherIndex = iType2Index(otherIType);

        useEffects[playerIndex][otherIndex] = useEffect;
    }

    public boolean hasScoreWithoutWinning() {
        return scoreWithoutWinning;
    }

    public void setScoreWithoutWinning(boolean scoreWithoutWinning) {
        this.scoreWithoutWinning = scoreWithoutWinning;
    }

    public int getITypeCategory(int iType) {
        int index = iType2Index(iType);
        return iTypeCategories[index];
    }

    private void storeITypeCategory(int iType, int category) {
        int index = iType2Index(iType);
        iTypeCategories[index] = category;
    }

    public PlayerEvent getPlayerEventController() {
        return playerEventController;
    }


    public boolean avatarLooksOutOfGame(YoloState state) {
        final ArrayList<Observation>[][] grid = state.getObservationGrid();
        final int MAX_X = grid.length - 1;
        final int MAX_Y = grid[0].length - 1;

        Vector2d orientation = state.getAvatarOrientation();

        if(orientation.equals(ORIENTATION_NULL))
            return false;
        else if(orientation.equals(ORIENTATION_DOWN))
            return state.getAvatarY() == MAX_Y;
        else if(orientation.equals(ORIENTATION_UP))
            return state.getAvatarY() == 0;
        else if(orientation.equals(ORIENTATION_RIGHT))
            return state.getAvatarX() == MAX_X;
        else if(orientation.equals(ORIENTATION_DOWN))
            return state.getAvatarX() == 0;

        return false;
    }

    /**
     * Returns a forest compatible (i.e. with the count of the conditions) inventory.
     *
     * @param state The YoloState to retrieve the inventory from.
     * @return The inventory as byte array. if a iType was not available in the inventory it is stored with an amount of zero.
     */
    public byte[] getInventoryArray(YoloState state) {
        Map<Integer, Integer> inventory = state.getAvatarResources();
        byte[] inventoryArray = new byte[MAX_INDICES];

        for (int i = 0; i<inventoryArray.length; i++) {
            Integer itemAmount = inventory.get(i);
            if (itemAmount == null) {
                inventoryArray[i] = 0;
            } else {
                inventoryArray[i] = itemAmount.byteValue();
            }
        }

        return inventoryArray;
    }

    private boolean positionOnGrid(YoloState state, int x, int y) {
        ArrayList<Observation>[][] grid = state.getObservationGrid();
        return (x >= 0 && x < grid.length) && (y >= 0 && y < grid[0].length);
    }

    public boolean moveWillCancel(YoloState state, Types.ACTIONS action, boolean defeatIsCancel, boolean ignoreDefeatByStochasticEnemy) {

        if(state.getAvatar() == null)
            return true;

        int avatarIndex = iType2Index(state.getAvatar().itype);
        int x = state.getAvatarX();
        int y = state.getAvatarY();
        byte[] inventory = getInventoryArray(state);
        boolean noMove = false;
        Vector2d orientation = state.getAvatarOrientation();


        switch (action) {
            case ACTION_DOWN:
                if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_DOWN))
                    noMove = true;
                y++;
                break;
            case ACTION_UP:
                if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_UP))
                    noMove = true;
                y--;
                break;
            case ACTION_RIGHT:
                if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_RIGHT))
                    noMove = true;
                x++;
                break;
            case ACTION_LEFT:
                if(!orientation.equals(ORIENTATION_NULL) && !orientation.equals(ORIENTATION_LEFT))
                    noMove = true;
                x--;
                break;
            default:
                //TODO: Action use auf singleton checken! Wenn schon geschossen, dann true!
                noMove = true;
        }

        // Target is not on the grid
        if(!positionOnGrid(state, x, y))
            return true;

        //Check enemy:
        if(!ignoreDefeatByStochasticEnemy && defeatIsCancel && canBeKilledByStochasticEnemyAt(state, x, y))
            return true;
        int mask = state.getSimpleState().getMask(x, y);

        boolean surelyWillNotBlock = (blockingMaskTheorie[avatarIndex] & mask) == 0;

        //Might Block, check PlayerEvents:
        int playerIndex = iType2Index(state.getAvatar().itype);
        for (Observation obs : state.getObservationGrid()[x][y]) {
            int index = iType2Index(obs.itype);

            //Bad-SpawnerCheck:
            int iTypeOfSpawnedNpc = getSpawnedNpcIType(obs.itype);
            if (iTypeOfSpawnedNpc != UNDEFINED) {
                PlayerEvent spawnedPEvent = getPlayerEvent(	state.getAvatar().itype,
                        indexToItype(iTypeIndexOfSpawner), true);
                YoloEvent spawnedEvent = playerEventController.getEvent(state.getInventoryArray());
                boolean isBadSpawner = spawnedEvent.getKill() || spawnedPEvent.getObserveCount() == 0;
                if(isBadSpawner){
                    return true;
                }
            }


            if(activeObjectEffects[playerIndex][index] != null){
                PlayerEvent pEvent = (PlayerEvent) activeObjectEffects[playerIndex][index];
                if(pEvent.getObserveCount() > 20 && (pEvent.willCancel(inventory) && !canInteractWithUse(avatarIndex,index)) || (defeatIsCancel && !canInteractWithUse(avatarIndex,index) && pEvent.getEvent(inventory).getKill()))
                    return true;
            }
        }
        //Nothing found that will block for sure, so guess action will work!
        return false;
    }




    public Observation canBeKilledByEnemyNearby(YoloState currentState, int x, int y, boolean ignoreTicks)
    {
        ArrayList<Observation>[][] grid = currentState.getObservationGrid();
        if(positionOnGrid(currentState, x, y)) {
            ArrayList<Observation> observations = grid[x][y];
            for (Observation observation : observations) {
                int obsIndex = iType2Index(observation.itype);
                
                if (isContinuousMovingEnemy[obsIndex] && (ignoreTicks || movesAtTickOrDirectFollowing(obsIndex, currentState.getGameTick())))
                {
                    //stochastic enemy at testing field, but how near is he to avatar?
                    int halfBlock = currentState.getBlockSize()/2;


                    Vector2d enemyPosition = observation.position;
                    Vector2d avatarPosition = currentState.getAvatarPosition();
                    double ePosX = enemyPosition.x+ halfBlock;
                    double ePosY = enemyPosition.y+ halfBlock;
                    double aPosX = avatarPosition.x+ halfBlock;
                    double aPosY = avatarPosition.y+ halfBlock;

                    Vector2d diffVec = new Vector2d(ePosX - aPosX, ePosY - aPosY);
                    double diff = diffVec.dist(diffVec);
                    double diffBlocks = diff / currentState.getBlockSize();

                    if (diffBlocks < 2.0)
                    {
                        System.out.println("Enemy nearby. Diff:"+diff+", enemyPos x|y:"+enemyPosition.x+"|"+enemyPosition.y+", avatarPos x|y"+avatarPosition.x+"|"+avatarPosition.y);
                        //enemy is nearby, could kill avatar!!
                        return observation;
                    }
                }
            }
        }
        return null;
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
        Learner.learnNpcMovementRange(currentState, previousState);
        Learner.learnSpawner(currentState, previousState);
        Learner.learnDynamicObjects(currentState, previousState);

        // learn collision events
        TreeSet<core.game.Event> history = currentState.getEventsHistory();
        while (history.size() > 0) {
            Event collider = history.pollLast();

            if (collider.gameStep != previousState.getGameTick()) {
                break;
            }

            int activeItype = collider.activeTypeId;

            // if the active iType is no known player iType
            if (!isPlayerIType(activeItype)) {
                // add it as an player iType
                storePlayerIType(activeItype);
            }

            // was the collision provoked by a sprite created from the player
            if (!collider.fromAvatar) {
                // learn the use event
                Learner.learnUseEvent(currentState, previousState, collider);
            }
            // the player collided
            else {
                // learn the collision event
                Learner.learnCollisionEvent(currentState, previousState, actionDone, collider.passiveTypeId);
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
            List<Observation>[] currentStateNpcPos = states[posIndex].getNpcPositions();
            if (currentStateNpcPos == null) {
                return;
            }

            // for every npc of the current state
            for (List<Observation> observations : currentStateNpcPos) {
                if (observations == null || observations.isEmpty()) {
                    continue;
                }

                Observation obs = observations.get(0);
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
        Map<Integer, Vector2d> positions = new HashMap<>();
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

            if (nextStateNPCs == null) {
                continue;
            }

            for (ArrayList<Observation> npcs : nextStateNPCs) {

                // do we even have something to do here?
                if (npcs == null || npcs.isEmpty()) {
                    continue;
                }


                // Check if this NPC was not recognized as stochastic before
                int iTypeIndex = iType2Index(npcs.get(0).itype);
                if (!isStochasticEnemy[iTypeIndex]) {

                    // was not recognized as stochastic before, then check the position.
                    // if it is not stochastic all observation position of this iType should be the same position
                    // over all iterations
                    for (Observation obs : npcs) {
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

    /**
     * Learns the categories of iTypes
     */
    public static void learnObjectCategories(YoloState state) {
        learnObjectCategories(state.getImmovablePositions());
        learnObjectCategories(state.getFromAvatarSpritesPositions());
        learnObjectCategories(state.getMovablePositions());
        learnObjectCategories(state.getNpcPositions());
        learnObjectCategories(state.getPortalsPositions());
        learnObjectCategories(state.getResourcesPositions());
    }

    private static void learnObjectCategories(ArrayList<Observation>[] list) {
        YoloKnowledge knowledge = YoloKnowledge.getInstance();

        if (list == null)
            return;

        for (ArrayList<Observation> observationList : list) {
            if (observationList != null && !observationList.isEmpty()) {
                Observation obs = observationList.get(0);

                knowledge.storeITypeCategory(obs.itype, obs.category);

                if (obs.category == Types.TYPE_NPC) {
                    knowledge.storeDynamicIType(obs.itype);
                }
            }
        }
    }
}

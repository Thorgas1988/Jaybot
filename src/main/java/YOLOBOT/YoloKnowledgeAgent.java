package YOLOBOT;

import core.game.Observation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import YOLOBOT.Util.Planner.KnowledgeBasedAStar;
import YOLOBOT.Util.Wissensdatenbank.PlayerEvent;
import YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;

import java.util.*;

/**
 * This has to be implemented! It is currently only an random agent.
 */
public class YoloKnowledgeAgent {
    public static final boolean UPLOAD_VERSION = false;
    private static final Random randomGenerator = new Random();
    private HashMap<Integer, Integer> singleObjectCooldownSet;

    public YoloKnowledgeAgent(YoloState startYoloState, ElapsedCpuTimer elapsedTimer) {
        runFirstSecond(startYoloState, elapsedTimer);
        singleObjectCooldownSet = new HashMap<Integer, Integer>();

    }

    public void runFirstSecond(YoloState state, ElapsedCpuTimer elapseTimer) {

    }

    public ACTIONS act(YoloState state, ElapsedCpuTimer elapseTimer) {

        KnowledgeBasedAStar aStar = new KnowledgeBasedAStar(state);
        aStar.setCountFoundItypes(true);
        aStar.setBlacklistedObjects(singleObjectCooldownSet.keySet());
        aStar.calculate(state.getAvatarX(), state.getAvatarY(),
                state.getAvatar().itype, new int[0], false, true, false);
        aStar.setBlacklistedObjects(null);


        Observation[] nearestITypesFound = aStar.nearestITypeObservationFound;
        int[] iTypeFoundCount = aStar.iTypesFoundCount;
        List<InterestingTarged> possibleTargets = new LinkedList<InterestingTarged>();
        for (int i = 0; i < nearestITypesFound.length; i++) {
            if (nearestITypesFound[i] == null
                    || nearestITypesFound[i].category == Types.TYPE_FROMAVATAR
                    || nearestITypesFound[i].category == Types.TYPE_AVATAR) {
                continue;
            }

            InterestingTarged target = new InterestingTarged(
                    nearestITypesFound[i]);
            calculatePriorityValue(target, iTypeFoundCount[i], state, aStar);
            possibleTargets.add(target);
        }
        InterestingTarged bestTarged = null;


        if (possibleTargets.isEmpty()) {
            //No target found at all!
            Random r = new Random();
            List<ACTIONS> actions =  state.getAvailableActions();
            return actions.get(r.nextInt(actions.size()));
        }

        //do best First:
        Collections.sort(possibleTargets);
        bestTarged = possibleTargets.get(0);


        //Get the available actions in this game.
        ArrayList<ACTIONS> actions = state.getAvailableActions();

        //Determine an index randomly and get the action to return.



        //Return the action.
        return getAction(bestTarged, state);
    }

    private boolean isMovable(Vector2d pos, Vector2d dir, YoloState state) {
        YoloState newState = state.copy();

        if (dir.y < 0) {
            newState.advance(ACTIONS.ACTION_UP);
        }
        if (dir.y > 0) {
            newState.advance(ACTIONS.ACTION_DOWN);
        }
        if (dir.x > 0) {
            newState.advance(ACTIONS.ACTION_RIGHT);
        }

        if (dir.x < 0) {
            newState.advance(ACTIONS.ACTION_LEFT);
        }

        return !(pos.equals(newState.getAvatarPosition()));
    }

    private ACTIONS getAction(InterestingTarged target, YoloState state) {
        Vector2d targetPos = target.getObs().position;
        System.out.println(targetPos);
        Vector2d curPos = state.getAvatarPosition();
        Vector2d dir = targetPos.copy();
        dir.subtract(curPos);
        System.out.println("C: " + curPos + " - T: " + targetPos + " - D: " + dir);

        Vector2d up = new Vector2d(0, -1);
        Vector2d down = new Vector2d(0, 1);
        Vector2d left = new Vector2d(-1, 0);
        Vector2d right = new Vector2d(1, 0);

        if (dir.y < 0 && isMovable(curPos, up, state)) {
            return ACTIONS.ACTION_UP;
        }
        if (dir.y > 0 && isMovable(curPos, down, state)) {
            return ACTIONS.ACTION_DOWN;
        }
        if (dir.x > 0 && isMovable(curPos, right, state)) {
            return ACTIONS.ACTION_RIGHT;
        }
        if (dir.x < 0 && isMovable(curPos, left, state)) {
            return ACTIONS.ACTION_LEFT;
        }
        if (isMovable(curPos, up, state)) {
            return ACTIONS.ACTION_UP;
        }
        if (isMovable(curPos, down, state)) {
            return ACTIONS.ACTION_DOWN;
        }
        if (isMovable(curPos, right, state)) {
            return ACTIONS.ACTION_RIGHT;
        }

        if (isMovable(curPos, left, state)) {
            return ACTIONS.ACTION_LEFT;
        }

        return ACTIONS.ACTION_NIL;
    }

    private double calculatePriorityValue(InterestingTarged target,
                                          int iTypeFoundCount, YoloState state, KnowledgeBasedAStar kBA) {
        Observation observation = target.getObs();
        double prioValue = 0;
        int posX = (int) (observation.position.x / state.getBlockSize());
        int posY = (int) (observation.position.y / state.getBlockSize());
        PlayerEvent pEvent = YoloKnowledge.instance.getPlayerEvent(
                state.getAvatar().itype, observation.itype, true);
        YoloEvent event = pEvent.getEvent(state.getInventoryArray());
        int slot = event.getAddInventorySlotItem();
        boolean winState = event.getWinGame();
        boolean scoreIncrease = event.getScoreDelta() > 0;
        boolean notSeenYet = pEvent.getObserveCount() == 0;
        boolean useActionEffective = YoloKnowledge.instance
                .canInteractWithUse(state.getAvatar().itype, observation.itype);
        boolean inventoryIncrease = event.getAddInventorySlotItem() != -1;
        boolean isBadSpawner = false;
        boolean decreaseScore = event.getScoreDelta() < 0;
        boolean willCancel = pEvent.willCancel(state.getInventoryArray());
        boolean isProbablyWall = (double)pEvent.getCancelCount()/(double)pEvent.getObserveCount() > 0.95;
        boolean killedOnColision = event.getKill();
        boolean isPortal = observation.category == Types.TYPE_PORTAL;
        boolean iTypeChange = event.getIType() != -1;
        boolean gotScoreThroughUseAction = YoloKnowledge.instance.getIncreaseScoreIfInteractWith(state.getAvatar().itype, observation.itype);

        //Is alone:
        int mask = state.getSimpleState().getMask(posX,posY);
        boolean standsOnWall = Integer.numberOfTrailingZeros(mask) + Integer.numberOfLeadingZeros(mask) != 31;

        if (YoloKnowledge.instance.isSpawner(observation.itype)) {
            isPortal = false;
            int iTypeIndexOfSpawner = YoloKnowledge.instance
                    .getSpawnIndexOfSpawner(observation.itype);
            PlayerEvent spawnedPEvent = YoloKnowledge.instance.getPlayerEvent(
                    state.getAvatar().itype,
                    YoloKnowledge.instance.indexToItype(iTypeIndexOfSpawner),
                    true);
            YoloEvent spawnedEvent = spawnedPEvent.getEvent(state
                    .getInventoryArray());
            isBadSpawner = spawnedEvent.getKill() || spawnedPEvent.getObserveCount() == 0;
        }
        boolean isInventoryFull = false;
        if (inventoryIncrease) {

            isInventoryFull = YoloKnowledge.instance.getInventoryMax(slot) == state
                    .getInventoryArray()[slot];
        }else{
            for (int i = 0; i < 5; i++) {
                if(YoloKnowledge.instance.getInventoryMax(i) == state.getInventoryArray()[i]){
                    isInventoryFull = true;
                    break;
                }
            }

        }
        double scoreIncreaseValue = event.getScoreDelta();
        int distanceToAvatar = kBA.distance[posX][posY];
        int index = YoloKnowledge.instance.itypeToIndex(observation.itype);

        //TODO: calculate priority values! (sample following)
        // Sample Dictionary-Lookup Priority values:

        if (isBadSpawner && !useActionEffective) {
            prioValue -= 1000;
        }
        if (decreaseScore) {
            prioValue -= 1000;
        }
        if (distanceToAvatar <= 6) {
            prioValue += 21;
        }

        if (winState) {
            prioValue += 1000;
        } else if (notSeenYet && isPortal) {
            prioValue += 150;
        } else if (isPortal && isInventoryFull) {
            prioValue += 150;
        }else if (willCancel) {
            if(isProbablyWall){
                prioValue -= 250;
            }else
                prioValue -= 200;
        } else if (scoreIncrease && inventoryIncrease) {
            if(isInventoryFull)
                prioValue += 130;
            else
                prioValue += 140;
        } else if (scoreIncrease) {
            prioValue += 135;
        } else if (inventoryIncrease) {
            prioValue += 132;
        } else if (notSeenYet) {
            prioValue += 131;
        } else if (!killedOnColision && useActionEffective) {
            if(gotScoreThroughUseAction){
                prioValue += 130;
            }
            else
                prioValue += 90;
        } else if (iTypeChange) {
            prioValue += 100;
        } else if (killedOnColision && useActionEffective) {
            if(gotScoreThroughUseAction){
                if(YoloKnowledge.instance.getNpcMovesEveryXTicks(YoloKnowledge.instance.itypeToIndex(observation.itype))>2)
                    prioValue += 125;
                else
                    prioValue += 105;
            }
            else
                prioValue += 85;
        } else if (killedOnColision && !useActionEffective) {
            prioValue -= 100;
        }

        target.setPriorityValue(prioValue);
        target.setWinCondition(winState);
        target.setIsUseable(useActionEffective);
        target.setUnseen(notSeenYet);
        target.setDistance(distanceToAvatar);;
        target.setScoreIncrease(gotScoreThroughUseAction || scoreIncrease);
        return prioValue;
    }
}

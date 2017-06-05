package Jaybot.YOLOBOT.Util.Wissensdatenbank.Helper;

import Jaybot.YOLOBOT.Agent;
import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.PlayerUseEvent;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import Jaybot.YOLOBOT.YoloState;
import core.game.Event;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

import java.util.*;

/**
 * Created by Torsten on 05.06.17.
 */
public class Learner {

    /**
     * Learns collision events (YoloEvent)
     */
    public static void learnCollisionEvent(YoloState currentState, YoloState previousState, Types.ACTIONS actionDone) {
        final YoloKnowledge knowledge = YoloKnowledge.getInstance();
        // get the inventory of the current state
        byte[] inventory = knowledge.getRandomForestClassifier().getInventoryArray(currentState);
        // create the YoloEvent 2 learn
        YoloEvent event2Learn = YoloEvent.create(currentState, previousState, actionDone, inventory);
        // train the random forest with the YoloEvent
        knowledge.getRandomForestClassifier().train(inventory, event2Learn);
    }

    /**
     * Learns use events occured because of a collision from the player created projectile with another object.
     */
    public static void learnUseEvent(YoloState currentState, YoloState lastState, Event collider) {

        YoloKnowledge knowledge = YoloKnowledge.getInstance();

        if (!Agent.UPLOAD_VERSION)
            System.out.println("Learn Use Event: " + collider.activeSpriteId + " -> " + collider.passiveSpriteId);

        boolean wall = currentState.getSimpleState().getObservationWithIdentifier(collider.passiveSpriteId) != null;

        if (knowledge.getUseEffect(collider.activeTypeId, collider.passiveTypeId) == null) {
            knowledge.storeUseEffect(collider.activeTypeId, collider.passiveTypeId, new PlayerUseEvent());
        }

        PlayerUseEvent uEvent = knowledge.getUseEffect(collider.activeTypeId, collider.passiveTypeId);
        byte deltaScore = (byte) (currentState.getGameScore() - lastState.getGameScore());

        if (!knowledge.hasScoreWithoutWinning() && deltaScore > 0 && !currentState.isGameOver())
            knowledge.setScoreWithoutWinning(true);

        uEvent.learnTriggerEvent(deltaScore, wall);
    }


    /**
     * Learns how far (maximum) a NPCs can walk/travel in one gametick.
     */
    public static void learnNpcMovementRange(YoloState currentState, YoloState previousState) {
        YoloKnowledge knowledge = YoloKnowledge.getInstance();

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

                int iType = currentObs.itype;

                Vector2d currentPos = currentObs.position;
                Vector2d previousPos = previousObs.position;

                int moveX = (int) Math.abs(currentPos.x - previousPos.x);
                int moveY = (int) Math.abs(currentPos.y - previousPos.y);

                if (moveX > knowledge.getMaxMoveInPixelPerNpcIType(iType, YoloKnowledge.AXIS_X)) {
                    knowledge.storeMaxMoveInPixelPerNpcIType(iType, YoloKnowledge.AXIS_X, moveX);
                }
                if (moveY > knowledge.getMaxMoveInPixelPerNpcIType(iType, YoloKnowledge.AXIS_Y)) {
                    knowledge.storeMaxMoveInPixelPerNpcIType(iType, YoloKnowledge.AXIS_Y, moveY);
                }
            }
        }
    }

    private static List<Observation> getAdjacentObservations(YoloState state, int x, int y) {
        final List<Observation> result = new LinkedList<>();
        final ArrayList<Observation>[][] grid = state.getObservationGrid();
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

            if (tempY < 0 || tempY > yMax) {
                continue;
            }

            result.addAll(grid[x][tempY]);
        }

        return result;
    }

    private static Observation findITypeSpawnedOnPosition(YoloState currentState, YoloState previousState, int x, int y) {
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

        return null;
    }

    /**
     * Checks every portal object if it has spawned something and what it has spawned.
     * Therefore it checks the field of the portal itself for any objects which did not exist before.
     * And it check the adjacent fields as well, as portals could spawn onto the adjacent fields.
     */
    public static void learnSpawner(YoloState currentState, YoloState previousState) {
        final YoloKnowledge knowledge = YoloKnowledge.getInstance();
        final ArrayList<Observation>[] portalObservationsArray = currentState.getPortalsPositions();

        for (ArrayList<Observation> portalObs : portalObservationsArray) {
            for (Observation portal : portalObs) {

                if (knowledge.getSpawnedNpcIType(portal.itype) != YoloKnowledge.UNDEFINED ||
                        knowledge.getSpawnedRessourceIType(portal.itype) != YoloKnowledge.UNDEFINED) {
                    // we assume that spawners do not change their spawned object type
                    continue;
                }

                Observation spawnedIType;
                Vector2d portalPos = portal.position;
                int x = (int) portalPos.x;
                int y = (int) portalPos.y;

                spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, x, y);
                if (spawnedIType != null) {
                    knowledge.storeSpawnedIType(spawnedIType, portal.itype);
                    continue;
                }


                // step one to the left and one to the right or one up and one down
                final int[] steps = new int[]{-1, 1};
                // check the field "left" and "right" from the current field.
                for (int xStep : steps) {
                    int tempX = x + xStep;

                    spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, tempX, y);
                    if (spawnedIType != null) {
                        knowledge.storeSpawnedIType(spawnedIType, portal.itype);
                        break;
                    }
                }

                if (knowledge.getSpawnedNpcIType(portal.itype) != YoloKnowledge.UNDEFINED ||
                        knowledge.getSpawnedRessourceIType(portal.itype) != YoloKnowledge.UNDEFINED) {
                    continue;
                }

                // check the field "left" and "right" from the current field.
                for (int yStep : steps) {
                    int tempY = y + yStep;

                    spawnedIType = findITypeSpawnedOnPosition(currentState, previousState, x, tempY);
                    if (spawnedIType != null) {
                        knowledge.storeSpawnedIType(spawnedIType, portal.itype);
                        break;
                    }
                }
            }
        }
    }

    public static void learnDynamicObjects(YoloState currentState, YoloState previousState) {
        final YoloKnowledge knowledge = YoloKnowledge.getInstance();
        // Avatar things are not interpreted as dynamic! Therefore we only check the other types.
        int[] categories2Check = new int[]{Types.TYPE_MOVABLE, Types.TYPE_NPC, Types.TYPE_PORTAL, Types.TYPE_RESOURCE, Types.TYPE_STATIC};
        SimpleState simplePrevious = previousState.getSimpleState();
        simplePrevious.fullInit();

        for (int category : categories2Check) {
            ArrayList<Observation>[] obsByCategory = currentState.getObservationList(category);

            if (obsByCategory == null) {
                continue;
            }

            for (ArrayList<Observation> observations : obsByCategory) {

                if (observations == null || observations.isEmpty()) {
                    continue;
                }

                for (Observation obsCurrent : observations) {
                    int iType = obsCurrent.itype;

                    if (knowledge.isDynamic(iType)) {
                        continue;
                    }

                    Observation obsPrevious = simplePrevious.getObservationWithIdentifier(iType);
                    // observation did not exist before, it was spawned --> it is dynamic
                    if (obsPrevious == null) {
                        knowledge.storeDynamicIType(iType);
                        break;
                    }
                    // the observation did move --> it is dynamic
                    else if (!obsPrevious.position.equals(obsCurrent.position)) {
                        knowledge.storeDynamicIType(iType);
                        break;
                    }
                }

            }
        }
    }
}

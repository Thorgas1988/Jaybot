package Jaybot.YOLOBOT;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import Jaybot.YOLOBOT.SubAgents.BreitenSucheAgent;
import Jaybot.YOLOBOT.SubAgents.HandleMCTS.MCTHandler;
import Jaybot.YOLOBOT.SubAgents.SubAgent;
import Jaybot.YOLOBOT.SubAgents.SubAgentStatus;
import Jaybot.YOLOBOT.SubAgents.bfs.BFS;
import Jaybot.YOLOBOT.Util.Heatmap;
import Jaybot.YOLOBOT.Util.Heuristics.HeuristicList;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Agent extends AbstractPlayer {

	public final static boolean UPLOAD_VERSION = true;
	public final static boolean DRAW_TARGET_ONLY = false;
	public final static boolean FORCE_PAINT = false;
	public final static boolean VIEW_ADVANCES = false;
	public static final double PAINT_SCALE = 1;

	private final List<SubAgent> subAgents;
	private SubAgent currentSubAgent;
	private long sum;
	public static ElapsedCpuTimer curElapsedTimer;

	//CheckVariablen um ersten Schritt-Bug zu umgehen:
	private int avatarXSpawn = -1, avatarYSpawn = -1;
	private StateObservation lastStateObs;
	public static YoloState currentYoloState;

	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		curElapsedTimer = elapsedTimer;
		YoloState startYoloState = new YoloState(so);
		avatarXSpawn = startYoloState.getAvatarX();
		avatarYSpawn = startYoloState.getAvatarY();
		//YoloKnowledge und sonstiges Wissen hier generieren
		YoloKnowledge.instance = new YoloKnowledge(startYoloState);
		Heatmap.instance = new Heatmap(startYoloState);
		HeuristicList.instance = new HeuristicList();

		// Liste von SubAgents wird hier stellt
		BFS bfs = new BFS(startYoloState, elapsedTimer);
		BreitenSucheAgent bsa = new BreitenSucheAgent(startYoloState,
				elapsedTimer);
		// PlannerAgent planner = new PlannerAgent(startYoloState,
		// elapsedTimer);
		subAgents = new LinkedList<>();
		subAgents.add(bsa);

		subAgents.add(new MCTHandler(startYoloState));
		subAgents.add(bfs);
//		subAgents.add(new TestSubAgent());

//		new TestSubAgent().preRun(startYoloState, elapsedTimer);
		// subAgents.add(planner);
		// bsa.preRun(startYoloState, elapsedTimer);

		// Planner deactiveated
		// ElapsedCpuTimer bfsTimer = new ElapsedCpuTimer();
		// bfsTimer.setMaxTimeMillis(elapsedTimer.remainingTimeMillis()-100);

		// bfs.preRun(startYoloState, bfsTimer);
		// if(YoloKnowledge.instance.getPushableITypes().size() == 1){
		// ACTIONS solutionFound = planner.act(startYoloState, elapsedTimer);
		// if(solutionFound != ACTIONS.ACTION_NIL){
		// //Planner has solution:
		// planner.actionsToDo.addFirst(solutionFound);
		// }
		// }

		bfs.preRun(startYoloState, elapsedTimer);

		if (!Agent.UPLOAD_VERSION)
			System.out.println(YoloKnowledge.instance.toString());
//		YoloKnowledge.instance.learnDeactivated = true;
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		curElapsedTimer = elapsedTimer;
		// if (currentYoloState != null && VIEW_ADVANCES) {
		// if (lastStateObs != null)
		// VGDLViewer.paint(lastStateObs.model);
		// lastStateObs = stateObs.copy();
		// }
		currentYoloState = new YoloState(stateObs);
		YoloKnowledge.instance.learnStochasticEffekts(currentYoloState);
//		YoloKnowledge.instance.learnContinuousMovingEnemies(currentYoloState);

//		System.out.println(YoloKnowledge.instance.toString());

		if(currentYoloState.getGameTick() == 1){
			//Ist erster Tick nach Spielstart:
			//Ueberpruefe, ob Erster-Schritt-Bug aufgetreten ist:
			if(ersterSchrittBugAufgetreten(currentYoloState)){
				return currentYoloState.getAvatarLastAction();
			}
		}
		//System.out.println(YoloKnowledge.instance.toString());
		// TODO: Hier koennten allgemeine, agentunabhaengige Auswertungen
		// geschehen, welche den aktuellen SubAgent auswaehlen
		YoloState.currentGameScore = currentYoloState.getGameScore();
		Heatmap.instance.stepOn(currentYoloState);

		checkIfAndDoAgentChange();

		if (!Agent.UPLOAD_VERSION)
			System.out.println("Chosen Agent: "
					+ currentSubAgent.getClass().getName());

		ACTIONS chosenAction = currentSubAgent.act(currentYoloState,
				elapsedTimer);

		if (chosenAction == ACTIONS.ACTION_NIL
				&& currentSubAgent.Status == SubAgentStatus.POSTPONED) {
			// Old agent give up!
			if (elapsedTimer.remainingTimeMillis() > 10) {
				// If we have time for another agent run, do so:
				checkIfAndDoAgentChange();
				chosenAction = currentSubAgent.act(currentYoloState,
						elapsedTimer);
			}
		}

		if (!Agent.UPLOAD_VERSION) {
			System.out.println("\t Chosen Action: " + chosenAction.toString());
			System.out.println("Advance Steps used: "
					+ YoloState.advanceCounter);
			System.out.println("Time remaining: "
					+ elapsedTimer.remainingTimeMillis());
			sum += YoloState.advanceCounter;
			// int avg = (int) (sum / currentYoloState.getGameTick());
			// System.out.println("Average Steps: " + avg);

			String dynamics = "Dynamic Objects:";
			for (int i = 0; i < 32; i++) {
				if (YoloKnowledge.instance.isDynamic(i)) {
					dynamics += "\t " + YoloKnowledge.instance.indexToItype(i);
				}
			}
			System.out.println(dynamics);

		}
		//TODO: Norman: Delete if you want, i use this for monitor the traffic on my pc
		//System.out.println("Advances:"+YoloState.advanceCounter);

		YoloState.advanceCounter = 0;

		return chosenAction;
	}

	private void checkIfAndDoAgentChange() {
		// Pruefe, ob ein neuer SubAgent ausgesucht werden muss
		if (currentSubAgent == null
				|| currentSubAgent.Status != SubAgentStatus.IN_PROGRESS) {
			currentSubAgent = ChooseNewIdleSubAgent(currentYoloState);

			// Falls kein Agent bereit ist, setze erneut alle auf Status "IDLE"
			// und suche erneut nach einem neuen Agent
			if (currentSubAgent == null) {
				for (SubAgent subAgent : subAgents) {
					subAgent.Status = SubAgentStatus.IDLE;
				}
				currentSubAgent = ChooseNewIdleSubAgent(currentYoloState);
			}

			currentSubAgent.Status = SubAgentStatus.IN_PROGRESS;
		}
	}

	private boolean ersterSchrittBugAufgetreten(YoloState yoloState) {
		boolean error = true;
		error &= yoloState.getGameTick() == 1;
		error &= yoloState.getAvatarOrientation().equals(
				YoloKnowledge.ORIENTATION_NULL);
		error &= yoloState.getAvatarX() == avatarXSpawn;
		error &= yoloState.getAvatarY() == avatarYSpawn;
		return error;

	}

	/**
	 * Finde einen neuen Agent mit Status "IDLE" und maximalem Gewicht
	 */
	private SubAgent ChooseNewIdleSubAgent(YoloState yoloState) {
		SubAgent newAgent = null;

		double maxWeight = -Double.MAX_VALUE;
		for (SubAgent subAgent : subAgents) {
			if (subAgent.Status == SubAgentStatus.IDLE) {
				double subAgentWeight = subAgent.EvaluateWeight(yoloState);

				if (maxWeight < subAgentWeight) {
					maxWeight = subAgentWeight;
					newAgent = subAgent;
				}
			}
		}

		return newAgent;
	}

	//TODO: The method is totally ruined by Norman
	@Override
	public void draw(Graphics2D g) {
		if (Agent.UPLOAD_VERSION && !FORCE_PAINT)
			return;
		try {
			if (currentSubAgent != null) {
				//currentSubAgent.draw(g);
			}

			if (currentYoloState == null)
				return;

			//Draw KillByStochastic:
			int block_size = currentYoloState.getBlockSize();
			int half_block = (int) (block_size * 0.5);

			g.setColor(Color.black);
			for (int j = 0; j < currentYoloState.getObservationGrid().length; ++j) {
				for (int i = 0; i < currentYoloState.getObservationGrid().length; ++i) {

/*
					for (Observation obs : currentYoloState.getObservationGrid()[i][j]) {
						int index = YoloKnowledge.instance.itypeToIndex(obs.itype);

						//Bad-SpawnerCheck:
						if (YoloKnowledge.instance.isSpawner(obs.itype)) {
							int iTypeIndexOfSpawner = YoloKnowledge.instance.getSpawnIndexOfSpawner(obs.itype);
							PlayerEvent spawnedPEvent = YoloKnowledge.instance.getPlayerEvent(currentYoloState.getAvatar().itype,
									YoloKnowledge.instance.indexToItype(iTypeIndexOfSpawner), true);
							YoloEvent spawnedEvent = spawnedPEvent.getEvent(currentYoloState.getInventoryArray());
							boolean isBadSpawner = spawnedEvent.getKill() || spawnedPEvent.getObserveCount() == 0;
							if (isBadSpawner) {
								g.drawRect(i * block_size, j * block_size, block_size, block_size);
							}
						}
					}
					//Draw TOD:
					String print = "";
					if (!Agent.DRAW_TARGET_ONLY) {

						print = "TOD";
					}
					if (YoloKnowledge.instance.canBeKilledByStochasticEnemyAt(currentYoloState, i, j)) {
					}

					if (!Agent.DRAW_TARGET_ONLY) {
						g.drawString(print, i * block_size, j * block_size +
								half_block + 12);
					}

					//Draw (stupid) raster:
					if (!Agent.DRAW_TARGET_ONLY) {
						g.drawRect(i * block_size, j * block_size, block_size, block_size);
					}*/
				}
			}
/*
			ArrayList<Observation> portals[] = currentYoloState.getPortalsPositions();
			for (int k = 0; k < portals.length; k++) {
				for (Observation temp : portals[k]) {
					int[] posXY = YoloKnowledge.instance.vectorPosToGridPos(temp.position, block_size);

					g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y+12);
				}
			}

			ArrayList<Observation> movPos[] = currentYoloState.getMovablePositions();
			for (int k = 0; k < movPos.length; k++) {
				for (Observation temp : movPos[k]) {

					g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y+12);
				}
			}

			ArrayList<Observation> imMovPos[] = currentYoloState.getImmovablePositions();
			for (int k = 0; k < imMovPos.length; k++) {
				for (Observation temp : imMovPos[k]) {

					g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y+12);
				}
			}

			ArrayList<Observation> avPos[] = currentYoloState.getFromAvatarSpritesPositions();
			for (int k = 0; k < avPos.length; k++) {
				for (Observation temp : avPos[k]) {

					g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y);
				}
			}
*/
			ArrayList<Observation> immPos[] = currentYoloState.getResourcesPositions();
			for (int k = 0; k < immPos.length; k++) {
				for (Observation temp : immPos[k]) {
					if (temp.itype != 2)
					{
						g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y+12);
					}

				}
			}

			ArrayList<Observation> observations[] = currentYoloState.getNpcPositions();

//			for (int k = 0; k < observations.length; k++) {
//				for (Observation temp : observations[k]) {
//					int obsIndex = YoloKnowledge.instance.itypeToIndex(temp.itype);
//
//					if (YoloKnowledge.instance.isContinuousMovingEnemy(obsIndex)) {
//						double diff = currentYoloState.getAvatar().position.dist(temp.position);
//
//						g.drawString(temp.itype+"", (int) temp.position.x, (int) temp.position.y);
//						//g.drawString(temp.position.toString(), (int) temp.position.x, (int) temp.position.y+12);
//						g.drawString(diff / half_block + "", (int) temp.position.x, (int) temp.position.y + 12);
//						//g.drawLine((int) temp.position.x + half_block, (int) temp.position.y + half_block, (int) currentYoloState.getAvatar().position.x, (int) currentYoloState.getAvatar().position.y);
//					}
//
//				}
//			}

			g.setColor(Color.MAGENTA);

			boolean spawnMap[][] = YoloKnowledge.instance.continuousKillerMap;

			for (int i = 0; i < YoloKnowledge.instance.MAX_X; i++) {
				for (int j = 0; j < YoloKnowledge.instance.MAX_Y; j++) {
					if (spawnMap[i][j])
					{
						g.drawRect(i*currentYoloState.getBlockSize(),
								j*currentYoloState.getBlockSize(),
								currentYoloState.getBlockSize(),
								currentYoloState.getBlockSize());
					}
					//TODO: IMPORTANT, if you delete this go to Yoloknowledgebase -> calculateContinuousKillerMap and switch on deletion
					//spawnMap[i][j] = false;
				}
			}


		} catch (Exception e) {
		}

	}
}
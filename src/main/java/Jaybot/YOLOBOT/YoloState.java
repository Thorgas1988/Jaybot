package Jaybot.YOLOBOT;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import Jaybot.YOLOBOT.Util.SimpleState;
import Jaybot.YOLOBOT.Util.StochasticKillmap;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;
import ontology.Types;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.Vector2d;

import java.awt.*;
import java.util.*;

/**
 *	It's an enhanced version of Forward Model and State Observation. Commented by thomas <br><br>
 *
 *	Code by Elvir: record state observation queries<br>
 *<ul>
 *	<li>Part 0: forward model</li>
 *	<li>Part 1: state observation - game state</li>
 *	<li>Part 2: state observation - avatar state</li>
 *	<li>Part 3: state observation - events</li>
 *	<li>Part 4: state observation - other sprites</li>
 *</ul>
 *
 * Code by Tobias: added some advanced function<br>
 *<ul>
 *	<li>(1) state hash code</li>
 *		<ul>
 *		 	<li>getHash()</li>
 *		 	<li>getBFSCheckHash() </li>
 *		</ul>
 *	<li>(2) advanced pruning of available actions</li>
 *		<ul>
 *		 	<li>getValidActions()</li>
 *		</ul>
 *	<li>(3) others</li>
 *		<ul>
 *		 	<li>isOneDimension()</li>
 *		 	<li>setNewSeed</li>
 *		</ul>
 *	<li>(4) Advanced getters: </li>
 *		<ul>
 *  		<li>avatar observation</li>
 *  		<li>observations by category or itypes</li>
 *  		<li>all itypes in the game</li>
 *  		<li>inventory items in array form</li>
 *  		<li>simple state</li>
 * 		</ul>
 * 	<li>(5) Raw getters: </li>
 *</ul>
 *
 *
 */

public class YoloState {

	private StateObservation _stateObservation = null;
	private HashSet<Integer> _allItypes = null;

	// Add by Elvir:
	public static double currentGameScore;

/* Following are member variables which are used to store the result of calling functions of StateObservation */

	// 1) Record: state of the game. See category Game
	private double _gameScore = -1.0;
	private int _gameTick = -1;
	private WINNER _gameWinner = WINNER.PLAYER_DISQ;
	private Dimension _worldDimension = null;
	private int _blockSize = -1;

	// 2) Record: the state of the avatar. See  category Avatar
	private ArrayList<ACTIONS> _availableActionsIncludeNil = null;
	private Vector2d _avatarPosition = null;
	private double _avatarSpeed = -1.0;
	private Vector2d _avatarOrientation = null;
	private HashMap<Integer, Integer> _avatarResources = null;
	private ArrayList<ACTIONS> _availableActions = null;
	private ArrayList<ACTIONS> _advancedActions = null;
	private ACTIONS _avatarLastAction = null;

	// 3) Record: events happened in the game. See category Events
	private TreeSet<Event> _eventHistory = null;

	// 4) Record: information about other sprites in the game (Now sorted in the same order as in the table). See category Observations
	private ArrayList<Observation>[][] _observationGrid = null;
	private ArrayList<Observation>[] _npcPositions = null;
	private ArrayList<Observation>[] _npcPositionsToAvatar = null;
	private ArrayList<Observation>[] _immovablePositions = null;
	private ArrayList<Observation>[] _immovablePositionsToAvatar = null;
	private ArrayList<Observation>[] _movablePositions = null;
	private ArrayList<Observation>[] _movablePositionsToAvatar = null;
	private ArrayList<Observation>[] _resourcesPositions = null;
	private ArrayList<Observation>[] _resourcesPositionsToAvatar = null;
	private ArrayList<Observation>[] _portalPositions = null;
	private ArrayList<Observation>[] _portalPositionsToAvatar = null;
	private ArrayList<Observation>[] _fromAvatarSpritesPositions = null;
	private ArrayList<Observation>[] _fromAvatarSpritesPositionsToAvatar = null;
	private ArrayList<Observation> _allObservations = null;

	// Add by Tobi:
	private Observation _agent = null;
	private int _agentX = -1;
	private int _agentY = -1;
	private byte[] _inventoryArray = null;
	private int _inventoryArrayUsageSize = -1;
	private SimpleState _simpleState = null;
	private LinkedList<Integer> _objectIdsUnderObservation = null;
	public static int advanceCounter;
	public static int advanceCounterPerRun;
	private ArrayList<ACTIONS> _availableActionsStochastic,
			_availableActionsNonStochastic;
	private boolean _oneDimensionalIsSet;
	private boolean _oneDimensional;
	private int _maxObsId = -1;
	private double _targetReachedCost;
	private boolean _isGameOverSet = false;
	private boolean _isGameOver;
	public static double avgAdvanceStepTimeNeeded;
	private StochasticKillmap _stochasticKillMap;
	private Integer _hp, _maxHP;



/* Constructors */
	// Constructor from StateObservation
	public YoloState(StateObservation so) {
		_stateObservation = so;
		_advancedActions = new ArrayList<ACTIONS>();
	}
	// Constructor from State Observation and Action History
	private YoloState(StateObservation so, ArrayList<ACTIONS> advancedActions) {
		_stateObservation = so;
		_advancedActions = advancedActions;
	}
	// Pseudo destructor
	private void clear() {
		_availableActions = null;
		_availableActionsIncludeNil = null;
		_avatarLastAction = null;
		_avatarOrientation = null;
		_avatarPosition = null;
		_avatarResources = null;
		_avatarSpeed = -1.0;
		_blockSize = -1;
		_eventHistory = null;
		_fromAvatarSpritesPositions = null;
		_gameScore = -1.0;
		_gameTick = -1;
		_gameWinner = WINNER.PLAYER_DISQ;
		_immovablePositions = null;
		_movablePositions = null;
		_npcPositions = null;
		_observationGrid = null;
		_portalPositions = null;
		_resourcesPositions = null;
		_worldDimension = null;

		// Add by Tobi:
		_agent = null;
		_agentX = -1;
		_agentY = -1;
		_inventoryArray = null;
		_inventoryArrayUsageSize = -1;
		_simpleState = null;
		_objectIdsUnderObservation = null;
		_availableActionsStochastic = null;
		_availableActionsNonStochastic = null;
		_oneDimensionalIsSet = false;
		_maxObsId = -1;
		_targetReachedCost = -1;
		_isGameOverSet = false;
		_stochasticKillMap = null;
		_hp = null;
		_maxHP = null;
	}


/* 0) Forward Model */
	/**
	 * Do one advance operation.
	 * Update average time needed for one advance step
	 * Update actions history array
	 * @param action selected action to advance
	 */
	public void advance(ACTIONS action) {
		advanceCounter++;
		advanceCounterPerRun++;
		double timeBeforAdvance = Agent.curElapsedTimer.remainingTimeMillis();
		_stateObservation.advance(action);
		double timeAfterAdvance = Agent.curElapsedTimer.remainingTimeMillis();
		avgAdvanceStepTimeNeeded = ((avgAdvanceStepTimeNeeded * (advanceCounterPerRun-1)) + (timeBeforAdvance - timeAfterAdvance))/advanceCounterPerRun;
		_advancedActions.add(action);
		clear();
	}
	/**
	 * @return An exact copy of the YoloState observation which only contains actions history and the raw StateObservation
	 */
	public YoloState copy() {
		return new YoloState(_stateObservation.copy(), _advancedActions);
	}
	/**
	 * Do one advance operation by calling advance()
	 * Learn from old state and advanced state by YoloKnowledge
	 * @param action selected action to advance
	 * @return An exact copy of the advanced YoloState observation which only contains actions history and the raw StateObservation
	 */
	public YoloState copyAdvanceLearn(ACTIONS action) {
		YoloState advancedState = copy();
		advancedState.advance(action);
		YoloKnowledge.getInstance().learnFrom(advancedState, this, action);
		return advancedState;
	}



	public StateObservation getStateObservation() {
		return _stateObservation;
	}



/* 1) State Observation: Game */
	public double getGameScore() {
		if (_gameScore == -1.0)
			_gameScore = _stateObservation.getGameScore();
		return _gameScore;
	}
	public int getGameTick() {
		if (_gameTick == -1)
			_gameTick = _stateObservation.getGameTick();
		return _gameTick;
	}
	public WINNER getGameWinner() {
		if (_gameWinner == WINNER.PLAYER_DISQ)
			_gameWinner = _stateObservation.getGameWinner();
		return _gameWinner;
	}
	public Dimension getWorldDimension() {
		if (_worldDimension == null)
			_worldDimension = _stateObservation.getWorldDimension();
		return _worldDimension;
	}
	public int getBlockSize() {
		if (_blockSize == -1.0)
			_blockSize = _stateObservation.getBlockSize();
		return _blockSize;
	}
	public boolean isGameOver() {
		if(!_isGameOverSet){
			_isGameOverSet = true;
			_isGameOver = _stateObservation.isGameOver();
		}
		return _isGameOver;
	}



/* 2) State Observation: Avatar */
	// Getters of available actions.
	// Additionally it did a pre-pruning.
	// ACTION.USE will be pruned if the avatar is at the border and facing to this border
	public ArrayList<ACTIONS> getAvailableActions() {
		if (_availableActions == null){
			_availableActions = _stateObservation.getAvailableActions();

			if(_availableActions.contains(ACTIONS.ACTION_USE) && YoloKnowledge.getInstance().avatarLooksOutOfGame(this))
				_availableActions.remove(ACTIONS.ACTION_USE);
		}
		return _availableActions;
	}
	public ArrayList<ACTIONS> getAvailableActions(boolean includeNil) {
		if (_availableActionsIncludeNil == null){
			_availableActionsIncludeNil = _stateObservation.getAvailableActions(includeNil);
			if(_availableActionsIncludeNil.contains(ACTIONS.ACTION_USE) && YoloKnowledge.getInstance().avatarLooksOutOfGame(this))
				_availableActionsIncludeNil.remove(ACTIONS.ACTION_USE);
		}
		return _availableActionsIncludeNil;
	}
	// 3 getters of actions history
	public ArrayList<ACTIONS> getAdvancedActions() {
		return _advancedActions;
	}
	public ACTIONS getAvatarLastAction() {
		if (_avatarLastAction == null)
			_avatarLastAction = _stateObservation.getAvatarLastAction();
		return _avatarLastAction;
	}
	public ACTIONS getLastAdvancedAction() {
		if (_advancedActions == null)
			return ACTIONS.ACTION_NIL;
		return _advancedActions.get(_advancedActions.size() - 1);
	}
	// Getters of avatar state
	public Vector2d getAvatarPosition() {
		if (_avatarPosition == null)
			_avatarPosition = _stateObservation.getAvatarPosition();
		return _avatarPosition;
	}
	public double getAvatarSpeed() {
		if (_avatarSpeed == -1.0)
			_avatarSpeed = _stateObservation.getAvatarSpeed();
		return _avatarSpeed;
	}
	public Vector2d getAvatarOrientation() {
		if (_avatarOrientation == null)
			_avatarOrientation = _stateObservation.getAvatarOrientation();
		return _avatarOrientation;
	}
	public HashMap<Integer, Integer> getAvatarResources() {
		if (_avatarResources == null)
			_avatarResources = _stateObservation.getAvatarResources();
		return _avatarResources;
	}
	// Point Position --> Grid Position
	public Vector2d getAvatarGridPosition() {
		return getGridPosition(getAvatarPosition());
	}
	public Vector2d getGridPosition(Vector2d position) {
		Vector2d vec = position;
		vec.x = vec.x / getBlockSize();
		vec.y = vec.y / getBlockSize();
		return vec;
	}




/* 3) State Observation: Events */
	public TreeSet<Event> getEventsHistory() {
		if (_eventHistory == null) {
			_eventHistory = _stateObservation.getEventsHistory();
		}
		return _eventHistory;
	}



/* 4) State Observation: Observations */

	// Following are raw getters of state observation category "other sprites"
	// Sorted in the same order as in the table http://www.gvgai.net/forwardModel.php
	// Additionally for each function a new function _ToAvatar is added by calling the variant with reference

	public ArrayList<Observation>[][] getObservationGrid() {
		if (_observationGrid == null)
			_observationGrid = _stateObservation.getObservationGrid();
		return _observationGrid;
	}

	public ArrayList<Observation>[] getNpcPositions() {
		if (_npcPositions == null)
			_npcPositions = _stateObservation.getNPCPositions();
		return _npcPositions;
	}
	public ArrayList<Observation>[] getNpcPositions(Vector2d reference) {
		return _stateObservation.getNPCPositions(reference);
	}
	public ArrayList<Observation>[] getNpcPositionsToAvatar() {
		if (_npcPositionsToAvatar == null)
			_npcPositionsToAvatar = _stateObservation
					.getNPCPositions(getAvatarPosition());
		return _npcPositionsToAvatar;
	}

	public ArrayList<Observation>[] getImmovablePositions() {
		if (_immovablePositions == null)
			_immovablePositions = _stateObservation.getImmovablePositions();
		return _immovablePositions;
	}
	public ArrayList<Observation>[] getImmovablePositions(Vector2d reference) {
		return _stateObservation.getImmovablePositions(reference);
	}
	public ArrayList<Observation>[] getImmovablePositionsToAvatar() {
		if (_immovablePositionsToAvatar == null)
			_immovablePositionsToAvatar = _stateObservation
					.getImmovablePositions(getAvatarPosition());
		return _immovablePositionsToAvatar;
	}

	public ArrayList<Observation>[] getMovablePositions() {
		if (_movablePositions == null)
			_movablePositions = _stateObservation.getMovablePositions();
		return _movablePositions;
	}
	public ArrayList<Observation>[] getMovablePositions(Vector2d reference) {
		return _stateObservation.getMovablePositions(reference);
	}
	public ArrayList<Observation>[] getMovablePositionsToAvatar() {
		if (_movablePositionsToAvatar == null)
			_movablePositionsToAvatar = _stateObservation
					.getMovablePositions(getAvatarPosition());
		return _movablePositionsToAvatar;
	}

	public ArrayList<Observation>[] getResourcesPositions() {
		if (_resourcesPositions == null)
			_resourcesPositions = _stateObservation.getResourcesPositions();
		return _resourcesPositions;
	}
	public ArrayList<Observation>[] getResourcesPositions(Vector2d reference) {
		return _stateObservation.getResourcesPositions(reference);
	}
	public ArrayList<Observation>[] getResourcesPositionsToAvatar() {
		if (_resourcesPositionsToAvatar == null)
			_resourcesPositionsToAvatar = _stateObservation
					.getResourcesPositions(getAvatarPosition());
		return _resourcesPositionsToAvatar;
	}

	public ArrayList<Observation>[] getPortalsPositions() {
		if (_portalPositions == null)
			_portalPositions = _stateObservation.getPortalsPositions();
		return _portalPositions;
	}
	public ArrayList<Observation>[] getPortalsPositions(Vector2d reference) {
		return _stateObservation.getPortalsPositions(reference);
	}
	public ArrayList<Observation>[] getPortalsPositionsToAvatar() {
		if (_portalPositionsToAvatar == null)
			_portalPositionsToAvatar = _stateObservation
					.getPortalsPositions(getAvatarPosition());
		return _portalPositionsToAvatar;
	}

	public ArrayList<Observation>[] getFromAvatarSpritesPositions() {
		if (_fromAvatarSpritesPositions == null)
			_fromAvatarSpritesPositions = _stateObservation
					.getFromAvatarSpritesPositions();
		return _fromAvatarSpritesPositions;
	}
	public ArrayList<Observation>[] getFromAvatarSpritesPositions(Vector2d reference) {
		return _stateObservation.getFromAvatarSpritesPositions(reference);
	}
	public ArrayList<Observation>[] getFromAvatarSpritesPositionsToAvatar() {
		if (_fromAvatarSpritesPositionsToAvatar == null)
			_fromAvatarSpritesPositionsToAvatar = _stateObservation
					.getFromAvatarSpritesPositions(getAvatarPosition());
		return _fromAvatarSpritesPositionsToAvatar;
	}

	// Getters of grid observation w.r.t avatar position and action direction
	public ArrayList<Observation> geObservationsInActionDirection(ACTIONS action) {
		Vector2d vec = getAvatarGridPosition();
		int x = (int) vec.x;
		int y = (int) vec.y;
		switch (action) {
			case ACTION_LEFT: x -= 1; break;
			case ACTION_RIGHT: x += 1; break;
			case ACTION_UP: y -= 1; break;
			case ACTION_DOWN: y += 1; break;
			default: break;
		}
		if (x < 0 || x >= getObservationGrid().length ||
			y < 0 || y >= getObservationGrid()[0].length )
			return null;
		return getObservationGrid()[x][y];
	}
	public ArrayList<Observation> getObservationsAtAvatarPosition() {
		return getObservationGrid()[(int) getAvatarGridPosition().x][(int) getAvatarGridPosition().y];
	}



// Add by Tobi:


/* Following are methods concerning about state hash code, see technical report capitel 6 */

	// 1) getModifiedHash(boolean ignoreNPCs, ..., boolean forBFSCheck) is the main method for hashing
	// 2) getModifiedHash(boolean ignoreNPCs, ...) is the intermediate caller of main method
	// 3) getHash(boolean ignoreNPCs) get hash code without BFS check by calling (2)
	// 4) getBfsCheckHash(boolean ignoreNPCs) get hash code with BFS check by calling (1) setting forBFSCheck true

	/**
	 * 1) Hash on avatar information
	 * 2) Hash on non-avatar and non-npc objects
	 * 		IF forBFSCheck: hash only on indexes of observationGrid
	 * 		ELSE: hash on these non-avatar and non-npc objects ID and itypes
	 * 3) Hash on inventory ID and item count
	 * @param ignoreNPCs if NPC are ignored
	 * @param avatarX avatar information
	 * @param avatarY avatar information
	 * @param avatarId avatar information
	 * @param avatarOrientationX avatar information
	 * @param avatarOrientationY avatar information
	 * @param forBFSCheck
	 * @return
	 */
	public long getModifiedHash(boolean ignoreNPCs, int avatarX, int avatarY,
			int avatarId, double avatarOrientationX, double avatarOrientationY, boolean forBFSCheck) {
		long prime = 31;
		long result = 17;
		result = result * prime + avatarX;
		result = result * prime + avatarY;
		result = result * prime + avatarId;
		result = result * prime + Double.doubleToLongBits(avatarOrientationX);
		result = result * prime + Double.doubleToLongBits(avatarOrientationY);

		for (int i = 0; i < getObservationGrid().length; i++) {
			result = result * prime + i;
			for (int j = 0; j < getObservationGrid()[i].length; j++) {
				result = result * prime + j;
				if(!forBFSCheck || j != 0 || i != 0)
				{
					for (Observation obs : getObservationGrid()[i][j]) {
						if (obs.category != Types.TYPE_AVATAR
								&& (obs.category != Types.TYPE_NPC || !ignoreNPCs)) {
							result = result * prime + obs.obsID;
							result = result * prime + obs.itype;
						}
					}
				}

			}
		}

		HashMap<Integer, Integer> inventory = getAvatarResources();
		for (int itemId : inventory.keySet()) {
			result = result * prime + itemId;
			result = result * prime + inventory.get(itemId);
		}

		// StringBuilder sb = new StringBuilder();
		// sb.append(avatarOrientationX);
		// sb.append(avatarOrientationY);
		// sb.append(">");
		// sb.append(avatarX);
		// sb.append(",");
		// sb.append(avatarY);
		// sb.append(",");
		// sb.append(avatarId);
		// sb.append(">");
		// for (int i = 0; i < getObservationGrid().length; i++) {
		// for (int j = 0; j < getObservationGrid()[i].length; j++) {
		// sb.append(i);
		// sb.append(",");
		// sb.append(j);
		// sb.append("_");
		// for (Observation obs : getObservationGrid()[i][j]) {
		// if(obs.category != Types.TYPE_AVATAR && (obs.category !=
		// Types.TYPE_NPC || ignoreNPCs)){
		// sb.append(obs.obsID);
		// sb.append(obs.itype);
		// sb.append(";");
		// }
		// }
		// }
		// }
		// sb.append(" ");
		// //Res:
		// HashMap<Integer, Integer> inventory = getAvatarResources();
		// for (int item : inventory.keySet()) {
		// sb.append(item);
		// sb.append(":");
		// sb.append(inventory.get(item));
		// }
		// retVal += "Score:" + state.getGameScore();

		return result;
	}
	public long getModifiedHash(boolean ignoreNPCs, int avatarX, int avatarY,
								int avatarId, double avatarOrientationX, double avatarOrientationY) {
		return getModifiedHash(ignoreNPCs, avatarX, avatarY, avatarId, avatarOrientationX, avatarOrientationY, false);
	}
	public long getHash(boolean ignoreNPCs) {
		int itype = (getAvatar() != null) ? _agent.itype : -1;

		Vector2d orientation = getAvatarOrientation();
		return getModifiedHash(ignoreNPCs, getAvatarX(), getAvatarY(), itype,
				orientation.x, orientation.y);
	}
	public long getBfsCheckHash(boolean ignoreNPCs) {
		int itype = (getAvatar() != null) ? _agent.itype : -1;

		Vector2d orientation = getAvatarOrientation();
		return getModifiedHash(ignoreNPCs, getAvatarX(), getAvatarY(), itype,
				orientation.x, orientation.y, true);
	}


/* Advanced pruning of available actions */

	/**
	 * IF corresponding pruned available actions was set: return directly
	 * ELSE: iterate over all raw available actions
	 * 			remove those which will be blocked, predicated by using knowledge base classifier
	 * 		 return the pruned available actions
	 */
	public ArrayList<ACTIONS> getValidActions(
			boolean ignoreStochasticEnemyKilling) {

		if (ignoreStochasticEnemyKilling
				&& _availableActionsNonStochastic != null)
			return _availableActionsNonStochastic;
		else if (!ignoreStochasticEnemyKilling
				&& _availableActionsStochastic != null)
			return _availableActionsStochastic;

		// ansonsten muss berechnet werden
		ArrayList<ACTIONS> validActions = new ArrayList<ACTIONS>(
				getAvailableActions(true));

		for (Iterator<ACTIONS> iterator = validActions.iterator(); iterator
				.hasNext();) {
			ACTIONS actions = (ACTIONS) iterator.next();
			// TODO: stehen bleiben check -> kill?
			if (YoloKnowledge.getInstance().moveWillCancel(this, actions, true,
					ignoreStochasticEnemyKilling)) {
				iterator.remove();
			}
		}
		if (ignoreStochasticEnemyKilling)
			_availableActionsNonStochastic = validActions;
		else
			_availableActionsStochastic = validActions;

		return validActions;
	}


/* Others */

	// By checking if vertical actions and horizonal actions are allowed
	public boolean isOneDimensionGame() {
		if (_oneDimensionalIsSet)
			return _oneDimensional;
		getAvailableActions();
		boolean yAxis = _availableActions.contains(ACTIONS.ACTION_DOWN)
				&& _availableActions.contains(ACTIONS.ACTION_UP);
		boolean xAxis = _availableActions.contains(ACTIONS.ACTION_LEFT)
				&& _availableActions.contains(ACTIONS.ACTION_RIGHT);

		_oneDimensionalIsSet = true;
		_oneDimensional = (yAxis != xAxis);

		return _oneDimensional;
	}
	public void setNewSeed(int seed) {
		_stateObservation.setNewSeed(seed);
	}


/* Advanced getters of member variables */

	// Getter of avatar observation
	/**
	 * Transform avatar point position to grid position.
	 * Get all observations in this grid.
	 * Among these observations, set the one with MAXIMAL itype value as avatar and return this
	 */
	public Observation getAvatar() {
		if (_agent != null) return _agent;
		// init needed values:
		if (_blockSize == -1) getBlockSize();
		if (_avatarPosition == null) getAvatarPosition();
		if (_observationGrid == null) getObservationGrid();
		// among all observations on this grid, set the one with maximal itype value as avatar
		_agentX = (int) _avatarPosition.x / _blockSize;
		_agentY = (int) _avatarPosition.y / _blockSize;
		int nr = 0;
		if (_agentX >= 0 && _agentY >= 0 && _agentX < _observationGrid.length
				&& _agentY < _observationGrid[0].length)
			for (Observation obs : _observationGrid[_agentX][_agentY]) {
				if (obs.category == Types.TYPE_AVATAR) {
					if (obs.itype > nr) {
						_agent = obs;
						nr = obs.itype;
					}
				}
			}
		return _agent;
	}

	// Getters of observations by itype and category
	public ArrayList<Observation>[] getObservationList(int category) {
		switch (category) {
			case Types.TYPE_FROMAVATAR:
				return getFromAvatarSpritesPositions();
			case Types.TYPE_MOVABLE:
				return getMovablePositions();
			case Types.TYPE_NPC:
				return getNpcPositions();
			case Types.TYPE_PORTAL:
				return getPortalsPositions();
			case Types.TYPE_RESOURCE:
				return getResourcesPositions();
			default:
				// case Types.TYPE_STATIC:
				return getImmovablePositions();
		}
	}
	public ArrayList<Observation> getObservationsByItype(int itype) {
		ArrayList<Observation> dynamics = new ArrayList<Observation>();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for (ArrayList<Observation>[] arr : getObservationGrid()) {
			for (ArrayList<Observation> list : arr) {
				for (Observation o : list) {
					if (o.itype == itype && !ids.contains(o.obsID)){
						dynamics.add(o);
						ids.add(o.obsID);
					}
				}
			}
		}

		return dynamics;
	}

	// Iterate observations on all grids, return all itypes occurred in the game
	public HashSet<Integer> getAllItypes() {
		if (_allItypes == null) {
			_allItypes = new HashSet<Integer>();

			for (ArrayList<Observation>[] arr : getObservationGrid()) {
				for (ArrayList<Observation> list : arr) {
					for (Observation o : list) {
						_allItypes.add(o.itype);
					}
				}
			}
		}

		return _allItypes;
	}

	// Getter of inventory items
	/**
	 * Transform the raw form (hash map) of avatar resources into byte array form
	 * By calling getInventoryArray() in YoloKnowledge
	 */
	public byte[] getInventoryArray() {
		if (_inventoryArray == null) {
			getAvatarResources();
			_inventoryArray = YoloKnowledge.getInstance().getInventoryArray(_avatarResources, getHP());
			// ? Why HP, in the function in YoloKnowledge HP value is also not used
			_inventoryArrayUsageSize = _avatarResources.keySet().size();
		}
		return _inventoryArray;
	}
	public int getInventoryArrayUsageSize() {
		if (_inventoryArrayUsageSize == -1)
			getInventoryArray();
		return _inventoryArrayUsageSize;
	}

	// Getter of simple state observation
	public SimpleState getSimpleState() {
		if (_simpleState == null){
			_simpleState = new SimpleState(this, false);
			_simpleState.fullInit();
		}
		return _simpleState;
	}



/* Raw getters and setters of some member variables */
	public StochasticKillmap getStochasticKillMap() {
		if(_stochasticKillMap == null){
			_stochasticKillMap = new StochasticKillmap(this);
		}
		return _stochasticKillMap;
	}

	public int getAvatarX() {
		if (_agentX == -1)
			getAvatar();
		return _agentX;
	}
	public int getAvatarY() {
		if (_agentY == -1)
			getAvatar();
		return _agentY;
	}

	private void calcHP() {
		_hp = _stateObservation.getAvatarHealthPoints();
		_maxHP = _stateObservation.getAvatarHealthPoints();
	}
	public int getMaxHp(){
		if(_maxHP == null)
			calcHP();
		return _maxHP;
	}
	public int getHP(){
		if(_hp == null)
			calcHP();
		return _hp;
	}

	public void setTargetReachedCost(double targetReachedCost) {
		_targetReachedCost = targetReachedCost;
	}
	public double getTargetReachedDepth() {
		return _targetReachedCost;
	}

	public int getMaxObsId() {
		return _maxObsId;
	}
	public int setMaxObsId(int id) {
		return _maxObsId = id;
	}

	public LinkedList<Integer> getObjectIdsUnderObservation() {
		return _objectIdsUnderObservation;
	}
	public void addObjectToObservate(int objectIdentifier) {
		if (_objectIdsUnderObservation == null)
			_objectIdsUnderObservation = new LinkedList<Integer>();
		_objectIdsUnderObservation.add(objectIdentifier);
	}





}

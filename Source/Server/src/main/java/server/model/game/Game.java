package server.model.game;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.UniqueGameIdentifier;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.EPlayerGameState;
import MessagesGameState.FullMap;
import MessagesGameState.GameState;
import server.controller.GameController;
import server.model.map.FullMapRaw;
import server.model.map.HalfMapRaw;
import MessagesGameState.PlayerState;

public class Game {
	
	private static final Logger GameLogger = LoggerFactory.getLogger(Game.class);
	
	private UniqueGameIdentifier gameId;
	private ArrayList<Player> players=new ArrayList<>();
	private FullMapRaw fullMap= new FullMapRaw();
	private ArrayList<HalfMapRaw> halfMaps=new ArrayList<>();
	private LocalDateTime gameCreationTimeStamp;
	private GameState gameState= new GameState();
	private boolean gameover=false;
	private boolean movementIntitialized=false;
	private int roundCounter=0;
	private final static int MAXIMUM_NUMBER_OF_ROUNDS=200;
	public final static int MAX_PLAYER_NUMBER=2;
	
	public Game() {
		gameId=genereateGameId();
		gameCreationTimeStamp= LocalDateTime.now();
	}
	

	/*
	 * To get a random id for the game, found it much easier to simply take the
	 * first 5 characters from an UUID
	 */
	public UniqueGameIdentifier genereateGameId() {
		UUID rawId= UUID.randomUUID();
		return new UniqueGameIdentifier(rawId.toString().substring(0, 5));
	}
	
	
	/*
	 * After both players have been registered, we randomly assign a player as
	 * shouldActNext though the toggle method
	 */
	public void initializeTurns() {
		Random random = new Random();
		int shouldActNextIndex = random.nextInt(MAX_PLAYER_NUMBER);
		players.get(shouldActNextIndex).togglePlayerState();
		
	}
	/*
	 * Toggle who has to act next
	 */
	public void changePlayerTurns() {
		for(Player p: players) {
			p.togglePlayerState();
			GameLogger.debug("changePlayerTurns - player: {}, state: {}", p.getPlayerId().toString(), p.getPlayerState());
		}
	}
	
	/*
	 * Declare both the winner and looser, but we have different methods
	 * Because it's simply easier
	 */
	public void declareWinner(UniquePlayerIdentifier playerId) {
		for(Player p: players) {
			if(p.getPlayerId().equals(playerId)) {
				p.setPlayerGameState(EPlayerGameState.Won);
			}
			else {
				p.setPlayerGameState(EPlayerGameState.Lost);
			}
			gameover=true;
			GameLogger.debug("declareWinner - player: {}, state: {}", p.getPlayerId().toString(), p.getPlayerState());
		}
	}
	
	public void declareLooser(UniquePlayerIdentifier playerId) {
		for(Player p: players) {
			if(p.getPlayerId().equals(playerId)) {
				p.setPlayerGameState(EPlayerGameState.Lost);
			}
			else {
				p.setPlayerGameState(EPlayerGameState.Won);
			}
			gameover=true;
			GameLogger.debug("declareLooser - player: {}, state: {}", p.getPlayerId().toString(), p.getPlayerState());
		}
	}
	
	/*
	 * When you get the game state, you need the gamestate to update because it depends on
	 * the player who calls it, but it shouldn't actually be updated
	 * AdaptGameState updates the gamestate, but i doens't change the id,
	 * because it couldn't have changed
	 */
	public GameState getGameState(UniquePlayerIdentifier playerId) {
		updateLastRequest(playerId);
		adaptGameState(playerId);
		return gameState;
	}
	
	
	/*
	 * Meanwhile, when we update the game, we generate a new game state id.
	 * There are 3 different cases where we update the gamestate:
	 */
	public void updateGameState(UniquePlayerIdentifier playerId) {
		String gameStateId = UUID.randomUUID().toString();
		if(halfMaps.size()==0) {
			updateGameStateNoMap(gameStateId, playerId);
		}
		else {
			updateGameStateWithMap(gameStateId, playerId);
		}
		
	}
	
	/*
	 * 1. when there aren't any maps, then we don't include any map in the gamestate
	 */
	private void updateGameStateNoMap(String gameStateId, UniquePlayerIdentifier playerId) {
		//TODO: make gameStateId dependent on if gamestate has changed
		//String gameStateId = UUID.randomUUID().toString();
		gameState = new GameState(getPlayerStateCollection(playerId), gameStateId);
	}
	
	/*
	 * 2. when there is only one map, we need an incomplete full map
	 */
	private void updateGameStateWithMap(String gameStateId, UniquePlayerIdentifier playerId) {
		//String gameStateId = UUID.randomUUID().toString();
		FullMap fullMapReply= new FullMap();
		if(halfMaps.size()==1) {
			fullMapReply=fullMap.generateIncompleteFullMap(halfMaps.get(0), playerId, true);
		}
		/*
		 * 3. when it is the first time that we form the FullMapRaw
		 */
		else {
			if(!movementIntitialized) {
				fullMap = new FullMapRaw(halfMaps);
				movementIntitialized=true;
			}
			/*
			 * 4. the rest of the times
			 */
			fullMapReply=fullMap.generateFullMap(playerId, roundCounter>10 ? false:true);
		}
		Optional<FullMap> fullMapOptional= Optional.of(fullMapReply);
		gameState = new GameState(fullMapOptional, getPlayerStateCollection(playerId), gameStateId);
	}
	
	
	private void adaptGameState(UniquePlayerIdentifier playerId) {
		String gameStateId = gameState.getGameStateId();
		if(halfMaps.size()==0) {
			updateGameStateNoMap(gameStateId, playerId);
		}
		else {
			updateGameStateWithMap(gameStateId, playerId);
		}
	}
	
	/*
	 * Because we need a collection for the gamestate, we convert the ArrayList
	 */
	private Collection<PlayerState> getPlayerStateCollection(UniquePlayerIdentifier playerId) {
		Collection<PlayerState> playerStateCollection = new ArrayList<PlayerState>();
		for(Player p: players) {
			if(p.getPlayerId().equals(playerId)) {
				playerStateCollection.add(p.getPlayerState());
			}
			/*
			 * We hide the opponent's id
			 */
			else {
				Player opponent = new Player(p);
				playerStateCollection.add(opponent.getPlayerState());
			}
		}
		return playerStateCollection;
	}
	
	
	
	public Player getPlayerById(UniquePlayerIdentifier playerId) {
		for(Player p: players) {
			if(p.getPlayerId().equals(playerId)) {
				return p;
			}
		}
		return null;
	}
 	
	public ArrayList<Player> getPlayers() {
		return players;
	}

	public void addPlayer(Player newPlayer) {
		this.players.add(newPlayer);
	}
	
	public int getPlayerNumber() {
		return this.players.size();
	}

	public FullMapRaw getFullMap() {
		return fullMap;
	}

	public void generateFullMap() {
		this.fullMap = new FullMapRaw();
	}

	public ArrayList<HalfMapRaw> getHalfMaps() {
		return halfMaps;
	}

	public void addHalfMap(HalfMapRaw halfMap) {
		this.halfMaps.add(halfMap);
	}

	public UniqueGameIdentifier getGameId() {
		return gameId;
	}

	public LocalDateTime getGameCreationTimeStamp() {
		return gameCreationTimeStamp;
	}
	
	public int getRoundCounter() {
		return roundCounter;
	}
	
	public void incrementRoundCounter() {
		roundCounter++;
	}

	public boolean isGameover() {
		return gameover;
	}
	
	/*
	 * Update the time of the last requests
	 */
	public void updateLastRequest(UniquePlayerIdentifier playerId) {
		for(Player p: players) {
			if(p.getPlayerId().equals(playerId)) {
				p.setLastRequest(LocalDateTime.now());
			}
		}
	}
	
	
	
	
	
	
}

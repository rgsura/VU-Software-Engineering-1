package server.model.game;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.PlayerRegistration;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.EPlayerGameState;
import MessagesGameState.PlayerState;

public class Player {
	
	private static final Logger PlayerLogger = LoggerFactory.getLogger(Player.class);
	
	private UniquePlayerIdentifier playerId;
	private String firstName;
	private String lastName;
	private String studentId;
	private int position;
	private boolean hasTransferedHalfMap=false;
	private boolean hasCollectedTreasure=false;
	private boolean hasFoundFort=false;
	private LocalDateTime lastRequest=null;
	private EPlayerGameState playerGameState=EPlayerGameState.ShouldWait;
	private int moveCounter=0;

	public Player() {
		playerId=UniquePlayerIdentifier.random();
	}
	
	
	/*
	 * We can create a new Player by using a playerRegistration which is what we receive
	 * from the clients
	 */
	public Player(PlayerRegistration playerRegistration) {
		playerId=UniquePlayerIdentifier.random();
		this.firstName=playerRegistration.getStudentFirstName();
		this.lastName=playerRegistration.getStudentLastName();
		this.studentId=playerRegistration.getStudentID();
		
	}
	
	/*
	 * When we need to hide the opponent's playerId, we need to create a new random one
	 * instead of modifying the existing id, which would not go well,
	 * we create a new player from an existing one
	 */
	public Player(Player player) {
		this.playerId = UniquePlayerIdentifier.random();
		this.firstName = player.getFirstName();
		this.lastName = player.getLastName();
		this.studentId = player.getStudentId();
		this.position = player.getPosition();
		this.hasTransferedHalfMap = player.hasTransferedHalfMap();
		this.hasCollectedTreasure = player.isHasCollectedTreasure();
		this.hasFoundFort = player.isHasFoundFort();
		this.playerGameState = player.getPlayerGameState();
		this.moveCounter = player.getMoveCounter();
	}
	
	/*
	 * Not used in current implementation
	 */
	
	public Player(Player player, boolean random_location) {
		this.playerId = UniquePlayerIdentifier.random();
		this.firstName = player.getFirstName();
		this.lastName = player.getLastName();
		this.studentId = player.getStudentId();
		this.position = player.getPosition();
		this.hasTransferedHalfMap = player.hasTransferedHalfMap();
		this.hasCollectedTreasure = player.isHasCollectedTreasure();
		this.hasFoundFort = player.isHasFoundFort();
		this.playerGameState = player.getPlayerGameState();
		this.moveCounter = player.getMoveCounter();
	}

	/*
	 * Instead of needing to constantly set the player state, we can call
	 * the toggle method from the other methods, such as from Game
	 */
	public void togglePlayerState() {
		if(playerGameState==EPlayerGameState.ShouldActNext) {
			playerGameState=EPlayerGameState.ShouldWait;
		}
		else if(playerGameState==EPlayerGameState.ShouldWait) {
			playerGameState=EPlayerGameState.ShouldActNext;
		}
		else {
			PlayerLogger.error("togglePlayerState - method called unneccesarely; playerState: {}", playerGameState);
		}
	}
	
	public PlayerState getPlayerState() {
		return new PlayerState(firstName, lastName, studentId, playerGameState, playerId, hasCollectedTreasure);
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public boolean hasTransferedHalfMap() {
		return hasTransferedHalfMap;
	}

	public void setHasTransferedHalfMap() {
		this.hasTransferedHalfMap = true;
	}

	public boolean isHasCollectedTreasure() {
		return hasCollectedTreasure;
	}

	public void setHasCollectedTreasure() {
		this.hasCollectedTreasure = true;
	}

	public boolean isHasFoundFort() {
		return hasFoundFort;
	}

	public void setHasFoundFort() {
		this.hasFoundFort = true;
	}

	public EPlayerGameState getPlayerGameState() {
		return playerGameState;
	}

	public void setPlayerGameState(EPlayerGameState playerGameState) {
		this.playerGameState = playerGameState;
	}

	public UniquePlayerIdentifier getPlayerId() {
		return playerId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getStudentId() {
		return studentId;
	}

	public int getMoveCounter() {
		return moveCounter;
	}

	public LocalDateTime getLastRequest() {
		return lastRequest;
	}

	public void setLastRequest(LocalDateTime lastRequest) {
		this.lastRequest = lastRequest;
	}
	
	


}

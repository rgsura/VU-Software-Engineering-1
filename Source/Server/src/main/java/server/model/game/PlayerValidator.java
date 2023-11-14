package server.model.game;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.PlayerRegistration;
import MessagesBase.UniqueGameIdentifier;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.EPlayerGameState;

public class PlayerValidator {
	
	private static final Logger PlayerValidatorLogger = LoggerFactory.getLogger(PlayerValidator.class);
	private final static int MAXIMUM_TURN_DURATION_MILIS=3000;
	private final static int MINIMUM_DURATION_BETWEEN_REQUESTS=400;
	
	public static boolean validatePlayerData(PlayerRegistration playerRegistration) {
		
		try {
			
			PlayerValidatorLogger.debug("validatePlayerData -  new player details: {}, {}, {}", 
					playerRegistration.getStudentFirstName(), playerRegistration.getStudentLastName(), playerRegistration.getStudentID());
			
			String firstName=playerRegistration.getStudentFirstName();
			String lastName=playerRegistration.getStudentLastName();
			String studentId=playerRegistration.getStudentID();
			if(firstName=="" || lastName=="" || studentId=="") {
				return false;
			}
		}
		catch (Exception e) {
			PlayerValidatorLogger.error("validatePlayerData - exception: {}", e);
			return false;
		}
		return true;
	}
	
	public static boolean validatePlayerId(String gameId, String playerId) {
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		UniqueGameIdentifier uGameId= new UniqueGameIdentifier(gameId);
		Game currentGame=RunningGames.getGameById(uGameId);
		Player currentPlayer=currentGame.getPlayerById(uPlayerId);
		//PlayerValidatorLogger.info("validatePlayerId - player Id: {}, ", playerId);
		return currentPlayer==null ? false:true;
	}
	
	public static boolean validatePlayerTurn(String gameId, String playerId) {
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		UniqueGameIdentifier uGameId= new UniqueGameIdentifier(gameId);
		Game currentGame=RunningGames.getGameById(uGameId);
		Player currentPlayer=currentGame.getPlayerById(uPlayerId);
		//PlayerValidatorLogger.info("validatePlayerTurn - player: {}, gameState: {}", playerId, currentPlayer.getPlayerGameState());
		return currentPlayer.getPlayerGameState()==EPlayerGameState.ShouldActNext ? true:false;
	}
	
	public static boolean validatePlayerHasTransferedMap(String gameId, String playerId) {
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		UniqueGameIdentifier uGameId= new UniqueGameIdentifier(gameId);
		Game currentGame=RunningGames.getGameById(uGameId);
		Player currentPlayer=currentGame.getPlayerById(uPlayerId);
		//PlayerValidatorLogger.info("validatePlayerHasTransferedMap - player: {}, hasTransferedMap: {}", playerId, currentPlayer.hasTransferedHalfMap());
		return currentPlayer.hasTransferedHalfMap();
	}
	
	public static boolean validateRequestBufferTime(String gameId, String playerId) {
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		UniqueGameIdentifier uGameId= new UniqueGameIdentifier(gameId);
		Game currentGame=RunningGames.getGameById(uGameId);
		Player currentPlayer=currentGame.getPlayerById(uPlayerId);
		if(currentPlayer.getLastRequest()==null) {
			return true;
		}
		long timeDifference=ChronoUnit.MILLIS.between(currentPlayer.getLastRequest(), LocalDateTime.now());
		return timeDifference>MINIMUM_DURATION_BETWEEN_REQUESTS ? true:false;
	}
	
	/*
	 * Did not have time to finish this function
	 * 
	 * Needs for players to have another dataTime field, not for last request, but for beginning of turn.
	 * 
	public static boolean validateMaxTurnDuration(String gameId, String playerId) {
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		UniqueGameIdentifier uGameId= new UniqueGameIdentifier(gameId);
		Game currentGame=RunningGames.getGameById(uGameId);
		Player currentPlayer=currentGame.getPlayerById(uPlayerId);
		if(currentPlayer.getLastRequest()==null) {
			return true;
		}
		long timeDifference=ChronoUnit.MILLIS.between(currentPlayer.getLastRequest(), LocalDateTime.now());
		return timeDifference>MAXIMUM_TURN_DURATION_MILIS ? false:true;
	}
	*/
	

}

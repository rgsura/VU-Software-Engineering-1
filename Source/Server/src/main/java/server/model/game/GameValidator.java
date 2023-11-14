package server.model.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.UniqueGameIdentifier;

public class GameValidator {
	
	private static final Logger GameValidatorLogger = LoggerFactory.getLogger(GameValidator.class);
	
	public static boolean validateGameId(String newGameId) {
		GameValidatorLogger.debug("validateGameId - new game id: {}", newGameId);
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		return RunningGames.checkIfKeyExists(gameId);
	}
	
	public static boolean validatePlayerNumber(String newGameId) {
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		GameValidatorLogger.debug("validatePlayerNumber - number of players: {}", RunningGames.getGameById(gameId).getPlayerNumber());
		if(RunningGames.getGameById(gameId).getPlayerNumber()==Game.MAX_PLAYER_NUMBER) {
			return false;
		}
		return true;
	}
	
	public static boolean validateGameOver(String newGameId) {
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		GameValidatorLogger.debug("validatePlayerNumber - number of players: {}", RunningGames.getGameById(gameId).getPlayerNumber());
		return RunningGames.getGameById(gameId).isGameover();
	}
	

}

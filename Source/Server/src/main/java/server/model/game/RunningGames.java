package server.model.game;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import MessagesBase.UniqueGameIdentifier;

public class RunningGames {
	
	private static final Logger RunningGamesLogger = LoggerFactory.getLogger(RunningGames.class);
	
	private final static int MAXIMUM_RUNNING_GAMES=999;
	private final static int MAXIMUM_TIME_RUNNING_GAME_SECONDS=600;
	
	/*
	 * I firstly use a map to be able to access the running games in an easy way
	 * I used LinkedHashMap because it maintains the insertion order, so one is 
	 * able to delete the oldest game, which is required when we have a limit on running games
	 */
	private static LinkedHashMap<UniqueGameIdentifier, Game> runningGames = new LinkedHashMap<UniqueGameIdentifier, Game>() {protected boolean removeEldestEntry(Map.Entry eldest) { 
		return size() > MAXIMUM_RUNNING_GAMES; 
		}};
	
	public RunningGames() {
		
	}
	
	
	/*
	 * takes care of the creation of a new game.
	 * Simply creates a new one and adds it to the map
	 */
	public static UniqueGameIdentifier addNewGame() {
		Game newGame = new Game();
		RunningGamesLogger.info("addNewGame: Number of Runnign Games: {}", getNumberOfRunningGames());
		if(getNumberOfRunningGames()>MAXIMUM_RUNNING_GAMES) {
			RunningGamesLogger.error("addNewGame: Number of Runnign Games after removing: {}", getNumberOfRunningGames());
		}
		while(checkIfKeyExists(newGame.getGameId())) {
			RunningGamesLogger.debug("addNewGame: repeated game ID: {}", newGame.getGameId());
			newGame = new Game();
		}
		RunningGamesLogger.info("addNewGame: New Game ID: {}", newGame.getGameId());
		runningGames.put(newGame.getGameId(), newGame);
		return newGame.getGameId();
	}
	
	
	/*
	 * deletes the games that are too old: currently more than 600 seconds or 10 minutes
	 * Firstly adds the games to a list and then we remove them one by one
	 */
	public static void deleteOldGames() {
		ArrayList<UniqueGameIdentifier> oldGames = new ArrayList<>();
		for(UniqueGameIdentifier id : runningGames.keySet()) {
			long timeDifference= ChronoUnit.SECONDS.between(runningGames.get(id).getGameCreationTimeStamp(), LocalDateTime.now());
			if(timeDifference>MAXIMUM_TIME_RUNNING_GAME_SECONDS) {
				oldGames.add(id);
			}
		}
		for(UniqueGameIdentifier id :oldGames) {
			runningGames.remove(id);
		}
	}
	
	public static int getNumberOfRunningGames() {
		return runningGames.size();
	}
	
	public static Game getGameById(UniqueGameIdentifier gameId) {
		if(checkIfKeyExists(gameId)) {
			return runningGames.get(gameId);
		}
		return null;
	}
	
	public static boolean checkIfKeyExists(UniqueGameIdentifier gameId) {
		return runningGames.containsKey(gameId);
	}
	
	public static LinkedHashMap<UniqueGameIdentifier, Game> getRunningGames() {
		return runningGames;
	}

}

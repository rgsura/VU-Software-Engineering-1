package server.controller;

import MessagesBase.UniqueGameIdentifier;
import server.model.game.RunningGames;

public class RunningGamesController {
	
	
	/*
	 * All controller methods are static so that they can be called from anywhere, 
	 * mostly because I decided that RunningGames, which serves as the database class for the server,
	 * is also static.
	 */
	
	
	/*
	 * Add a new game to the RunningGames
	 */
	public static UniqueGameIdentifier createNewGame() {
		return RunningGames.addNewGame();
	}
	
	
	/*
	 * Delete old games (the ones that are older than 10 minutes). Called automatically every 1 seconds
	 */
	public static void deleteOldGames() {
		RunningGames.deleteOldGames();
	}
}

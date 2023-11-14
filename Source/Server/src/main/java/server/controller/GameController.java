package server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.HalfMap;
import MessagesBase.PlayerRegistration;
import MessagesBase.UniqueGameIdentifier;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.GameState;
import server.model.game.Game;
import server.model.game.GameValidator;
import server.model.game.Player;
import server.model.game.RunningGames;
import server.model.map.HalfMapRaw;

public class GameController {
	
	private static final Logger GameControllerLogger = LoggerFactory.getLogger(GameController.class);
	
	/*
	 * we insert a new player to the game specified and return it's random uuid
	 */
	public static UniquePlayerIdentifier registerPlayer(String newGameId, PlayerRegistration newPlayer) {
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		Player player = PlayerController.createPlayer(newPlayer);
		
		GameControllerLogger.info("registerPlayer - gameId: {}, playerId: {}", newGameId, player.getPlayerId().toString());
		
		Game currentGame=RunningGames.getGameById(gameId);
		currentGame.addPlayer(player);
		if(currentGame.getPlayerNumber()==Game.MAX_PLAYER_NUMBER) {
			currentGame.initializeTurns();
		}
		currentGame.updateGameState(player.getPlayerId());
		return player.getPlayerId();
	}
	
	/*
	 * we get the game state of the specified game and player
	 */
	public static GameState getGameState(String newGameId, String playerId) {
		
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		GameControllerLogger.debug("getGanmeState - game state: {}", RunningGames.getGameById(gameId).toString());
		return RunningGames.getGameById(gameId).getGameState(uPlayerId);
	}
	
	/*
	 * we add the half map to the game specified.
	 * The half map is wrapped within another Class, HalfMapRaw 
	 * the halfmap already has the player id
	 */
	public static void addHalfMap(String newGameId, HalfMapRaw halfMap) {
		GameControllerLogger.info("addHalfMap - player ID: {}", halfMap.getPlayerId().toString());
		GameControllerLogger.debug("addHalfMap - node: {}", halfMap.getNodeArray().toString());
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		Game currentGame=RunningGames.getGameById(gameId);
		MapController.addHalfMap(currentGame, halfMap);
		currentGame.incrementRoundCounter();
		currentGame.changePlayerTurns();
		currentGame.updateGameState(halfMap.getPlayerId());
	}
	
	
	/*
	 * Whenever something happens that leads one player to win or loose,
	 * one of these two methods are called.
	 * they both do the same thing, but it's easier to be able to specify
	 * if the action that triggered these methods was a loosing action, 
	 * like an illegal move, or a winning action, like fining the fort
	 */
	public static void declareWinner(String newGameId, String playerId) {
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		Game currentGame=RunningGames.getGameById(gameId);
		currentGame.declareWinner(uPlayerId);
		currentGame.updateGameState(uPlayerId);
	}
	
	public static void declareLooser(String newGameId, String playerId) {
		UniqueGameIdentifier gameId= new UniqueGameIdentifier(newGameId);
		UniquePlayerIdentifier uPlayerId= new UniquePlayerIdentifier(playerId);
		Game currentGame=RunningGames.getGameById(gameId);
		currentGame.declareLooser(uPlayerId);
		currentGame.updateGameState(uPlayerId);
	}
	
	
	
}

package server.controller;

import MessagesBase.PlayerRegistration;
import MessagesBase.UniquePlayerIdentifier;
import server.model.game.Game;
import server.model.game.Player;

public class PlayerController {
	
	
	/*
	 * we call this method from he gameController when we create a player
	 * Player is a custom class used to encapsulate the players and all 
	 * the other data which would be useful 
	 */
	public static Player createPlayer(PlayerRegistration playerRegistration) {
		return new Player(playerRegistration);
	}
	
	
	/*
	 * likewise called by the gameController. We set that the player has already transfered the map
	 */
	public static void setHasTransferedMap(Game game, UniquePlayerIdentifier playerId) {
		Player player = game.getPlayerById(playerId);
		player.setHasTransferedHalfMap();
	}

}

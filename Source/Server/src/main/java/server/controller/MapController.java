package server.controller;

import MessagesBase.HalfMap;
import MessagesBase.UniqueGameIdentifier;
import MessagesBase.UniquePlayerIdentifier;
import server.model.game.Game;
import server.model.game.RunningGames;
import server.model.map.HalfMapRaw;

public class MapController {
	
	
	/*
	 * called from gameController
	 * takes care of the procedures of adding the map which are more
	 * map related than game related
	 */
	public static void addHalfMap(Game game, HalfMapRaw halfMapRaw) {
		game.addHalfMap(halfMapRaw);
		UniquePlayerIdentifier playerId=halfMapRaw.getPlayerId();
		PlayerController.setHasTransferedMap(game, playerId);
	}
	
	/*
	 * this method is called from the server endpoints.
	 * it is useful to instantiate it there when we want to
	 * validate the map.
	 */
	
	public static HalfMapRaw createHalfMapRaw(HalfMap halfMap) {
		return new HalfMapRaw(halfMap);
	}

}

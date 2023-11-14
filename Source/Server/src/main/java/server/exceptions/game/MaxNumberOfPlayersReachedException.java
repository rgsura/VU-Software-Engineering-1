package server.exceptions.game;

import server.exceptions.GenericExampleException;

public class MaxNumberOfPlayersReachedException extends GenericExampleException {

	public MaxNumberOfPlayersReachedException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

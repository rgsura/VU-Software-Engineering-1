package server.exceptions.player;

import server.exceptions.GenericExampleException;

public class PlayerHasAlreadyTransferedHalfMapException extends GenericExampleException {

	public PlayerHasAlreadyTransferedHalfMapException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

package server.exceptions.player;

import server.exceptions.GenericExampleException;

public class NotPlayersTurnException extends GenericExampleException {

	public NotPlayersTurnException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

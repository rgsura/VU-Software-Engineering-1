package server.exceptions.game;

import server.exceptions.GenericExampleException;

public class InvalidGameIdException extends GenericExampleException {

	public InvalidGameIdException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

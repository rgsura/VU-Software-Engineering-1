package server.exceptions.player;

import server.exceptions.GenericExampleException;

public class InvalidPlayerIdException extends GenericExampleException{

	public InvalidPlayerIdException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

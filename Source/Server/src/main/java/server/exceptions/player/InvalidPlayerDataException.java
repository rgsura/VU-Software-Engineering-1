package server.exceptions.player;

import server.exceptions.GenericExampleException;

public class InvalidPlayerDataException extends GenericExampleException{

	public InvalidPlayerDataException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

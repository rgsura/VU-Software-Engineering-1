package server.exceptions.game;

import server.exceptions.GenericExampleException;

public class GameAlreadyOverException extends GenericExampleException{

	public GameAlreadyOverException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

package server.exceptions.player;

import server.exceptions.GenericExampleException;

public class InsufficientTimeBetweenRequestsException extends GenericExampleException {

	public InsufficientTimeBetweenRequestsException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}
	

}

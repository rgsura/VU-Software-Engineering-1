package server.exceptions.map;

import server.exceptions.GenericExampleException;

public class IncorrectNumberOfFieldsException extends GenericExampleException {

	public IncorrectNumberOfFieldsException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

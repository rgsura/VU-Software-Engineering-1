package server.exceptions.map;

import server.exceptions.GenericExampleException;

public class IncorrectNumberOfMountainFieldsException extends GenericExampleException {

	public IncorrectNumberOfMountainFieldsException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

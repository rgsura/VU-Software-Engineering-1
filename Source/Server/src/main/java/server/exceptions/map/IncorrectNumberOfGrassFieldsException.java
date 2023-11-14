package server.exceptions.map;

import server.exceptions.GenericExampleException;

public class IncorrectNumberOfGrassFieldsException extends GenericExampleException {

	public IncorrectNumberOfGrassFieldsException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

package server.exceptions.map;

import server.exceptions.GenericExampleException;

public class IncorrectNumberOfWaterFieldsException extends GenericExampleException {

	public IncorrectNumberOfWaterFieldsException(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

package server.exceptions.map;

import server.exceptions.GenericExampleException;

public class FortNotFoundExpection extends GenericExampleException {

	public FortNotFoundExpection(String errorName, String errorMessage) {
		super(errorName, errorMessage);
	}

}

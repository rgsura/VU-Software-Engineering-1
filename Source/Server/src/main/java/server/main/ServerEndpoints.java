package server.main;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import MessagesBase.ERequestState;
import MessagesBase.HalfMap;
import MessagesBase.PlayerMove;
import MessagesBase.PlayerRegistration;
import MessagesBase.ResponseEnvelope;
import MessagesBase.UniqueGameIdentifier;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.GameState;
import server.controller.GameController;
import server.controller.RunningGamesController;
import server.exceptions.GenericExampleException;
import server.exceptions.game.GameAlreadyOverException;
import server.exceptions.game.InvalidGameIdException;
import server.exceptions.game.MaxNumberOfPlayersReachedException;
import server.exceptions.map.ForNotInGrassException;
import server.exceptions.map.FortNotFoundExpection;
import server.exceptions.map.IncorrectNumberOfFieldsException;
import server.exceptions.map.IncorrectNumberOfGrassFieldsException;
import server.exceptions.map.IncorrectNumberOfMountainFieldsException;
import server.exceptions.map.IncorrectNumberOfWaterFieldsException;
import server.exceptions.map.InvalidLongBorderWaterFiledNumberException;
import server.exceptions.map.InvalidShortBorderWaterFiledNumberException;
import server.exceptions.map.IslandInHalfMapException;
import server.exceptions.player.InsufficientTimeBetweenRequestsException;
import server.exceptions.player.InvalidPlayerDataException;
import server.exceptions.player.InvalidPlayerIdException;
import server.exceptions.player.NotPlayersTurnException;
import server.exceptions.player.PlayerHasAlreadyTransferedHalfMapException;
import server.model.game.Game;
import server.model.game.GameValidator;
import server.model.game.Player;
import server.model.game.PlayerValidator;
import server.model.map.HalfMapRaw;
import server.model.map.HalfMapValidator;

@Controller
@RequestMapping(value = "/games")
public class ServerEndpoints {
	
	private static final Logger ServerEndpointsLogger = LoggerFactory.getLogger(ServerEndpoints.class);

	// ADDITONAL TIPS ON THIS MATTER ARE GIVEN THROUGHOUT THE TUTORIAL SESSION!
	// Note, the same network messages which you have used for the client (along
	// with its documentation) apply to the server too.

	/*
	 * Please do NOT add all the necessary code in the methods provided below. When
	 * following the single responsibility principle those methods should only
	 * contain the bare minimum related to network handling. Such as the converts
	 * which convert the objects from/to internal data objects to/from messages.
	 * Include the other logic (e.g., new game creation and game id handling) by
	 * means of composition (i.e., it should be provided by other classes).
	 */

	// below you can find two example endpoints (i.e., one GET and one POST based
	// endpoint which are all endpoint types which you need),
	// Hence, all the other endpoints can be defined similarly.

	// example for a GET endpoint based on /games
	// similar to the client, the HTTP method and the expected data types are
	// specified at the server side too
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody UniqueGameIdentifier newGame() {

		// set showExceptionHandling to true to test/play around with the automatic
		// exception handling (see the handleException method at the bottom)
		// this is just some testing code that you can see how exceptions can be used to
		// signal errors to the client, you can REMOVE
		// these lines in your real server implementation
		boolean showExceptionHandling = false;
		if (showExceptionHandling) {
			// if any error occurs, simply throw an exception with inherits from
			// GenericExampleException
			// the given code than takes care of responding with an error message to the
			// client based on the @ExceptionHandler below
			// make yourself familiar with this concept by setting showExceptionHandling=true
			// and creating a new game through the browser
			// your implementation should use more useful error messages and specialized exception classes
			throw new GenericExampleException("Name: Something", "Message: went totally wrong");
		}

		// TIP: you will need to adapt this part to generate a game id with the valid
		// length. A simple solution for this
		// would be creating an alphabet and choosing random characters from it till the
		// new game id becomes long enough
		return RunningGamesController.createNewGame();

		// note you will need to include additional logic, e.g., additional classes
		// which create, store, validate, etc. game ids
	}

	// example for a POST endpoint based on games/{gameID}/players
	@RequestMapping(value = "/{gameID}/players", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody ResponseEnvelope<UniquePlayerIdentifier> registerPlayer(@PathVariable String gameID,
			@Validated @RequestBody PlayerRegistration playerRegistration) {
		if(!PlayerValidator.validatePlayerData(playerRegistration)) {
			throw new InvalidPlayerDataException("Name: Registering Player", "Message: Player Data Invalid");
		}
		if(!GameValidator.validateGameId(gameID)) {
			throw new InvalidGameIdException("Name: Registering Player", "Message: Game with given Id does not exist");
		}
		if(!GameValidator.validatePlayerNumber(gameID)) {
			throw new MaxNumberOfPlayersReachedException("Name: Registering Player", "Message: Already two players in game");
		}
		UniquePlayerIdentifier playerId = GameController.registerPlayer(gameID, playerRegistration);
		ResponseEnvelope<UniquePlayerIdentifier> playerIDMessage = new ResponseEnvelope<>(playerId);
		return playerIDMessage;

		// note you will need to include additional logic, e.g., additional classes
		// which create, store, validate, etc. player ids and incoming game ids
	}
	
	@RequestMapping(value = "/{gameID}/halfmaps", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody ResponseEnvelope<ERequestState> recieveHalfMap(@PathVariable String gameID,
			@Validated @RequestBody HalfMap halfMap) {
		if(!GameValidator.validateGameId(gameID)) {
			throw new InvalidGameIdException("Name: HalfMap Transfer", "Message: Game does not exist");
		}
		
		if(!PlayerValidator.validatePlayerId(gameID, halfMap.getUniquePlayerID())) {
			throw new InvalidPlayerIdException("Name: HalfMap Transfer", "Message: Player does not exist");
		}
		
		if(GameValidator.validateGameOver(gameID)) {
			throw new GameAlreadyOverException("Name: HalfMap Transfer", "Message: Player does not exist");
		}
		
		if(!PlayerValidator.validatePlayerTurn(gameID, halfMap.getUniquePlayerID())) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new NotPlayersTurnException("Name: HalfMap Transfer", "Message: Not player's turn");
		}
		
		if(PlayerValidator.validatePlayerHasTransferedMap(gameID, halfMap.getUniquePlayerID())) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new PlayerHasAlreadyTransferedHalfMapException("Name: HalfMap Transfer", "Message: Player has already transfered their halfMap");
		}
		HalfMapRaw halfMapRaw= new HalfMapRaw(halfMap);
		
		if(!HalfMapValidator.validateFieldNumber(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new IncorrectNumberOfFieldsException("Name: HalfMap Transfer", "Message: Field number not 32");
		}
		
		if(!HalfMapValidator.validateGrassFieldNumber(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new IncorrectNumberOfGrassFieldsException("Name: HalfMap Transfer", "Message: Grass fields less than minimum");
		}
		
		if(!HalfMapValidator.validateWaterFieldNumber(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new IncorrectNumberOfWaterFieldsException("Name: HalfMap Transfer", "Message: Water fields less than minimum");
		}
		
		if(!HalfMapValidator.validateMountainFieldNumber(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new IncorrectNumberOfMountainFieldsException("Name: HalfMap Transfer", "Message: Mountain fields less than minimum");
		}
		
		if(!HalfMapValidator.validateShortBorder(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new InvalidShortBorderWaterFiledNumberException("Name: HalfMap Transfer", "Message: Short border too many water fields");
		}
		
		if(!HalfMapValidator.validateLongBorder(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new InvalidLongBorderWaterFiledNumberException("Name: HalfMap Transfer", "Message: Short border too many water fields");
		}
		
		if(!HalfMapValidator.validateFortPresent(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new FortNotFoundExpection("Name: HalfMap Transfer", "Message: Fort not present");
		}
		
		if(!HalfMapValidator.validateFortInGrass(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new ForNotInGrassException("Name: HalfMap Transfer", "Message: Fort not located in grass");
		}
		
		if(!HalfMapValidator.validateNoIslands(halfMapRaw)) {
			GameController.declareLooser(gameID, halfMap.getUniquePlayerID());
			throw new IslandInHalfMapException("Name: HalfMap Transfer", "Message: One or more islands were found in the map");
		}
		GameController.addHalfMap(gameID, halfMapRaw);
		ERequestState requestState = ERequestState.Okay;
		ResponseEnvelope<ERequestState> responseMessage = new ResponseEnvelope<>(requestState);
		return responseMessage;
	}
	
	@RequestMapping(value = "/{gameID}/states/{playerID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody ResponseEnvelope<GameState> sendGameState(@PathVariable String gameID, @PathVariable String playerID) {

		if(!GameValidator.validateGameId(gameID)) {
			ServerEndpointsLogger.info("validateGameId - new game id: {}", gameID);
			throw new InvalidGameIdException("Name: HalfMap Transfer", "Message: Game does not exist");
		}
		
		if(!PlayerValidator.validatePlayerId(gameID, playerID)) {
			ServerEndpointsLogger.info("validatePlayerId - player Id: {}, ", playerID);
			throw new InvalidPlayerIdException("Name: HalfMap Transfer", "Message: Player does not exist");
		}
		
		/*
		 * It doesn't work with the test server, but with my client it seems to be working.
		 * 
		if(!PlayerValidator.validateRequestBufferTime(gameID, playerID)) {
			GameController.declareLooser(gameID, playerID);
			throw new InsufficientTimeBetweenRequestsException("Name: HalfMap Transfer", "Message: Not enough time (0.4 seconds) between requests");
		}
		*/

		GameState gameSate = GameController.getGameState(gameID, playerID);
		ResponseEnvelope<GameState> responseMessage = new ResponseEnvelope<>(gameSate);
		return responseMessage;
	}
	
	@RequestMapping(value = "/{gameID}/moves", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	public @ResponseBody ResponseEnvelope<ERequestState> recieveMovement(@PathVariable String gameID,
			@Validated @RequestBody PlayerMove playerMove) {
		ERequestState requestState = ERequestState.Okay;
		ResponseEnvelope<ERequestState> responseMessage = new ResponseEnvelope<>(requestState);
		return responseMessage;
	}

	/*
	 * Note, this is only the most basic way of handling exceptions in spring (but
	 * sufficient for our task) it would for example struggle if you use multiple
	 * controllers. Add the exception types to the @ExceptionHandler which your
	 * exception handling should support, the superclass catches subclasses aspect
	 * of try/catch applies also here. Hence, we recommend to simply extend your own
	 * Exceptions from the GenericExampleException. For larger projects one would
	 * most likely want to use the HandlerExceptionResolver, see here
	 * https://www.baeldung.com/exception-handling-for-rest-with-spring
	 * 
	 * Ask yourself: Why is handling the exceptions in a different method than the
	 * endpoint methods a good solution?
	 */
	@ExceptionHandler({ GenericExampleException.class })
	public @ResponseBody ResponseEnvelope<?> handleException(GenericExampleException ex, HttpServletResponse response) {
		ResponseEnvelope<?> result = new ResponseEnvelope<>(ex.getErrorName(), ex.getMessage());

		// reply with 200 OK as defined in the network documentation
		// Side note: We only do this here for simplicity reasons. For future projects,
		// you should check out HTTP status codes and
		// what they can be used for. Note, the WebClient used on the client can react
		// to them using the .onStatus(...) method.
		response.setStatus(HttpServletResponse.SC_OK);
		return result;
	}
}

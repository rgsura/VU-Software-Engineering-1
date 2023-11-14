package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import MessagesBase.EMove;
import MessagesBase.ERequestState;
import MessagesBase.PlayerRegistration;
import MessagesBase.ResponseEnvelope;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.GameState;
import client.Controller.MainController;
import client.Controller.NetworkController;
import client.Model.Map.HalfMapCreator;
import reactor.core.publisher.Mono;

public class MainClient {

	// ADDITONAL TIPS ON THIS MATTER ARE GIVEN THROUGHOUT THE TUTORIAL SESSION!

	/* Below you can find an example how to use both required HTTP operations i.e., POST and GET
	 * to communicate with the server.
	 * 	
	 * Note, this is only an example, hence, your own implementation should NOT 
	 * place all the logic in a single main method! 
	 * 
	 * Further, I would recommend that you check out:
	 * a) The JavaDoc of the network message library which describes all messages, and their ctors/methods.
	 * You can find it here http://swe.wst.univie.ac.at/
	 * b) The informal network documentation given in Moodle which describes which messages must be used when and how.
	 */	
	private static Logger logger = LoggerFactory.getLogger(MainClient.class);
	public static void main(String[] args) {
		
		/* IMPORTANT: Parsing/Handling of starting parameters.
		 * args[0] = Game Mode, you can use this to know that you code is running on the evaluation server
		 *  (if this is the case args[0] = TR). If this is the case only a command line interface must be displayed.
		 *  Also, no JavaFX and Swing UI component and classes must be used/executed by your Client in any way IF args[0]=TR. 
		 * args[1] = Server url, will hold the server url which your client should use. Note, only use the server url 
		 *  supplied here as the url used by you during the development and by the evaluation server (for grading) is
		 *  NOT the same! args[1] enables your client to always get the correct one.
		 * args[2] = Holds the game ID which your client should use. For testing purposes you can create a new one by accessing
		 *  http://swe.wst.univie.ac.at:18235/games with your web browser.
		 *  IMPORANT: If there is a value stored in args[2] you MUST use it! 
		 *  DO NOT create new games in your code in such a case! 
		 *  
		 *  DON'T FORGET TO EVALUATE YOUR FINAL IMPLEMENTATION WITH OUR TEST SERVER. THIS IS ALSO THE BASE FOR GRADING.
		 *  THE TEST SERVER CAN BE FOUND AT: http://swe.wst.univie.ac.at/ 
		 *  
		 *  HINT: The assignment section in Moodle also explains all the important aspects about the start parameters/arguments.
		 *  Use the Run Configurations (as shown during the first lecture) in Eclipse to simulate the starting of an application 
		 *  with start parameters or implement your own argument parsing code to become more flexible (e.g., to mix hard coded and 
		 *  supplied parameters whenever the one or the other is available). 
		 */
		
		if(args.length>=3) {
			MainController mainController = new MainController(args[1], args[2]);
			mainController.startGame("Rigers", "Ura", "rigersu84");
			mainController.playGame();
		}
		else {
			logger.error(String.format("Need three parameters to start the application"));
		}
		
		// parse the parameters, otherwise the automatic evaluation will not work on http://swe.wst.univie.ac.at
				
		// template webclient configuration, will be reused/customized for each individual endpoint
		// TIP: create it once in the CTOR of your network class and subsequently use it in each communication method
		/*
		 * Note, EACH client must only register a SINGLE player (i.e., you) ONCE!
		 * It is OK if you hard code your private data in your code.
		 * Here, this example shows you how to perform a POST request (and a client registration),
		 * you can build on this example to implement all the other messages which use POST.
		 * An example how to use GET requests is given below.
		 */		
		
		/* TIP: Check out the network protocol documentation. It shows you with a nice sequence diagram all
		 * the steps which are required to be executed by your client along with a general overview on the required behavior (e.g., 
		 * when it is necessary to repeatedly ask the server for its state to determine if actions can be sent or not).
		 * When the client will need to wait for the other client and when you client should stop with 
		 * sending any more messages to the server.   
		 */
		
		/* TIP: A game consist out of two clients, how can I get two clients for testing purposes?
		 * Start your client two times. You can do this in Eclipse by hitting the green start button twice.
		 * Or you can start your jar file twice in two different terminals.
		 * When you hit the debug button twice you can even debug both clients "independently" from each other.
		 */
		logger.info("Client Terminating");
	}
		
	/*
	 * This example method shall show you how to create a get request.
	 * Here, it shows you how to use a GET request to request the state of a game.
	 * You can define all GET requests accordingly.
	 * 
	 * The only reason to use get requests in the client should be to request states (i.e., we strongly advice NOT to use the 
	 * client to create new games using the game new endpoint)
	 */
	
}

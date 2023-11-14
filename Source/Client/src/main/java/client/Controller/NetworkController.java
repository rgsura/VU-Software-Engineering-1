package client.Controller;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import MessagesBase.EMove;
import MessagesBase.ERequestState;
import MessagesBase.HalfMap;
import MessagesBase.PlayerMove;
import MessagesBase.PlayerRegistration;
import MessagesBase.ResponseEnvelope;
import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.GameState;
import client.View.CLI;
import reactor.core.publisher.Mono;

public class NetworkController {
	
	private static Logger logger = LoggerFactory.getLogger(NetworkController.class);
	
	private String serverBaseUrl;
	private String gameId;
	private WebClient baseWebClient;
	private UniquePlayerIdentifier uniqueId;
	
	
	
	public NetworkController(String serverBaseUrl, String gameId) {
		this.gameId=gameId;
		this.serverBaseUrl=serverBaseUrl;
	}
	
	public void registerClient() throws Exception  {
		baseWebClient = WebClient.builder().baseUrl(serverBaseUrl + "/games")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
			    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
			    .build();
		CLI.clientRegistered();

	}
	
	public boolean registerPlayer(String studentFirstName, String studentLastName, String studentID) throws Exception  {
		PlayerRegistration playerReg = new PlayerRegistration(studentFirstName, studentLastName, studentID);
		Mono<ResponseEnvelope> webAccess = baseWebClient.method(HttpMethod.POST)
				.uri("/" + gameId + "/players")
				.body(BodyInserters.fromValue(playerReg))
				.retrieve()
				.bodyToMono(ResponseEnvelope.class);

		ResponseEnvelope<UniquePlayerIdentifier> requestResult = webAccess.block();
	
		if(requestResult.getState() == ERequestState.Error) {
			CLI.clientError(requestResult.getExceptionMessage());
			return false;
		}
		else {
			uniqueId = requestResult.getData().get();
			CLI.playerRegistered(uniqueId);
			return true;
		}
	}
	
	public boolean sendHalfMap(HalfMap halfMap) throws Exception  {
		Mono<ResponseEnvelope> webAccess = baseWebClient.method(HttpMethod.POST)
				.uri("/" + gameId + "/halfmaps")
				.body(BodyInserters.fromValue(halfMap))
				.retrieve()
				.bodyToMono(ResponseEnvelope.class);

		ResponseEnvelope<UniquePlayerIdentifier> requestResult = webAccess.block();
	
		if(requestResult.getState() == ERequestState.Error) {
			CLI.clientError(requestResult.getExceptionMessage());
			return false;
		}
		return true;
	}
	
	public GameState queryGameStatus() throws Exception {
		
		Mono<ResponseEnvelope> webAccess = baseWebClient.method(HttpMethod.GET)
				.uri("/" + gameId + "/states/" + uniqueId.getUniquePlayerID())
				.retrieve()
				.bodyToMono(ResponseEnvelope.class); // specify the object returned by the server

		//WebClient support asynchronous message exchange, in SE1 we use a synchronous one for the sake of simplicity. So calling block is fine.		
		ResponseEnvelope<GameState> requestResult = webAccess.block();

		
		// always check for errors, and if some are reported at least print them to the console (logging should be preferred)
		// so that you become aware of them during debugging! The provided server gives you very helpful error messages.
		if(requestResult.getState() == ERequestState.Error) {
			CLI.clientError(requestResult.getExceptionMessage());
		}
		
		return requestResult.getData().get();
	}
	
	public boolean sendGameMove(EMove move) throws Exception  {
		new PlayerMove();
		PlayerMove playerMove=PlayerMove.of(uniqueId, move);
		Mono<ResponseEnvelope> webAccess = baseWebClient.method(HttpMethod.POST)
				.uri("/" + gameId + "/moves")
				.body(BodyInserters.fromValue(playerMove))
				.retrieve()
				.bodyToMono(ResponseEnvelope.class);

		ResponseEnvelope<UniquePlayerIdentifier> requestResult = webAccess.block();
	
		if(requestResult.getState() == ERequestState.Error) {
			CLI.clientError(requestResult.getExceptionMessage());
			return false;
		}
		return true;
	}
	
	public UniquePlayerIdentifier getUniquePlayerIdentifier() {
		return uniqueId;
	}

}

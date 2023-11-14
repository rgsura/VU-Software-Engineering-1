package client.Controller;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.EMove;
import MessagesBase.ETerrain;
import MessagesBase.HalfMap;
import MessagesBase.PlayerMove;
import MessagesBase.ResponseEnvelope;
import MessagesGameState.EFortState;
import MessagesGameState.EPlayerGameState;
import MessagesGameState.EPlayerPositionState;
import MessagesGameState.ETreasureState;
import MessagesGameState.FullMap;
import MessagesGameState.FullMapNode;
import MessagesGameState.GameState;
import MessagesGameState.PlayerState;
import client.Model.Exception.HalfMapCheckedExcpetion;
import client.Model.Exception.PathFinderCheckedException;
import client.Model.Map.FullMapWeight;
import client.Model.Map.HalfMapCreator;
import client.Model.Movement.PathFinder;
import client.View.CLI;

public class MainController {
	
	private static Logger logger = LoggerFactory.getLogger(MainController.class);
	
	private String serverBaseUrl;
	private String gameId;
	private NetworkController networkController;
	private HalfMap halfMap;
	private FullMap fullMap;
	private GameState currentGameState;
	private long queryTimeStamp;
	private boolean treasureFound = false;
	private boolean boarderCrossed = false;
	private boolean fortFound = false;
	private boolean gameLost=false;
	private boolean gameWon=false;
	private PathFinder pathFinder= new PathFinder();
	private CLI cli= new CLI();
	
	public MainController(String serverBaseUrl, String gameId) {
		this.gameId=gameId;
		this.serverBaseUrl=serverBaseUrl;
		this.currentGameState=null;
		networkController = new NetworkController(serverBaseUrl, gameId);
	}
	
	public void startGame(String studentFirstName, String studentLastName, String studentID) {
		try {
			networkController.registerClient();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			networkController.registerPlayer(studentFirstName, studentLastName, studentID);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			updateGameState();
			if(currentGameState!=null) {
				logger.debug("Initial Game State updated successfully");
			}
			else {
				logger.debug("Initial Game State NOT updated successfully");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void playGame() {

		sendHalfMap();
		cli.startPlaying();
		buildFullMap();
		findTreasure();
		crossBoarder();
		findFort();
	}
	
	private void findTreasure() {
		pathFinder.initialiseNodesWeight(fullMap);
		do {
			if(gameLost) {
				cli.gameLost();
				return;
			}
			if(gameWon) {
				cli.gameWon();
				return;
			}
			
			makeCompleteMovement();
			if(treasureFound) {
				break;
			}
		}while(!treasureFound);
		cli.treasureFound(pathFinder.getCurrentPosition());
	}
	
	private void crossBoarder() {
		try {
			pathFinder.buildPathToBorder();
		} catch (PathFinderCheckedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		do {
			if(gameLost) {
				cli.gameLost();
				return;
			}
			if(gameWon) {
				cli.gameWon();
				return;
			}
			
			makeCompleteMovement();
			logger.debug(String.format("crossBoarder: Player Position: [%s, %s]", pathFinder.getCurrentPosition().getX(), pathFinder.getCurrentPosition().getY()));
			if(pathFinder.borderIsCrossed()) {
				boarderCrossed=true;
				break;
			}
		}while(!boarderCrossed);
		logger.info(String.format("crossBoarder: Player Position (border crossed): [%s, %s]", pathFinder.getCurrentPosition().getX(), pathFinder.getCurrentPosition().getY()));
		cli.boarderCrossed(pathFinder.getCurrentPosition());
	}
	
	private void findFort() {
		pathFinder.findingFort();
		do {
			if(gameLost) {
				cli.gameLost();
				return;
			}
			if(gameWon) {
				if(pathFinder.getCurrentPosition().getFortState()==EFortState.EnemyFortPresent) {
					fortFound=true;
					cli.fortFound(pathFinder.getCurrentPosition());
				}else {
					cli.gameWon();
				}
				return;
			}
			
			makeCompleteMovement();
			if(pathFinder.getCurrentPosition().getFortState()==EFortState.EnemyFortPresent) {
				fortFound=true;
				break;
			}
		}while(!fortFound);
		cli.fortFound(pathFinder.getCurrentPosition());
	}
	
	private void makeCompleteMovement() {
		EMove nextMove = null;
		logger.info(String.format("makeCompleteMovement: Old Player Position: [%d, %d]", pathFinder.getCurrentPosition().getX(),
				pathFinder.getCurrentPosition().getY()));
		try {
			nextMove=pathFinder.getNextMove();
		} catch (PathFinderCheckedException ep) {
			logger.error("PathFinding algorithm malfunctioned");
			ep.printStackTrace();
			gameLost=true;
			return;
		}
		catch (Exception e) {
			logger.error("PathFinding algorithm malfunctioned in an unpredictable way");
			e.printStackTrace();
			gameLost=true;
			return;
			
		}
		logger.debug(String.format("makeCompleteMovement: Next move: %s", nextMove.toString()));
		try {
			waitForTurn();
			if(gameLost || gameWon) return;
			networkController.sendGameMove(nextMove);
			logger.info(String.format("makeCompleteMovement: 1st move made"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int oldX=pathFinder.getCurrentPosition().getX();
		int oldY=pathFinder.getCurrentPosition().getY();
		while(oldX==pathFinder.getCurrentPosition().getX() && oldY==pathFinder.getCurrentPosition().getY()) {
			
			try {
				waitForTurn();
				if(gameLost || gameWon) return;
				
				networkController.sendGameMove(nextMove);
				logger.info(String.format("makeCompleteMovement: Additional move made"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				gameLost=true;
				return;
			}
			try {
				queryGameStatus();
				if(gameLost || gameWon) return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(String.format("makeCompleteMovement: Couldn't query GameState"));
				e.printStackTrace();
			}
			updateFullMap();
			logger.debug(String.format(" makeCompleteMovement: currentNodeTerrain: %s", pathFinder.getCurrentNodeTerrain().toString()));
		}
		logger.info(String.format("makeCompleteMovement: New Player Position: [%d, %d]", pathFinder.getCurrentPosition().getX(),
				pathFinder.getCurrentPosition().getY()));

		cli.showMove(pathFinder.getCurrentPosition());
	}
	
	private void generateHalfMap() {
		HalfMapCreator halfMapCreator = null;
		try {
			halfMapCreator = new HalfMapCreator(networkController.getUniquePlayerIdentifier());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			halfMap = halfMapCreator.generateMap();
		} catch (HalfMapCheckedExcpetion e) {
			
			e.printStackTrace();
			logger.debug("Generating half map didn't work properly, trying again");
			generateHalfMap();
		}
	}
	
	private void sendHalfMap() {
		generateHalfMap();
		logger.info("HalfMap Generated");
		try {
			waitForTurn();
			if(gameLost || gameWon) return;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("game status exception :" + e1.getMessage());
			e1.printStackTrace();
		}
		try {
			networkController.sendHalfMap(halfMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("sending halfmap exception");
			e.printStackTrace();
			gameLost=true;
			return;
		}
		cli.halfMapSent();
	}
	
	private void buildFullMap() {
		while(!currentGameState.getMap().isPresent() || currentGameState.getMap().get().getMapNodes().size()<64) {
			try {
				queryGameStatus();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				gameLost=true;
				return;
			}
		}
		fullMap=currentGameState.getMap().get();
		pathFinder.updateCurrentPosition(fullMap);
		
	}
	
	private void updateFullMap() {
		fullMap=currentGameState.getMap().get();
		pathFinder.updateCurrentPosition(fullMap);
		logger.info("Map updated");
	}
	
	private void updateGameState() throws Exception {
		currentGameState=networkController.queryGameStatus();
		queryTimeStamp=System.nanoTime();
	}
	
	private void queryGameStatus() throws Exception{
		do{
			queryBufferWaiter();
			try {
				updateGameState();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				gameLost=true;
				return;
			}
		}while(currentGameState==null);

	}
	
	private void waitForTurn() throws Exception{
		do{
			queryBufferWaiter();
			try {
				updateGameState();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				gameLost=true;
				return;
			}
		}while(!checkIfCurrentTurn() || currentGameState==null);
	}

	private void queryBufferWaiter() {
		long tempTime=System.nanoTime();
		while((tempTime - queryTimeStamp)/1e6 < 400) {
			tempTime=System.nanoTime();
		}
	}
	
	private boolean checkIfCurrentTurn() {
		ArrayList<PlayerState> players = new ArrayList<>(currentGameState.getPlayers());
		String playerUniqueId=networkController.getUniquePlayerIdentifier().getUniquePlayerID();
		if(players.size()!=2) {
			return false;
		}
		if(players.get(0).getUniquePlayerID().equals(playerUniqueId)) {
			if(players.get(0).hasCollectedTreasure()) {
				treasureFound=true;
			}
			if(players.get(0).getState()==EPlayerGameState.Lost) {
				gameLost=true;
				return true;
			}
			else if(players.get(0).getState()==EPlayerGameState.Won) {
				gameWon=true;
				return true;
			}
			else if(players.get(0).getState()==EPlayerGameState.ShouldActNext)
				return true;
			return false;
		}
		if(players.get(1).getUniquePlayerID().equals(playerUniqueId)) {
			if(players.get(1).hasCollectedTreasure()) {
				treasureFound=true;
			}
			if(players.get(1).getState()==EPlayerGameState.Lost) {
				gameLost=true;
				return true;
			}
			else if(players.get(1).getState()==EPlayerGameState.Won) {
				gameWon=true;
				return true;
			}
			else if(players.get(1).getState()==EPlayerGameState.ShouldActNext)
				return true;
			return false;
		}
		return false;
		
	}

}

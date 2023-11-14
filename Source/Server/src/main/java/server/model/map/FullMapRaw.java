package server.model.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.ETerrain;
import MessagesBase.HalfMap;
import MessagesBase.HalfMapNode;
import MessagesGameState.EFortState;
import MessagesGameState.EPlayerPositionState;
import MessagesGameState.ETreasureState;
import MessagesGameState.FullMap;
import MessagesGameState.FullMapNode;
import server.model.game.Game;
import MessagesBase.UniquePlayerIdentifier;

public class FullMapRaw {
	
	private static final Logger FullMapRawLogger = LoggerFactory.getLogger(FullMapRaw.class);
	
	/*
	 * FullMapRaw is a wrapper class for the FullMap, although FullMap is not it's instance
	 * We use it to store the real state of the map of the game, along with other instances
	 * meant to help on the manipulation and generation of the FullMap that needs to be returned
	 */
	
	/*
	 * Instead of using x and y, we use an index which works as follows:
	 * When the map is listed, it goes from 0 to the size()-1 of the map
	 * [X,Y]-> index= X + yMultiplyer * Y
	 * This allows me to use a simple arraylist and not any other complex data structure
	 */
	private ArrayList<HalfMapNode> nodeArray = new ArrayList<>();
	private ArrayList<UniquePlayerIdentifier> playerIds= new ArrayList<>();
	/*
	 * Using HasMap allows the progrem to access easily the indices of the relevant locations
	 */
	private HashMap<UniquePlayerIdentifier, Integer> playerLocation = new HashMap<>();
	private HashMap<UniquePlayerIdentifier, Integer> treasureLocation = new HashMap<>();
	private HashMap<UniquePlayerIdentifier, Integer> fortLocation = new HashMap<>();
	/*
	 * yMultiplyer is basically the width of the map
	 * it can either be 8 (by default) for halfmap: 8x4 or for square fullmap: 8x8
	 * or 16 for the long fullmap: 16:4
	 */
	private int yMultiplyer=8;
	
	public FullMapRaw() {
		
	};
	

	/*
	 * we initialize the nodeArray and we determine two random numbers:
	 * yMultiplyer 	-> determines the shape
	 * firstMap 	-> determines which halfmap is first (left or top)
	 * combinations of these two values give the combinations of map arrangements
	 */
	public FullMapRaw(ArrayList<HalfMapRaw> halfMaps) {
		Random random = new Random();
		yMultiplyer*=random.nextInt(2)+1;
		int firstMap=random.nextInt(2);
		for(int i=0; i<64; i++) {
			nodeArray.add(null);
		}
		FullMapRawLogger.info("Constructer - yMultiplyer: {}, firstMap: {}", 
				yMultiplyer, firstMap);
		initializeNodeArray(halfMaps, firstMap);
	};
	
	/*
	 * we assign the values from the halfmaps by using the above-mentioned strategy
	 * By using maths, we can convert the index of the halfmap to the index of the FullMapRaw
	 */
	private void initializeNodeArray(ArrayList<HalfMapRaw> halfMaps, int firstMap) {
		for(int i=0; i<halfMaps.size(); i++) {
			HalfMapRaw halfMap=halfMaps.get(i);
			playerIds.add(halfMap.getPlayerId());
			int halfMapSize=halfMap.getNodeArray().size();
			if(yMultiplyer==8) {
				int locationIndexOffset=((i+firstMap)%2)*halfMapSize;
				playerLocation.put(halfMap.getPlayerId(), halfMap.getFortLocation()+locationIndexOffset);
				fortLocation.put(halfMap.getPlayerId(), halfMap.getFortLocation()+locationIndexOffset);
				treasureLocation.put(halfMap.getPlayerId(), halfMap.getTreasureLocation()+locationIndexOffset);
			}
			else {
				int locationIndexOffset=(halfMap.getNodeArray().get(halfMap.getFortLocation()).getY() + (i+firstMap)%2)*8;
				playerLocation.put(halfMap.getPlayerId(), halfMap.getFortLocation()+locationIndexOffset);
				fortLocation.put(halfMap.getPlayerId(), halfMap.getFortLocation()+locationIndexOffset);
				locationIndexOffset=(halfMap.getNodeArray().get(halfMap.getTreasureLocation()).getY() + (i+firstMap)%2)*8;
				treasureLocation.put(halfMap.getPlayerId(), halfMap.getTreasureLocation()+locationIndexOffset);
			}
			
			FullMapRawLogger.info("initializeNodeArray - player: {}, location Index: {}", 
					playerIds.get(i).toString(), playerLocation.get(playerIds.get(i)));
			
			for(int index=0;index<halfMapSize;index++) {
				HalfMapNode halfMapNode = halfMap.getNodeArray().get(index);
				int nodeIndex=-1;
				
				if(yMultiplyer==8) {
					nodeIndex=index+((i+firstMap)%2)*halfMapSize;
				}
				else {
					nodeIndex=index+(halfMap.getNodeArray().get(index).getY() + (i+firstMap)%2)*8;
				}
				nodeArray.set(nodeIndex, halfMapNode);
			}
		}
	}
	
	
	/*
	 * When we need to return the FullMap in the game state
	 * we call this method
	 */
	public FullMap generateFullMap(UniquePlayerIdentifier playerId, boolean random_opponent) {
		ArrayList<FullMapNode> fullMapNodes = new ArrayList<>();
		/*
		 * if we are during the first 10 rounds, we need to hide the location of the opponent's location
		 */
		int opponentIndex=-1;
		if(random_opponent) {
			Random random = new Random();
			opponentIndex=random.nextInt(nodeArray.size());
			while(nodeArray.get(opponentIndex).getTerrain().equals(ETerrain.Water)) {
				opponentIndex=random.nextInt(64);
			}
		}
		
		for(int i=0;i<nodeArray.size();i++) {
			/*
			 * for every node, we convert it to FullMapNode
			 */
			fullMapNodes.add(convertHalfMapNodeToFullMapNode(nodeArray.get(i), i, playerId, random_opponent, opponentIndex));
		}
		
		FullMap fullMap=new FullMap(fullMapNodes);
		return fullMap;
	}
	/*
	 * when we request the gamestate when only one player has transfered the map, we need an incomplete "FullMap"
	 * this process requires different procedures, because we use a halfMapRaw directly, not the local nodeArray
	 */
	public FullMap generateIncompleteFullMap(HalfMapRaw halfMap, UniquePlayerIdentifier playerId, boolean random_opponent) {
		ArrayList<FullMapNode> fullMapNodes = new ArrayList<>();
		playerIds.add(playerId);
		playerLocation.put(playerId, halfMap.getFortLocation());
		fortLocation.put(playerId, halfMap.getFortLocation());
		treasureLocation.put(playerId, halfMap.getTreasureLocation());
		
		int opponentIndex=-1;
		if(random_opponent) {
			Random random = new Random();
			opponentIndex=random.nextInt(halfMap.getNodeArray().size());
			while(halfMap.getNodeArray().get(opponentIndex).getTerrain().equals(ETerrain.Water)) {
				opponentIndex=random.nextInt(halfMap.getNodeArray().size());
			}
		}
		
		for(int i=0;i<halfMap.getNodeArray().size();i++) {
			HalfMapNode halfMapNode= halfMap.getNodeArray().get(i);
			fullMapNodes.add(convertHalfMapNodeToFullMapNode(halfMapNode, halfMapNode.getX()+yMultiplyer*halfMapNode.getY(), playerId, random_opponent, opponentIndex));
		}
		
		
		FullMap fullMap=new FullMap(fullMapNodes);
		return fullMap;
	}
	/*
	 * We convert HalfMapNodes to the FullMapNodes
	 * To do so, we need the different Enum States
	 */
	private FullMapNode convertHalfMapNodeToFullMapNode(HalfMapNode node, int index, UniquePlayerIdentifier playerId, boolean random_opponent, int opponentIndex) {
		
		EPlayerPositionState playerPositionState=getPlayerPositionState(node,index, playerId, random_opponent, opponentIndex);
		EFortState fortState= getFortState(node,index, playerId);
		ETreasureState treasureState=getTreasureState(node,index, playerId);	
		FullMapNode fullMapNode= new FullMapNode(node.getTerrain(), playerPositionState, treasureState, fortState, index%yMultiplyer, (int) index/yMultiplyer);
		
		return fullMapNode;
		
	}
	
	private EFortState getFortState(HalfMapNode node, int index, UniquePlayerIdentifier playerId) {
		for(UniquePlayerIdentifier id: playerIds) {
			if(id.equals(playerId)) {
				if(playerLocation.get(playerId)==fortLocation.get(id) && playerLocation.get(playerId)==index) {
					return EFortState.MyFortPresent;
				}
			}
			else {
				if(node.getTerrain().equals(ETerrain.Grass)) {
					if(playerLocation.get(playerId)==fortLocation.get(id)) {
						return EFortState.EnemyFortPresent;
					}
				}
				else if(node.getTerrain().equals(ETerrain.Mountain) && playerLocation.get(playerId)==index) {
					int enemyfortIndex=fortLocation.get(id);
					if(playerLocation.get(playerId)==enemyfortIndex+1 || playerLocation.get(playerId)==enemyfortIndex-1 
							|| playerLocation.get(playerId)==enemyfortIndex+yMultiplyer || playerLocation.get(playerId)==enemyfortIndex-yMultiplyer) {
						return EFortState.EnemyFortPresent;
					}
				}
			}
		}
		return EFortState.NoOrUnknownFortState;
	}
	
	private ETreasureState getTreasureState(HalfMapNode node, int index, UniquePlayerIdentifier playerId) {
		for(UniquePlayerIdentifier id: playerIds) {
			if(!id.equals(playerId)) {
				if(node.getTerrain().equals(ETerrain.Mountain) && playerLocation.get(playerId)==index) {
					int enemyfortIndex=fortLocation.get(id);
					if(playerLocation.get(playerId)==enemyfortIndex+1 || playerLocation.get(playerId)==enemyfortIndex-1 
							|| playerLocation.get(playerId)==enemyfortIndex+yMultiplyer || playerLocation.get(playerId)==enemyfortIndex-yMultiplyer) {
						return ETreasureState.MyTreasureIsPresent;
					}
				}
			}
		}
		return ETreasureState.NoOrUnknownTreasureState;
	}
	
	private EPlayerPositionState getPlayerPositionState(HalfMapNode node, int index, UniquePlayerIdentifier playerId, boolean random_opponent,int opponentIndex) {
		EPlayerPositionState tempPlayerPositionState =EPlayerPositionState.NoPlayerPresent;
		for(UniquePlayerIdentifier id: playerIds) {
			if(id.equals(playerId)) {
				if(playerLocation.get(id)==index) {
					tempPlayerPositionState=EPlayerPositionState.MyPosition;
				}
			}
			
			/*
			 * Depending on wether we are during the first 10 rounds or not, we decide to hide or not the real EnemyPloyerPosition
			 */
			else if(!random_opponent && opponentIndex!=-1) {
				if(playerLocation.get(id)==index) {
					if(tempPlayerPositionState.equals(EPlayerPositionState.MyPosition)) {
						tempPlayerPositionState=EPlayerPositionState.BothPlayerPosition;
					}
					else {
						tempPlayerPositionState=EPlayerPositionState.EnemyPlayerPosition;
					}
				}
			}
			else if(random_opponent && opponentIndex!=-1) {
				if(opponentIndex==index) {
					if(tempPlayerPositionState.equals(EPlayerPositionState.MyPosition)) {
						tempPlayerPositionState=EPlayerPositionState.BothPlayerPosition;
					}
					else {
						tempPlayerPositionState=EPlayerPositionState.EnemyPlayerPosition;
					}
				}
			}
		}
		return tempPlayerPositionState;
	}
	

}

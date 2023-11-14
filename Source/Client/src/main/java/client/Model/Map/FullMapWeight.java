package client.Model.Map;

import java.util.ArrayList;

import javax.validation.constraints.Min;

import MessagesBase.EMove;
import MessagesBase.ETerrain;
import MessagesGameState.EPlayerPositionState;
import MessagesGameState.FullMap;
import MessagesGameState.FullMapNode;
import client.Model.Exception.PathFinderUncheckedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullMapWeight {
	
	private static Logger logger = LoggerFactory.getLogger(FullMapWeight.class);
	
	private ArrayList<MapWeightNode> nodeWeightList=new ArrayList<MapWeightNode>();
	private int yMulitplier=8; //it is used to determine what map shape there is with the added benifit of using it directly in calculations
	private MapWeightNode currentPosition;
	private ETerrain currentTerrain;
	
	public FullMapWeight() {
		for(int i=0;i<64;i++)
			nodeWeightList.add(null);
	}
	
	public void initialiseNodeWeights(FullMap fullMap) {
		ArrayList<FullMapNode> fullMapNodes= new ArrayList<FullMapNode>(fullMap.getMapNodes());
		
		/*
		 * If there is an X that is higher than 7, then it is a wide map, and so yMuliplier is changed to 16, otherwise left at 8.
		 * Since for the wide map the rows are with 16 fields, than to get the index you need to multiply the y with 16 and add the x.
		 */
		
		for(FullMapNode currentNode: fullMapNodes) {
			if(currentNode.getX()>7) {
				yMulitplier=16;
				break;
			}
		}
		logger.info(String.format("yMultiplier: %d", yMulitplier));
		
		/*
		 * Instead of assigning terrain type, I assign weights which I can use later for calculations as well for the path finding algorithm.
		 */
		
		int tempWeight=0;
		for(FullMapNode currentNode: fullMapNodes) {
			switch(currentNode.getTerrain()) {
				case Grass: tempWeight=1;
						break;
				case Mountain: tempWeight=2;
						break;
				case Water: tempWeight=9999;
						break;
			}
			logger.info(String.format("Assigning grid: [X;Y]: [%s;%s], Index: %s, Weight: %s", currentNode.getX(), currentNode.getY(), currentNode.getX()+currentNode.getY()*yMulitplier, tempWeight));
			
			//I insert the nodes into the weight list ordered using the same logic as in HalfMapCretor so that I have it easier to determine which fields are around the current position
			
			MapWeightNode tempWeightNode = new MapWeightNode(currentNode.getX(), currentNode.getY(), tempWeight);
			
			if(currentNode.getPlayerPositionState()==EPlayerPositionState.MyPosition || currentNode.getPlayerPositionState()==EPlayerPositionState.BothPlayerPosition) {
				currentPosition=tempWeightNode;
				currentTerrain=currentNode.getTerrain();
				tempWeightNode.setVisited(true);
			}
			nodeWeightList.set(currentNode.getX()+currentNode.getY()*yMulitplier,tempWeightNode);
		}
		logger.info(String.format("My Position: [X;Y]: [%s,%s] , Weight: %s", currentPosition.getX(), currentPosition.getY(), currentPosition.getWeight()));
	}
	
	public void updateCurrentPosition(FullMapNode currentNode) throws PathFinderUncheckedException {
		int tempWeight=0;
		if(currentNode.getTerrain()==ETerrain.Grass) {
			tempWeight=1;
		}
		else if(currentNode.getTerrain()==ETerrain.Mountain) {
			tempWeight=2;
		}
		else {
			tempWeight=9999;
			logger.error(String.format("updateCurrentPosition: Currently in water!! : [%s,%s]", currentNode.getX(), currentNode.getY()));
			throw new PathFinderUncheckedException(String.format("Current position is in water in the system: [%s;%s]", currentNode.getX(), currentNode.getY()));
		}
		MapWeightNode tempWeightNode = new MapWeightNode(currentNode.getX(), currentNode.getY(), tempWeight, true);
		nodeWeightList.set(currentNode.getX()+currentNode.getY()*yMulitplier,tempWeightNode);
		
	}
	
	/*
	 * I provide some get and set methods that I foudn that I would need
	 */
	
	public MapWeightNode getMapWeightNodeByCoordinates(int x, int y) {
		return nodeWeightList.get(x+y*yMulitplier);
	}
	public void setMapWeightNodeByCoordinates(int x, int y, MapWeightNode node) {
		nodeWeightList.set(x+y*yMulitplier, node);
	}
	public MapWeightNode getMapWeightNodeByIndex(int index) {
		return nodeWeightList.get(index);
	}
	public void setMapWeightNodeByIndex(int index, MapWeightNode node) {
		nodeWeightList.set(index, node);
	}
	
	public int getYMultiplyer() {
		return yMulitplier;
	}
	
}

package client.Model.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.ETerrain;
import MessagesBase.HalfMap;
import MessagesBase.HalfMapNode;
import MessagesBase.UniquePlayerIdentifier;
import client.Controller.MainController;
import client.Model.Exception.HalfMapCheckedExcpetion;

public class HalfMapCreator {
	
	/*
	 * To make sure that I don't go over assigned fields, I create a Map<Integer, Boolean>, where the key will be from 0 to 31, 
	 * where key%8 gives the X value, and (int) key/8 gives the Y value.
	 * If the value is true, it means that it has already been assigned, but we use map.containsKey to make our lives easier.
	 */
	
	private static Logger logger = LoggerFactory.getLogger(HalfMapCreator.class);
	
	private Map<Integer,Boolean> alreadySetMapNodes = new HashMap<Integer,Boolean>();
	private ArrayList<HalfMapNode> arrayOfNodes = new ArrayList<>();
	private Random random = new Random();
	private UniquePlayerIdentifier uniquePlayerID;
	private int waterNodes=0;
	private int mountainNodes=0;
	
	public HalfMapCreator(UniquePlayerIdentifier uniquePlayerID) {
		this.uniquePlayerID=uniquePlayerID;
	}
	
	public HalfMap generateMap() throws HalfMapCheckedExcpetion
	{
		
		setWaterFields();
		setMountainFields();
		setFort();
		setGrassFields();
		validateMap();
		HalfMap halfMap = new HalfMap(uniquePlayerID, arrayOfNodes);
		return halfMap;
	}
	
	private void setWaterFields() {
		
		int waterNodeCounter=0;
		int topBoarderWaterFields=0;
		/*
		 * Randomly assigning side border water field
		 * we randomly pick the left or right border
		 */
		
		int leftOrRightBoarder=random.nextInt(2);
		
		int tempNodeIndex=random.nextInt(4);
		HalfMapNode tempNode=null;
		if(leftOrRightBoarder==0) {
			alreadySetMapNodes.put(tempNodeIndex*8, true);
			tempNode= new HalfMapNode(0, tempNodeIndex, ETerrain.Water);
			
		}
		else
		{
			alreadySetMapNodes.put(7+tempNodeIndex*8, true);
			tempNode = new HalfMapNode(7, tempNodeIndex, ETerrain.Water);
			leftOrRightBoarder=7;
		}
		waterNodeCounter++;
		
		/*
		 * In the case that the left border water field is in coordinate <0;0>,
		 * we must prevent the top boarder from having an additional 3 water fields,
		 * as that would clearly make 4 water field in total at the top boarder, violating the rules.
		 */
		
		if(tempNodeIndex==0) {
			topBoarderWaterFields=1;
		}
		arrayOfNodes.add(tempNode);
		/*
		 * Randomly assigning top border water fields
		 * Must assign at most 3
		 */
		
		for(int i=0;i<3-topBoarderWaterFields;++i) {
			
			do {
				tempNodeIndex=random.nextInt(8);
			}while(alreadySetMapNodes.containsKey(tempNodeIndex) || tempNodeIndex==0 || tempNodeIndex==7 || checkIfIsland(tempNodeIndex));
			
			alreadySetMapNodes.put(tempNodeIndex, true);
			waterNodeCounter++;
			tempNode= new HalfMapNode(tempNodeIndex, 0, ETerrain.Water);
			arrayOfNodes.add(tempNode);
		}
		
		/*
		 * We randomly specify how many water nodes more than the minimum of 4 we will add.
		 * we set the limit of at most 3 more
		 * If there isn't already 4+additional water nodes, we add another one randomly in the map
		 */
		int additionalWaterNodes=random.nextInt(3);
		
		while(waterNodeCounter<4+additionalWaterNodes) {
			do {
				tempNodeIndex=random.nextInt(32);
			}while(alreadySetMapNodes.containsKey(tempNodeIndex) || tempNodeIndex<8 || tempNodeIndex%8==0 || tempNodeIndex%8==7 || checkIfIsland(tempNodeIndex)); // we make sure that the index is not in the boarder
			
			alreadySetMapNodes.put(tempNodeIndex, true);
			tempNode= new HalfMapNode(tempNodeIndex%8, (int) tempNodeIndex/8, ETerrain.Water);
			arrayOfNodes.add(tempNode);
			waterNodeCounter++;
		}
		waterNodes=waterNodeCounter;
		logger.info(String.format("Water fileds: %s", waterNodeCounter));
	}
	
	private boolean checkIfIsland(int index) {
		int x=0,y=0;
		for(HalfMapNode node: arrayOfNodes) {
			x=node.getX();
			y=node.getY();
			if((x+y*8)==(index-8-1) || (x+y*8)==(index-8+1) || (x+y*8)==(index+8-1) || (x+y*8)==(index+8+1)) {
				return true;
			}
			
		}
		return false;
	}
	/*
	 * We set three mountain fields which is the minimum requirement
	 */
	private void setMountainFields() {
		HalfMapNode tempNode;
		/*
		 * We randomly specify how many mountain nodes more than the minimum of 3 we will add.
		 * we set the limit of at most 4 more
		 */
		int additionalMountainNodes=random.nextInt(4);
		for(int i=0;i<3+additionalMountainNodes;++i) {
			int tempNodeIndex;
		
			do {
				tempNodeIndex=random.nextInt(32);
			}while(alreadySetMapNodes.containsKey(tempNodeIndex));
			
			alreadySetMapNodes.put(tempNodeIndex, true);
			tempNode= new HalfMapNode(tempNodeIndex%8, (int) tempNodeIndex/8, ETerrain.Mountain);
			arrayOfNodes.add(tempNode);
		}
		mountainNodes=3+additionalMountainNodes;
		logger.info(String.format("Mountain fileds: %s", 3+additionalMountainNodes));
	}
	/*
	 * We set the fort randomly in a not yet assigned field as a grass field
	 */
	private void setFort() {
		int tempNodeIndex;
		do {
			tempNodeIndex=random.nextInt(32);
		}while(alreadySetMapNodes.containsKey(tempNodeIndex));
		alreadySetMapNodes.put(tempNodeIndex, true);
		HalfMapNode tempNode= new HalfMapNode(tempNodeIndex%8, (int) tempNodeIndex/8, true,ETerrain.Grass);
		arrayOfNodes.add(tempNode);
		logger.info(String.format("Fort Location: [X:Y] [%s;%s]", tempNodeIndex%8, (int) tempNodeIndex/8));
	}
	/*
	 * We fill the rest of the map with grass fields
	 */
	private void setGrassFields() {
		HalfMapNode tempNode;
		for(int i=0;i<32;++i) {
			if(!alreadySetMapNodes.containsKey(i)) {
				alreadySetMapNodes.put(i, true);
				tempNode= new HalfMapNode(i%8,(int) i/8, ETerrain.Grass);
				arrayOfNodes.add(tempNode);
			}
		}
		
	}
	
	private void validateMap() throws HalfMapCheckedExcpetion {
		int waterCounter=0;
		int mountainCounter=0;
		
		for(int i=0;i<32;++i) {
			if(!alreadySetMapNodes.containsKey(i)) {
				throw new HalfMapCheckedExcpetion("Not all nodes are assigned");
			}
			if(arrayOfNodes.get(i).getTerrain()==ETerrain.Water) {
				waterCounter++;
			}
			else if(arrayOfNodes.get(i).getTerrain()==ETerrain.Mountain) {
				mountainCounter++;
			}
		}
		if(waterCounter!=waterNodes) {
			throw new HalfMapCheckedExcpetion("Water nodes are not as many as they should be");
		}
		if(mountainCounter!=mountainNodes) {
			throw new HalfMapCheckedExcpetion("Mountain nodes are not as many as they should be");
		}
		if(32-waterCounter+mountainCounter<15) {
			throw new HalfMapCheckedExcpetion("Too few grass nodes");
		}
	}
	
	
}

package server.model.map;

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.ETerrain;
import MessagesBase.HalfMap;
import MessagesBase.HalfMapNode;
import server.controller.GameController;

public class HalfMapValidator {
	
	private static final Logger HalfMapValidatorLogger = LoggerFactory.getLogger(HalfMapValidator.class);
	
	public static final int NUMBER_OF_FIELDS=32;
	public static final int MIN_NUMBER_OF_WATER_FIELDS=4;
	public static final int MIN_NUMBER_OF_GRASS_FIELDS=15;
	public static final int MIN_NUMBER_OF_MOUNTAIN_FIELDS=3;
	public static final int MAX_LONG_BORDER_WATER_FIELDS=3;
	public static final int MAX_SHORT_BORDER_WATER_FIELDS=1;
	
	public static boolean validateFieldNumber(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateFieldNumber - number of fields: {}", halfMapRaw.getNodeArray().size());
		return halfMapRaw.getNodeArray().size()==NUMBER_OF_FIELDS ? true:false;
	}
	
	public static boolean validateWaterFieldNumber(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateWaterFieldNumber - number of water fields: {}", halfMapRaw.getWaterCount());
		return halfMapRaw.getWaterCount()>=MIN_NUMBER_OF_WATER_FIELDS ? true:false;
	}
	
	public static boolean validateMountainFieldNumber(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateMountainFieldNumber - number of mountain fields: {}", halfMapRaw.getMountainCount());
		return halfMapRaw.getMountainCount()>=MIN_NUMBER_OF_MOUNTAIN_FIELDS ? true:false;
	}
	
	public static boolean validateGrassFieldNumber(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateGrassFieldNumber - number of grass fields: {}", halfMapRaw.getGrassCount());
		return halfMapRaw.getWaterCount()>=MIN_NUMBER_OF_WATER_FIELDS ? true:false;
	}
	
	public static boolean validateLongBorder(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateLongBorder - long border water fiels: top {}, bottom {}", halfMapRaw.getTopBorderWaterCount(), halfMapRaw.getBottomBorderWaterCount());
		if(halfMapRaw.getBottomBorderWaterCount()>MAX_LONG_BORDER_WATER_FIELDS || 
				halfMapRaw.getTopBorderWaterCount()>MAX_LONG_BORDER_WATER_FIELDS) {
			return false;
		}
		return true;
	}
	
	public static boolean validateShortBorder(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateShortBorder - short border water fiels: left {}, right {}", halfMapRaw.getLeftBorderWaterCount(), halfMapRaw.getRightBorderWaterCount());
		if(halfMapRaw.getLeftBorderWaterCount()>MAX_SHORT_BORDER_WATER_FIELDS || 
				halfMapRaw.getRightBorderWaterCount()>MAX_SHORT_BORDER_WATER_FIELDS) {
			return false;
		}
		return true;
	}
	
	public static boolean validateFortPresent(HalfMapRaw halfMapRaw) {
		HalfMapValidatorLogger.info("validateFortPresent - fort location index: {}", halfMapRaw.getFortLocation());
		return halfMapRaw.getFortLocation()==-1 ? false:true;
	}
	
	public static boolean validateFortInGrass(HalfMapRaw halfMapRaw) {
		int fortLocation=halfMapRaw.getFortLocation();
		HalfMapValidatorLogger.info("validateFortInGrass - terrain at fort location: {}", halfMapRaw.getNodeArray().get(fortLocation).getTerrain());
		return halfMapRaw.getNodeArray().get(fortLocation).getTerrain().equals(ETerrain.Grass) ? true: false;
	}
	
	/*
	 * To check if there is an island in the map,
	 * I use BFS to visit all the nodes that can be visited.
	 * I consider all grass and mountain fields as visitable.
	 * If the total number of visited is less to the total
	 * number of grass and mountain fields, than there is an island,
	 * because there were clearly fields that cannot be visited
	 */
	
	public static boolean validateNoIslands(HalfMapRaw halfMapRaw) {
		
		int nonWaterFieldsCount=halfMapRaw.getGrassCount()+halfMapRaw.getMountainCount();
		
		ArrayList<Integer> visitedNodes=new ArrayList<>();
		LinkedList<Integer> nodeQueue=new LinkedList<>();
		nodeQueue.add(halfMapRaw.getFortLocation());
		int currentIndex=-1;
		
		int leftBorder=0, topBorder=0, rightBorder=7, downBorder=3;
		int yMultiplier=8;
		
		ArrayList<HalfMapNode> nodes=halfMapRaw.getNodeArray();
		while(!nodeQueue.isEmpty()) {
			currentIndex=nodeQueue.pop();
			visitedNodes.add(currentIndex);
			int leftIndex=currentIndex-1;
			int upIndex=currentIndex-8;
			int rightIndex=currentIndex+1;
			int downIndex=currentIndex+8;
			
			if(currentIndex%yMultiplier!=leftBorder) {
				if(!nodes.get(leftIndex).getTerrain().equals(ETerrain.Water) && !visitedNodes.contains(leftIndex) && !nodeQueue.contains(leftIndex)) {
					nodeQueue.add(0,leftIndex);
				}
			}
			if(currentIndex/yMultiplier!=topBorder) {
				if(!nodes.get(upIndex).getTerrain().equals(ETerrain.Water) && !visitedNodes.contains(upIndex) && !nodeQueue.contains(upIndex)) {
					nodeQueue.add(0,upIndex);
				}
			}
			if(currentIndex%yMultiplier!=rightBorder) {
				if(!nodes.get(rightIndex).getTerrain().equals(ETerrain.Water) && !visitedNodes.contains(rightIndex) && !nodeQueue.contains(rightIndex)) {
					nodeQueue.add(0,rightIndex);
				}
			}
			if(currentIndex/yMultiplier!=downBorder) {
				if(!nodes.get(downIndex).getTerrain().equals(ETerrain.Water) && !visitedNodes.contains(downIndex) && !nodeQueue.contains(downIndex)) {
					nodeQueue.add(0,downIndex);
				}
			}
		}
		
		return visitedNodes.size()==nonWaterFieldsCount ? true:false;
	}

}

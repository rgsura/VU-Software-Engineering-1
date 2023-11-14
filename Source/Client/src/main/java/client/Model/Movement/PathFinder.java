package client.Model.Movement;

import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import MessagesBase.EMove;
import MessagesBase.ETerrain;
import MessagesGameState.EPlayerPositionState;
import MessagesGameState.ETreasureState;
import MessagesGameState.FullMap;
import MessagesGameState.FullMapNode;
import client.Controller.MainController;
import client.Model.Exception.PathFinderCheckedException;
import client.Model.Map.FullMapWeight;
import client.Model.Map.MapWeightNode;

public class PathFinder {
	
	private static Logger logger = LoggerFactory.getLogger(PathFinder.class);
	
	private FullMapWeight fullMapWeight=new FullMapWeight();
	private FullMapNode currentPosition=null;
	private LinkedList<EMove> moveQueue=new LinkedList<EMove>();
	private boolean crossingBorder=false;
	private int startingX=-1;
	private int startingY=-1;

	public PathFinder() {}
	
	public void initialiseNodesWeight(FullMap fullMap) {
		fullMapWeight.initialiseNodeWeights(fullMap);
	}
	
	/*
	 * there is queue of moves. If the algorithm determines a destination that is not adjacent to the current position, 
	 * it adds all the moves to the queue, and the getNextMove() gets them one by one.
	 * If one is currently crossing the boarder, then buildPathToNearestNotVisited() is not called, but buildPathToBorder() instead. 
	 */
	public EMove getNextMove() throws PathFinderCheckedException, Exception {
		if(moveQueue.size()==0 && !crossingBorder) {
			buildPathToNearestNotVisited();
		}
		if(moveQueue.size()==0 && !crossingBorder) {
			logger.error("getNextMove: moveQueue is still empty");
			throw new PathFinderCheckedException("getNextMove: moveQueue is still empty");
		}
		return moveQueue.pop();
	}
	
	public void updateCurrentPosition(FullMap fullMap) {
		for(FullMapNode currentNode: fullMap.getMapNodes()) {
			if(currentNode.getPlayerPositionState()==EPlayerPositionState.MyPosition || currentNode.getPlayerPositionState()==EPlayerPositionState.BothPlayerPosition) {
				currentPosition=currentNode;
				fullMapWeight.updateCurrentPosition(currentNode);
				
				//Save the starting point of the player
				if(startingX==-1 && startingY==-1) {
					startingX=currentPosition.getX();
					startingY=currentPosition.getY();
				}
				logger.info(String.format("updateCurrentPosition: New Player Position: [%d, %d]", currentPosition.getX(),currentPosition.getY()));
				break;
			}
		}
	}
	
	public FullMapNode getCurrentPosition() {
		return currentPosition;
	}
	
	public boolean getTreasureState() {
		if(currentPosition.getTreasureState()==ETreasureState.MyTreasureIsPresent) {
			return true;
		}
		return false;
	}
	
	public ETerrain getCurrentNodeTerrain() {
		return currentPosition.getTerrain();
	}
	public void findingFort() {
		crossingBorder=false;
		moveQueue.clear();
	}
	
	/*
	 * Using the starting position, I check if the border is crossed to know when to stop trying to it.
	 * The logic is straight forward 
	 */
	public boolean borderIsCrossed() {
		if(fullMapWeight.getYMultiplyer()==16) {
			if(startingX<8) {
				if(currentPosition.getX()>7) {
					crossingBorder=false;
					return true;
				}
				return false;
			}
			else {
				if(currentPosition.getX()<8) {
					crossingBorder=false;
					return true;
				}
				return false;
			}
		}
		else {
			if(startingY<4) {
				if(currentPosition.getY()>3) {
					crossingBorder=false;
					return true;
				}
				return false;
			}
			else {
				if(currentPosition.getY()<4) {
					crossingBorder=false;
					return true;
				}
				return false;
			}
		}
	}
	
	/*
	 * Using the same BFS algorithm as a base from the normal algo used to find the treasure and fort
	 * There are, however enough differences for it to not be worth it as the same function
	 */
	public void buildPathToBorder() throws PathFinderCheckedException {
		moveQueue.clear();
		crossingBorder=true;
		int currentX=currentPosition.getX();
		int currentY=currentPosition.getY();
		int xCorrection=0, yCorrection=0;
		if(currentX>7) {
			currentX-=8;
			xCorrection=8;
		}
		else if(currentY>3) {
			currentY-=4;
			yCorrection=4;
		}
		
		int borderX1=0, borderX2=7;
		int borderY1=0, borderY2=3;
		
		/*
		 * Using the same BFS algorithm as a base from the normal algo used to find the treasure and fort
		 * There are, however enough differences for it to not be worth it as the same function
		 */
		
		int[] mapNodes= new int[32];
		EMove[] pathMap= new EMove[32];
		for(int i=0;i<32;i++) {
			mapNodes[i]=9999;
			pathMap[i]=null;
		}
		/*
		 * I determine the last move neccesary to cross the boarder from the x-and-yCorrection and yMultiplyer 
		 * which basically tell me everything I need to determine the positions of the cards, which than will be added to the queue.
		 * I also use it to prohibit the BFS from looking at all the opposite direction, because there is never a case where going around is faster than going straight
		 * This makes the algo quite a bit faster than a full BFS
		 */
		EMove finalMove=null;
		if(xCorrection==0) {
			if(yCorrection==0) {
				if(fullMapWeight.getYMultiplyer()==8) {
					finalMove=EMove.Down;
				}
				else {
					finalMove = EMove.Right;
				}
			}
			else {
				finalMove = EMove.Up;
			}
		}
		else {
			finalMove = EMove.Left;
		}
		
		/*
		 * I determine a new array to hold the weights of the border, so that I can tell if there is a water in front or not, 
		 * without needing to change the mapNodes[] and pathMove[] arrays to accommodate for the extra row/column
		 */
		int borderLength=0;
		if(fullMapWeight.getYMultiplyer()==8)
			borderLength=8;
		else {
			borderLength=4;
		}
		int[] borderWeight= new int[borderLength];
		for(int i=0;i<borderLength;i++) {
			if(finalMove==EMove.Down) {
				borderWeight[i]=fullMapWeight.getMapWeightNodeByCoordinates(i, 4).getWeight();
			}
			else if(finalMove==EMove.Up) {
				borderWeight[i]=fullMapWeight.getMapWeightNodeByCoordinates(i, 3).getWeight();
			}
			else if(finalMove==EMove.Right) {
				borderWeight[i]=fullMapWeight.getMapWeightNodeByCoordinates(8, i).getWeight();
			}
			else {
				borderWeight[i]=fullMapWeight.getMapWeightNodeByCoordinates(7, i).getWeight();
			}
		}
		
		LinkedList<Integer> checkedNodes=new LinkedList<Integer>();
		checkedNodes.add(currentX+currentY*8);
		MapWeightNode currentNode;
		mapNodes[currentX+currentY*8]=0;
		EMove tempPath=null;
		moveQueue.add(finalMove);
		logger.info(String.format("buildPathToBorder: CurrentPosition: [%s,%s]", currentX, currentY));
		logger.info(String.format("buildPathToBorder: corrections: [%s,%s]", xCorrection, yCorrection));
		
		while(checkedNodes.size()!=0) {
			int currentIndex=checkedNodes.pop();
			currentX=currentIndex%8;
			currentY=(int) currentIndex/8;
			logger.info(String.format("buildPathToBorder: index: %s", currentIndex));
			logger.info(String.format("buildPathToBorder: CurrentPosition: [%s,%s]", currentX, currentY));
			int currentWeight=mapNodes[currentIndex];
			EMove previousMove = pathMap[currentIndex];
			int tempWeight=0;
			logger.info(String.format("buildPathToBorder: PrevMove: %s, weight: %s", pathMap[currentIndex], mapNodes[currentIndex]));

			currentNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection);
			
			/*
			 * We check if we are in the side border (first if), or in the top/down one, and also if the node in front is not a water node.
			 * If we pass the checks, than we add the moves until there to the queue, as we already have the last move from the finalMove we calculated earlier.
			 */
			
			if((currentX==borderX1 && finalMove==EMove.Left) || (currentX==borderX2 && finalMove==EMove.Right)) {
				if(borderWeight[currentY]!=9999) {
					if(pathMap[currentIndex]!=null) {
						generatePathQueue(pathMap, currentIndex);
					}
					return;
				}
			}
			else if((currentY==borderY1 && finalMove==EMove.Up) || (currentY==borderY2 && finalMove==EMove.Down)) {
				if(borderWeight[currentX]!=9999) {
					if(pathMap[currentIndex]!=null) {
						generatePathQueue(pathMap, currentIndex);
					}
					return;
				}
			}
			else {
				/*
				 * Here we don't need to check if a node is visited, because our designation is somewhat known
				 */
				
				//checking left
				if(currentX!=borderX1 && finalMove!=EMove.Right && previousMove!=EMove.Right) {
					
					MapWeightNode leftNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection-1, currentY+yCorrection);
					logger.debug(String.format("buildPathToBorder left:index: %s, weight: %s", currentIndex, leftNode.getWeight()));
					if(leftNode.getWeight()!=9999) {
						tempPath=pathMap[currentIndex-1];
						pathMap[currentIndex-1]=EMove.Left;
						tempWeight=mapNodes[currentIndex-1];
						mapNodes[currentIndex-1]=currentWeight+currentNode.getWeight()+leftNode.getWeight();
						logger.debug(String.format("buildPathToNearestNotVisited left: Move: %s", pathMap[currentIndex-1]));
						checkedNodes.add(currentIndex-1);
						
						if(tempWeight<mapNodes[currentIndex-1]) {
							mapNodes[currentIndex-1]=tempWeight;
							pathMap[currentIndex-1]=tempPath;
							
						}
						logger.info(String.format("buildPathToNearestNotVisited left: Move: %s: , index: %s, weight: %s", pathMap[currentIndex-1], currentIndex-1, mapNodes[currentIndex-1]));
						
					}
					
				}
				
				//checking up
				if(currentY!=borderY1 && finalMove!=EMove.Down && previousMove!=EMove.Down) {
					MapWeightNode upNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection-1);
					logger.debug(String.format("buildPathToBorder up: index: %s, weight: %s", currentIndex, upNode.getWeight()));
					if(upNode.getWeight()!=9999) {
						tempPath=pathMap[currentIndex-8];
						pathMap[currentIndex-8]=EMove.Up;
						tempWeight=mapNodes[currentIndex-8];
						mapNodes[currentIndex-8]=currentWeight+currentNode.getWeight()+upNode.getWeight();
						checkedNodes.add(currentIndex-8);
						
						if(tempWeight<mapNodes[currentIndex-8]) {
							mapNodes[currentIndex-8]=tempWeight;
							pathMap[currentIndex-8]=tempPath;			
						}
						logger.info(String.format("buildPathToNearestNotVisited up: Move: %s: , index: %s, weight: %s", pathMap[currentIndex-8], currentIndex-8, mapNodes[currentIndex-8]));

					}
					
				}
				
				//checking right
				if(currentX!=borderX2 && finalMove!=EMove.Left && previousMove!=EMove.Left) {
					MapWeightNode rightNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection+1, currentY+yCorrection);
					logger.debug(String.format("buildPathToBorder righer: index: %s, weight: %s", currentIndex, rightNode.getWeight()));
					if(rightNode.getWeight()!=9999) {
						tempPath=pathMap[currentIndex+1];
						pathMap[currentIndex+1]=EMove.Right;
						tempWeight=mapNodes[currentIndex+1];
						mapNodes[currentIndex+1]=currentWeight+currentNode.getWeight()+rightNode.getWeight();
						checkedNodes.add(currentIndex+1);
						
						if(tempWeight<mapNodes[currentIndex+1]) {
							mapNodes[currentIndex+1]=tempWeight;
							pathMap[currentIndex+1]=tempPath;
						}
						logger.info(String.format("buildPathToNearestNotVisited right: Move: %s: , index: %s, weight: %s", pathMap[currentIndex+1], currentIndex+1, mapNodes[currentIndex+1]));
					}
				}
				
				//checking down
				if(currentY!=borderY2 && finalMove!=EMove.Up && previousMove!=EMove.Up) {
					MapWeightNode downNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection+1);
					logger.debug(String.format("buildPathToBorder down: index: %s, weight: %s", currentIndex, downNode.getWeight()));
					if(downNode.getWeight()!=9999) {
						tempPath=pathMap[currentIndex+8];
						pathMap[currentIndex+8]=EMove.Down;
						tempWeight=mapNodes[currentIndex+8];
						mapNodes[currentIndex+8]=currentWeight+currentNode.getWeight()+downNode.getWeight();
						checkedNodes.add(currentIndex+8);
							
						if(tempWeight<mapNodes[currentIndex+8]) {
							mapNodes[currentIndex+8]=tempWeight;
							pathMap[currentIndex+8]=tempPath;
							
						}
						logger.info(String.format("buildPathToNearestNotVisited down: Move: %s: , index: %s, weight: %s", pathMap[currentIndex+8], currentIndex+8, mapNodes[currentIndex+8]));
					}		
				}
			}
		}
		
	}
		
	
	private void buildPathToNearestNotVisited() throws PathFinderCheckedException {
		
		/*
		 * for ease of calculations, we set the x and y always be between 0 and 7, and 0 and 3, 
		 * but to access the proper nodes in the fullMapWeight, we need to store the correction as well
		 */
		
		int currentX=currentPosition.getX();
		int currentY=currentPosition.getY();
		int xCorrection=0, yCorrection=0;
		if(currentX>7) {
			currentX-=8;
			xCorrection=8;
		}
		else if(currentY>3) {
			currentY-=4;
			yCorrection=4;
		}
		
		int borderX1=0, borderX2=7;
		int borderY1=0, borderY2=3;
		
		/*
		 * mapNodes[] stores the weight to going there which is the amount of moves it takes to arrive there, 
		 * which is why is usefull for FullMapNode to have an int weight instead of an ETerrain
		 * pathMap[] stores the move that it would be required for the character to arrive there. 
		 * So if the best path is from [1:2] to [2:2]; the pathMap of [2:2] is Right.
		 */
		
		int[] mapNodes= new int[32];
		EMove[] pathMap= new EMove[32];
		for(int i=0;i<32;i++) {
			mapNodes[i]=9999;
			pathMap[i]=null;
		}
		LinkedList<Integer> checkedNodes=new LinkedList<Integer>(); //the queue required for BFS
		checkedNodes.add(currentX+currentY*8);
		MapWeightNode currentNode;
		mapNodes[currentX+currentY*8]=0;
		EMove tempPath=null;
		int tempWeight=0;
		logger.info(String.format("buildPathToNearestNotVisited: CurrentPosition: [%s,%s]", currentX, currentY));
		logger.info(String.format("buildPathToNearestNotVisited: corrections: [%s,%s]", xCorrection, yCorrection));
		while(checkedNodes.size()!=0) {
			int currentIndex=checkedNodes.pop();
			currentX=currentIndex%8;
			currentY=(int) currentIndex/8;
			logger.info(String.format("buildPathToNearestNotVisited: index: %s", currentIndex));
			logger.info(String.format("buildPathToNearestNotVisited: CurrentPosition: [%s,%s]", currentX, currentY));
			int currentWeight=mapNodes[currentIndex];
			EMove previousMove = pathMap[currentIndex]; // we store the previous move so that we don't check the node from which we just came from
			logger.info(String.format("buildPathToNearestNotVisited: PrevMove: %s, weight: %s", pathMap[currentIndex], mapNodes[currentIndex]));

			currentNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection);
			
			/*
			 * We use the first unvisited grass field as the destination, since the treasure or fort can only be there
			 * once we reach that node, we then call the generatePathQueue() on that node to add the moves to the moveQueue and stop searching;
			 */
			
			if(!currentNode.isVisited()) {
				if(currentNode.getWeight()==1) {
					logger.debug(String.format("buildPathToNearestNotVisited calling generatePath: Index: %s, currentMove: %s",currentIndex,  pathMap[currentIndex]));
					generatePathQueue(pathMap, currentIndex);
					return;
				}
			}
			/*
			 * if the adjacent nodes are not water, they are added to the checkedNodes queue, but if there already is a weight and a path there,
			 * we replace the values only if the weight is smaller. 
			 * I actually replace them first and the substitute them if it was the wrong choice, but the same idea
			 */
			
			//checking left
			if(currentX!=borderX1 && previousMove!=EMove.Right) {
				
				MapWeightNode leftNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection-1, currentY+yCorrection);
				logger.debug(String.format("buildPathToNearestNotVisited left:index: %s, weight: %s", currentIndex, leftNode.getWeight()));
				if(leftNode.getWeight()!=9999) {
					tempPath=pathMap[currentIndex-1];
					pathMap[currentIndex-1]=EMove.Left;
					tempWeight=mapNodes[currentIndex-1];
					mapNodes[currentIndex-1]=currentWeight+currentNode.getWeight()+leftNode.getWeight();
					logger.debug(String.format("buildPathToNearestNotVisited left: Move: %s", pathMap[currentIndex-1]));
					checkedNodes.add(currentIndex-1);
					
					if(tempWeight<mapNodes[currentIndex-1]) {
						mapNodes[currentIndex-1]=tempWeight;
						pathMap[currentIndex-1]=tempPath;
						
					}
					logger.info(String.format("buildPathToNearestNotVisited left: Move: %s: , index: %s, weight: %s", pathMap[currentIndex-1], currentIndex-1, mapNodes[currentIndex-1]));
				}
				
			}
			
			//checking up
			if(currentY!=borderY1 && previousMove!=EMove.Down) {
				MapWeightNode upNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection-1);
				logger.debug(String.format("buildPathToNearestNotVisited up: index: %s, weight: %s", currentIndex, upNode.getWeight()));
				if(upNode.getWeight()!=9999) {
					tempPath=pathMap[currentIndex-8];
					pathMap[currentIndex-8]=EMove.Up;
					tempWeight=mapNodes[currentIndex-8];
					mapNodes[currentIndex-8]=currentWeight+currentNode.getWeight()+upNode.getWeight();
					checkedNodes.add(currentIndex-8);
					
					if(tempWeight<mapNodes[currentIndex-8]) {
						mapNodes[currentIndex-8]=tempWeight;
						pathMap[currentIndex-8]=tempPath;			
					}
					logger.info(String.format("buildPathToNearestNotVisited up: Move: %s: , index: %s, weight: %s", pathMap[currentIndex-8], currentIndex-8, mapNodes[currentIndex-8]));
				}
				
			}
			
			//checking right
			if(currentX!=borderX2 && previousMove!=EMove.Left) {
				MapWeightNode rightNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection+1, currentY+yCorrection);
				logger.debug(String.format("buildPathToNearestNotVisited right: index: %s, weight: %s", currentIndex, rightNode.getWeight()));
				if(rightNode.getWeight()!=9999) {
					tempPath=pathMap[currentIndex+1];
					pathMap[currentIndex+1]=EMove.Right;
					tempWeight=mapNodes[currentIndex+1];
					mapNodes[currentIndex+1]=currentWeight+currentNode.getWeight()+rightNode.getWeight();
					checkedNodes.add(currentIndex+1);
					
					if(tempWeight<mapNodes[currentIndex+1]) {
						mapNodes[currentIndex+1]=tempWeight;
						pathMap[currentIndex+1]=tempPath;
					}
					logger.info(String.format("buildPathToNearestNotVisited right: Move: %s: , index: %s, weight: %s", pathMap[currentIndex+1], currentIndex+1, mapNodes[currentIndex+1]));
				}
				
			}
			
			//checking down
			if(currentY!=borderY2 && previousMove!=EMove.Up) {
				MapWeightNode downNode=fullMapWeight.getMapWeightNodeByCoordinates(currentX+xCorrection, currentY+yCorrection+1);
				logger.debug(String.format("buildPathToNearestNotVisited down: index: %s, weight: %s", currentIndex, downNode.getWeight()));
				if(downNode.getWeight()!=9999) {
					tempPath=pathMap[currentIndex+8];
					pathMap[currentIndex+8]=EMove.Down;
					tempWeight=mapNodes[currentIndex+8];
					mapNodes[currentIndex+8]=currentWeight+currentNode.getWeight()+downNode.getWeight();
					checkedNodes.add(currentIndex+8);
						
					if(tempWeight<mapNodes[currentIndex+8]) {
						mapNodes[currentIndex+8]=tempWeight;
						pathMap[currentIndex+8]=tempPath;
						
					}
					logger.info(String.format("buildPathToNearestNotVisited down: Move: %s: , index: %s, weight: %s", pathMap[currentIndex+8], currentIndex+8, mapNodes[currentIndex+8]));
				}
				
			}
				
		}

	}
	
	/*
	 * when we have a final destination, we add the moves backward.
	 * after every move, we go the node, expressed by the index, from which it would be required to arrive there
	 * which is in the opposite direction of the move itself.
	 * we continue until we reach the starting position
	 */
	
	private void generatePathQueue(EMove[] pathMap, int index) throws PathFinderCheckedException {
		int currentX=currentPosition.getX();
		int currentY=currentPosition.getY();
		int xCorrection=0, yCorrection=0;
		if(currentX>7) {
			currentX-=8;
			xCorrection=8;
		}
		else if(currentY>3) {
			currentY-=4;
			yCorrection=4;
		}
		logger.debug(String.format("generatePathQueue: CurrentPosition: [%s,%s]", currentX, currentY));
		do {
			logger.debug(String.format("generatePathQueue: Move: %s: , index: %s", pathMap[index], index));
			moveQueue.push(pathMap[index]);
			if(pathMap[index]==EMove.Left) {
				index++;
			}
			else if(pathMap[index]==EMove.Right) {
				index--;
			}
			else if(pathMap[index]==EMove.Up) {
				index+=8;
			}
			else if(pathMap[index]==EMove.Down) {
				index-=8;
			}
			else {
				/*
				 * Throw exception
				 */
				
				logger.error("Error provideNextMove: EMove not any option!!!");
				throw new PathFinderCheckedException(String.format("Move at index: %s is null", index));
			}
			
		}while(index!=currentX+currentY*8);
	}
	

}

package server.model.map;

import java.util.ArrayList;
import java.util.Random;

import MessagesBase.ETerrain;
import MessagesBase.HalfMap;
import MessagesBase.HalfMapNode;
import MessagesBase.UniquePlayerIdentifier;

public class HalfMapRaw {
	
	/*
	 * We use for HalfMapRaw the same strategy for indexing as described in FullMapRaw
	 */
	
	private ArrayList<HalfMapNode> nodeArray = new ArrayList<>();
	private UniquePlayerIdentifier playerId;
	private int fortLocation=-1;
	private int treasureLocation;
	private int grassCount=0;
	private int mountainCount=0;
	private int waterCount=0;
	private int topBorderWaterCount=0;
	private int bottomBorderWaterCount=0;
	private int leftBorderWaterCount=0;
	private int rightBorderWaterCount=0;
	private final int yMultiplier=8;
	
	public HalfMapRaw () {
		
	}
	
	public HalfMapRaw (HalfMap halfMap) {
		for (int i = 0; i < HalfMapValidator.NUMBER_OF_FIELDS; i++) {
				nodeArray.add(null);
			}
		setNodeArray(halfMap);
	}
	
	/*
	 * We set the nodes in HalfMapRaw.
	 * We store all the counters so that we can use them to validate the halfMap
	 */
	private void setNodeArray(HalfMap halfMap) {
		ArrayList<HalfMapNode> tempraryNodes = new ArrayList<>(halfMap.getNodes());
		setPlayerId(new UniquePlayerIdentifier(halfMap.getUniquePlayerID()));
		for(HalfMapNode node: tempraryNodes) {
			HalfMapNode newNode=new HalfMapNode(node.getX(), node.getY(), node.isFortPresent(), node.getTerrain());
			int index=newNode.getX()+yMultiplier*newNode.getY();
			nodeArray.set(index, newNode);
			if(newNode.isFortPresent()) {
				setFortLocation(index);
			}
			if(newNode.getTerrain()==ETerrain.Grass) {
				setGrassCount(getGrassCount()+1);
			}
			else if(newNode.getTerrain()==ETerrain.Mountain) {
				setMountainCount(getMountainCount()+1);
			}
			else if(newNode.getTerrain()==ETerrain.Water) {
				setWaterCount(getWaterCount()+1);
				if(newNode.getY()==0) {
					setTopBorderWaterCount(getTopBorderWaterCount()+1);
				}
				else if(newNode.getY()==3) {
					setBottomBorderWaterCount(getBottomBorderWaterCount()+1);
				}
				if(newNode.getX()==0) {
					setLeftBorderWaterCount(getLeftBorderWaterCount()+1);
				}
				else if(newNode.getX()==7) {
					setRightBorderWaterCount(getRightBorderWaterCount()+1);
				}
			}
		}
		setTreasureLocation();
	}
	
	
	public UniquePlayerIdentifier getPlayerId() {
		return playerId;
	}

	public void setPlayerId(UniquePlayerIdentifier playerId) {
		this.playerId = playerId;
	}

	public int getFortLocation() {
		return fortLocation;
	}

	private void setFortLocation(int index) {
		this.fortLocation = index;
	}

	public int getGrassCount() {
		return grassCount;
	}

	private void setGrassCount(int grassCount) {
		this.grassCount = grassCount;
	}

	public int getMountainCount() {
		return mountainCount;
	}

	private void setMountainCount(int mountainCount) {
		this.mountainCount = mountainCount;
	}

	public int getWaterCount() {
		return waterCount;
	}

	private void setWaterCount(int waterCount) {
		this.waterCount = waterCount;
	}

	public ArrayList<HalfMapNode> getNodeArray() {
		return nodeArray;
	}

	public int getTreasureLocation() {
		return treasureLocation;
	}

	private void setTreasureLocation() {
		Random random = new Random();
		int index=-1;
		do {
			index= random.nextInt(nodeArray.size());
		}while(nodeArray.get(index).getTerrain()!=ETerrain.Grass || index==fortLocation);
		this.treasureLocation=index;
	}

	public int getTopBorderWaterCount() {
		return topBorderWaterCount;
	}

	public void setTopBorderWaterCount(int topBorderWaterCount) {
		this.topBorderWaterCount = topBorderWaterCount;
	}

	public int getBottomBorderWaterCount() {
		return bottomBorderWaterCount;
	}

	public void setBottomBorderWaterCount(int bottomBorderWaterCount) {
		this.bottomBorderWaterCount = bottomBorderWaterCount;
	}

	public int getLeftBorderWaterCount() {
		return leftBorderWaterCount;
	}

	public void setLeftBorderWaterCount(int leftBorderWaterCount) {
		this.leftBorderWaterCount = leftBorderWaterCount;
	}

	public int getRightBorderWaterCount() {
		return rightBorderWaterCount;
	}

	public void setRightBorderWaterCount(int rightBorderWaterCount) {
		this.rightBorderWaterCount = rightBorderWaterCount;
	}

	public int getyMultiplier() {
		return yMultiplier;
	}
	
	
	
	
	
	

}

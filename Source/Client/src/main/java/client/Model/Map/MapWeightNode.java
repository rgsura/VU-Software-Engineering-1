package client.Model.Map;

public class MapWeightNode {
	
	private int x;
	private int y;
	private int weight;
	private boolean visited;
	public MapWeightNode(int x, int y, int weight) {
		this.x = x;
		this.y = y;
		this.weight = weight;
		this.visited=false;
	}
	public MapWeightNode(int x, int y, int weight, boolean visited) {
		this.x = x;
		this.y = y;
		this.weight = weight;
		this.visited=visited;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public boolean isVisited() {
		return visited;
	}
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	

}

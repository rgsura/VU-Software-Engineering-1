package client.View;

import MessagesBase.UniquePlayerIdentifier;
import MessagesGameState.FullMap;
import MessagesGameState.FullMapNode;

public class CLI {
	
	private boolean gameLost=false;
	private boolean gameWon=false;
	private boolean treasureFound=false;
	private boolean boarderCrossed=false;
	private boolean fortFound=false;
	
	private boolean noOtherPlayer=false;
	private FullMap fullMap=null;
	private FullMapNode currentPosition=null;
	
	public CLI() {}
	
	private void setcurrentPosition (FullMapNode newPosition) {
		this.currentPosition=newPosition;
	}
	
	public void halfMapSent() {
		System.out.println("Half Map was Sent!");
	}
	public void startPlaying() {
		System.out.println("--------------\tLET THE GAMES BEGIN!!!!!\t--------------");
	}
	
	public void gameLost() {
		if(!gameLost)
		{
			gameLost=true;
			System.out.println(":( :( :( :( :( Game Lost!!! :( :( :( :( :( ");
		}
	}
	
	public void gameWon() {
		if(!gameWon)
		{
			gameWon=true;
			System.out.println("**************\tGame Won!!!\t**************");
		}
	}
	
	public void treasureFound(FullMapNode position) {
		if(!treasureFound)
		{
			treasureFound=true;
			System.out.println(String.format("--------------\tTreasure found at [%s;%s]\t--------------", position.getX(), position.getY()));
		}
	}
	
	public void boarderCrossed(FullMapNode position) {
		if(!boarderCrossed)
		{
			boarderCrossed=true;
			System.out.println(String.format("--------------\tBorder crossed at [%s;%s]\t--------------", position.getX(), position.getY()));
		}
	}
	
	public void fortFound(FullMapNode position) {
		if(!fortFound)
		{
			System.out.println(String.format("--------------\tEnemy fort found at [%s;%s]\t--------------", position.getX(), position.getY()));
			gameWon();
		}
	}
	
	
	
	public static void clientRegistered() {
		System.out.println("Client registed");
	}
	
	public static void playerRegistered(UniquePlayerIdentifier uniquePlayerId) {
		System.out.println("My Player ID:"+uniquePlayerId.getUniquePlayerID());
	}
	
	public static void clientError(String errorMessage) {
		System.out.println("Client error, errormessage: " + errorMessage);
	}
	
	public void showMove(FullMapNode newPosition) {
		
		if(currentPosition!=null) {
			System.out.println(String.format("Move Made: [%s;%s]: %s \t-> [%s;%s]: %s", 
					currentPosition.getX(), currentPosition.getY(),currentPosition.getTerrain() , newPosition.getX(), newPosition.getY(), newPosition.getTerrain()));
		}
		else {
			System.out.println(String.format("Starting postion: [%s;%s]: %s", 
					newPosition.getX(), newPosition.getY(), newPosition.getTerrain()));
		}
		setcurrentPosition(newPosition);
	}
	
	public void genericOutput(String message) {
		
	}
	
	
}

package game.engine;

import java.io.IOException;
import java.util.ArrayList;

import game.engine.dataloader.DataLoader;
import game.engine.monsters.Monster;
import game.engine.Role;

public class Game {
	private Board board;
	private	ArrayList<Monster> allMonsters;
	private	Monster player;
	private	Monster opponent;
	private	Monster current;
	
	
	public Game (Role playerRole) throws IOException{
		board = new Board(DataLoader.readCards());
		allMonsters = DataLoader.readMonsters();
		player = selectRandomMonsterByRole(playerRole);
		Role opponentRole =	(playerRole == Role.SCARER)? Role.LAUGHER:Role.SCARER;
		opponent = selectRandomMonsterByRole(opponentRole);
		setCurrent(player);
	}
	
	
	
	public Monster getCurrent() {
		return current;
	}
	public void setCurrent(Monster current) {
		this.current = current;
	}
	public Board getBoard() {
		return board;
	}
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters;
	}
	public Monster getPlayer() {
		return player;
	}
	public Monster getOpponent() {
		return opponent;
	}
	
	
	
	private Monster selectRandomMonsterByRole(Role role){
		int sizeMonster = allMonsters.size();
		int indexPlayer = (int)(Math.random()*sizeMonster);
		Monster x = allMonsters.get(indexPlayer);
		while (x.getOriginalRole()!= role){
			indexPlayer = (int)(Math.random()*sizeMonster);
			x = allMonsters.get(indexPlayer);
		}
		return x;
	}
}

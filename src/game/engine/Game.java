package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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
		ArrayList<Monster> m = new ArrayList<>();
		for (int i =0 ; i < sizeMonster ; i++){
			if(role == allMonsters.get(i).getRole()){
				m.add(allMonsters.get(i));
			}
		}
		Random r = new Random();
		int indexRandPlayer = r.nextInt(m.size());
		return m.get(indexRandPlayer);
	}
}

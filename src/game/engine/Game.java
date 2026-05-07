package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.*;
import game.engine.monsters.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		allMonsters.remove(player);
		allMonsters.remove(opponent);
		Board.setStationedMonsters(allMonsters);
		board.initializeBoard(DataLoader.readCells());
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
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	private Monster getCurrentOpponent() {
		return (current == player)? opponent:player;
	}
	
	private int rollDice() {
		Random r = new Random();
		int x = r.nextInt(6)+1;
		return x;
	}
	
	public void usePowerup() throws OutOfEnergyException {
		Monster currentOpponent = getCurrentOpponent();
		if (current.getEnergy() >= Constants.POWERUP_COST) {
			current.executePowerupEffect(currentOpponent);
			current.setEnergy(current.getEnergy()-500);}
		else
			throw new OutOfEnergyException();
	}
	
	public void playTurn() throws InvalidMoveException {
		int roll;
		if (current.isFrozen()) {
			current.setFrozen(false);
		}
		else {
			roll = rollDice();
			Monster currentOpponent = getCurrentOpponent(); 
			board.moveMonster(current, roll, currentOpponent);
		}
		switchTurn();
	}
	
	private void switchTurn() {
		Monster currentOpponent = getCurrentOpponent(); 
		setCurrent(currentOpponent);
	}
	
	private boolean checkWinCondition(Monster monster) {
		int position = monster.getPosition();
		int energy = monster.getEnergy();
		return (position == Constants.WINNING_POSITION && energy >= Constants.WINNING_ENERGY);
	}
	public Monster getWinner() {
		 if (checkWinCondition(player)) {
			 return player;
		 }
		 else if (checkWinCondition(opponent)) {
			 return opponent;
		 }
		 else
			 return null;
	}
	
}
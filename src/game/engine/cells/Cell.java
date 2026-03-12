package game.engine.cells;


import game.engine.monsters.Monster;
import game.engine.interfaces.*;

public class Cell {
	
	private String name;
	private Monster monster;

	public Cell(String name) {
	this.name = name;	
	this.monster=null;
	}

	public String getName() {
		return this.name;	
	}
	
	public Monster getMonster() {
		return this.monster;	
	}
	public void setMonster(Monster monster) {
		this.monster = monster;	
	}
	
}

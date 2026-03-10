package game.engine.cells;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;
import game.engine.interfaces.*;

public class Cell implements CanisterModifier {
private String name;
private Monster monster;

public Cell(String name){
this.name=name;	
this.monster=null;
}
public Cell() { 
	
}
public String getName() {
return this.name;	
}
public Monster getMonster() {
return this.monster;	
}
public void setMonster(Monster monster) {
this.monster=monster;	
}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	@Override
	public void modifyCanister(Monster monster) {
		// TODO Auto-generated method stub
		
	}
	

}

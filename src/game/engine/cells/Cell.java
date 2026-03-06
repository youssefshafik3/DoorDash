package game.engine.cells;

public class Cell {
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

}

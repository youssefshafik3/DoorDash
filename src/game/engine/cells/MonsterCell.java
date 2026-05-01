package game.engine.cells;

import game.engine.monsters.*;

public class MonsterCell extends Cell {
	private Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    if (landingMonster.getRole() == cellMonster.getRole()) {
	        cellMonster.executePowerupEffect(landingMonster);
	    } 
	    else if (landingMonster.getEnergy() > cellMonster.getEnergy()) {
	        int landingEnergy = landingMonster.getEnergy();
	        int cellMonsterEnergy = cellMonster.getEnergy();
	        landingMonster.setEnergy(cellMonsterEnergy);
	        cellMonster.setEnergy(landingEnergy);
	    }
	}
}

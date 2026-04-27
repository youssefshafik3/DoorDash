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
	        cellMonster.applyPowerUp(landingMonster);
	    } else {
	        if (landingMonster.getEnergy() > cellMonster.getEnergy()) {
	            int stolenAmount = cellMonster.getEnergy();
	            cellMonster.alterEnergy(-stolenAmount);
	            landingMonster.alterEnergy(stolenAmount);
	        } else if (landingMonster.getEnergy() < cellMonster.getEnergy()) {
	            int currentEnergy = landingMonster.getEnergy();
	            landingMonster.setEnergy(opponentMonster.getEnergy());
	            opponentMonster.setEnergy(currentEnergy);
	        }
	    }
	}
}

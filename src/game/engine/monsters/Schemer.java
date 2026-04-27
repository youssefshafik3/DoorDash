package game.engine.monsters;



import game.engine.Board;
import game.engine.Constants;
import game.engine.Role;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	private int stealEnergyFrom(Monster target) {
        int stolen = Math.min(target.getEnergy(), Constants.SCHEMER_STEAL);
        target.setEnergy(target.getEnergy() - stolen);
        return stolen;
    }
	@Override
    public void setEnergy(int energy) {
        int change = energy - this.getEnergy();
        if (change != 0) {
            super.setEnergy(energy + 10);
        } else {
            super.setEnergy(energy);
        }
    }
	@Override
    public void executePowerupEffect(Monster opponentMonster) {
        int total = stealEnergyFrom(opponentMonster);
        for (Monster m : Board.getStationedMonsters()) {
            total += stealEnergyFrom(m);
        }
        this.setEnergy(this.getEnergy() + total);
    }
	
}

package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}
	
	@Override
    public void setEnergy(int energy) {
        int change = energy - this.getEnergy();
        if (change != 0) {
            super.setEnergy(energy + Constants.MULTITASKER_BONUS);
        } else {
            super.setEnergy(energy);
        }
    }

    @Override
    public void executePowerupEffect(Monster opponentMonster) {
        this.setNormalSpeedTurns(2);
    }

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}

}
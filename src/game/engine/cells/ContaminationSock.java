package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {
public void transport(Monster monster) {
if(monster.isShielded())
monster.setShielded(false);
else 
monster.setEnergy(monster.getEnergy()-Constants.SLIP_PENALTY);	
monster.setPosition(monster.getPosition()+getEffect());
}
	
	public ContaminationSock(String name, int effect) {
		super(name, effect);
	}
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    
	    this.modifyCanisterEnergy(landingMonster, -Constants.SLIP_PENALTY);
	}

	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
	    monster.alterEnergy(canisterValue);
	}
}

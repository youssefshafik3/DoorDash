package game.engine.cells;

import game.engine.interfaces.*;
import game.engine.monsters.Monster;
public class ContaminationSock extends TransportCell implements CanisterModifier {
	
	public ContaminationSock(String name, int effect) {
		super(name, effect);	
	}

	@Override
	public void modifyCanister(Monster monster) {
		if(!(monster.isShielded()))
			monster.setEnergy(monster.getEnergy()-100);
		
	}

}

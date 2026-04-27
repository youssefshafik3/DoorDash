package game.engine.cells;

import game.engine.Constants;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {

	public ContaminationSock(String name, int effect) {
		super(name, effect);
	}
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    landingMonster.alterEnergy(-Constants.SLIP_PENALTY);
	}
}


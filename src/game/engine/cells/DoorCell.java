package game.engine.cells;

import game.engine.Board;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;

public class DoorCell extends Cell implements CanisterModifier {
	private Role role;
	private int energy;
	private boolean activated;
	
	public DoorCell(String name, Role role, int energy) {
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	
	public Role getRole() {
		return role;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean isActivated) {
		this.activated = isActivated;
	}
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		if (!this.isActivated()) {
			boolean f=(landingMonster.getRole()!=this.getRole() && landingMonster.isShielded()==true);
			this.modifyCanisterEnergy(landingMonster,this.getEnergy());
			if  (! f) {
	        for(int i=0;i<Board.getStationedMonsters().size();i++) {
				if(Board.getStationedMonsters().get(i).getRole()==landingMonster.getRole())
					this.modifyCanisterEnergy(Board.getStationedMonsters().get(i),this.getEnergy());
			}
	        
	        }
		}
	    
	    
	    }
	
	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue) {
		int n=monster.getEnergy();
		 canisterValue = (monster.getRole() == this.getRole()) ? canisterValue : (-canisterValue);
		monster.alterEnergy(canisterValue);
		if(n!=monster.getEnergy())
		this.setActivated(true);	
	    }
	
	}


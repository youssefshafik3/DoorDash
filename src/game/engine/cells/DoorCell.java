package game.engine.cells;

import game.engine.Role;

public class DoorCell extends Cell{
private Role role;
private int energy;
private boolean activated;
public DoorCell(String name,Role role,int energy) {
super(name);
this.role=role;
this.energy=energy;
activated=false;
}
public Role getRole() {
return this.role;	
}
public int getEnergy() {
return this.energy;	
}
public boolean isActivated() {
return this.activated;	
}
public void isActivated(boolean activated) {
this.activated=activated;	
}

}

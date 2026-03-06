package game.engine.cells;

public class DoorCell extends Cell{
private Role role;
private int energy;
private boolean activated;
public DoorCell(String name,Role role,int energy) {
this.name=name;
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

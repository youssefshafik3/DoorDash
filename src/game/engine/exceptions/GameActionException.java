package game.engine.exceptions;

public abstract class GameActionException extends Exception {
	
	protected GameActionException() {
		super();
	}
	protected GameActionException(String message) {
		super(message);
	}

}

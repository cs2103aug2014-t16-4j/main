package logic;

public abstract class Command {

	public abstract boolean executeCommand();
	public abstract boolean undo();
}

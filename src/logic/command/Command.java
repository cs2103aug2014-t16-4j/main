//@author A0117993R
package logic.command;

public abstract class Command {

	public abstract boolean executeCommand();
	public abstract boolean undo();
}

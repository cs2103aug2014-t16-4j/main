import gui.TextBuddyUI;

public class TextBuddy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//new TextBuddyUI(args);
		TextBuddyUI ui = new TextBuddyUI(args);
		ui.checkArgs(args);
	}
}

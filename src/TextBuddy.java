import java.io.IOException;

import gui.TextBuddyUI;

public class TextBuddy {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		//new TextBuddyUI(args);
		TextBuddyUI ui = new TextBuddyUI(args);
		ui.checkArgs(args);
	}
}

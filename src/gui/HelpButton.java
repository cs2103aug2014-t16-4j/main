package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

public class HelpButton extends UI {
	public Button help;
	public boolean renderUI() {
		try {
			help = new Button(UIController.SHELL, SWT.NONE);
			if(UIController.ISMAC) {
				help.setBounds(261, 8, 35, 25);
			}
			else {
				help.setBounds(261, 9, 30, 21);
			}
			help.setText("?");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

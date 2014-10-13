package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Shell;

public class DisplayShell extends UI {
	public boolean renderUI() {
		try {
			UIController.SHELL = new Shell (UIController.DISPLAY, SWT.ON_TOP | SWT.MODELESS);
			UIController.SHELL.setSize(300, 620);
			UIController.SHELL.setLayout(null);
			
			FocusListener listener = new FocusListener() {
				public void focusGained(FocusEvent event) {
					UIController.SHELL.setFocus();
				}
				public void focusLost(FocusEvent event) {
					//shell.setMinimized(true);
					//shell.setVisible(false);
				}
			};
			UIController.SHELL.addFocusListener(listener);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

public class StatusIndicator extends UI {
	public Composite statusComposite;
	public Label statusInd;
	public boolean renderUI() {
		try {
			statusComposite = new Composite(UIController.SHELL, SWT.NONE);
			statusComposite.setBounds(10, 596, 280, 14);
			
			int fontSize = 10;
			if(!UIController.ISMAC) {
				fontSize = 8;
			}
			
			statusInd = new Label(statusComposite, SWT.NONE);
			statusInd.setFont(SWTResourceManager.getFont("Lucida Grande", fontSize, SWT.NORMAL));
			statusInd.setBounds(0, 0, 280, 14);
			statusInd.setAlignment(SWT.CENTER);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

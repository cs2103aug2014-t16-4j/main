package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

public class InputBox extends UI {
	public boolean renderUI() {
		try {
			UIController.input = new Text(UIController.SHELL, SWT.BORDER);
			UIController.input.setFocus();
			UIController.input.setBounds(10, 10, 245, 19);
//			input.addKeyListener(new KeyAdapter() {
//				@Override
//				public void keyPressed(KeyEvent e) {
//					if(e.keyCode == SWT.CR){
//						//TaskBoxUI.delegateTask(input.getText());
//						input.setText("");
//					}
//				}
//			});

			FocusListener listener = new FocusListener() {
				public void focusGained(FocusEvent event) {
					UIController.input.setFocus();
				}
				public void focusLost(FocusEvent event) {
				}
			};
			UIController.input.addFocusListener(listener);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}

package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class TrayIcon extends UI {
	public Tray tray;
	public boolean renderUI() {
		try {
			Image image = new Image (UIController.DISPLAY, 16, 16);
			Image image2 = new Image (UIController.DISPLAY, 16, 16);
			GC gc = new GC(image2);
			gc.setBackground(UIController.DISPLAY.getSystemColor(SWT.COLOR_BLACK));
			gc.fillRectangle(image2.getBounds());
			gc.dispose();
			tray = UIController.DISPLAY.getSystemTray ();
			if (tray == null) {
				System.out.println ("The system tray is not available");
			} else {
				final TrayItem item = new TrayItem (tray, SWT.NONE);
				item.setToolTipText("SWT TrayItem");
				item.addListener (SWT.Show, new Listener () {
					@Override
					public void handleEvent (Event event) {
						System.out.println("show");
					}
				});
				item.addListener (SWT.Hide, new Listener () {
					@Override
					public void handleEvent (Event event) {
						System.out.println("hide");
					}
				});
				item.addListener (SWT.Selection, new Listener () {
					@Override
					public void handleEvent (Event event) {
						if(!UIController.SHELL.isVisible()){
							System.out.println("showing window");
							UIController.SHELL.setVisible(true);
							UIController.SHELL.setMinimized(false); 
							UIController.input.setFocus();
							UIController.SHELL.forceActive();
						}
						else{
							System.out.println("hiding window");
							UIController.SHELL.setMinimized(true);
							UIController.SHELL.setVisible(false);
						}
					}
				});
				item.addListener (SWT.DefaultSelection, new Listener () {
					@Override
					public void handleEvent (Event event) {
						System.out.println("default selection");
					}
				});
				final Menu menu = new Menu (UIController.SHELL, SWT.POP_UP);
				for (int i = 0; i < 8; i++) {
					MenuItem mi = new MenuItem (menu, SWT.PUSH);
					mi.setText ("Item" + i);
					mi.addListener (SWT.Selection, new Listener () {
						@Override
						public void handleEvent (Event event) {
							System.out.println("selection " + event.widget);
						}
					});
					if (i == 0) menu.setDefaultItem(mi);
				}
				item.addListener (SWT.MenuDetect, new Listener () {
					@Override
					public void handleEvent (Event event) {
						menu.setVisible (true);
					}
				});
				item.setImage (image2);
				item.setHighlightImage (image);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

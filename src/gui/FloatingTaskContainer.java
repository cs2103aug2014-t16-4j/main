package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class FloatingTaskContainer extends UI {
	public Table floatingTaskTable;
	public ScrolledComposite floatingTaskComposite;
	public boolean renderUI() {
		try {
			floatingTaskComposite = new ScrolledComposite(UIController.SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			floatingTaskComposite.setBounds(10, 446, 280, 144);
			floatingTaskComposite.setExpandHorizontal(true);
			floatingTaskComposite.setExpandVertical(true);
			
			floatingTaskTable = new Table(floatingTaskComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			floatingTaskTable.setHeaderVisible(false);
			floatingTaskTable.setLinesVisible(true);
			floatingTaskComposite.setContent(floatingTaskTable);
			floatingTaskComposite.setMinSize(floatingTaskTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			TableColumn names = new TableColumn(floatingTaskTable, SWT.LEFT);
			names.setWidth(276);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

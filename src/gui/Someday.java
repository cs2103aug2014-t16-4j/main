package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class Someday extends UI {
	public Table somedayTable;
	public ScrolledComposite somedayComposite;
	public boolean renderUI() {
		try {
			somedayComposite = new ScrolledComposite(UIController.SHELL, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
			somedayComposite.setBounds(10, 446, 280, 144);
			somedayComposite.setExpandHorizontal(true);
			somedayComposite.setExpandVertical(true);
			
			somedayTable = new Table(somedayComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			somedayTable.setHeaderVisible(false);
			somedayTable.setLinesVisible(true);
			somedayComposite.setContent(somedayTable);
			somedayComposite.setMinSize(somedayTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			TableColumn names = new TableColumn(somedayTable, SWT.LEFT);
			names.setWidth(276);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

package de.yamori.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public abstract class AbstractSelctionTable extends JPanel {

	protected final JTable jTable;

	public AbstractSelctionTable() {
		setLayout(new BorderLayout());
		
		jTable = new JTable(null);
		jTable.setAutoCreateColumnsFromModel(false);

		// Editable-String-Cols: selectAll() on enter
		TableCellEditor defaultEditor = jTable.getDefaultEditor(String.class);
		Component c = ((DefaultCellEditor)defaultEditor).getComponent();
		if (c instanceof JTextField) {
			final JTextField editorField = (JTextField) c;
			editorField.addFocusListener(new FocusAdapter() {
	
				@Override
				public void focusGained(FocusEvent e) {
					editorField.selectAll();
				}
	
			});
		}		
		init();
		
		add(new JScrollPane(jTable), BorderLayout.CENTER);
	}
	
	protected abstract void init();

	protected TableColumn createColumn(int index, int width, String header, boolean editable) {
		TableColumn column = new TableColumn(index, width);
		column.setHeaderValue(header);
		if (!editable) {
			column.setCellEditor(Disabled.INSTANCE);
		}
		
		return column;
	}

	private final static class Disabled extends AbstractCellEditor implements TableCellEditor {
		
		private final static Disabled INSTANCE = new Disabled();
		
		@Override
		public boolean isCellEditable(EventObject e) {
			return false;
		}

		@Override
		public Object getCellEditorValue() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static abstract class AbstractRenderer<T extends JComponent> extends JPanel implements TableCellRenderer {
		
		private final T component;
		
		private AbstractRenderer(T component) {
			this.component = component;

			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			component.setOpaque(false);
			add(Box.createHorizontalGlue());
			add(component);
			add(Box.createHorizontalGlue());
		}
		
		protected T getComponent() {
			return component;
		}
		
		protected abstract void prepareComponent(T component, Object value, boolean isSelected, boolean hasFocus, int row, int column);
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setOpaque(true);
			} else {
				setBackground(null);
				setOpaque(false);
			}
			prepareComponent(component, value, isSelected, hasFocus, row, column);
			return this;
		}
		
	}
	
	protected final static class CheckRenderer extends AbstractRenderer<JCheckBox> {
		
		public final static CheckRenderer INSTANCE = new CheckRenderer();
		
		private CheckRenderer() {
			super(new JCheckBox());
		}
		
		@Override
		protected void prepareComponent(JCheckBox component, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			component.setSelected(Boolean.TRUE.equals(value));
		}
				
	}
	
	protected final static class IconRenderer extends AbstractRenderer<JLabel> {
		
		public final static IconRenderer INSTANCE = new IconRenderer();
		
		private IconRenderer() {
			super(new JLabel());
			
			getComponent().setIcon(Icons.getIcon("de/yamori/gui/icons/edit.png"));
		}
		
		@Override
		protected void prepareComponent(JLabel component, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			// nothing to do
		}
				
	}
	
	protected final static class CheckEditor extends AbstractCellEditor implements TableCellEditor {
		
		private final JPanel panel = new JPanel();
		private final JCheckBox check = new JCheckBox();
		
		private boolean fill = false;
		
		public CheckEditor() {
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setOpaque(true);
			check.setOpaque(false);
			panel.add(Box.createHorizontalGlue());
			panel.add(check);
			panel.add(Box.createHorizontalGlue());
			
			check.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (!fill) {
						fireEditingStopped();
					}
				}

			});
		}
		
		@Override
		public boolean isCellEditable(EventObject e) {
			return true;
		}

		@Override
		public Object getCellEditorValue() {
			return Boolean.valueOf(check.isSelected());
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//			if (isSelected) {
				panel.setBackground(table.getSelectionBackground());
//				panel.setOpaque(true);
//			} else {
//				panel.setBackground(null);
//				panel.setOpaque(false);
//			}
			fill = true;
			check.setSelected(Boolean.TRUE.equals(value));
			fill = false;
			return panel;
		}
		
	}

}
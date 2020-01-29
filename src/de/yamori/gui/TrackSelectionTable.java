package de.yamori.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.yamori.api.DataStream;

public class TrackSelectionTable<T extends DataStream> extends AbstractSelectionTable {
	
	private final static int COL_CHECKED = 0;
	private final static int COL_ID = 1;
	private final static int COL_LANG = 2;
	
	private final Runnable selectionChanged;
	
	public TrackSelectionTable(Runnable selectionChanged) {
		super(false);
		this.selectionChanged = selectionChanged;
	}
	
	@Override
	protected void init() {
		setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));

		TableColumn checkCol = createColumn(COL_CHECKED, 15, "", true);
		checkCol.setCellRenderer(CheckRenderer.INSTANCE);
		checkCol.setCellEditor(new CheckEditor());
		checkCol.getCellEditor().addCellEditorListener(new CellEditorListener() {
			
			@Override
			public void editingStopped(ChangeEvent e) {
				fireSelectionChanged();
			}
			
			@Override
			public void editingCanceled(ChangeEvent e) {}

		});
		jTable.addColumn(checkCol);			// selected
		jTable.addColumn(createColumn(COL_ID, 15, "ID", false));
		jTable.addColumn(createColumn(COL_LANG, 80, "Language", false));
	}
	
	public void setTracks(Collection<T> tracks, Collection<T> selectedTracks) {
		MyTableModel model = new MyTableModel(tracks != null ? tracks.size() : 0, jTable);
		
		if (tracks != null) {
			int i = 0;
			for (T t : tracks) {
				model.setValueAt(Boolean.valueOf(selectedTracks.contains(t)), i, COL_CHECKED);
				model.setValueAt(t.getId(), i, COL_ID);
				model.setValueAt(t.getLangIso2(), i, COL_LANG);
				
				// store in hidden col:
				model.setTrack(t, i);
				i++;
			}
		}
		
		jTable.setModel(model);
		
		fireSelectionChanged();
	}
	
	public Collection<T> getSelectedTracks() {
		List<T> titles = new ArrayList<>();
		MyTableModel model = (MyTableModel) jTable.getModel();
		for (int i = 0; i < jTable.getRowCount(); i++) {
			if (Boolean.TRUE.equals(model.getValueAt(i, COL_CHECKED))) {
				titles.add(model.getTrack(i));
			}
		}
		return titles;
	}
	
	private void fireSelectionChanged() {
		if (selectionChanged != null) {
			selectionChanged.run();
		}
	}
	
	
	private final class MyTableModel extends DefaultTableModel {
		
		private MyTableModel(int rows, JTable table) {
			super(rows, table.getColumnCount() + 1);
		}
		
		private void setTrack(T track, int row) {
			// store in hidden col:
			setValueAt(track, row, getColumnCount() - 1);
		}
		
		@SuppressWarnings("unchecked")
		private T getTrack(int row) {
			return (T) getValueAt(row, getColumnCount() - 1);
		}

	}
	
}
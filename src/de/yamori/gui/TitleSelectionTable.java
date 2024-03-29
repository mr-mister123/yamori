package de.yamori.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import de.yamori.api.Disc;
import de.yamori.api.Title;
import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.DataStream;
import de.yamori.util.api.Subtitle;
import de.yamori.util.common.YamoriUtils;

public class TitleSelectionTable extends AbstractSelectionTable {
	
	private final static int COL_CHECKED = 0;
	private final static int COL_ID = 1;
	private final static int COL_DURATION = 2;
	private final static int COL_DESCRIPTION = 3;
	private final static int COL_AUDIO = 4;
	private final static int COL_AUDIO_EDIT = 5;
	private final static int COL_SUBTITLE = 6;
	private final static int COL_SUBTITLE_EDIT = 7;
	
	private final Runnable selectionChanged;
	
	private TrackSelectionFrame<AudioTrack> audioSelectionFrame = null;
	private TrackSelectionFrame<Subtitle> subSelectionFrame = null;
	
	public TitleSelectionTable(Runnable selectionChanged) {
		super(true);
		this.selectionChanged = selectionChanged;
	}
	
	@Override
	protected void init() {
		TableColumn checkCol = createColumn(COL_CHECKED, 32, "", true);
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
		jTable.addColumn(createColumn(COL_ID, 22, "ID", false));
		jTable.addColumn(createColumn(COL_DURATION, 120, "Duration", false));
		jTable.addColumn(createColumn(COL_DESCRIPTION, 400, "Title", true));

		// Audio
		jTable.addColumn(createColumn(COL_AUDIO, 180, "Audio", false));
		TableColumn editAudioCol = createColumn(COL_AUDIO_EDIT, 22, "", false);
		editAudioCol.setCellRenderer(IconRenderer.INSTANCE);
		jTable.addColumn(editAudioCol);

		// Subtitle
		jTable.addColumn(createColumn(COL_SUBTITLE, 180, "Subtitles", false));
		TableColumn editSubCol = createColumn(COL_SUBTITLE_EDIT, 22, "", false);
		editSubCol.setCellRenderer(IconRenderer.INSTANCE);
		jTable.addColumn(editSubCol);

		jTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int col = jTable.columnAtPoint(e.getPoint());
				int row = jTable.rowAtPoint(e.getPoint());
				if (col != -1 && row != -1) {
					col = jTable.convertColumnIndexToModel(col);
					if (col == COL_AUDIO_EDIT) {
						final int theRow = jTable.convertRowIndexToModel(row);
						
						SwingUtilities.invokeLater(() -> {
							TrackSelectionFrame<AudioTrack> frame = getAudioSelectionFrame();
							
							TitleHolder title = ((MyTableModel) jTable.getModel()).getTitle(theRow);
							frame.setTracks(row, title.getTitle().getAudioTracks(), title.getSelectedAudioTracks());
							
							Point p = jTable.getLocationOnScreen();
							frame.setLocation(p.x + e.getX(), p.y + e.getY());
							frame.setVisible(true);
						});
					} else if (col == COL_SUBTITLE_EDIT) {
						final int theRow = jTable.convertRowIndexToModel(row);
						
						SwingUtilities.invokeLater(() -> {
							TrackSelectionFrame<Subtitle> frame = getSubSelectionFrame();
							
							TitleHolder title = ((MyTableModel) jTable.getModel()).getTitle(theRow);
							frame.setTracks(row, title.getTitle().getSubtitles(), title.getSelectedSubtitles());
							
							Point p = jTable.getLocationOnScreen();
							frame.setLocation(p.x + e.getX(), p.y + e.getY());
							frame.setVisible(true);
						});
					}
				}
			}

		});
	}
	
	public void setDiscStructure(Disc disc) {
		MyTableModel model = new MyTableModel(disc != null && disc.getTitles() != null ? disc.getTitles().size() : 0, jTable);
		
		if (disc != null && disc.getTitles() != null) {
			int i = 0;
			for (Title t : disc.getTitles()) {
				TitleHolder holder = new TitleHolder(t);
				
				model.setValueAt(Boolean.FALSE, i, COL_CHECKED);
				model.setValueAt(t.getId(), i, COL_ID);
				model.setValueAt(t.getDuration(), i, COL_DURATION);
				String title = t.getDescription();
				if (title == null || title.isEmpty()) {
					title = "title " + (i + 1);
				}
				model.setValueAt(title, i, COL_DESCRIPTION);

				// Audio
				holder.getSelectedAudioTracks().addAll(YamoriUtils.getDefaultAudioTracks(t));
				model.setValueAt(buildStreamString(t.getAudioTracks(), holder.getSelectedAudioTracks()), i, COL_AUDIO);
				
				// Subtitles
				holder.getSelectedSubtitles().addAll(YamoriUtils.getDefaultSubtitles(t));
				model.setValueAt(buildStreamString(t.getSubtitles(), holder.getSelectedSubtitles()), i, COL_SUBTITLE);
				
				// store in hidden col:
				model.setTitle(holder, i);
				i++;
			}
		}
		
		jTable.setModel(model);
		
		fireSelectionChanged();
	}
	
	public Collection<TitleHolder> getSelectedTitles() {
		List<TitleHolder> titles = new ArrayList<>();
		MyTableModel model = (MyTableModel) jTable.getModel();
		for (int i = 0; i < jTable.getRowCount(); i++) {
			if (Boolean.TRUE.equals(model.getValueAt(i, COL_CHECKED))) {
				titles.add(model.getTitle(i));
			}
		}
		return titles;
	}
	
	private void fireSelectionChanged() {
		if (selectionChanged != null) {
			selectionChanged.run();
		}
	}
	
	private <T extends DataStream> String buildStreamString(List<T> all, Collection<T> selected) {
		StringBuilder audio = new StringBuilder();
		audio.append("<html>");
		int a = 0;
		for (DataStream track : all) {
			if (a > 0) {
				audio.append(", ");
			}
			if (selected.contains(track)) {
				audio.append("<b>");
			}
			audio.append(track.getLangIso2());
			if (selected.contains(track)) {
				audio.append("</b>");
			}
			a++;
		}
		audio.append("</html>");
		
		return audio.toString();
	}
	
	private TrackSelectionFrame<AudioTrack> getAudioSelectionFrame()  {
		if (audioSelectionFrame == null)  {
			audioSelectionFrame = new TrackSelectionFrame<>((row, selectedTracks) -> {

				MyTableModel model = (MyTableModel) jTable.getModel();
				TitleHolder title = model.getTitle(row);
				title.getSelectedAudioTracks().clear();
				title.getSelectedAudioTracks().addAll(selectedTracks);
				
				model.setValueAt(buildStreamString(title.getTitle().getAudioTracks(), selectedTracks), row, COL_AUDIO);

			}, (JFrame) SwingUtilities.getWindowAncestor(this));
		}
		return audioSelectionFrame;
	}
	
	private TrackSelectionFrame<Subtitle> getSubSelectionFrame()  {
		if (subSelectionFrame == null)  {
			subSelectionFrame = new TrackSelectionFrame<>((row, selectedTracks) -> {

				MyTableModel model = (MyTableModel) jTable.getModel();
				TitleHolder title = model.getTitle(row);
				title.getSelectedSubtitles().clear();
				title.getSelectedSubtitles().addAll(selectedTracks);
				
				model.setValueAt(buildStreamString(title.getTitle().getSubtitles(), selectedTracks), row, COL_SUBTITLE);

			}, (JFrame) SwingUtilities.getWindowAncestor(this));
		}
		return subSelectionFrame;
	}

	private final static class MyTableModel extends DefaultTableModel {
		
		private MyTableModel(int rows, JTable table) {
			super(rows, table.getColumnCount() + 1);
		}
		
		private void setTitle(TitleHolder title, int row) {
			// store in hidden col:
			setValueAt(title, row, getColumnCount() - 1);
		}
		
		private TitleHolder getTitle(int row) {
			TitleHolder t = (TitleHolder) getValueAt(row, getColumnCount() - 1);
			t.getTitle().setDescription((String) getValueAt(row, COL_DESCRIPTION));
			return t;
		}

	}
	
	public final static class TitleHolder {
		private final Set<AudioTrack> selectedAudioTracks = new HashSet<>();
		private final Set<Subtitle> selectedSubtitles = new HashSet<>();

		private final Title title;

		private TitleHolder(Title title) {
			this.title = title;
		}
		
		public Title getTitle() {
			return title;
		}
		
		public Set<AudioTrack> getSelectedAudioTracks() {
			return selectedAudioTracks;
		}
		
		public Set<Subtitle> getSelectedSubtitles() {
			return selectedSubtitles;
		}

	}
	
}
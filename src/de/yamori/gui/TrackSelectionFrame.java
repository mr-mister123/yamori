package de.yamori.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JFrame;

import de.yamori.api.DataStream;

public class TrackSelectionFrame<T extends DataStream> extends JDialog {
	
	private final TrackSelectionTable<T> table;
	private final SelectionChanged<T> onChange;
	private int currentRow;
	
	public TrackSelectionFrame(SelectionChanged<T> onChange, JFrame parent) {
		super(parent);
		
		setLayout(new BorderLayout());
		
		this.onChange = onChange;
		table = createTable(this::selectionChanged);
		add(table, BorderLayout.CENTER);
		
		setUndecorated(true);
//		setSize(150, 200);
		
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				boolean isChild = false;
				if (e.getOppositeWindow() != null && e.getOppositeWindow().getOwner() == TrackSelectionFrame.this) {
					isChild = true;
				}
				if (!isChild) {
					setVisible(false);
				}
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {}

		});

	}
	
	protected TrackSelectionTable<T> createTable(Runnable selectionChanged) {
		return new TrackSelectionTable<>(selectionChanged);
	}
	
	private void selectionChanged() {
		onChange.setTracks(currentRow, table.getSelectedTracks());
	}
	
	public void setTracks(int row, Collection<T> tracks, Collection<T> selectedTracks) {
		currentRow = row;
		table.setTracks(tracks, selectedTracks);
		
		// pack
		setSize(150, table.getPreferredSize().height);
	}
	
	public interface SelectionChanged<T extends DataStream> {

		public void setTracks(int row, Collection<T> selectedTracks);

	}

}
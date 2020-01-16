package de.yamori.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JFrame;

import de.yamori.api.AudioTrack;

public class AudioSelectionFrame extends JDialog {
	
	private final AudioSelectionTable table;
	private final SelectionChanged onChange;
	private int currentRow;
	
	public AudioSelectionFrame(SelectionChanged onChange, JFrame parent) {
		super(parent);
		
		setLayout(new BorderLayout());
		
		this.onChange = onChange;
		table = new AudioSelectionTable(this::selectionChanged);
		add(table, BorderLayout.CENTER);
		
		setUndecorated(true);
//		setSize(150, 200);
		
		addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				boolean isChild = false;
				if (e.getOppositeWindow() != null && e.getOppositeWindow().getOwner() == AudioSelectionFrame.this) {
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
	
	private void selectionChanged() {
		onChange.setTracks(currentRow, table.getSelectedTracks());
	}
	
	public void setTracks(int row, Collection<AudioTrack> tracks, Collection<AudioTrack> selectedTracks) {
		currentRow = row;
		table.setTracks(tracks, selectedTracks);
		
		// pack
		setSize(150, table.getPreferredSize().height);
	}
	
	public interface SelectionChanged {

		public void setTracks(int row, Collection<AudioTrack> selectedTracks);

	}

}
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
	
	public AudioSelectionFrame(JFrame parent) {
		super(parent);
		
		setLayout(new BorderLayout());
		
		// TODO
		table = new AudioSelectionTable(null);
		add(table, BorderLayout.CENTER);
		
		setUndecorated(true);
		setSize(150, 200);
		
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
	
	public void setTracks(Collection<AudioTrack> tracks, Collection<AudioTrack> selectedTracks) {
		table.setTracks(tracks, selectedTracks);
	}

}
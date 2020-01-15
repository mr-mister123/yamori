package de.yamori.api;

import java.util.List;

import de.yamori.gui.ProgressTracker;

public interface ReaderBackend {
	
	public Disc getStructure();
	
	public void copyTo(Title title, List<AudioTrack> audioTracks, String fileName, ProgressTracker tracker);

}
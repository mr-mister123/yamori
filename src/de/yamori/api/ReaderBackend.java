package de.yamori.api;

import java.util.Collection;

import de.yamori.gui.ProgressTracker;

public interface ReaderBackend {
	
	public Disc getStructure();
	
	public void copyTo(Title title, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles, String fileName, ProgressTracker tracker);

}
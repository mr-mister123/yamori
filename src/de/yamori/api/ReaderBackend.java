package de.yamori.api;

import java.util.Collection;

import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.Subtitle;
import de.yamori.util.common.ProgressTracker;

public interface ReaderBackend {
	
	public Disc getStructure();
	
	public void copyTo(Title title, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles, String fileName, ProgressTracker tracker);

}
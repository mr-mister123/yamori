package de.yamori.util.api;

import java.util.ArrayList;
import java.util.List;

public class MovieContainer {

	private final List<VideoStream> videoStreams = new ArrayList<>();
	private final List<AudioTrack> audioTracks = new ArrayList<>();
	private final List<Subtitle> subtitles = new ArrayList<>();

	public List<VideoStream> getVideoStreams() {
		return videoStreams;
	}

	public List<AudioTrack> getAudioTracks() {
		return audioTracks;
	}

	public List<Subtitle> getSubtitles() {
		return subtitles;
	}

}
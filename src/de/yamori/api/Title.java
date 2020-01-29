package de.yamori.api;

import java.util.ArrayList;
import java.util.List;

public class Title {

	private final List<AudioTrack> audioTracks = new ArrayList<>();
	private final List<Subtitle> subtitles = new ArrayList<>();

	private int id;
	private String duration;
	private String description;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<AudioTrack> getAudioTracks() {
		return audioTracks;
	}
	
	public List<Subtitle> getSubtitles() {
		return subtitles;
	}

}
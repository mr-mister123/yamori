package de.yamori.util.tmdb.api;

public abstract class TMDBEpisodes {
	private final int id;
	
	private String name;
	
	private int season;
	private int episode;

	public TMDBEpisodes(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getSeason() {
		return season;
	}

	public int getEpisode() {
		return episode;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setSeason(int season) {
		this.season = season;
	}

	protected void setEpisode(int episode) {
		this.episode = episode;
	}

}

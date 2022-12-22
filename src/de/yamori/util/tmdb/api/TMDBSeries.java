package de.yamori.util.tmdb.api;

import java.util.Date;

public abstract class TMDBSeries {

	private final int id;
	
	private String name;
	private String originalName;
	private String originalLangIso2;
	private Date firstAirDate;

	public TMDBSeries(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getOriginalLangIso2() {
		return originalLangIso2;
	}

	public Date getFirstAirDate() {
		return firstAirDate;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	protected void setOriginalLangIso2(String originalLangIso2) {
		this.originalLangIso2 = originalLangIso2;
	}

	protected void setFirstAirDate(Date firstAirDate) {
		this.firstAirDate = firstAirDate;
	}

}

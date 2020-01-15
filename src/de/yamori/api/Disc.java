package de.yamori.api;

import java.util.ArrayList;
import java.util.List;

public class Disc {
	
	private final List<Title> titles = new ArrayList<>();
	private final String discTitle;
	
	public Disc(String discTitle) {
		this.discTitle = discTitle;
	}
	
	public String getDiscTitle() {
		return discTitle;
	}
	
	public List<Title> getTitles() {
		return titles;
	}

}
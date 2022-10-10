package de.yamori.util.api;

import java.awt.Dimension;

public class VideoStream extends DataStream {
	
	private Dimension displayDimension = null;

	public Dimension getDisplayDimension() {
		return displayDimension;
	}

	public void setDisplayDimension(Dimension displayDimension) {
		this.displayDimension = displayDimension;
	}

}
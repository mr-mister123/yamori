package de.yamori.impl.common;

import de.yamori.gui.ProgressTracker;

public interface Task {
	
	public boolean execute(ProgressTracker tracker);

}
package de.yamori.api;

import java.util.List;

import de.yamori.impl.os.LinuxOperatingSystem;

public abstract class OperatingSystem {

	private final static OperatingSystem CURRENT = new LinuxOperatingSystem();		// TODO
	
	public static OperatingSystem getCurrent() {
		return CURRENT;
	}
	
	public abstract List<Device> getDevices();

}
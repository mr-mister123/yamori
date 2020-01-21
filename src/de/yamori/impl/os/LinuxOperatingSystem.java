package de.yamori.impl.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import de.yamori.api.Device;
import de.yamori.api.OperatingSystem;

public class LinuxOperatingSystem extends OperatingSystem {
	
	@Override
	public List<Device> getDevices() {
		List<Device> list = new LinkedList<>();

		try (BufferedReader reader = Files.newBufferedReader(new File("/proc/sys/dev/cdrom/info").toPath())) {
			String line;
			boolean done = false;
			while ((line = reader.readLine()) != null && !done) {
				if (line.startsWith("drive name:")) {
					line = line.substring(11).trim();
					
					String[] devices = line.split("\\s+");
					for (String dev : devices) {
						list.add(new Device("/dev/" + dev));
					}
					done = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}

}
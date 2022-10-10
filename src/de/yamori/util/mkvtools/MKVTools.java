package de.yamori.util.mkvtools;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.yamori.util.common.ProcessBuilder;

public class MKVTools {
	
	private MKVTools() {
		// hide me
	}
	
	public static Version getVersion() {
		return Version.INSTANCE;
	}
	
	public final static class Version {

		private final static Version INSTANCE = new Version();

		private final int major;
		private final int minor;
		private final int build;
		
		private Version() {
			Pattern pattern = Pattern.compile("mkvmerge v([0-9]+)\\.([0-9]+)\\.([0-9]+).*");
			
			int _major = 0;
			int _minor = 0;
			int _build = 0;
			ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "mkvmerge", "--version" });
			try {
				String version = processBuilder.execute();
				if (version != null) {
					version = version.trim();
					
					Matcher matcher = pattern.matcher(version);
					if (matcher.matches()) {
						_major = Integer.parseInt(matcher.group(1));
						_minor = Integer.parseInt(matcher.group(2));
						_build = Integer.parseInt(matcher.group(3));
					}
				}
				System.out.println(version);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			major = _major;
			minor = _minor;
			build = _build;
		}
		
		public int getMajor() {
			return major;
		}
		
		public int getMinor() {
			return minor;
		}
		
		public int getBuild() {
			return build;
		}

	}

}
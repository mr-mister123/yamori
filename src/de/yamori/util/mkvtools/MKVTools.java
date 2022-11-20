package de.yamori.util.mkvtools;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.DataStream;
import de.yamori.util.api.MovieContainer;
import de.yamori.util.api.VideoStream;
import de.yamori.util.common.ProcessBuilder;
import de.yamori.util.common.YamoriUtils;

public class MKVTools {
	
	private final static Pattern PATTERN_PIXEL_DIMS = Pattern.compile("([0-9]+)x([0-9]+)");
	
	private MKVTools() {
		// hide me
	}
	
	public static boolean isInstalled() {
		return Version.INSTANCE.installed;
	}
	
	public static Version getVersion() {
		return Version.INSTANCE;
	}
	
	public static MovieContainer analyseFile(File file) {
		if (!isInstalled()
				|| getVersion().getMajor() < 45) {
			// not supported
			return null;
		}
		
		// PENDING: use mplayer to detect file-length?
		
		ProcessBuilder processBuilder = new ProcessBuilder(new String[] { "mkvmerge", "-J", file.getAbsolutePath() });
		try {
			String info = processBuilder.execute();

			// T O D O debug!
			// System.out.println(info);
			
			if (processBuilder.getExitCode() == 0) {
				JSONObject json = new JSONObject(info);
				
				MovieContainer c = new MovieContainer();
				
				JSONArray tracks = json.optJSONArray("tracks");
				if (tracks != null) {
					for (int i = 0; i < tracks.length(); i++) {
						JSONObject track = tracks.getJSONObject(i);
						
						String type = track.optString("type");
						if ("video".equals(type)) {
							VideoStream v = new VideoStream();
							
							readBaseParameter(track, v);
							readVideoParameter(track, v);
							
							c.getVideoStreams().add(v);
						} else if ("audio".equals(type)) {
							AudioTrack a = new AudioTrack();
							
							readBaseParameter(track, a);
							
							c.getAudioTracks().add(a);
						} // TODO: subtitle --> example needed
					}
				}
				
				return c;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return null;
	}
	
	private static void readBaseParameter(JSONObject track, DataStream stream) {
		stream.setId(track.getInt("id"));
		
		JSONObject props = track.getJSONObject("properties");
		if (props != null) {
			String language = props.getString("language");
			if (language != null && !"und".equals(language)) {
				// mkvtools use iso3
				stream.setLangIso2(YamoriUtils.langToIso2(language));
			}
			
			if (props.has("number") && !props.isNull("number")) {
				stream.setStreamId(props.getInt("number"));
			}
		}
	}
	
	private static void readVideoParameter(JSONObject track, VideoStream stream) {
		JSONObject props = track.getJSONObject("properties");
		if (props != null) {
			String dims = props.optString("pixel_dimensions");
			if (dims != null) {
				Matcher matcher = PATTERN_PIXEL_DIMS.matcher(dims);
				if (matcher.matches()) {
					int width = Integer.parseInt(matcher.group(1));
					int height = Integer.parseInt(matcher.group(2));
					
					stream.setDisplayDimension(new Dimension(width, height));
				}
			}
		}		
	}

	public final static class Version {

		private final static Version INSTANCE = new Version();

		private final int major;
		private final int minor;
		private final int build;
		
		private final String name;
		
		private final boolean installed;
		
		private Version() {
			Pattern pattern = Pattern.compile("mkvmerge v([0-9]+)\\.([0-9]+)\\.([0-9]+)\\s+(.*)");
			
			int _major = 0;
			int _minor = 0;
			int _build = 0;
			String _name = "";
			boolean _installed = false;
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
						_name = matcher.group(4);
						if (_name == null) {
							_name = "";
						} else {
							_name = _name.trim();
						}
					}
				}
				
				if (processBuilder.getExitCode() == 0) {
					_installed = true;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			major = _major;
			minor = _minor;
			build = _build;
			name = _name;
			installed = _installed;
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
		
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return major + "." + minor + "." + build + " " + name;
		}

	}

}
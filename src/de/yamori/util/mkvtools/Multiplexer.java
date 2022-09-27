package de.yamori.util.mkvtools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.Subtitle;
import de.yamori.util.api.VideoStream;
import de.yamori.util.common.ProcessBuilder;
import de.yamori.util.common.ProgressTracker;
import de.yamori.util.common.Task;
import de.yamori.util.common.YamoriUtils;

public class Multiplexer implements Task {
	
	private final static Pattern PATTERN_MKVMERGE_PROGRESS = Pattern.compile("\\:\\ ([0-9]{1,3})\\%");
	
	private final List<InputFile> inputFiles = new LinkedList<>();
	
	private String title;
	private File outputFile;

	public Multiplexer() {
		
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public File getOutputFile() {
		return outputFile;
	}
	
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public void addMovie(File inputFile, Collection<VideoStream> videoStreams, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles) {
		inputFiles.add(new InputFile(inputFile, videoStreams, audioTracks, subtitles));
	}
	
	@Override
	public boolean execute(ProgressTracker tracker) {
		Objects.requireNonNull(outputFile);
		
		// Überhaupt was zu tun?
		if (!inputFiles.stream().anyMatch(i -> i.isAnyStreamSelected())) {
			// nein...
			return false;
		}
		
		List<String> cmd = new ArrayList<>();
		cmd.add("mkvmerge");
		
		// globale Optionen
		cmd.add("-o");
		cmd.add(outputFile.getAbsolutePath());
		
		if (title != null && !title.isEmpty()) {
			cmd.add("--title");
			cmd.add(title);
		}

		StringBuilder trackOrder = new StringBuilder();
		
		int fileIndex = 0;
		Iterator<InputFile> iter = inputFiles.stream().filter(i -> i.isAnyStreamSelected()).iterator();
		while (iter.hasNext()) {
			InputFile inputFile = iter.next();

			Collection<VideoStream> videoStreams = inputFile.getVideoStreams();
			Collection<AudioTrack> audioTracks = inputFile.getAudioTracks();
			Collection<Subtitle> subtitles = inputFile.getSubtitles();

			if (videoStreams != null && !videoStreams.isEmpty()) {
				StringBuilder all = new StringBuilder();
				
				for (VideoStream t : videoStreams) {
					cmd.add("--language");
					cmd.add(t.getId() + ":" + toIso3(t.getLangIso2()));
					cmd.add("--forced-track");
					cmd.add(t.getId() + ":no");
					cmd.add("--display-dimensions");
					
					// TODO: Aspect-Ratio korrekt errechnen. Hier aktuell fest 1:1.78 (=16/9) hinterlegt...:
					// mplayer -vo null -ao null -frames 1 -identify dvd://4 -dvd-device /dev/sr0
					cmd.add(t.getId() + ":1024x576");
					// cmd.add("0:768x576");
					
					if (all.length() > 0) {
						all.append(",");
					}
					all.append(t.getId());
					
					trackOrder.append(",");
					trackOrder.append(fileIndex);
					trackOrder.append(":");
					trackOrder.append(t.getId());
				}
				
				cmd.add("-d");
				cmd.add(all.toString());
			} else {
				cmd.add("-D");
			}
			
			if (audioTracks != null && !audioTracks.isEmpty()) {
				StringBuilder all = new StringBuilder();
	
				for (AudioTrack t : audioTracks) {
					cmd.add("--language");
					cmd.add(t.getId() + ":" + toIso3(t.getLangIso2()));
					cmd.add("--forced-track");
					cmd.add(t.getId() + ":no");
					
					if (all.length() > 0) {
						all.append(",");
					}
					all.append(t.getId());
					
					trackOrder.append(",");
					trackOrder.append(fileIndex);
					trackOrder.append(":");
					trackOrder.append(t.getId());
				}
				
				cmd.add("-a");
				cmd.add(all.toString());
			} else {
				cmd.add("-A");
			}
	
			if (subtitles != null && !subtitles.isEmpty()) {
				StringBuilder all = new StringBuilder();
	
				int index = 0;
				for (Subtitle sub : subtitles) {
					cmd.add("--language");
					cmd.add(index + ":" + toIso3(sub.getLangIso2()));
					cmd.add("--forced-track");
					cmd.add(index + ":no");
	
					if (all.length() > 0) {
						all.append(",");
					}
					all.append(index);
	
					trackOrder.append(",");
					trackOrder.append(fileIndex);
					trackOrder.append(":");
					trackOrder.append(index);
					
					index++;
				}
	
				cmd.add("-s");
				cmd.add(all.toString());
			} else {
				cmd.add("-S");
			}
			
			// keine spurspezifischen Tags aus Quelldatei kopieren
			// keine globalen Tags aus Quelldatei übernehmen
			// keine Kapitel aus Quelldatei übernehmen
			cmd.add("-T");
			cmd.add("--no-global-tags");
			cmd.add("--no-chapters");
			
			cmd.add(inputFile.getInputFile().getAbsolutePath());
			
			fileIndex++;
		}
		
		cmd.add("--track-order");
		// skip leading ','
		cmd.add(trackOrder.substring(1).toString());
		
		ProcessBuilder processBuilder = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
		try {
			tracker.setInfo("Building MKV");
			tracker.setProgress(0);
			processBuilder.execute(row -> {
				System.out.println(row);

				Matcher matcher = PATTERN_MKVMERGE_PROGRESS.matcher(row);
				if (matcher.find()) {
					String percent = matcher.group(1);
					tracker.setProgress(Integer.parseInt(percent));
				}
			});
			
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	// ISO 639-2
	private final static String toIso3(String iso2) {
		String iso3b = YamoriUtils.langToIso3B(iso2);
		if (iso3b != null) {
			return iso3b;
		}

		// undefined:
		return "und";
	}
	
	private final static class InputFile {

		private final File inputFile;
		private final Collection<VideoStream> videoStreams;
		private final Collection<AudioTrack> audioTracks;
		private final Collection<Subtitle> subtitles;

		public InputFile(File inputFile, Collection<VideoStream> videoStreams, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles) {
			this.inputFile = inputFile;
			this.videoStreams = videoStreams;
			this.audioTracks = audioTracks;
			this.subtitles = subtitles;
		}

		public File getInputFile() {
			return inputFile;
		}
		
		public Collection<VideoStream> getVideoStreams() {
			return videoStreams;
		}

		public Collection<AudioTrack> getAudioTracks() {
			return audioTracks;
		}

		public Collection<Subtitle> getSubtitles() {
			return subtitles;
		}
		
		private boolean isAnyStreamSelected() {
			return (videoStreams != null && !videoStreams.isEmpty())
					|| (audioTracks != null && !audioTracks.isEmpty())
					|| (subtitles != null && !subtitles.isEmpty());
		}

	}

}
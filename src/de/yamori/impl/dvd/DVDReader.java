package de.yamori.impl.dvd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.yamori.api.Device;
import de.yamori.api.Disc;
import de.yamori.api.ReaderBackend;
import de.yamori.api.Title;
import de.yamori.config.Config;
import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.Subtitle;
import de.yamori.util.api.VideoStream;
import de.yamori.util.common.Job;
import de.yamori.util.common.ProcessBuilder;
import de.yamori.util.common.ProcessBuilder.OutputProcessor;
import de.yamori.util.common.ProgressTracker;
import de.yamori.util.common.YamoriUtils;
import de.yamori.util.mkvtools.Multiplexer;

public class DVDReader implements ReaderBackend {
	
	private final static Pattern PATTERN_MPLAYER_PROGRESS = Pattern.compile("\\(\\~([0-9]{1,3})\\.[0-9]{1}\\%\\)");
	private final static Pattern PATTERN_MENCODER_PROGRESS = Pattern.compile("\\(([0-9]{1,3})\\%\\)");
//	private final static Pattern PATTERN_MKVMERGE_PROGRESS = Pattern.compile("\\:\\ ([0-9]{1,3})\\%");
	
	private final Device device;

	public DVDReader(File isoImage) {
		this(new Device(isoImage.getAbsolutePath()));
	}

	public DVDReader(Device device) {
		this.device = device;
	}

	@Override
	public Disc getStructure() {
		
		ProcessBuilder processBuilder = new ProcessBuilder(new String[] {
				
				"lsdvd",
				"-as",		// with (a)udio and (s)ubtitle information
				device.getPath()
				
		});
		
		
		
		try {
			LSDVD lsdvd = new LSDVD();

			processBuilder.execute(lsdvd);

			return lsdvd.getDisc();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void copyTo(Title title, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles, String fileName, ProgressTracker tracker) {
		// String tmp = config.getTmp() + "/yamori" + title.getId() + "_" + ((int)(Math.random() * 1000)) + ".vob";
		
//		String tmp = config.getTmp() + "/yamori" + title.getId() + "_" + ((int)(Math.random() * 1000)) + ".vob";
		File tmpFolder = new File(Config.getInstance().getTmp(), "yamori" + title.getId() + "_" + ((int)(Math.random() * 1000)));
		if (!tmpFolder.mkdir()) {
			throw new RuntimeException();
		}

		String tmp = tmpFolder + "/tmp.vob";
		
		Job job = new Job();
		job.addTask(p -> this.dumpStream(title, tmp, p));
	
		String subs;
		List<Subtitle> subsOrdered = new LinkedList<Subtitle>();
		if (!subtitles.isEmpty()) {
			subs = tmpFolder + "/subs";
			
			// preserve original order of tracks:
			for (Subtitle sub : title.getSubtitles()) {
				if (subtitles.contains(sub)) {
					subsOrdered.add(sub);
					job.addTask(p -> this.extractSubtitle(sub, subsOrdered.size() - 1, subs, tmp, p), 0.2d);
				}
			}
		} else {
			subs = null;
		}

		job.addTask(p -> this.mkvMerge(title, audioTracks, subsOrdered, fileName, tmp, subs, p));
		
		// and execute:
		job.execute(tracker);

		
		new File(tmp).delete();
		if (subs != null) {
			new File(subs + ".idx").delete();
			new File(subs + ".sub").delete();
		}
		tmpFolder.delete();
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
	
	private boolean dumpStream(Title title, String tmp, ProgressTracker tracker) {
		ProcessBuilder processBuilder = new ProcessBuilder(new String[] {
				
				"mplayer",
				"dvd://" + title.getId(),
				"-dvd-device",
				device.getPath(),
//				"-v",
				"-dumpstream",
				"-dumpfile",
				tmp
				
		});
		try {
			tracker.setInfo("Dumping stream");
			processBuilder.execute(row -> {
				System.out.println(row);

				Matcher matcher = PATTERN_MPLAYER_PROGRESS.matcher(row);
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
	
	private boolean extractSubtitle(Subtitle subtitle, int index, String subFile, String tmp, ProgressTracker tracker) {
		// mencoder tmp.vob -nosound -ovc frameno -o /dev/null -vobsubout subs -vobsuboutindex 0 -sid 0
		ProcessBuilder processBuilder = new ProcessBuilder(new String[] {
				
				"mencoder",
				tmp,
				"-nosound",
				"-ovc",
				"frameno",
				"-o",
				"/dev/null",
				"-vobsubout",
				subFile,
				"-vobsuboutindex",
				Integer.toString(index),
				"-sid",
				Integer.toString(subtitle.getId() - 1)	// 0-basiert
				
		});
		try {
			tracker.setInfo("Extracting subtitle (" + subtitle.getLangIso2() + " " + subtitle.getId() + ")");
			processBuilder.execute(row -> {
				System.out.println(row);

				Matcher matcher = PATTERN_MENCODER_PROGRESS.matcher(row);
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
	
	private boolean mkvMerge(Title title, Collection<AudioTrack> audioTracks, Collection<Subtitle> subtitles, String fileName, String tmp, String subs, ProgressTracker tracker) {
		
		Multiplexer multiplexer = new Multiplexer();
		
		VideoStream videoStream = new VideoStream();
		videoStream.setLangIso2(null);	// undefined
		videoStream.setId(0);			// bei dvd immer 0

		multiplexer.addMovie(new File(tmp),
								Collections.singletonList(videoStream),
								// preserve order of audio-tracks:
								title.getAudioTracks().stream()
														.filter(t -> audioTracks != null && audioTracks.contains(t))
														.collect(Collectors.toList()),
								null);
		
		if (subs != null) {
			multiplexer.addMovie(new File(subs), null, null, subtitles);
		}
		
		return multiplexer.execute(tracker);
		
		/*
		"mkvmerge"
		-o "/home/karsten/yamori3_503.mkv"
		"--forced-track" "0:no" "--display-dimensions" "0:720x576"
		"--language" "1:eng" "--forced-track" "1:no"
		"--language" "2:ger" "--forced-track" "2:no"
		"-a" "1,2"
		"-d" "0"
		"-S"
		"-T"
		"--no-global-tags"
		"--no-chapters"
		"/home/karsten/yamori3_503.vob"
		"--track-order" "0:0,0:1,0:2"
		*/
		
		/*
		List<String> cmd = new ArrayList<>();
		cmd.add("mkvmerge");
		cmd.add("-o");
		cmd.add(fileName);
		cmd.add("--forced-track");
		cmd.add("0:no");
		cmd.add("--display-dimensions");
		// TODO: Aspect-Ratio korrekt errechnen. Hier aktuell fest 1:1.78 (=16/9) hinterlegt...:
		// mplayer -vo null -ao null -frames 1 -identify dvd://4 -dvd-device /dev/sr0
		cmd.add("0:1024x576");
//		cmd.add("0:768x576");
		
		StringBuilder trackOrder = new StringBuilder();
		trackOrder.append("0:0");
		
		if (audioTracks != null && !audioTracks.isEmpty()) {
			StringBuilder all = new StringBuilder();

			// for (AudioTrack t : audioTracks) {
			// preserve original order of tracks:
			for (AudioTrack t : title.getAudioTracks()) {
				if (audioTracks.contains(t)) {
					cmd.add("--language");
					cmd.add(t.getId() + ":" + toIso3(t.getLangIso2()));
					cmd.add("--forced-track");
					cmd.add(t.getId() + ":no");
					
					if (all.length() > 0) {
						all.append(",");
					}
					all.append(t.getId());
					
					trackOrder.append(",");
					trackOrder.append("0:");
					trackOrder.append(t.getId());
				}
			}
			
			cmd.add("-a");
			cmd.add(all.toString());
		}
		
		cmd.add("-d");
		cmd.add("0");
		cmd.add("-S");
		cmd.add("-T");
		cmd.add("--no-global-tags");
		cmd.add("--no-chapters");
		
		if (title.getDescription() != null && !title.getDescription().isEmpty()) {
			cmd.add("--title");
			cmd.add(title.getDescription());
		}
		
		cmd.add(tmp);
		
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
				trackOrder.append("1:");
				trackOrder.append(index);
				
				index++;
			}

			cmd.add("-s");
			cmd.add(all.toString());

			cmd.add("-D");
			cmd.add("-A");
			cmd.add("-T");
			cmd.add("--no-global-tags");
			cmd.add("--no-chapters");

			cmd.add(subs + ".idx");
		}
		
		cmd.add("--track-order");
		cmd.add(trackOrder.toString());
		
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
		*/
	}
	
	private final static class LSDVD implements OutputProcessor {
		
		private final List<Title> titles = new ArrayList<>();

		private String discTitle;
		private Title last;

		@Override
		public void process(String line) {
			int audio;
			int subtitle;
			if (line.startsWith("Disc Title: ")) {
				discTitle = line.substring("Disc Title: ".length());
			} else if (line.startsWith("Title:")) {
				last = new Title();
				last.setId(Integer.parseInt(line.substring(7, line.indexOf(','))));
				
				int ids = line.indexOf("Length: ");
				if (ids != -1) {
					ids += 8;
					
					last.setDuration(line.substring(ids, ids + 12));
				}
				
				titles.add(last);
			} else if ((audio = line.indexOf("Audio: ")) != -1) {
				AudioTrack track = new AudioTrack();
				track.setId(Integer.parseInt(line.substring(audio + 7, line.indexOf(','))));
				
				int ids = line.indexOf("Language: ");
				if (ids != -1) {
					ids += 10;
					
					track.setLangIso2(line.substring(ids, ids + 2));
				}
				
				ids = line.indexOf("Stream id: ");
				if (ids != -1) {
					ids += 11;
					
					String sId = line.substring(ids);
					if (sId.startsWith("0x")) {
						sId = sId.substring(2);
					}
					
					track.setStreamId(Integer.parseInt(sId, 16));
				}
				
				last.getAudioTracks().add(track);
			} else if ((subtitle = line.indexOf("Subtitle: ")) != -1) {
				Subtitle sub = new Subtitle();
				sub.setId(Integer.parseInt(line.substring(subtitle + 10, line.indexOf(','))));
				
				int ids = line.indexOf("Language: ");
				if (ids != -1) {
					ids += 10;
					
					sub.setLangIso2(line.substring(ids, ids + 2));
				}
				
				ids = line.indexOf("Stream id: ");
				if (ids != -1) {
					ids += 11;
					
					String sId = line.substring(ids).trim();
					if (sId.startsWith("0x")) {
						sId = sId.substring(2);
					}
					if (sId.endsWith(",")) {
						sId = sId.substring(0, sId.length() - 1);
					}
					
					sub.setStreamId(Integer.parseInt(sId, 16));
				}
				
				last.getSubtitles().add(sub);
			}
		}
		
		public Disc getDisc() {
			Disc disc = new Disc(discTitle);
			disc.getTitles().addAll(titles);
			
			return disc;
		}
		
	}

}
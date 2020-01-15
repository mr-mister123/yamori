package de.yamori.main;

import java.util.Arrays;

import de.yamori.api.AudioTrack;
import de.yamori.api.Disc;
import de.yamori.api.ReaderBackend;
import de.yamori.api.Title;
import de.yamori.config.Config;
import de.yamori.impl.dvd.DVDReader;

public class Test {

	public static void main(String[] args) {
		testStoreConfig();
	}
	
	private static void testStoreConfig() {
		Config.storeConfig(Config.getInstance());
	}
	
	private static void testCopy() {
		Config.getInstance().setTmp("/home/karsten");

		ReaderBackend dvd = new DVDReader();
		
		Disc disc = dvd.getStructure();
		if (disc != null) {
			System.out.println(disc.getDiscTitle());
			
			if (disc.getTitles() != null) {
				for (Title t : disc.getTitles()) {
					System.out.println(t.getId() + " (" + t.getDuration() + ") - " + t.getDescription());
					
					for (AudioTrack a : t.getAudioTracks()) {
						System.out.println("  Audiotrack " + a.getId() + " : " + a.getLangIso2() + " [" + a.getStreamId() + "]");
					}
				}
				
				// Copy title 3:
				
				Title title3 = disc.getTitles().get(2);
				title3.setDescription("Toller test 123");
				AudioTrack a1 = title3.getAudioTracks().get(0);
				AudioTrack a2 = title3.getAudioTracks().get(1);
				dvd.copyTo(title3, Arrays.asList(a1, a2) , "/home/karsten/title3.mkv", null);
			}
		}
	}

}
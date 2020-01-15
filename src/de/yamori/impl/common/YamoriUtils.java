package de.yamori.impl.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.yamori.api.AudioTrack;
import de.yamori.api.Title;

public class YamoriUtils {

	private YamoriUtils() {
		// hide me
	}
	
	public static Collection<AudioTrack> getDefaultAudioTracks(Title title) {
		// TODO
		
		Set<AudioTrack> _default = new HashSet<>();
		Set<String> codes = new HashSet<>();
		for (AudioTrack t : title.getAudioTracks()) {
			if ("de".equals(t.getLangIso2())
					|| "en".equals(t.getLangIso2())) {
				if (!codes.contains(t.getLangIso2())) {
					_default.add(t);
					codes.add(t.getLangIso2());
				}
			}
		}
		
		return _default;
	}

}
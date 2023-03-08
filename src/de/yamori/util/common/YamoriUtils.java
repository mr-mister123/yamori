package de.yamori.util.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.yamori.api.Title;
import de.yamori.util.api.AudioTrack;
import de.yamori.util.api.Subtitle;

public class YamoriUtils {
	
	private final static Map<String, String> iso3BToIso2 = new HashMap<>();
	private final static Map<String, String> iso2ToIso3B = new HashMap<>();

	private YamoriUtils() {
		// hide me
	}
	
	static {
		iso3BToIso2.put("aar", "aa");
		iso3BToIso2.put("abk", "ab");
		iso3BToIso2.put("afr", "af");
		iso3BToIso2.put("aka", "ak");
		iso3BToIso2.put("alb", "sq");
		iso3BToIso2.put("amh", "am");
		iso3BToIso2.put("ara", "ar");
		iso3BToIso2.put("arg", "an");
		iso3BToIso2.put("arm", "hy");
		iso3BToIso2.put("asm", "as");
		iso3BToIso2.put("ava", "av");
		iso3BToIso2.put("ave", "ae");
		iso3BToIso2.put("aym", "ay");
		iso3BToIso2.put("aze", "az");
		iso3BToIso2.put("bak", "ba");
		iso3BToIso2.put("bam", "bm");
		iso3BToIso2.put("baq", "eu");
		iso3BToIso2.put("bel", "be");
		iso3BToIso2.put("ben", "bn");
		iso3BToIso2.put("bih", "bh");
		iso3BToIso2.put("bis", "bi");
		iso3BToIso2.put("bos", "bs");
		iso3BToIso2.put("bre", "br");
		iso3BToIso2.put("bul", "bg");
		iso3BToIso2.put("bur", "my");
		iso3BToIso2.put("cat", "ca");
		iso3BToIso2.put("cha", "ch");
		iso3BToIso2.put("che", "ce");
		iso3BToIso2.put("chi", "zh");
		iso3BToIso2.put("chu", "cu");
		iso3BToIso2.put("chv", "cv");
		iso3BToIso2.put("cor", "kw");
		iso3BToIso2.put("cos", "co");
		iso3BToIso2.put("cre", "cr");
		iso3BToIso2.put("cze", "cs");
		iso3BToIso2.put("dan", "da");
		iso3BToIso2.put("div", "dv");
		iso3BToIso2.put("dut", "nl");
		iso3BToIso2.put("dzo", "dz");
		iso3BToIso2.put("eng", "en");
		iso3BToIso2.put("epo", "eo");
		iso3BToIso2.put("est", "et");
		iso3BToIso2.put("ewe", "ee");
		iso3BToIso2.put("fao", "fo");
		iso3BToIso2.put("fij", "fj");
		iso3BToIso2.put("fin", "fi");
		iso3BToIso2.put("fre", "fr");
		iso3BToIso2.put("fry", "fy");
		iso3BToIso2.put("ful", "ff");
		iso3BToIso2.put("geo", "ka");
		iso3BToIso2.put("ger", "de");
		iso3BToIso2.put("gla", "gd");
		iso3BToIso2.put("gle", "ga");
		iso3BToIso2.put("glg", "gl");
		iso3BToIso2.put("glv", "gv");
		iso3BToIso2.put("gre", "el");
		iso3BToIso2.put("grn", "gn");
		iso3BToIso2.put("guj", "gu");
		iso3BToIso2.put("hat", "ht");
		iso3BToIso2.put("hau", "ha");
		iso3BToIso2.put("heb", "he");
		iso3BToIso2.put("her", "hz");
		iso3BToIso2.put("hin", "hi");
		iso3BToIso2.put("hmo", "ho");
		iso3BToIso2.put("hrv", "hr");
		iso3BToIso2.put("hun", "hu");
		iso3BToIso2.put("ibo", "ig");
		iso3BToIso2.put("ice", "is");
		iso3BToIso2.put("ido", "io");
		iso3BToIso2.put("iii", "ii");
		iso3BToIso2.put("iku", "iu");
		iso3BToIso2.put("ile", "ie");
		iso3BToIso2.put("ina", "ia");
		iso3BToIso2.put("ind", "id");
		iso3BToIso2.put("ipk", "ik");
		iso3BToIso2.put("ita", "it");
		iso3BToIso2.put("jav", "jv");
		iso3BToIso2.put("jpn", "ja");
		iso3BToIso2.put("kal", "kl");
		iso3BToIso2.put("kan", "kn");
		iso3BToIso2.put("kas", "ks");
		iso3BToIso2.put("kau", "kr");
		iso3BToIso2.put("kaz", "kk");
		iso3BToIso2.put("khm", "km");
		iso3BToIso2.put("kik", "ki");
		iso3BToIso2.put("kin", "rw");
		iso3BToIso2.put("kir", "ky");
		iso3BToIso2.put("kom", "kv");
		iso3BToIso2.put("kon", "kg");
		iso3BToIso2.put("kor", "ko");
		iso3BToIso2.put("kua", "kj");
		iso3BToIso2.put("kur", "ku");
		iso3BToIso2.put("lao", "lo");
		iso3BToIso2.put("lat", "la");
		iso3BToIso2.put("lav", "lv");
		iso3BToIso2.put("lim", "li");
		iso3BToIso2.put("lin", "ln");
		iso3BToIso2.put("lit", "lt");
		iso3BToIso2.put("ltz", "lb");
		iso3BToIso2.put("lub", "lu");
		iso3BToIso2.put("lug", "lg");
		iso3BToIso2.put("mac", "mk");
		iso3BToIso2.put("mah", "mh");
		iso3BToIso2.put("mal", "ml");
		iso3BToIso2.put("mao", "mi");
		iso3BToIso2.put("mar", "mr");
		iso3BToIso2.put("may", "ms");
		iso3BToIso2.put("mlg", "mg");
		iso3BToIso2.put("mlt", "mt");
		iso3BToIso2.put("mon", "mn");
		iso3BToIso2.put("nau", "na");
		iso3BToIso2.put("nav", "nv");
		iso3BToIso2.put("nbl", "nr");
		iso3BToIso2.put("nde", "nd");
		iso3BToIso2.put("ndo", "ng");
		iso3BToIso2.put("nep", "ne");
		iso3BToIso2.put("nno", "nn");
		iso3BToIso2.put("nob", "nb");
		iso3BToIso2.put("nor", "no");
		iso3BToIso2.put("nya", "ny");
		iso3BToIso2.put("oci", "oc");
		iso3BToIso2.put("oji", "oj");
		iso3BToIso2.put("ori", "or");
		iso3BToIso2.put("orm", "om");
		iso3BToIso2.put("oss", "os");
		iso3BToIso2.put("pan", "pa");
		iso3BToIso2.put("per", "fa");
		iso3BToIso2.put("pli", "pi");
		iso3BToIso2.put("pol", "pl");
		iso3BToIso2.put("por", "pt");
		iso3BToIso2.put("pus", "ps");
		iso3BToIso2.put("que", "qu");
		iso3BToIso2.put("roh", "rm");
		iso3BToIso2.put("rum", "ro");
		iso3BToIso2.put("run", "rn");
		iso3BToIso2.put("rus", "ru");
		iso3BToIso2.put("sag", "sg");
		iso3BToIso2.put("san", "sa");
		iso3BToIso2.put("sin", "si");
		iso3BToIso2.put("slo", "sk");
		iso3BToIso2.put("slv", "sl");
		iso3BToIso2.put("sme", "se");
		iso3BToIso2.put("smo", "sm");
		iso3BToIso2.put("sna", "sn");
		iso3BToIso2.put("snd", "sd");
		iso3BToIso2.put("som", "so");
		iso3BToIso2.put("sot", "st");
		iso3BToIso2.put("spa", "es");
		iso3BToIso2.put("srd", "sc");
		iso3BToIso2.put("srp", "sr");
		iso3BToIso2.put("ssw", "ss");
		iso3BToIso2.put("sun", "su");
		iso3BToIso2.put("swa", "sw");
		iso3BToIso2.put("swe", "sv");
		iso3BToIso2.put("tah", "ty");
		iso3BToIso2.put("tam", "ta");
		iso3BToIso2.put("tat", "tt");
		iso3BToIso2.put("tel", "te");
		iso3BToIso2.put("tgk", "tg");
		iso3BToIso2.put("tgl", "tl");
		iso3BToIso2.put("tha", "th");
		iso3BToIso2.put("tib", "bo");
		iso3BToIso2.put("tir", "ti");
		iso3BToIso2.put("ton", "to");
		iso3BToIso2.put("tsn", "tn");
		iso3BToIso2.put("tso", "ts");
		iso3BToIso2.put("tuk", "tk");
		iso3BToIso2.put("tur", "tr");
		iso3BToIso2.put("twi", "tw");
		iso3BToIso2.put("uig", "ug");
		iso3BToIso2.put("ukr", "uk");
		iso3BToIso2.put("urd", "ur");
		iso3BToIso2.put("uzb", "uz");
		iso3BToIso2.put("ven", "ve");
		iso3BToIso2.put("vie", "vi");
		iso3BToIso2.put("vol", "vo");
		iso3BToIso2.put("wel", "cy");
		iso3BToIso2.put("wln", "wa");
		iso3BToIso2.put("wol", "wo");
		iso3BToIso2.put("xho", "xh");
		iso3BToIso2.put("yid", "yi");
		iso3BToIso2.put("yor", "yo");
		iso3BToIso2.put("zha", "za");
		iso3BToIso2.put("zul", "zu");
		
		for (Entry<String, String> e : iso3BToIso2.entrySet()) {
			iso2ToIso3B.put(e.getValue(), e.getKey());
		}
	}
	
	public static Collection<AudioTrack> getDefaultAudioTracks(Title title) {
		// TODO from konfig
		
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
		if (_default.isEmpty() && title.getAudioTracks().size() == 1) {
			_default.add(title.getAudioTracks().get(0));
		}
		
		return _default;
	}
	
	public static Collection<Subtitle> getDefaultSubtitles(Title title) {
		// TODO from konfig
		
		Set<Subtitle> _default = new HashSet<>();
		Set<String> codes = new HashSet<>();
		for (Subtitle t : title.getSubtitles()) {
			if ("de".equals(t.getLangIso2())
					|| "en".equals(t.getLangIso2())) {
				if (!codes.contains(t.getLangIso2())) {
					_default.add(t);
					codes.add(t.getLangIso2());
				}
			}
		}
		if (_default.isEmpty() && title.getSubtitles().size() == 1) {
			_default.add(title.getSubtitles().get(0));
		}
		
		return _default;
	}

	public static String langToIso3B(String iso2) {
		return iso2ToIso3B.get(iso2);
	}
	
	public static String langToIso2(String iso3B) {
		return iso3BToIso2.get(iso3B);
	}

}
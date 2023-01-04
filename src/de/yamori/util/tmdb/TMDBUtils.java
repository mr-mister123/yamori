package de.yamori.util.tmdb;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import de.yamori.config.Config;
import de.yamori.util.tmdb.api.TMDBEpisode;
import de.yamori.util.tmdb.api.TMDBSeries;

public class TMDBUtils {

	private final static String API_KEY = "a5834e4d0f1aac25103bcba30467ff85";

	private final static Pattern FIRST_AIR_DATE = Pattern.compile("\\(([0-9]{4})\\)");

	private final static CacheKeyType<List<TMDBSeries>> TYPE_SERIES_LOOKUP = new CacheKeyType<>("SL");
	private final static CacheKeyType<List<TMDBEpisode>> TYPE_SERIES = new CacheKeyType<>("S");
	private final static CacheKeyType<List<TMDBEpisode>> TYPE_EPISODES = new CacheKeyType<>("E");

	private final static File CACHE_DIR = new File(Config.getInstance().getConfigFolder(), "tmdb");

	// https://api.themoviedb.org/3/search/tv?api_key=a5834e4d0f1aac25103bcba30467ff85&language=de-DE&query=blutsbande&page=1&include_adult=false

	private final static Cache cache = new Cache();

	private TMDBUtils() {
		// hide me
	}

	public static List<TMDBSeries> querySeries(String query) {
		if (query == null) {
			return Collections.emptyList();
		}

		query = query.trim();
		if (query.isEmpty()) {
			return Collections.emptyList();
		}

		// etwas normalisieren
		query = query.toLowerCase();
		query = query.replaceAll("\\s+", " ");

		// first air date?!
		Matcher m = FIRST_AIR_DATE.matcher(query);
		String firstAirDate = null;
		if (m.find()) {
			do {
				// wir suchen das letzte vorkommen...:
				query = query.substring(0, m.start());
				firstAirDate = m.group(1);
			} while (m.find());

			query = query.trim();
		}

		final String _query = query;
		final String _firstAirDate = firstAirDate;

		String key = query;
		if (firstAirDate != null) {
			key += "__" + firstAirDate;
		}

		return new ArrayList<>(queryOrFromCache(TYPE_SERIES_LOOKUP, key, () -> querySeriesTMDB(_query, _firstAirDate), TMDBUtils::convertSeries));
	}

	public static List<TMDBEpisode> queryEpisodes(TMDBSeries series) {
		if (series == null) {
			return Collections.emptyList();
		}

		return new ArrayList<>(queryOrFromCache(TYPE_SERIES, Integer.toString(series.getId()), () -> queryEpisodesTMDB(series), json -> convertEpisodes(series, json)));
	}

	private static List<TMDBEpisode> querySeasonEpisodes(TMDBSeries series, int season) {
		if (series == null) {
			return null;
		}

		return queryOrFromCache(TYPE_EPISODES, Integer.toString(series.getId()) + "_" + season, () -> querySeasonEpisodesTMDB(series, season), TMDBUtils::convertSeasonEpisodes);
	}

	private static JSONObject querySeriesTMDB(String query, String firstAirDate) {
		StringBuilder url = new StringBuilder("/3/search/tv?query=");
		url.append(encodeURI(query));
		if (firstAirDate != null) {
			url.append("&first_air_date_year=");
			url.append(firstAirDate);
		}

		return queryTMDB(url.toString());
	}

	private static List<TMDBSeries> convertSeries(JSONObject json) {
		List<TMDBSeries> list = new LinkedList<>();

		JSONArray array = json.optJSONArray("results");
		if (array != null) {

			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			for (int i = 0; i < array.length(); i++) {
				JSONObject s = array.optJSONObject(i);
				if (s != null) {
					TMDBSeriesImpl series = new TMDBSeriesImpl(s.getInt("id"));
					series.setName(s.optString("name"));
					series.setOriginalName(s.optString("original_name"));
					series.setOriginalLangIso2(s.optString("original_language"));

					String firstAirDate = s.optString("first_air_date");
					if (firstAirDate != null) {
						try {
							series.setFirstAirDate(sdf.parse(firstAirDate));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}

					list.add(series);
				}
			}
		}

		return list;
	}

	private static JSONObject queryEpisodesTMDB(TMDBSeries series) {
		StringBuilder url = new StringBuilder("/3/tv/");
		url.append(series.getId());

		return queryTMDB(url.toString());
	}

	private static List<TMDBEpisode> convertEpisodes(TMDBSeries series, JSONObject json) {
		List<TMDBEpisode> list = new LinkedList<>();

		JSONArray array = json.optJSONArray("seasons");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject s = array.optJSONObject(i);
				if (s != null) {
					int season = s.getInt("season_number");

					List<TMDBEpisode> episodes = querySeasonEpisodes(series, season);
					if (episodes != null) {
						list.addAll(episodes);
					}
				}
			}
		}

		return list;
	}

	private static JSONObject querySeasonEpisodesTMDB(TMDBSeries series, int season) {
		StringBuilder url = new StringBuilder("/3/tv/");
		url.append(series.getId())
			.append("/season/")
			.append(season);

		return queryTMDB(url.toString());
	}

	private static List<TMDBEpisode> convertSeasonEpisodes(JSONObject json) {
		List<TMDBEpisode> list = new LinkedList<>();

		JSONArray array = json.optJSONArray("episodes");
		if (array != null) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject s = array.optJSONObject(i);
				if (s != null) {
					TMDBEpisodeImpl episode = new TMDBEpisodeImpl(s.getInt("id"));
					episode.setName(s.optString("name"));
					episode.setEpisode(s.optInt("episode_number"));
					episode.setSeason(s.optInt("season_number"));
					
					list.add(episode);
				}
			}
		}

		return list;
	}

	private static String encodeURI(String string) {
		String ret = "";

		if (string != null && !string.isEmpty()) {
			try {
				String encodedString = URLEncoder.encode(string.trim(), "UTF-8");
				ret = encodedString.replaceAll("\\+", "%20");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private static <T> T queryOrFromCache(CacheKeyType<T> type, String key, Supplier<JSONObject> queryFunction, Function<JSONObject, T> converterFunction) {
		// Haben wir schon was im cache?
		Optional<T> o = cache.get(type, key);
		if (o != null) {
			return o.orElse(null);
		}

		// nein. Haben wir es im Filesystem?
		// check file-system:
		o = Optional.ofNullable(fromFileSystemCache(type, key)).map(converterFunction);
		if (o.isPresent()) {
			// und in den cache...:
			cache.put(type, key, o);
			
			return o.get();
		}

		// nein. Online anfragen:
		Optional<JSONObject> json = Optional.ofNullable(queryFunction.get());
		if (json.isPresent()) {
			// und in den Filesystem-cache:
			toFileSystemCache(type, key, json.get());
		}

		o = json.map(converterFunction);

		// und in den cache...:
		cache.put(type, key, o);

		return o.orElse(null);
	}

	private static JSONObject queryTMDB(String url) {

		// complete url..:
		StringBuilder b = new StringBuilder("https://api.themoviedb.org").append(url);

		if (url.contains("?")) {
			b.append("&");
		} else {
			b.append("?");
		}

		b.append("api_key=").append(API_KEY).append("&language=").append("de-DE");

		url = b.toString();
		System.out.println(url);

		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();
			con.setInstanceFollowRedirects(false);
			con.setConnectTimeout(10000); // 5 sek max
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
				try (InputStream in = con.getInputStream(); InputStreamReader reader = new InputStreamReader(in, "UTF-8")) {

					StringBuilder stringBuilder = new StringBuilder();
					char[] buff = new char[1024];
					int len;
					while ((len = reader.read(buff)) != -1) {
						stringBuilder.append(buff, 0, len);
					}

					return new JSONObject(stringBuilder.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private synchronized static JSONObject fromFileSystemCache(CacheKeyType<?> type, String key) {
		String fileName = getCacheFileName(type, key);
		
		File f = new File(CACHE_DIR, fileName);
		if (f.exists() && f.isFile() && f.canRead()) {
			try {
				return new JSONObject(new String(Files.readAllBytes(f.toPath())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	private synchronized static void toFileSystemCache(CacheKeyType<?> type, String key, JSONObject json) {
		boolean cacheDirExists = CACHE_DIR.exists();
		if (!cacheDirExists) {
			cacheDirExists = CACHE_DIR.mkdirs();
		}
		
		if (cacheDirExists) {
			String fileName = getCacheFileName(type, key);
			
			String tmpfileName = new StringBuilder(fileName)
										.append(".")
										.append((int)(Math.random() * 1000.d))
										.append(".tmp")
										.toString();
			
			File f = new File(CACHE_DIR, tmpfileName);
			
			try {
				Files.write(f.toPath(), json.toString(2).getBytes());
				
				File dest = new File(CACHE_DIR, fileName);
				
				Files.move(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static String getCacheFileName(CacheKeyType<?> type, String key) {
		return new StringBuilder()
				.append(type.getId())
				.append("_")
				.append(key.replaceAll("[^a-zA-Z0-9_-]+", "_"))
				.append("_")
				.append(Integer.toHexString(Math.abs(key.hashCode())))
				.append(".json")
				.toString();
	}

	private final static class CacheKey<T> {

		private final CacheKeyType<T> type;
		private final String key;

		private CacheKey(CacheKeyType<T> type, String key) {
			this.type = type;
			this.key = key;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey<?> other = (CacheKey<?>) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

	}

	private final static class CacheKeyType<T> {
		
		private final static Set<String> ids = new HashSet<>();
		
		private final String id;

		private CacheKeyType(String id) {
			if (!ids.add(id)) {
				throw new IllegalArgumentException("Duplicate CacheKeyType-Id " + id);
			}

			this.id = id;
		}
		
		public String getId() {
			return id;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this;
		}

	}

	private final static class Cache {

		private final ConcurrentMap<CacheKey<?>, Optional<?>> cache = new ConcurrentHashMap<>();

		@SuppressWarnings("unchecked")
		public <T> Optional<T> get(CacheKeyType<T> type, String key) {
			return (Optional<T>) cache.get(new CacheKey<>(type, key));
		}

		public <T> void put(CacheKeyType<T> type, String key, Optional<T> value) {
			cache.put(new CacheKey<T>(type, key), value);
		}

	}

	private final static class TMDBSeriesImpl extends TMDBSeries {

		public TMDBSeriesImpl(int id) {
			super(id);
		}

		// make visible:

		@Override
		protected void setName(String name) {
			super.setName(name);
		}

		@Override
		protected void setOriginalName(String originalName) {
			super.setOriginalName(originalName);
		}

		@Override
		protected void setOriginalLangIso2(String originalLangIso2) {
			super.setOriginalLangIso2(originalLangIso2);
		}

		@Override
		protected void setFirstAirDate(Date firstAirDate) {
			super.setFirstAirDate(firstAirDate);
		}

	}
	
	private final static class TMDBEpisodeImpl extends TMDBEpisode {

		public TMDBEpisodeImpl(int id) {
			super(id);
		}
		
		// make visible:
		
		@Override
		protected void setName(String name) {
			super.setName(name);
		}
		
		@Override
		protected void setSeason(int season) {
			super.setSeason(season);
		}
		
		@Override
		protected void setEpisode(int episode) {
			super.setEpisode(episode);
		}

	}

	public static void main(String[] args) {
		List<TMDBSeries> list = TMDBUtils.querySeries("Blutsbande (2014)");
		for (TMDBSeries s : list) {
			System.out.println(s.getId() + " / " + s.getName());

			List<TMDBEpisode> episodes = TMDBUtils.queryEpisodes(s);
			
			for (TMDBEpisode e : episodes) {
				System.out.println("S" + e.getSeason() + "E" + e.getEpisode() + "- " + e.getName());
			}
		}

		System.out.println("-----------");

		// TMDBUtils.querySeries("Blutsbande");
	}

}
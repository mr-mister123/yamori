package de.yamori.util.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ProcessBuilder {
	
	private final String[] cmd;
	
	public ProcessBuilder(String[] cmd) {
		this.cmd = cmd;
	}
	
	public int execute(OutputProcessor processor) throws IOException {
		
		Arrays.asList(cmd).stream().forEach(s -> System.out.print(s + " "));
		System.out.println();
		
		Process process = Runtime.getRuntime().exec(cmd);
		if (processor != null) {
			Thread consumeError = new Thread("error consumer") {
			
				@Override
				public void run() {
					/*
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
						String line;
						while ((line = reader.readLine()) != null) {
							System.out.println("[error] " + line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					*/
					
					try (InputStream err = process.getErrorStream()) {
						byte[] buff = new byte[1024];
						while (err.read(buff) != -1) {}
					} catch (Exception e) {}
				}

			};
			consumeError.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					processor.process(line);
				}
			}
		}
		try {
			return process.waitFor();
		} catch (InterruptedException e) {}
		return -999;
	}
	
	public interface OutputProcessor {

		public void process(String line);

	}

}
package de.yamori.impl.common;

import java.io.BufferedReader;
import java.io.IOException;
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
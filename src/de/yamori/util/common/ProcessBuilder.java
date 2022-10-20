package de.yamori.util.common;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ProcessBuilder {
	
	private final String[] cmd;
	
	private int exitCode = -1;
	
	public ProcessBuilder(String[] cmd) {
		this.cmd = cmd;
	}
	
	public String execute() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		_execute(in -> {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		});
		
		return out.toString();
	}

	public int execute(OutputProcessor processor) throws IOException {
		return _execute(in -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
				String line;
				while ((line = reader.readLine()) != null) {
					processor.process(line);
				}
			}
		});
	}
	
	/**
	 * Liefert den ExitCode des zuletzt durchgeführten Aufrufs
	 * 
	 * @return den exitCode oder -1, falls noch kein Aufruf erfolgte
	 */
	public int getExitCode() {
		return exitCode;
	}

	private int _execute(InputStreamHandler stdOutConsumer) throws IOException {
		
		Arrays.asList(cmd).stream().forEach(s -> System.out.print(s + " "));
		System.out.println();
		
		Process process = Runtime.getRuntime().exec(cmd);
		if (stdOutConsumer != null) {
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
			
			try (InputStream in = process.getInputStream()) {
				stdOutConsumer.handle(in);
			}
		}
		try {
			return (exitCode = process.waitFor());
		} catch (InterruptedException e) {}
		return (exitCode = -999);
	}
	
	public interface OutputProcessor {

		public void process(String line);

	}
	
	@FunctionalInterface
	private interface InputStreamHandler {
		
		public void handle(InputStream i) throws IOException;
		
	}

}
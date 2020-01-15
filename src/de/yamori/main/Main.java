package de.yamori.main;

import javax.swing.SwingUtilities;

import de.yamori.config.Config;
import de.yamori.gui.MainFrame;

public class Main {
	
	private Main() {
		
	}
	
	public static void main(String[] args) {
		Config.setInstance(Config.loadConfig());

		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				new MainFrame();
			}

		});
	}

}
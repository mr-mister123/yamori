package de.yamori.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Icons {
	
	private final static Map<String, Icon> icons = new HashMap<>();

	private Icons() {
		// hide me
	}
	
	public static Icon getIcon(String name) {
		Icon icon = icons.get(name);
		
		if (icon == null) {
			icon = new ImageIcon(Thread.currentThread().getContextClassLoader().getResource(name));
			icons.put(name, icon);
		}
		
		return icon;
	}

}
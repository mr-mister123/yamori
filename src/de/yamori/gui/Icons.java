package de.yamori.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
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
	
	public static Icon down() {
		return IconDown.INSTANCE;
	}

	private final static class IconDown implements Icon {
		
		private final static Icon INSTANCE = new IconDown();

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.DARK_GRAY);
			for (int ty = 0; ty < getIconHeight(); ty++) {
				g.drawLine(x + ty, y + ty, x + getIconWidth() - ty, y + ty);
			}
		}

		@Override
		public int getIconWidth() {
			return 9;
		}

		@Override
		public int getIconHeight() {
			return 5;
		}
		
	}

}
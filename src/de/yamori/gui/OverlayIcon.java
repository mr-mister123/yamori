package de.yamori.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JComponent;

public class OverlayIcon implements Icon {
	
	private final Icon icon;
	private final JComponent component;
	
	private String text;

	public OverlayIcon(Icon icon, JComponent component) {
		this.icon = icon;
		this.component = component;
	}
	
	public void setText(String text) {
		this.text = text;
		
		component.repaint();
	}
	
	public String getText() {
		return text;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		icon.paintIcon(c, g, x, y);
		
		if (text != null) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setColor(Color.BLACK);
			int posY = y + getIconHeight()  - 4;
			g2d.drawString(text, x + 2, posY);
		}
	}

	@Override
	public int getIconWidth() {
		return icon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return icon.getIconHeight();
	}

}

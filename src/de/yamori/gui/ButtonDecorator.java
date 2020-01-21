package de.yamori.gui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class ButtonDecorator {

	private ButtonDecorator() {
		// hide me
	}
	
	public static JToggleButton createDropDown(final JButton button, final Supplier<JPopupMenu> supplier) {
		DropDownToggleButton dropDown = new DropDownToggleButton(button);
		PopupMenuListener closeListener = new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(() -> { 
					if (!dropDown.isPressed) {
						dropDown.setSelected(false);
					}
				});
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

		};
		
		dropDown.addActionListener(e -> {
			if (dropDown.isSelected()) {
				JPopupMenu popupMenu = supplier.get();
				// remove --> may be, the popupmenu was recylced...
				popupMenu.removePopupMenuListener(closeListener);
				popupMenu.addPopupMenuListener(closeListener);
				
				// and show:
				SwingUtilities.invokeLater(() -> popupMenu.show(button, 0, button.getHeight()));
			}
		});
		
		return dropDown;
	}
	
	private final static class DropDownToggleButton extends JToggleButton {
		
		private final JButton button;

		private boolean isPressed = false;
		
		private DropDownToggleButton(JButton button) {
			super(Icons.down());
			this.button = button;

			setFocusable(false);
			addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					isPressed = true;
				}

				public void mouseReleased(MouseEvent e) {
					isPressed = false;
				}

			});
		}
		
		@Override
		public Dimension getMinimumSize() {
			return new Dimension(super.getMinimumSize().width, button.getMinimumSize().height);
		}
		
		@Override
		public Dimension getMaximumSize() {
			return new Dimension(super.getMaximumSize().width, button.getMaximumSize().height);
		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(super.getPreferredSize().width, button.getPreferredSize().height);
		}

	}

}
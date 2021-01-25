package de.yamori.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import de.yamori.api.Device;
import de.yamori.api.Disc;
import de.yamori.api.OperatingSystem;
import de.yamori.api.ReaderBackend;
import de.yamori.api.Title;
import de.yamori.config.Config;
import de.yamori.gui.TitleSelectionTable.TitleHolder;
import de.yamori.impl.dvd.DVDReader;
import de.yamori.main.Version;

public class MainFrame extends JFrame {
	
	private final JLabel discTitle;
	private final TitleSelectionTable table;
	private final JButton copyDisc;
	private final JTextField outputPath;
	private final OverlayIcon deviceIcon;
	private final JPopupMenu deviceSelectionMenu;
	
	private Device currentSelected = null;
	private ReaderBackend currentBackend = null;

	public MainFrame() {
		setLayout(new BorderLayout());
		
		setTitle(Version.APPLICATION_NAME + " v" + Version.VERSION);
		setIconImage(((ImageIcon)Icons.getIcon("de/yamori/gui/icons/media-cdrom32.png")).getImage());
		
		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);
		
		JButton openDisc = new JButton();
		deviceIcon = new OverlayIcon(Icons.getIcon("de/yamori/gui/icons/media-cdrom.png"), openDisc);
		openDisc.setIcon(deviceIcon);

		List<Device> devices = OperatingSystem.getCurrent().getDevices();
		if (!devices.isEmpty()) {
			currentSelected = devices.get(0);
			deviceIcon.setText(currentSelected.getPath());
			
			deviceSelectionMenu = new JPopupMenu();
			for (final Device dev : devices) {
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(dev.getPath());
				item.setSelected(dev == currentSelected);
				item.addActionListener(e -> {
					if (item.isSelected()) {
						deviceSelected(item, dev);
					} else {
						// abwählen nicht möglich...
						item.setSelected(true);
					}
				});
				deviceSelectionMenu.add(item);
			}
		} else {
			deviceSelectionMenu = null;
			openDisc.setEnabled(false);
		}

		toolBar.add(openDisc);
		toolBar.add(ButtonDecorator.createDropDown(openDisc, () -> { return deviceSelectionMenu; }));

		openDisc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				BlockingFrame.run(MainFrame.this, new Runnable() {
					
					@Override
					public void run() {
//						Disc disc = DVD.getStructure();
//						SwingUtilities.invokeLater(() -> {
//							table.setDiscStructure(disc);
//							discTitle.setText(disc.getDiscTitle());
//						});
						currentBackend = new DVDReader(currentSelected);
						Disc disc = currentBackend.getStructure();
						SwingUtilities.invokeLater(() -> { discSelected(disc); });
					}

				}, "reading disc", false);
			}

		});
		copyDisc = new JButton(Icons.getIcon("de/yamori/gui/icons/copy-disc.png"));
		copyDisc.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final Collection<TitleHolder> titles = table.getSelectedTitles();
				BlockingFrame.run(MainFrame.this, new Trackable() {
					
					@Override
					public void run(ProgressTracker tracker) {
						int i = 1;
						for (TitleHolder titleHolder : titles) {
							Title title = titleHolder.getTitle();

							int progress = (int)((100.0d / (double)titles.size()) * (i - 1));
							String info = "Processing Title " + i + "/" + titles.size();
							tracker.setInfo(info);
							tracker.setProgress(progress);

							currentBackend.copyTo(title, titleHolder.getSelectedAudioTracks(), titleHolder.getSelectedSubtitles(), outputPath.getText() + File.separator + title.getDescription() + ".mkv", new ProgressTracker() {
								
								@Override
								public void setProgress(int value) {
									value = (int)(((double)value) / (double)titles.size());
									tracker.setProgress(progress + value);
								}
								
								@Override
								public void setInfo(String text) {
									tracker.setInfo(info + ": " + text);
								}

							});
							
							i++;
						}
					}

				}, true);
			}

		});
		toolBar.add(copyDisc);
		copyDisc.setEnabled(false);
		
		JPanel center = new JPanel(new BorderLayout());
		center.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(center, BorderLayout.CENTER);
		
		discTitle = new JLabel(" ");
		Font labelFont = discTitle.getFont().deriveFont(16.0f);
		discTitle.setFont(labelFont);
		discTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		center.add(discTitle, BorderLayout.NORTH);
		
		table = new TitleSelectionTable(this::titlesSelected);
		center.add(table, BorderLayout.CENTER);

		JPanel output = new JPanel(new BorderLayout());
		output.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
		center.add(output, BorderLayout.SOUTH);

		
		JLabel copyTo = new JLabel("Copy to: ");
		copyTo.setFont(labelFont);
		output.add(copyTo, BorderLayout.WEST);
		outputPath = new JTextField();
		outputPath.setText(Config.getInstance().getOutputPath());
		output.add(outputPath, BorderLayout.CENTER);
		
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				saveSettings();
				setVisible(false);
				System.exit(0);
			}
			
		});
		
		setSize(800, 600);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void saveSettings() {
		// save current settings
		Config config = Config.getInstance();
		config.setOutputPath(outputPath.getText());
		Config.storeConfig(config);
	}
	
	private void titlesSelected() {
		if (!table.getSelectedTitles().isEmpty()) {
			copyDisc.setEnabled(true);
		} else {
			copyDisc.setEnabled(false);
		}
	}
	
	private void discSelected(Disc disc) {
		if (disc != null) {
			table.setDiscStructure(disc);
			String title = disc.getDiscTitle();
			if (title == null || title.isEmpty()) {
				title = " ";
			}
			discTitle.setText(title);
		} else {
			table.setDiscStructure(null);
			discTitle.setText(" ");
		}
	}
	
	private void deviceSelected(JMenuItem item, Device device) {
		if (currentSelected != device) {
			currentSelected = device;
			currentBackend = null;

			deviceIcon.setText(currentSelected.getPath());
	
			for (int i = 0; i < deviceSelectionMenu.getComponentCount(); i++) {
				Component c = deviceSelectionMenu.getComponent(i);
				if (c instanceof JCheckBoxMenuItem && c != item) {
					((JCheckBoxMenuItem)c).setSelected(false);
				}
			}
			
			// clear table
			discSelected(null);
		}
	}

}
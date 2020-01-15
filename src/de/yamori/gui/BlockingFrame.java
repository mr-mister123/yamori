package de.yamori.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class BlockingFrame extends JDialog {
	
	
	private final static ExecutorService progressGenerator = Executors.newFixedThreadPool(1);
	private final static ExecutorService pool = Executors.newFixedThreadPool(1);
	
	private final static BlockingFrame INSTANCE = new BlockingFrame();
	
	private static boolean blocked = false;
	private static boolean done = false;
	
	private final JLabel infoLabel = new JLabel(" ");
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	
	private BlockingFrame() {
		setModal(true);
		setLayout(new BorderLayout());
		setResizable(false);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		infoLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 16));
		add(infoLabel, BorderLayout.NORTH);
		
		progressBar.setPreferredSize(new Dimension(300, 32));
		progressBar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		add(progressBar, BorderLayout.CENTER);

		pack();
	}
	
	public static void run(JFrame parent, Runnable run, String title, boolean blockImmediatly) {
		run(parent, new Trackable() {
			
			@Override
			public void run(final ProgressTracker tracker) {
				Future<?> future = progressGenerator.submit(new Runnable() {
					
					@Override
					public void run() {
						tracker.setInfo(title);
						try {
							int v = 0;
							for (;;) {
								v++;
								if (v > 100) {
									v = 0;
								}
								
								tracker.setProgress(v);
								
								Thread.sleep(50);
							}
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}

				});
				try {
					// call delegate
					run.run();
					
				} finally {
					future.cancel(true);
				}
			}

		}, blockImmediatly, false);
	}
	
	public static void run(JFrame parent, Trackable trackable, boolean blockImmediatly) {
		run(parent, trackable, blockImmediatly, true);
	}

	private static void run(JFrame parent, Trackable trackable, boolean blockImmediatly, boolean showPercentage) {
		SwingUtilities.invokeLater(() -> _run(parent, trackable, blockImmediatly, showPercentage));
	}
	
	private static void _run(JFrame parent, final Trackable trackable, boolean blockImmediatly, boolean showPercentage) {
		synchronized (BlockingFrame.class) {
			if (blocked) {
				throw new RuntimeException();
			}
			blocked = true;
			done = false;
			
			INSTANCE.infoLabel.setText(" ");
			INSTANCE.progressBar.setValue(0);
			INSTANCE.progressBar.setStringPainted(showPercentage);
			
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					try {
						trackable.run(new ProgressTracker() {
							
							@Override
							public void setProgress(int value) {
								SwingUtilities.invokeLater(() -> INSTANCE.progressBar.setValue(value));
							}
							
							@Override
							public void setInfo(String text) {
								SwingUtilities.invokeLater(() -> INSTANCE.infoLabel.setText(text));
							}

						});
					} finally {
						synchronized (BlockingFrame.class) {
							done = true;
							blocked = false;
							BlockingFrame.class.notifyAll();

							SwingUtilities.invokeLater(() -> unblock());
						}
					}
				}
	
			});
			
			if (!blockImmediatly) {
				try {
					BlockingFrame.class.wait(2000l);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
			if (!done) {
				SwingUtilities.invokeLater(() -> block(parent));
			}
		}
	}
	
	private static void block(Component parent) {
		INSTANCE.setLocationRelativeTo(parent);
		INSTANCE.setVisible(true);
	}
	
	private static void unblock() {
		INSTANCE.setVisible(false);
	}
	
}
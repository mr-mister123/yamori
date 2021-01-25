package de.yamori.impl.common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.yamori.gui.ProgressTracker;

public class Job implements Task {
	
	private final List<_Task> tasks = new LinkedList<>();
	
	public void addTask(Task task) {
		addTask(task, 1.0d);
	}
	
	public void addTask(Task task, double weight) {
		tasks.add(new _Task(task, weight));
	}

	@Override
	public boolean execute(ProgressTracker tracker) {
		if (tasks.isEmpty()) {
			return true;
		}

		double weightSum = tasks.stream().mapToDouble(t -> t.weight).sum();
		
		double progress = 0.0d;
		boolean success = true;
		Iterator<_Task> iter = tasks.iterator();
		while (iter.hasNext() && success) {
			_Task t = iter.next();

			_MyProgressTracker track = new _MyProgressTracker(tracker, progress, t.weight, weightSum);
			success = t.task.execute(track);
			
			progress += 100d * t.weight / weightSum;
		}

		return success;
	}
	
	private final static class _MyProgressTracker implements ProgressTracker {
		
		private final ProgressTracker delegate;
		private final double initialProgress;
		private final double weight;
		private final double weightSum;

		private double progress;
		
		private _MyProgressTracker(ProgressTracker delegate, double progress, double weight, double weightSum) {
			this.delegate = delegate;
			this.initialProgress = progress;
			this.weight = weight;
			this.weightSum = weightSum;

			this.progress = progress;
		}

		@Override
		public void setInfo(String text) {
			delegate.setInfo(text);
		}

		@Override
		public void setProgress(int value) {
			if (value < 0) {
				value = 0;
			} else if (value > 100) {
				value = 100;
			}
			
			progress = initialProgress + (((double)value) * weight / weightSum); 
			
			delegate.setProgress((int) progress);
		}
		
	}
	
	private final static class _Task {
		private final Task task;
		private final double weight;

		private _Task(Task task, double weight) {
			this.task = task;
			this.weight = weight;
		}

	}

}
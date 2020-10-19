package es.uca.vedils.tracker.helpers;

import java.util.TimerTask;

public class DataBatchTimerTask extends TimerTask {
	
	private LearningRecordStore currentActivityTrackerManager;
	
	public DataBatchTimerTask(LearningRecordStore currentActivityTrackerManager) {
		this.currentActivityTrackerManager = currentActivityTrackerManager;
	}
	
	@Override
	public void run() {
		currentActivityTrackerManager.recordDataBatch();
	}

}
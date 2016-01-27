package uk.gov.dstl.baleen.core.job.schedulers;

import org.apache.uima.fit.descriptor.ConfigurationParameter;

import uk.gov.dstl.baleen.core.job.BaleenScheduler;

public class PeriodicScheduler extends BaleenScheduler {

	/**
	 * The time in seconds between runs
	 * 
	 * @baleen.config 3600
	 */
	public static final String PARAM_PERIOD = "period";
	@ConfigurationParameter(name = PARAM_PERIOD, defaultValue = "3600")
	private long period;

	private long lastRunTime = -1;

	@Override
	protected boolean await() {

		long periodInMs = period * 1000;
		long timeSinceLast = System.currentTimeMillis() - lastRunTime;

		// We are are already over due, run now
		if (timeSinceLast < periodInMs) {
			lastRunTime = System.currentTimeMillis();
			return true;
		}

		// Otherwise block and wait
		try {
			Thread.sleep(timeSinceLast - periodInMs);
			lastRunTime = System.currentTimeMillis();
			return true;
		} catch (InterruptedException e) {
			getMonitor().warn("Interrupted, stopping the scheduler");
			return false;
		}

	}

}

package uk.gov.dstl.baleen.core.job.schedulers;

import uk.gov.dstl.baleen.core.job.BaleenScheduler;

public class OnceScheduler extends BaleenScheduler {

	private boolean hasRun = false;

	@Override
	protected boolean await() {
		if (hasRun) {
			return false;
		} else {
			hasRun = true;
			return true;
		}
	}

}

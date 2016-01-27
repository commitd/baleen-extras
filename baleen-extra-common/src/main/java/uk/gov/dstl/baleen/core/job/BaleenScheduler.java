package uk.gov.dstl.baleen.core.job;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import uk.gov.dstl.baleen.core.metrics.MetricsFactory;
import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.utils.UimaUtils;

public abstract class BaleenScheduler extends JCasCollectionReader_ImplBase {
	private UimaMonitor monitor;

	private Map<String, String> config;

	@Override
	public final void initialize(UimaContext context) throws ResourceInitializationException {
		// This will do initialization of resources,
		// but won't be included in the metrics
		super.initialize(context);

		String pipelineName = UimaUtils.getPipelineName(context);
		monitor = new UimaMonitor(pipelineName, this.getClass());

		monitor.startFunction("initialize");

		// Pull the config parameters out for job settings
		config = getConfigParameters(context);

		doInitialize(context);

		monitor.finishFunction("initialize");

	}

	/**
	 * Called when the collection reader is being initialized. Any required resources, for example,
	 * should be opened at this point.
	 *
	 * @param context
	 *            The UimaContext for the collection reader
	 */
	protected void doInitialize(UimaContext context) throws ResourceInitializationException {
		// Do nothing by default
	}

	@Override
	public final void getNext(JCas jCas) throws IOException, CollectionException {
		monitor.startFunction("getNext");
		MetricsFactory.getInstance().getPipelineMetrics(monitor.getPipelineName()).startDocumentProcess();

		JobSettings settings = new JobSettings(jCas);
		for (Map.Entry<String, String> e : config.entrySet()) {
			settings.set(e.getKey(), e.getValue());
		}

		monitor.finishFunction("getNext");
		monitor.persistCounts();
	}

	/**
	 * Called when the collection reader has finished and is closing down. Any open resources, for
	 * example, should be closed at this point.
	 */
	protected void doDestroy() throws IOException {
		// Do nothing
	}

	@Override
	public void destroy() {
		super.destroy();
		try {
			doDestroy();
		} catch (IOException e) {
			getMonitor().warn("Close on destroy", e);
		}
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[0];
	}

	/**
	 * Override of the UIMA hasNext() method with logic to continuously check for new documents
	 * until one is found. This prevents the collection reader from exiting (unless asked to), and
	 * so creates a persistent collection reader and pipeline.
	 */
	@Override
	public final boolean hasNext() throws IOException, CollectionException {
		return await();
	}

	protected abstract boolean await();

	protected final UimaMonitor getMonitor() {
		return monitor;
	}

	/**
	 * Create a configuration map from a context.
	 *
	 * @param context
	 *            the context
	 * @return non-empty map of config param name to config param value
	 */
	protected static Map<String, String> getConfigParameters(UimaContext context) {
		// TODO: String, String due to Metadata (probably correct but limiting)
		Map<String, String> ret = new HashMap<>();
		for (String name : context.getConfigParameterNames()) {
			ret.put(name, context.getConfigParameterValue(name).toString());
		}

		return ret;
	}
}
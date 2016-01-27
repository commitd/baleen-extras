package uk.gov.dstl.baleen.core.job;

import java.util.Optional;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.metadata.Metadata;

public class JobSettings {

	private final JCas jCas;

	public JobSettings(JCas jCas) {
		this.jCas = jCas;
	}

	private Optional<Metadata> getMetadata(String key) {
		return JCasUtil.select(jCas, Metadata.class).stream()
				.filter(m -> m.getKey().equals(key))
				.findFirst();

	}

	public String get(String key, String defaultValue) {
		return get(key).orElse(defaultValue);
	}

	public Optional<String> get(String key) {
		return getMetadata(key)
				.map(Metadata::getValue);
	}

	public void set(String key, String value) {
		// Do we have any existing metadata this this key?
		Optional<Metadata> metadata = getMetadata(key);

		// If so, update or else create
		if (metadata.isPresent()) {
			metadata.get().setValue(value);
		} else {
			Metadata md = new Metadata(jCas);
			md.setBegin(0);
			md.setEnd(0);
			md.setKey(key);
			md.setValue(value);
			md.addToIndexes();
		}
	}

	public void remove(String key) {
		Optional<Metadata> metadata = getMetadata(key);
		if (metadata.isPresent()) {
			metadata.get().removeFromIndexes();
		}
	}

}

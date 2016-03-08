package com.tenode.baleen.extras.common.annotators;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.types.common.Buzzword;
import uk.gov.dstl.baleen.types.common.Nationality;
import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public abstract class AbstractPerfectCorpusExtractor extends BaleenAnnotator {

	private final Map<Pattern, Class<? extends Entity>> missing = new HashMap<>();

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		Stream.of(getOrganisations()).forEach(addToMap(Organisation.class));
		Stream.of(getPeople()).forEach(addToMap(Person.class));
		Stream.of(getLocations()).forEach(addToMap(Location.class));
		Stream.of(getBuzzwords()).forEach(addToMap(Buzzword.class));
		Stream.of(getNationalities()).forEach(addToMap(Nationality.class));

	}

	protected String[] getNationalities() {
		return new String[] {};
	}

	protected String[] getBuzzwords() {
		return new String[] {};
	}

	protected String[] getLocations() {
		return new String[] {};
	}

	protected String[] getPeople() {
		return new String[] {};
	}

	protected String[] getOrganisations() {
		return new String[] {};
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		final String text = jCas.getDocumentText();
		final Collection<Entity> entities = JCasUtil.select(jCas, Entity.class);

		missing.entrySet().stream().forEach(m -> {
			final Matcher matcher = m.getKey().matcher(text);
			while (matcher.find()) {
				final int begin = matcher.start();
				final int end = matcher.end();

				if (!SpanUtils.existingEntity(entities, begin, end)) {
					try {
						final Constructor<? extends Entity> constructor = m.getValue().getConstructor(JCas.class);
						final Entity instance = constructor.newInstance(jCas);

						instance.setBegin(begin);
						instance.setEnd(end);
						instance.setConfidence(1.0);

						addToJCasIndex(instance);
					} catch (final Exception e) {
						getMonitor().warn("Unable to create annotation of class {}", m.getClass().getSimpleName());
					}

				}
			}
		});
	}

	private Consumer<? super String> addToMap(Class<? extends Entity> clazz) {
		return s -> missing.put(Pattern.compile("\\b" + s + "\\b"), clazz);
	};

}

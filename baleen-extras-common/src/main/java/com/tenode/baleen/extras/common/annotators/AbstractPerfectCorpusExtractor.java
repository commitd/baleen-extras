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

import com.tenode.baleen.extras.common.jcas.SpanUtils;

import uk.gov.dstl.baleen.types.common.Buzzword;
import uk.gov.dstl.baleen.types.common.Nationality;
import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.common.Vehicle;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Used to supply lists of words to match directly - effectively a simple gazetteer which is
 * precompiled.
 *
 * Override the get methods to provide the terms.
 *
 * @baleen.javadoc
 */
public abstract class AbstractPerfectCorpusExtractor extends BaleenAnnotator {

	private final Map<Pattern, Class<? extends Entity>> missing = new HashMap<>();

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doInitialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		Stream.of(getOrganisations()).forEach(addToMap(Organisation.class));
		Stream.of(getPeople()).forEach(addToMap(Person.class));
		Stream.of(getLocations()).forEach(addToMap(Location.class));
		Stream.of(getBuzzwords()).forEach(addToMap(Buzzword.class));
		Stream.of(getNationalities()).forEach(addToMap(Nationality.class));
		Stream.of(getVehicles()).forEach(addToMap(Vehicle.class));

	}

	/**
	 * Gets the nationalities.
	 *
	 * @return the nationalities
	 */
	protected String[] getNationalities() {
		return new String[] {};
	}

	/**
	 * Gets the vehicles.
	 *
	 * @return the vehicles
	 */
	protected String[] getVehicles() {
		return new String[] {};
	}

	/**
	 * Gets the buzzwords.
	 *
	 * @return the buzzwords
	 */
	protected String[] getBuzzwords() {
		return new String[] {};
	}

	/**
	 * Gets the locations.
	 *
	 * @return the locations
	 */
	protected String[] getLocations() {
		return new String[] {};
	}

	/**
	 * Gets the people.
	 *
	 * @return the people
	 */
	protected String[] getPeople() {
		return new String[] {};
	}

	/**
	 * Gets the organisations.
	 *
	 * @return the organisations
	 */
	protected String[] getOrganisations() {
		return new String[] {};
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
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
						getMonitor().warn("Unable to create annotation of class {}", m.getClass().getSimpleName(), e);
					}

				}
			}
		});
	}

	/**
	 * Adds all the strings to map as the class.
	 *
	 * @param clazz
	 *            the clazz
	 * @return the consumer<? super string>
	 */
	private Consumer<? super String> addToMap(Class<? extends Entity> clazz) {
		return s -> missing.put(Pattern.compile("\\b" + s + "\\b"), clazz);
	}

}

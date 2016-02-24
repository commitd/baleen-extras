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

	private String[] getNationalities() {
		return new String[] {};
	}

	private String[] getBuzzwords() {
		return new String[] {};
	}

	private String[] getLocations() {
		return new String[] {};
	}

	private String[] getPeople() {
		return new String[] {};
	}

	private String[] getOrganisations() {
		return new String[] {};
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		String text = jCas.getDocumentText();
		Collection<Entity> entities = JCasUtil.select(jCas, Entity.class);

		missing.entrySet().stream().forEach(m -> {
			Matcher matcher = m.getKey().matcher(text);
			while (matcher.find()) {
				int begin = matcher.start();
				int end = matcher.end();

				if (!SpanUtils.existingEntity(entities, begin, end)) {
					try {
						Constructor<? extends Entity> constructor = m.getValue().getConstructor(JCas.class);
						Entity instance = constructor.newInstance(jCas);

						instance.setBegin(begin);
						instance.setEnd(end);
						instance.setConfidence(1.0);

						addToJCasIndex(instance);
					} catch (Exception e) {
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

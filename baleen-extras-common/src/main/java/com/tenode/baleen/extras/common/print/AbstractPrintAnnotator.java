package com.tenode.baleen.extras.common.print;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;
import uk.gov.dstl.baleen.uima.utils.UimaTypesUtils;

public abstract class AbstractPrintAnnotator<T extends Base> extends BaleenAnnotator {

	private final Class<T> clazz;

	public AbstractPrintAnnotator(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		JCasUtil.select(jCas, clazz).stream()
				.map(this::print)
				.filter(Objects::nonNull)
				.forEach(s -> {
					System.out.println(clazz.getName() + ":");
					System.out.println(s);
				});
	}

	protected void writeLine(StringBuilder sb, String value) {
		sb.append("\t");
		sb.append(value);
		sb.append("\n");
	}

	protected void writeLine(StringBuilder sb, Base annotation) {
		writeLine(sb, annotation.getCoveredText() + "[" + annotation.getType().getName() + "]");
	}

	protected void writeLine(StringBuilder sb, StringArray array) {
		writeLine(sb, asString(array, ";"));
	}

	protected void writeLine(StringBuilder sb, FSArray array) {
		writeLine(sb, asString(array, Annotation.class, fs -> fs.getCoveredText(), ";"));
	}

	protected <S extends Base> void writeLine(StringBuilder sb, FSArray array, Class<S> clazz,
			Function<S, String> toString) {
		writeLine(sb, asString(array, clazz, toString, ";"));
	}

	// NOTE This is checked by the filter
	@SuppressWarnings("unchecked")
	protected <S> String asString(FSArray array, Class<S> clazz, Function<S, String> toString, String separator) {
		FeatureStructure[] fses = array.toArray();

		if (fses == null) {
			return "";
		}

		return Arrays.stream(fses)
				.filter(fs -> fs != null)
				.filter(fs -> clazz.isAssignableFrom(fs.getClass()))
				.map(fs -> toString.apply((S) fs))
				.collect(Collectors.joining(separator));
	}

	protected String asString(StringArray array, String separator) {
		return Arrays.stream(UimaTypesUtils.toArray(array))
				.collect(Collectors.joining(separator));
	}

	protected abstract String print(T t);
}

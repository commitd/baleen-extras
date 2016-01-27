package com.tenode.baleen.abta.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;

public final class AnnotationUtils {
	private AnnotationUtils() {
		// Singleton
	}

	public static <T extends Annotation> Optional<T> getSingleCovered(Class<T> clazz,
			Annotation annotation) {
		List<T> list = JCasUtil.selectCovered(clazz, annotation);
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	// TODO: This is not too efficient (slightly unclear on the ordering here)
	public static <T extends Annotation> List<T> filterToTopLevelAnnotations(List<T> annotations) {
		List<T> topLevel = new LinkedList<>();

		for (T a : annotations) {
			boolean covered = false;
			for (T b : annotations) {
				if (a != b && b.getBegin() <= a.getBegin() && a.getEnd() <= b.getEnd()) {
					covered = true;
					break;
				}
			}

			if (!covered) {
				topLevel.add(a);
			}
		}

		return topLevel;
	}

	public static boolean isInBetween(Annotation interaction, Annotation source, Annotation target) {
		int left;
		int right;
		if (source.getEnd() <= target.getBegin()) {
			left = source.getEnd();
			right = target.getBegin();
		} else {
			left = target.getBegin();
			right = source.getEnd();
		}

		if (left < interaction.getBegin() && interaction.getEnd() < right) {
			return true;
		} else {
			return false;
		}
	}
}

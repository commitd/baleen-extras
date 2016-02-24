package com.tenode.baleen.extras.common.jcas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class ReferentUtils {

	public static final Predicate<? super Base> NOT_ENTITY_OR_REFERENT = e -> !(e instanceof Entity)
			&& e.getReferent() == null;

	public static final Predicate<? super Base> ENTITY_OR_REFERENT = e -> e instanceof Entity
			|| e.getReferent() != null;

	private ReferentUtils() {
		// Singleton
	}

	public static <T extends Base> Multimap<ReferenceTarget, T> createReferentMap(JCas jCas, Class<T> clazz) {
		Collection<T> potentialReferences = JCasUtil.select(jCas, clazz);

		Multimap<ReferenceTarget, T> targets = HashMultimap.create();

		potentialReferences.stream()
				.filter(p -> p.getReferent() != null)
				.forEach(e -> {
					ReferenceTarget referent = e.getReferent();
					targets.put(referent, e);
				});

		return targets;
	}

	public static <T> Map<ReferenceTarget, T> filterToSingle(Multimap<ReferenceTarget, T> referentMap,
			Function<Collection<T>, T> convert) {
		Map<ReferenceTarget, T> singleMap = new HashMap<>(referentMap.size());
		referentMap.asMap().entrySet().stream()
				.forEach(e -> {
					T t = convert.apply(e.getValue());
					if (t != null) {
						singleMap.put(e.getKey(), t);
					}
				});

		return singleMap;
	}

	public static List<Base> getAllEntityOrReferentToEntity(JCas jCas, Map<ReferenceTarget, Entity> referentMap) {
		return getAllAndReferents(jCas, Entity.class, referentMap);
	}

	public static <T extends Base> List<Base> getAllAndReferents(JCas jCas, Class<T> clazz,
			Map<ReferenceTarget, T> referentMap) {
		List<Base> list = new ArrayList<>();

		// Add all of the original class
		list.addAll(JCasUtil.select(jCas, clazz));

		// Now find all the referents which point to the same entity
		streamReferent(jCas, referentMap)
				// Filter out any existing classes
				.filter(p -> clazz.isAssignableFrom(p.getClass()))
				.map(referentMap::get)
				.forEach(list::add);

		return list;
	}

	public static Stream<Base> streamReferent(JCas jCas,
			Map<ReferenceTarget, ?> referentMap) {
		return JCasUtil.select(jCas, Base.class).stream()
				// Filter out anything we can't reference
				.filter(p -> p.getReferent() != null && referentMap.get(p.getReferent()) != null);
	}

	public static <T extends Base> T getLongestSingle(Collection<T> list) {
		return singleViaCompare(list,
				(a, b) -> Integer.compare(a.getCoveredText().length(), b.getCoveredText().length()));
	}

	public static <T> T singleViaCompare(Collection<T> list, Comparator<T> compare) {
		return list.stream().reduce((a, b) -> compare.compare(a, b) < 0 ? b : a).get();
	}

	public static Set<Entity> removeCoreferent(Collection<Entity> entities,
			Map<ReferenceTarget, Entity> referentMap) {
		Set<Entity> set = new HashSet<>(entities.size());

		for (Entity t : entities) {
			if (t.getReferent() == null) {
				set.add(t);
			} else {
				Entity entity = referentMap.get(t.getReferent());
				if (entity != null) {
					set.add(entity);
				} else {
					// Add the other in
					set.add(t);
				}
			}
		}

		return set;
	}

}

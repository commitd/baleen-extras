package com.tenode.baleen.annotators.coreference.detector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.extras.common.grammar.DependencyGraph;
import com.tenode.baleen.extras.common.grammar.ParseTree;

import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;

public class MentionDetector {

	private final JCas jCas;
	private final DependencyGraph dependencyGraph;
	private final ParseTree parseTree;
	private List<WordToken> pronouns;
	private Collection<Entity> entities;

	public MentionDetector(JCas jCas, DependencyGraph dependencyGraph, ParseTree parseTree) {
		this.jCas = jCas;
		this.dependencyGraph = dependencyGraph;
		this.parseTree = parseTree;

	}

	public List<Mention> detect() {
		setup();

		List<Mention> mentions = new ArrayList<>(pronouns.size() + entities.size());

		detectPronouns(mentions);

		detectEntities(mentions);

		new WordEnhancer(jCas, dependencyGraph, parseTree);

		new SentenceEnhancer().enhance(jCas, mentions);

		return mentions;
	}

	private void setup() {
		pronouns = JCasUtil.select(jCas, WordToken.class).stream()
				.filter(w -> w.getPartOfSpeech().startsWith("PP") || w.getPartOfSpeech().startsWith("WP")
						|| w.getPartOfSpeech().startsWith("PRP"))
				.collect(Collectors.toList());

		entities = JCasUtil.select(jCas, Entity.class);

	}

	private void detectPronouns(List<Mention> mentions) {
		pronouns.stream()
				.map(Mention::new)
				.map(m -> {
					List<WordToken> list = Collections.singletonList((WordToken) m.getAnnotation());
					m.setWords(list);
					return m;
				}).forEach(mentions::add);

	}

	private void detectEntities(Collection<Mention> mentions) {
		entities.stream()
				.map(Mention::new)
				.map(m -> {
					Collection<WordToken> list = JCasUtil.selectCovered(jCas, WordToken.class, m.getAnnotation());
					m.setWords(new ArrayList<WordToken>(list));
					m.setHeadWordToken(determineHead(m.getWords()));
					return m;
				}).forEach(mentions::add);

	}

	private WordToken determineHead(List<WordToken> words) {
		// A dependency grammar approach to head word extraction
		// - find the Noun in the noun phrase which is the link out of the words
		// - this seems to be the head word
		// TODO: Investigate other approachces Collin 1999, etc. Do they give the same/better
		// results?

		if (words.size() == 1) {
			return words.get(0);
		} else {

			List<WordToken> candidates = new LinkedList<WordToken>();
			for (WordToken word : words) {
				if (word.getPartOfSpeech().startsWith("N")) {
					Stream<WordToken> edges = dependencyGraph.getEdges(word);
					if (edges.anyMatch(p -> !words.contains(p))) {
						candidates.add(word);
					}
				}
			}

			if (candidates.isEmpty()) {
				return null;
			}

			// TODO: No idea if its it possible to get more than one if all things work.
			// I think this would be a case of marking an entity which cross the NP boundary and is
			// likely wrong.
			WordToken head = candidates.get(0);

			// TODO: Not sure if we should pull out compound words here... (its a head word but even
			// so)

			return head;
		}
	}

	private void detectMentions(List<Mention> mentions) {

		// Limit to noun phrases
		List<PhraseChunk> phrases = JCasUtil.select(jCas, PhraseChunk.class).stream()
				.filter(p -> p.getChunkType().startsWith("N"))
				.collect(Collectors.toList());

		// Remove any noun phrases which cover entities
		JCasUtil.indexCovering(jCas, Entity.class, PhraseChunk.class).values()
				.stream()
				.flatMap(e -> e.stream())
				.forEach(phrases::remove);

		// Map<PhraseChunk, Collection<PhraseChunk>> npCoveringNp = new HashMap<>(
		// JCasUtil.indexCovering(jCas, PhraseChunk.class,
		// PhraseChunk.class));
		//
		// // In the map only keep in the collection the values in phrases list
		// npCoveringNp.keySet().retainAll(phrases);
		// Iterator<Entry<PhraseChunk, Collection<PhraseChunk>>> npIterator =
		// npCoveringNp.entrySet().iterator();
		// while (npIterator.hasNext()) {
		// Entry<PhraseChunk, Collection<PhraseChunk>> e = npIterator.next();
		// e.getValue().retainAll(phrases);
		// }

		Map<PhraseChunk, Collection<WordToken>> phraseToWord = JCasUtil.indexCovered(jCas, PhraseChunk.class,
				WordToken.class);

		// Create an index for head words
		Multimap<WordToken, PhraseChunk> headToChunk = HashMultimap.create();
		phrases.stream()
				.forEach(p -> {
					Collection<WordToken> collection = phraseToWord.get(p);
					WordToken head = determineHead(new ArrayList<>(collection));
					if (head != null) {
						headToChunk.put(head, p);
					} // else what should we do to those without heads?
				});

		// Paper: Remove all phrases which are covered by another phrase
		// phrases.removeIf(p -> {
		// Collection<PhraseChunk> covered = npCoveringNp.get(p);
		// return covered != null && !covered.isEmpty();
		// });

		// Paper: keep the largest noun phrase which has the same head word.
		headToChunk.asMap().entrySet().stream()
				.filter(e -> e.getValue().size() > 1)
				.forEach(e -> {

					PhraseChunk largest = null;
					int largestSize = 0;

					for (PhraseChunk p : e.getValue()) {
						// the head is always common word, so we know they overlap
						int size = p.getEnd() - p.getBegin();
						if (largest == null || largestSize < size) {
							largest = p;
							largestSize = size;
						}
					}

					// Remove the largest (so we can delete the rest below)
					e.getValue().remove(largest);
				});

		// Remove all those left
		phrases.removeAll(headToChunk.values());

		// Remove all phrases based on their single content
		JCasUtil.indexCovering(jCas, PhraseChunk.class,
				WordToken.class)
				.entrySet()
				.stream()
				.filter(e -> e.getValue().size() == 1)
				.filter(e -> {
					WordToken t = e.getValue().iterator().next();
					if (pronouns.contains(t)) {
						// Remove NP which are
						return true;
					} else if (t.getPartOfSpeech().equalsIgnoreCase("CD")) {
						// Paper: Remove cardinal / numerics
						return true;
					} else {
						return false;
					}
				})
				.map(Entry::getKey)
				.forEach(phrases::remove);

		// TODO: Remove all pronouns which are covered by the phrases? I think not...

		// TODO: Paper removes It is possible (see Appendix B for tregex)
		// TODO: Paper removes static list of stop words (but we should determine that outselves)
		// TODO: Paper removes withs with partivit or quanitifer (millions of people). Unsure why
		// though

		phrases.forEach(e -> mentions.add(new Mention(e)));
	}
}

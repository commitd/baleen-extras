package com.tenode.baleen.annotators.coreference.enhancers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;
import com.tenode.baleen.extras.common.grammar.DependencyGraph;
import com.tenode.baleen.extras.common.grammar.ParseTree;

import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;

public class WordEnhancer implements MentionEnhancer {

	private final JCas jCas;
	private final DependencyGraph dependencyGraph;
	private final ParseTree parseTree;

	public WordEnhancer(JCas jCas, DependencyGraph dependencyGraph, ParseTree parseTree) {
		this.jCas = jCas;
		this.dependencyGraph = dependencyGraph;
		this.parseTree = parseTree;
	}

	@Override
	public void enhance(Mention mention) {
		List<WordToken> words;
		switch (mention.getType()) {
		default:
		case PRONOUN:
			words = Collections.singletonList((WordToken) mention.getAnnotation());
		case ENTITY:
			words = JCasUtil.selectCovered(jCas, WordToken.class, mention.getAnnotation());
			break;
		case NP:
			PhraseChunk chunk = (PhraseChunk) mention.getAnnotation();
			words = parseTree.getChildWords(chunk, p -> true).collect(Collectors.toList());
			break;
		}

		mention.setWords(words);

		if (mention.getType() == MentionType.PRONOUN) {
			return;
		}

		// A dependency grammar approach to head word extraction
		// - find the Noun in the noun phrase which is the link out of the words
		// - this seems to be the head word
		// TODO: Investigate other approachces Collin 1999, etc. Do they give the same/better
		// results?

		List<WordToken> candidates = new LinkedList<WordToken>();
		for (WordToken word : words) {
			if (word.getPartOfSpeech().startsWith("N")) {
				Set<Dependency> governors = dependencyGraph.getGovernors(word);
				if (!words.containsAll(governors)) {
					candidates.add(word);
				}
			}
		}

		if (candidates.isEmpty()) {
			return;
		}

		// TODO: No idea if its it possible to get more than one if all things work.
		// I think this would be a case of marking an entity which cross the NP boundary and is
		// likely wrong.
		WordToken head = candidates.get(0);

		// TODO: Not sure if we should pull out compound words here. ie the head word Bill Clinton
		// or Clinton
		// Set<WordToken> compoundWords = dependencyGraph.nearestWords(1,
		// d -> d.getDependencyType().equalsIgnoreCase("compound"), candidates);
		// words.removeIf(w -> !compoundWords.contains(w));
		// return words.stream().map(WordToken::getCoveredText).collect(Collectors.joining(" "));

		mention.setHeadWordToken(head);
	}

}

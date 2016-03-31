package com.tenode.baleen.extras.patterns.jobs.interactions.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.tenode.baleen.extras.patterns.jobs.interactions.data.ClusteredPatterns;
import com.tenode.baleen.extras.patterns.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.patterns.jobs.interactions.data.Word;

import net.sf.extjwnl.data.POS;

public class ClusteredPatternsTest {

	@Test
	public void testEmpty() {
		ClusteredPatterns cp = new ClusteredPatterns();

		assertEquals(0.0, cp.calculateSimilarity(null), 0.001);
		assertEquals(0, cp.size());
		assertTrue(cp.getPatterns().isEmpty());
		assertTrue(cp.getPairs().isEmpty());

	}

	@Test
	public void testSingle() {

		ClusteredPatterns cp = new ClusteredPatterns();

		Word word1 = new Word("lemma1", POS.NOUN);
		Word word2 = new Word("lemma2", POS.NOUN);

		PatternReference one = new PatternReference("1", Arrays.asList(word1));
		PatternReference two = new PatternReference("2", Arrays.asList(word1,
				word2));
		PatternReference three = new PatternReference("3", Arrays.asList(word2));

		Set<Word> set = new HashSet<>(Arrays.asList(word1, word2));
		one.calculateTermFrequency(set);
		two.calculateTermFrequency(set);
		three.calculateTermFrequency(set);

		cp.add(one);

		assertEquals(1.0, cp.calculateSimilarity(one), 0.001);
		assertEquals(0.5, cp.calculateSimilarity(two), 0.001);
		assertEquals(0.0, cp.calculateSimilarity(three), 0.001);

	}

}

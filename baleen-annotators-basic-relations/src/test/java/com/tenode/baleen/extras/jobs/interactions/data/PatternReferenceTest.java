package com.tenode.baleen.extras.jobs.interactions.data;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.tenode.baleen.extras.patterns.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.patterns.jobs.interactions.data.Word;

import net.sf.extjwnl.data.POS;

public class PatternReferenceTest {

	@Test
	public void test() {
		final PatternReference p = new PatternReference("id", new Word("a", POS.NOUN), new Word("b", POS.NOUN));

		assertEquals("id", p.getId());
		assertEquals("a", p.getTokens().get(0).getLemma());
		assertEquals(POS.NOUN, p.getTokens().get(0).getPos());

		p.setSourceType("st");
		p.setTargetType("tt");

		assertEquals("tt", p.getTargetType());
		assertEquals("st", p.getSourceType());

		final PatternReference p2 = new PatternReference("id", new Word("a", POS.NOUN));

		final HashSet<Word> tokens = new HashSet<>(Arrays.asList(new Word("a", POS.NOUN), new Word("b", POS.NOUN)));
		p.calculateTermFrequency(tokens);
		p2.calculateTermFrequency(tokens);

		final double similarity = p.calculateSimilarity(p2);
		assertEquals(0.5, similarity, 0.1);

	}

}

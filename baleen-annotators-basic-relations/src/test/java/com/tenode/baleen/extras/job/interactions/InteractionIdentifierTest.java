package com.tenode.baleen.extras.job.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.tenode.baleen.extras.jobs.interactions.InteractionIdentifier;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;
import com.tenode.baleen.extras.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.jobs.interactions.data.Word;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

@RunWith(MockitoJUnitRunner.class)
public class InteractionIdentifierTest {

	private InteractionIdentifier identifier;
	private Dictionary dictionary;;

	@Before
	public void before() throws JWNLException {
		dictionary = Dictionary.getDefaultResourceInstance();
		identifier = new InteractionIdentifier(1, 0.2);
	}

	@Test
	public void testProcess() {
		// Note in this test we are using non-lemma versions of the words (hence jumps / jumped are
		// different)
		List<PatternReference> patterns = Arrays.asList(
				new PatternReference("1", new Word("jumps", POS.VERB)),
				new PatternReference("13", new Word("jumped", POS.VERB)),
				new PatternReference("2", new Word("springs", POS.VERB)),
				new PatternReference("3", new Word("leaps", POS.VERB)),
				new PatternReference("4", new Word("brother", POS.NOUN)),
				new PatternReference("11", new Word("brother", POS.NOUN), new Word("law", POS.NOUN)),
				new PatternReference("12", new Word("step", POS.NOUN), new Word("brother", POS.NOUN),
						new Word("law", POS.NOUN)),
				new PatternReference("5", new Word("sister", POS.NOUN)),
				new PatternReference("6", new Word("sibling", POS.NOUN)),
				new PatternReference("7", new Word("sister", POS.NOUN), new Word("law", POS.NOUN)),
				new PatternReference("8", new Word("step", POS.NOUN), new Word("mother", POS.NOUN)),
				new PatternReference("9", new Word("mother", POS.NOUN)),
				new PatternReference("10", new Word("was", POS.VERB), new Word("penalised", POS.VERB),
						new Word("extent", POS.NOUN), new Word("law", POS.NOUN)));

		Stream<InteractionWord> words = identifier.process(patterns);

		List<String> list = words
				.flatMap(w -> w.getAlternativeWords(dictionary))
				.distinct()
				.collect(Collectors.toList());
		// Only mother, brother and law appear often enough to be consider interaction words
		assertTrue(list.contains("mother"));
		assertTrue(list.contains("law"));
		assertTrue(list.contains("brother"));
		assertEquals(3, list.size());

	}

}

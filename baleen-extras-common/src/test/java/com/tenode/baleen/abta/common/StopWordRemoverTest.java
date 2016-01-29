package com.tenode.baleen.abta.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.tenode.baleen.abta.common.stopwords.StopWordRemover;

public class StopWordRemoverTest {

	@Test
	public void testStopWordRemover() {
		StopWordRemover swr = new StopWordRemover();

		assertTrue(swr.getStopWords().containsAll(Arrays.asList(StopWordRemover.getDefaultStopword())));
		assertEquals(StopWordRemover.getDefaultStopword().length, swr.getStopWords().size());
	}

	@Test
	public void testStopWordRemoverStringArray() {
		String[] sw = { "stop", "words" };
		StopWordRemover swr = new StopWordRemover(sw);
		swr.getStopWords().containsAll(Arrays.asList(sw));
		assertEquals(sw.length, swr.getStopWords().size());
	}

	@Test
	public void testIsStopWord() {
		String[] sw = { "stop", "words" };
		StopWordRemover swr = new StopWordRemover(sw);

		assertTrue(swr.isStopWord("stop"));
		assertTrue(swr.isStopWord("STOP"));
		assertTrue(swr.isStopWord("StoP"));
		assertFalse(swr.isStopWord("stopping"));
		assertFalse(swr.isStopWord("wordy"));
		assertFalse(swr.isStopWord("notword"));
		assertFalse(swr.isStopWord("stop word"));
	}

	@Test
	public void testClean() {
		String[] sw = { "stop", "words" };
		StopWordRemover swr = new StopWordRemover(sw);

		String text = swr.clean("This has stop words");
		assertFalse(text.contains("stop"));
		assertFalse(text.contains("words"));

		String okText = "This has no stopwords";
		String text2 = swr.clean(okText);
		assertEquals(text2, okText);

	}

}

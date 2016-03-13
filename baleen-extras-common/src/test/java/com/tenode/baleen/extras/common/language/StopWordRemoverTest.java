package com.tenode.baleen.extras.common.language;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class StopWordRemoverTest {

	@Test
	public void testStopWordRemover() {
		final StopWordRemover swr = new StopWordRemover();

		Assert.assertTrue(swr.getStopWords().containsAll(Arrays.asList(StopWordRemover.getDefaultStopword())));
	}

	@Test
	public void testStopWordRemoverStringArray() {
		final String[] sw = { "stop", "words" };
		final StopWordRemover swr = new StopWordRemover(sw);
		swr.getStopWords().containsAll(Arrays.asList(sw));
		Assert.assertEquals(sw.length, swr.getStopWords().size());
	}

	@Test
	public void testIsStopWord() {
		final String[] sw = { "stop", "words" };
		final StopWordRemover swr = new StopWordRemover(sw);

		Assert.assertTrue(swr.isStopWord("stop"));
		Assert.assertTrue(swr.isStopWord("STOP"));
		Assert.assertTrue(swr.isStopWord("StoP"));
		Assert.assertFalse(swr.isStopWord("stopping"));
		Assert.assertFalse(swr.isStopWord("wordy"));
		Assert.assertFalse(swr.isStopWord("notword"));
		Assert.assertFalse(swr.isStopWord("stop word"));
	}

	@Test
	public void testClean() {
		final String[] sw = { "stop", "words" };
		final StopWordRemover swr = new StopWordRemover(sw);

		final String text = swr.clean("This has stop words");
		Assert.assertFalse(text.contains("stop"));
		Assert.assertFalse(text.contains("words"));

		final String okText = "This has no stopwords";
		final String text2 = swr.clean(okText);
		Assert.assertEquals(text2, okText);

	}

}

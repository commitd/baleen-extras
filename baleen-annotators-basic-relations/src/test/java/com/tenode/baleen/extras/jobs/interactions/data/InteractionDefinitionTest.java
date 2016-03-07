package com.tenode.baleen.extras.jobs.interactions.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Assert;
import org.junit.Test;

import net.sf.extjwnl.data.POS;

public class InteractionDefinitionTest {

	@Test
	public void test() {
		Word word = new Word("text", POS.NOUN);
		InteractionDefinition id = new InteractionDefinition("type", "subType", word, "source", "target");

		assertEquals("type", id.getType());
		assertEquals("subType", id.getSubType());
		assertSame(word, id.getWord());
		assertEquals("source", id.getSource());
		assertEquals("target", id.getTarget());

		assertEquals("text", id.toString());
	}

	@Test
	public void testEqualsAndHashcode() {
		Word word1 = new Word("text1", POS.NOUN);
		Word word2 = new Word("text2", POS.NOUN);

		// TODO: Weak test

		InteractionDefinition id1 = new InteractionDefinition("type", "subType", word1, "source", "target");
		InteractionDefinition id2 = new InteractionDefinition("type", "subType", word2, "source", "target");

		Assert.assertNotEquals(id1, id2);
		Assert.assertNotEquals(id1.hashCode(), id2.hashCode());
	}

}

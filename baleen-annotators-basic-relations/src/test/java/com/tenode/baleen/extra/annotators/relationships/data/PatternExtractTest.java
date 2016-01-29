package com.tenode.baleen.extra.annotators.relationships.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.tenode.baleen.extra.annotators.relationships.data.PatternExtract;

import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;

@RunWith(MockitoJUnitRunner.class)
public class PatternExtractTest {

	private PatternExtract pe;

	@Mock
	private Entity from;

	@Mock
	private Entity to;

	@Mock
	private WordToken token;

	@Before
	public void before() {
		pe = new PatternExtract(from, to, 0, 10);

		Mockito.when(token.getPartOfSpeech()).thenReturn("NN");
		Mockito.when(token.getCoveredText()).thenReturn("token");
	}

	@Test
	public void testFields() {
		assertSame(from, pe.getFrom());
		assertSame(to, pe.getTo());
		assertSame(0, pe.getStart());
		assertSame(10, pe.getEnd());
	}

	@Test
	public void testFromNew() {
		assertTrue(pe.isEmpty());

		assertNull(pe.getWordTokens());

		assertEquals("", pe.getText());
	}

	@Test
	public void testSetWordTokens() {
		final List<WordToken> list = new ArrayList<>();
		pe.setWordTokens(list);
		assertSame(list, pe.getWordTokens());
	}

	@Test
	public void testContains() {
		pe.setWordTokens(Collections.singletonList(token));
		assertTrue(pe.contains("this is sample text", "is"));
		assertFalse(pe.contains("this is sample text", "text"));
		assertTrue(pe.contains("this is sample text", "text", "this"));

	}

	@Test
	public void testGetText() {
		pe.setWordTokens(Collections.singletonList(token));
		assertEquals("token", pe.getText());

		pe.setWordTokens(Arrays.asList(token, token));
		assertEquals("token token", pe.getText());
	}

	@Test
	public void testIsEmpty() {
		pe.setWordTokens(Collections.singletonList(token));
		assertFalse(pe.isEmpty());

		pe.setWordTokens(Collections.emptyList());
		assertTrue(pe.isEmpty());

	}
}

package com.tenode.baleen.extras.readers.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.tenode.baleen.extras.readers.data.MucEntry;

public class MucEntryTest {

	@Test
	public void test() {
		final MucEntry e = new MucEntry("id", "text");
		assertEquals("id", e.getId());
		assertEquals("text", e.getText());

	}

}

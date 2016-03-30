package com.tenode.baleen.extras.wordnet;

import org.junit.Test;

import com.tenode.baleen.extras.wordnet.SuperSense;

import net.sf.extjwnl.JWNLException;

public class SuperSenseTest {

	@Test
	public void testMain() throws JWNLException {

		SuperSense.main(new String[] {});

		SuperSense.main(new String[] { "verb", "want" });

		SuperSense.main(new String[] { "noun", "murder", "investigation" });

		SuperSense.main(new String[] { "not-a-pos", "knows" });

		SuperSense.main(new String[] { "noun", "ascascdsacd" });

	}

}

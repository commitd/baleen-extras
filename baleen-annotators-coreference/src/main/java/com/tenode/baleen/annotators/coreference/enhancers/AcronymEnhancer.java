package com.tenode.baleen.annotators.coreference.enhancers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;

import uk.gov.dstl.baleen.types.language.WordToken;

public class AcronymEnhancer implements MentionEnhancer {

	@Override
	public void enhance(Mention mention) {
		// Stanford just use the uppercases but only on the NNP parts,
		// We are looking at everything in the next
		// If we have more than two upper cases, we do the lower case

		if (mention.getType() == MentionType.PRONOUN) {
			return;
		}

		Set<String> acronyms;

		if (mention.isAcronym()) {
			acronyms = Collections.singleton(mention.getText().toUpperCase());
		} else {

			Collection<WordToken> words = mention.getWords();

			acronyms = new HashSet<>();

			// Generate acrynoms based on the covered text

			String text = mention.getText();

			StringBuilder upperCase = new StringBuilder();
			StringBuilder upperAndLowerCase = new StringBuilder();

			boolean considerNext = true;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (considerNext == true) {
					if (Character.isUpperCase(c)) {
						upperCase.append(c);
						upperAndLowerCase.append(c);
					} else {
						upperAndLowerCase.append(c);
					}
				}

				if (Character.isWhitespace(c)) {
					considerNext = true;
				}
			}

			// We require two upper case to avoid obvious captialisation (start of sentences)
			if (upperCase.length() > 2) {
				acronyms.add(upperCase.toString());
			} else if (upperCase.length() > 2 && upperAndLowerCase.length() != upperCase.length()) {
				acronyms.add(upperAndLowerCase.toString().toUpperCase());
			}

			// Now create acronym based on just the NNS,
			// but unlike stanford use lower and upper case again

			StringBuilder upperCaseNNP = new StringBuilder();
			StringBuilder upperAndLowerCaseNNP = new StringBuilder();
			words.stream().filter(p -> p.getPartOfSpeech().equals("NNP")).map(w -> w.getCoveredText().charAt(0))
					.forEach(c -> {
						if (Character.isUpperCase(c)) {
							upperCaseNNP.append(c);
							upperAndLowerCaseNNP.append(c);
						} else {
							upperAndLowerCaseNNP.append(c);
						}
					});

			if (upperCaseNNP.length() > 2) {
				acronyms.add(upperCaseNNP.toString());
			} else if (upperCaseNNP.length() > 2 && upperAndLowerCase.length() != upperCaseNNP.length()) {
				acronyms.add(upperAndLowerCaseNNP.toString().toUpperCase());
			}
		}

		if (acronyms.isEmpty()) {
			acronyms = Collections.emptySet();
		}

		mention.setAcronym(acronyms);
	}

}
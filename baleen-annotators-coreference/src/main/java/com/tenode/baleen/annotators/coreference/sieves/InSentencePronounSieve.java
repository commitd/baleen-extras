package com.tenode.baleen.annotators.coreference.sieves;

import java.util.List;

import org.apache.uima.jcas.JCas;

import com.tenode.baleen.annotators.coreference.data.Cluster;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.annotators.coreference.data.MentionType;
import com.tenode.baleen.annotators.coreference.data.Person;

/**
 * This is not part of the original paper, and it might have been taken care of during their
 * implementation. However it seems sensible.
 *
 * There are probably areas of english where this does not work well.
 * "Jim saw James and he was going to town" (James = he?) vs "He saw James and he was going to town"
 * (he = ?).
 *
 */
public class InSentencePronounSieve extends AbstractCoreferenceSieve {

	// TODO: Not implemented, as these seem ambiguous, ven in subsets:
	// Ok if third singular in following sets
	// - {he his} {him himself} .
	// - I don't think we can do anything with she, her, hers,herself (since no equivalent of his so
	// her = him/his) which could be different in the sentence
	// Third plural: {they, their, theirs} {them, themselves}
	// Third neuter: {it,its}, {itself}?

	public InSentencePronounSieve(JCas jCas, List<Cluster> clusters, List<Mention> mentions) {
		super(jCas, clusters, mentions);
	}

	@Override
	public void sieve() {

		for (int i = 0; i < getMentions().size(); i++) {
			Mention a = getMentions().get(i);
			String aText = a.getText();

			if (a.getType() != MentionType.PRONOUN) {
				continue;
			}

			for (int j = i + 1; j < getMentions().size(); j++) {
				Mention b = getMentions().get(j);
				String bText = b.getText();

				if (b.getType() != MentionType.PRONOUN) {
					continue;
				}

				if (a.getSentenceIndex() != b.getSentenceIndex()) {
					continue;
				}

				// Ok if both from FIRST single: {i, me, mine, my, myself}
				// Ok if both from FIRST plural: {we, us, our, ours, ourselves}
				if (a.getPerson() == Person.FIRST && b.getPerson() == Person.FIRST) {
					addToCluster(a, b);
					continue;
				}

				// Ok if from second {yourself, yourselves, you your yours} not mixing plural and
				// singular here
				if (a.getPerson() == Person.SECOND && b.getPerson() == Person.SECOND
						&& a.getMultiplicity() == b.getMultiplicity()) {
					addToCluster(a, b);
					continue;
				}

				// Ok if from third, if you match on everything
				if (a.getPerson() == Person.THIRD && b.getPerson() == Person.THIRD
						&& a.getMultiplicity() == b.getMultiplicity()
						&& a.getGender() == b.getGender()) {
					addToCluster(a, b);
					continue;
				}

				// If the text is the same, then ok
				if (aText.equalsIgnoreCase(bText)) {
					addToCluster(a, b);
					continue;
				}

			}
		}
	}
}

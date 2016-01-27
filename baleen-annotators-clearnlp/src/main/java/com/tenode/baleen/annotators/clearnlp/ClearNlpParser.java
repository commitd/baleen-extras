package com.tenode.baleen.annotators.clearnlp;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import edu.emory.clir.clearnlp.component.mode.dep.AbstractDEPParser;
import edu.emory.clir.clearnlp.component.mode.dep.DEPConfiguration;
import edu.emory.clir.clearnlp.component.utils.NLPUtils;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.lang.TLanguage;
import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class ClearNlpParser extends BaleenAnnotator {

	private AbstractDEPParser depParser;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		TLanguage language = TLanguage.ENGLISH;

		depParser = NLPUtils.getDEPParser(language, "general-en-dep.xz",
				new DEPConfiguration("root"));
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			DEPTree tree = createTreeFromTokens(tokens);

			// Perform parsing
			depParser.process(tree);

			// Convert tree back to our annotations
			for (int i = 0; i < tree.size(); i++) {
				DEPNode node = tree.get(i);

				// Logic taken from DKPro Core (ASL)
				// https://github.com/dkpro/dkpro-core/blob/master/dkpro-core-clearnlp-asl/src/main/java/de/tudarmstadt/ukp/dkpro/core/clearnlp/ClearNlpParser.java
				if (node.hasHead()) {
					Dependency dep = new Dependency(jCas);
					if (node.getHead().getID() != 0) {
						dep.setGovernor(tokens.get(node.getHead().getID()));
						dep.setDependencyType(node.getLabel());
					} else {
						dep.setGovernor(tokens.get(node.getID()));
						dep.setDependencyType("ROOT");
					}
					dep.setDependent(tokens.get(node.getID()));
					dep.setBegin(dep.getDependent().getBegin());
					dep.setEnd(dep.getDependent().getEnd());
					addToJCasIndex(dep);
				}
			}
		}
	}

	/**
	 * Creates the ClearNLP Deptree from word tokens for a sentence.
	 *
	 * @param tokens
	 *            the tokens
	 * @return the DEP tree
	 */
	private static DEPTree createTreeFromTokens(List<WordToken> tokens) {
		// Generate DEPTree from WordTokens
		DEPTree tree = new DEPTree(tokens.size());
		int tokenIndex = 0;
		for (WordToken wt : tokens) {
			DEPNode node = new DEPNode(tokenIndex++, wt.getCoveredText());
			node.setPOSTag(wt.getPartOfSpeech());
			FSArray lemmas = wt.getLemmas();
			if (lemmas != null && lemmas.size() > 0) {
				WordLemma wl = (WordLemma) lemmas.get(0);
				node.setLemma(wl.getLemmaForm());
			}
			tree.add(node);
		}
		return tree;
	}

}

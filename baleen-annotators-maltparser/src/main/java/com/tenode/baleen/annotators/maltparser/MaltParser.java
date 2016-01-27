package com.tenode.baleen.annotators.maltparser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.maltparser.concurrent.ConcurrentMaltParserModel;
import org.maltparser.concurrent.ConcurrentMaltParserService;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;
import org.maltparser.concurrent.graph.ConcurrentDependencyNode;
import org.maltparser.core.exception.MaltChainedException;

import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordLemma;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

public class MaltParser extends BaleenAnnotator {

	/**
	 * The model file, (.mco), to be loaded into the parser.
	 *
	 * No protocol is provided will attempt to load from the classpath> Default to the inbuilt
	 * (universal dependency trained) model, though that behaves poorly currently.
	 *
	 * @baleen.config maltparser-universaldependencies-en.mco
	 */
	public static final String PARAM_FILE_NAME = "modelUrl";
	@ConfigurationParameter(name = PARAM_FILE_NAME, defaultValue = "maltparser-universaldependencies-en.mco")
	private String modelUrl;

	/**
	 * Convert to Universal Dependendency tags before input.
	 *
	 * @baleen.config true
	 */
	public static final String PARAM_CONVERT_TO_UD = "udTags";
	@ConfigurationParameter(name = PARAM_CONVERT_TO_UD, defaultValue = "true")
	private Boolean udTags;

	private ConcurrentMaltParserModel model;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		URL url;
		try {
			url = new URL(modelUrl);
		} catch (MalformedURLException e) {
			url = getClass().getClassLoader().getResource(modelUrl);
		}

		try {
			model = ConcurrentMaltParserService.initializeParserModel(url);
		} catch (MaltChainedException e) {
			throw new ResourceInitializationException(e);
		}

		udTags = udTags == null ? true : udTags;
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {

		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			List<WordToken> wordTokens = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			String[] tokens = new String[wordTokens.size()];

			int i = 0;
			for (WordToken wt : wordTokens) {

				// TODO: Can include the lemma too
				String pos = wt.getPartOfSpeech();
				String lemma = getLemma(wt);
				String tag = udTags ? convertPennToUniversal(pos) : pos;
				tokens[i] = String.format("%d\t%s\t%s\t%s\t%s\t_", i + 1, wt.getCoveredText(), lemma, tag, pos);
				// System.out.println(tokens[i]);
				i++;
			}

			try {
				ConcurrentDependencyGraph graph = model.parse(tokens);
				for (int j = 0; j < graph.nDependencyNodes(); j++) {
					ConcurrentDependencyNode node = graph.getDependencyNode(j);

					if (node.hasHead()) {
						Dependency dep = new Dependency(jCas);
						// System.out.println(node.getIndex() + " : " + node.isRoot() + ": " +
						// node.getHeadIndex());
						if (node.getHeadIndex() != 0) {
							dep.setGovernor(wordTokens.get(node.getHeadIndex() - 1));
							String label = node.getLabel(7);
							dep.setDependencyType(label);
						} else {
							dep.setGovernor(wordTokens.get(node.getIndex() - 1));
							dep.setDependencyType("ROOT");
						}
						dep.setDependent(wordTokens.get(node.getIndex() - 1));
						dep.setBegin(dep.getDependent().getBegin());
						dep.setEnd(dep.getDependent().getEnd());
						addToJCasIndex(dep);
					}
				}

			} catch (Exception e) {
				throw new AnalysisEngineProcessException(e);
			}

		}
	}

	private String getLemma(WordToken token) {
		FSArray array = token.getLemmas();
		if (array == null || array.size() == 0) {
			return "_";
		} else {
			return ((WordLemma) array.get(0)).getLemmaForm();
		}
	}

	private static final Map<String, String> pennToUniversalTags = new HashMap<>();

	static {
		// See http://universaldependencies.github.io/docs/tagset-conversion/en-penn-uposf.html
		pennToUniversalTags.put("#", "SYM");
		pennToUniversalTags.put("$", "SYM");
		pennToUniversalTags.put("\"", "PUNCT");
		pennToUniversalTags.put(",", "PUNCT");
		pennToUniversalTags.put("-LRB-", "PUNCT");
		pennToUniversalTags.put("-RRB-", "PUNCT");
		pennToUniversalTags.put(".", "PUNCT");
		pennToUniversalTags.put(":", "PUNCT");
		pennToUniversalTags.put("AFX", "ADJ");
		pennToUniversalTags.put("CC", "CONJ");
		pennToUniversalTags.put("CD", "NUM");
		pennToUniversalTags.put("DT", "DET");
		pennToUniversalTags.put("EX", "ADV");
		pennToUniversalTags.put("FW", "X");
		pennToUniversalTags.put("HYPH", "PUNCT");
		pennToUniversalTags.put("IN", "ADP");
		pennToUniversalTags.put("JJ", "ADJ");
		pennToUniversalTags.put("JJR", "ADJ");
		pennToUniversalTags.put("JJS", "ADJ");
		pennToUniversalTags.put("LS", "PUNCT");
		pennToUniversalTags.put("MD", "VERB");
		pennToUniversalTags.put("NN", "NOUN");
		pennToUniversalTags.put("NNP", "PROPN");
		pennToUniversalTags.put("NNPS", "PROPN");
		pennToUniversalTags.put("NNS", "NOUN");
		pennToUniversalTags.put("PDT", "DET");
		pennToUniversalTags.put("POS", "PART");
		pennToUniversalTags.put("PRP", "PRON");
		pennToUniversalTags.put("PRP$", "DET");
		pennToUniversalTags.put("RB", "ADV");
		pennToUniversalTags.put("RBR", "ADV");
		pennToUniversalTags.put("RBS", "ADV");
		pennToUniversalTags.put("RP", "PART");
		pennToUniversalTags.put("SYM", "SYM");
		pennToUniversalTags.put("TO", "PART");
		pennToUniversalTags.put("UH", "INTJ");
		pennToUniversalTags.put("VB", "VERB");
		pennToUniversalTags.put("VBD", "VERB");
		pennToUniversalTags.put("VBG", "VERB");
		pennToUniversalTags.put("VBN", "VERB");
		pennToUniversalTags.put("VBP", "VERB");
		pennToUniversalTags.put("VBZ", "VERB");
		pennToUniversalTags.put("WDT", "DET");
		pennToUniversalTags.put("WP", "PRON");
		pennToUniversalTags.put("WP$", "DET");
		pennToUniversalTags.put("WRB", "ADV");
		pennToUniversalTags.put("`", "PUNCT");
	}

	private String convertPennToUniversal(String tag) {
		return pennToUniversalTags.getOrDefault(tag, tag);
	}
}

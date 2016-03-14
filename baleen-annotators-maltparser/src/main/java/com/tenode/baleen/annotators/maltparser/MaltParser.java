package com.tenode.baleen.annotators.maltparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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

/**
 * Use MaltParser to create a dependency grammar.
 *
 * See http://www.maltparser.org/ for more details of the implementation.
 *
 * The English language model of maltparser is trained on the Penn Treebank corpus, and as such its
 * is not freely licensed. To avoid this this project contains an english model trained form the
 * English universal dependencies dataset (http://universaldependencies.org/docs/) where the
 * original data is licensed under https://creativecommons.org/licenses/by-sa/4.0/. As such the
 * training data is licenced under the same agreement.
 *
 * The universal depedency model uses their own tags. This annotator converts between the UD and
 * standard tags.
 *
 * The MaltParser appears to be fast, low memory use and stable. As all trained alogirthms it will
 * function only as well as its training set. We found the original Penn Treebank to be
 * (subjectively) better than the Universal Dependency model. However if an algorithm requires only
 * dependency distance or an understanding of word linkage the universal dependency model functions
 * well enough.
 *
 * The output of this annotator is Dependency annotations.
 *
 * @baleen.javadoc
 */
public class MaltParser extends BaleenAnnotator {

	/**
	 * The model file, (.mco), to be loaded into the parser.
	 *
	 * No protocol is provided will attempt to load from the classpath.
	 *
	 * Default to the inbuilt (universal dependency trained) model.
	 *
	 * @baleen.config maltparser-universaldependencies-en.mco
	 */
	public static final String PARAM_FILE_NAME = "model";
	@ConfigurationParameter(name = MaltParser.PARAM_FILE_NAME, defaultValue = "maltparser-universaldependencies-en.mco")
	private String modelFilename;

	/**
	 * Convert to POS annotations to Universal Dependendency tags before input.
	 *
	 * This is required if the model is trained on a UD dataset.
	 *
	 * @baleen.config true
	 */
	public static final String PARAM_CONVERT_TO_UD = "udTags";
	@ConfigurationParameter(name = MaltParser.PARAM_CONVERT_TO_UD, defaultValue = "true")
	private Boolean udTags;

	private ConcurrentMaltParserModel model;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doInitialize(org.apache.uima.UimaContext)
	 */
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);

		File modelFile = new File(modelFilename);

		if (!modelFile.exists()) {
			// If the file doesn't exist then we will use try reading from the classpath.

			// Unfortunately Maltparser.doInitialise doesn't seem to like reading it from the Baleen
			// shaded Jar
			// So we copy it our and delete it on exit

			InputStream is = getClass().getClassLoader().getResourceAsStream(modelFilename);
			if (is != null) {
				try {
					modelFile = File.createTempFile("baleen", "maltpaser-model");
					FileUtils.copyInputStreamToFile(is, modelFile);
					modelFile.deleteOnExit();
				} catch (IOException e) {
					getMonitor().error("Unable to copy internal model {}", e);
				}

			}
		}

		try {
			model = ConcurrentMaltParserService.initializeParserModel(modelFile);
		} catch (final MaltChainedException | MalformedURLException e) {
			throw new ResourceInitializationException(e);
		}

		udTags = udTags == null ? true : udTags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenAnnotator#doProcess(org.apache.uima.jcas.JCas)
	 */
	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {

		for (final Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

			final List<WordToken> wordTokens = JCasUtil.selectCovered(jCas, WordToken.class, sentence);

			final String[] tokens = new String[wordTokens.size()];

			int i = 0;
			for (final WordToken wt : wordTokens) {
				final String pos = wt.getPartOfSpeech();
				final String lemma = getLemma(wt);
				final String tag = udTags ? convertPennToUniversal(pos) : pos;
				tokens[i] = String.format("%d\t%s\t%s\t%s\t%s\t_", i + 1, wt.getCoveredText(), lemma, tag, pos);
				// System.out.println(tokens[i]);
				i++;
			}

			try {
				final ConcurrentDependencyGraph graph = model.parse(tokens);
				for (int j = 0; j < graph.nDependencyNodes(); j++) {
					final ConcurrentDependencyNode node = graph.getDependencyNode(j);

					if (node.hasHead()) {
						final Dependency dep = new Dependency(jCas);
						// System.out.println(node.getIndex() + " : " + node.isRoot() + ": " +
						// node.getHeadIndex());
						if (node.getHeadIndex() != 0) {
							dep.setGovernor(wordTokens.get(node.getHeadIndex() - 1));
							final String label = node.getLabel(7);
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

			} catch (final Exception e) {
				throw new AnalysisEngineProcessException(e);
			}

		}
	}

	/**
	 * Gets the lemma.
	 *
	 * @param token
	 *            the token
	 * @return the lemma
	 */
	private String getLemma(final WordToken token) {
		final FSArray array = token.getLemmas();
		if (array == null || array.size() == 0) {
			return "_";
		} else {
			return ((WordLemma) array.get(0)).getLemmaForm();
		}
	}

	private static final Map<String, String> pennToUniversalTags = new HashMap<>();

	static {
		// See http://universaldependencies.github.io/docs/tagset-conversion/en-penn-uposf.html
		MaltParser.pennToUniversalTags.put("#", "SYM");
		MaltParser.pennToUniversalTags.put("$", "SYM");
		MaltParser.pennToUniversalTags.put("\"", "PUNCT");
		MaltParser.pennToUniversalTags.put(",", "PUNCT");
		MaltParser.pennToUniversalTags.put("-LRB-", "PUNCT");
		MaltParser.pennToUniversalTags.put("-RRB-", "PUNCT");
		MaltParser.pennToUniversalTags.put(".", "PUNCT");
		MaltParser.pennToUniversalTags.put(":", "PUNCT");
		MaltParser.pennToUniversalTags.put("AFX", "ADJ");
		MaltParser.pennToUniversalTags.put("CC", "CONJ");
		MaltParser.pennToUniversalTags.put("CD", "NUM");
		MaltParser.pennToUniversalTags.put("DT", "DET");
		MaltParser.pennToUniversalTags.put("EX", "ADV");
		MaltParser.pennToUniversalTags.put("FW", "X");
		MaltParser.pennToUniversalTags.put("HYPH", "PUNCT");
		MaltParser.pennToUniversalTags.put("IN", "ADP");
		MaltParser.pennToUniversalTags.put("JJ", "ADJ");
		MaltParser.pennToUniversalTags.put("JJR", "ADJ");
		MaltParser.pennToUniversalTags.put("JJS", "ADJ");
		MaltParser.pennToUniversalTags.put("LS", "PUNCT");
		MaltParser.pennToUniversalTags.put("MD", "VERB");
		MaltParser.pennToUniversalTags.put("NN", "NOUN");
		MaltParser.pennToUniversalTags.put("NNP", "PROPN");
		MaltParser.pennToUniversalTags.put("NNPS", "PROPN");
		MaltParser.pennToUniversalTags.put("NNS", "NOUN");
		MaltParser.pennToUniversalTags.put("PDT", "DET");
		MaltParser.pennToUniversalTags.put("POS", "PART");
		MaltParser.pennToUniversalTags.put("PRP", "PRON");
		MaltParser.pennToUniversalTags.put("PRP$", "DET");
		MaltParser.pennToUniversalTags.put("RB", "ADV");
		MaltParser.pennToUniversalTags.put("RBR", "ADV");
		MaltParser.pennToUniversalTags.put("RBS", "ADV");
		MaltParser.pennToUniversalTags.put("RP", "PART");
		MaltParser.pennToUniversalTags.put("SYM", "SYM");
		MaltParser.pennToUniversalTags.put("TO", "PART");
		MaltParser.pennToUniversalTags.put("UH", "INTJ");
		MaltParser.pennToUniversalTags.put("VB", "VERB");
		MaltParser.pennToUniversalTags.put("VBD", "VERB");
		MaltParser.pennToUniversalTags.put("VBG", "VERB");
		MaltParser.pennToUniversalTags.put("VBN", "VERB");
		MaltParser.pennToUniversalTags.put("VBP", "VERB");
		MaltParser.pennToUniversalTags.put("VBZ", "VERB");
		MaltParser.pennToUniversalTags.put("WDT", "DET");
		MaltParser.pennToUniversalTags.put("WP", "PRON");
		MaltParser.pennToUniversalTags.put("WP$", "DET");
		MaltParser.pennToUniversalTags.put("WRB", "ADV");
		MaltParser.pennToUniversalTags.put("`", "PUNCT");
	}

	/**
	 * Convert penn to universal.
	 *
	 * @param tag
	 *            the tag
	 * @return the string
	 */
	private String convertPennToUniversal(final String tag) {
		return MaltParser.pennToUniversalTags.getOrDefault(tag, tag);
	}
}

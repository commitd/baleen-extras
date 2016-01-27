package com.tenode.baleen.abta.annotators;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.Span;
import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;

/**
 * Perform grammatical parsing with OpenNLP parser.
 *
 * <p>
 * The document content is passed through the OpenNLP parser in order to create a parse tree.
 * </p>
 *
 * <p>
 * It is assumed that first the document has been passed through the OpenNLP pipeline (or similar)
 * so that sentences, POS, etc are extracted into the jCAS.
 * </p>
 *
 */
public class OpenNLPParser extends BaleenAnnotator {

	private static final Set<String> PHRASE_TYPES = new HashSet<String>(Arrays.asList("ADJP",
			"ADVP",
			"FRAG",
			"INTJ",
			"LST",
			"NAC",
			"NP",
			"NX",
			"PP",
			"PRN",
			"PRT",
			"QP",
			"RRC",
			"UCP",
			"VP",
			"WHADJP",
			"WHAVP",
			"WHNP",
			"WHPP",
			"X"));

	/**
	 * OpenNLP Resource (chunker) - use en-parser-chunking.bin
	 *
	 * @baleen.resource uk.gov.dstl.baleen.resources.SharedOpenNLPModel
	 */
	public static final String KEY_TOKEN = "parserChunking";
	@ExternalResource(key = KEY_TOKEN)
	private SharedOpenNLPModel parserChunkingModel;

	private Parser parser;

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		try {
			parserChunkingModel.loadModel(ParserModel.class, getClass().getResourceAsStream("en-parser-chunking.bin"));
		} catch (BaleenException be) {
			getMonitor().error("Unable to load OpenNLP Language Models", be);
			throw new ResourceInitializationException(be);
		}

		try {
			parser = ParserFactory.create((ParserModel) parserChunkingModel.getModel());

		} catch (Exception e) {
			getMonitor().error("Unable to create OpenNLP parser", e);
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		// For each sentence (in the JCas)e, we recreate the spans from our WordTokens.

		Map<Sentence, Collection<WordToken>> sentences = JCasUtil.indexCovered(jCas, Sentence.class, WordToken.class);

		sentences.entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.forEach(e -> {

					Sentence sentence = e.getKey();
					Collection<WordToken> tokens = e.getValue();

					Parse parsed = parseSentence(sentence, tokens);

					updatePhraseChunks(jCas, sentence, parsed);

				});
	}

	private void updatePhraseChunks(JCas jCas, Sentence sentence, Parse parsed) {
		// We remove all the existing PhraseChunks as they are going to be replace with the parsed
		// version
		// TODO: Or should we create a new ConstiuentPhraseChunk?
		removeFromJCasIndex(JCasUtil.selectCovered(jCas, PhraseChunk.class, sentence));

		addParsedAsAnnotations(jCas, sentence.getBegin(), parsed);

	}

	private void addParsedAsAnnotations(JCas jCas, int offset, Parse parsed) {
		String type = parsed.getType();

		// Ignore non phrase types
		if (PHRASE_TYPES.contains(type)) {
			// Otherwise add new ParseChunks

			Span span = parsed.getSpan();
			PhraseChunk phraseChunk = new PhraseChunk(jCas);
			phraseChunk.setBegin(offset + span.getStart());
			phraseChunk.setEnd(offset + span.getEnd());
			phraseChunk.setChunkType(parsed.getType());

			addToJCasIndex(phraseChunk);
		}

		Arrays.stream(parsed.getChildren()).forEach(p -> addParsedAsAnnotations(jCas, offset, p));

	}

	private Parse parseSentence(Sentence sentence, Collection<WordToken> tokens) {
		String text = sentence.getCoveredText();

		final Parse parse = new Parse(text,
				new Span(0, text.length()),
				AbstractBottomUpParser.INC_NODE,
				1,
				0);

		// Add in the POS
		int index = 0;
		for (WordToken token : tokens) {
			Span span = new Span(token.getBegin() - sentence.getBegin(),
					token.getEnd() - sentence.getBegin());

			parse.insert(new Parse(text,
					span,
					AbstractBottomUpParser.TOK_NODE,
					0,
					index));
			index++;
		}

		// Parse the sentence
		return parser.parse(parse);
	}

	@Override
	public void doDestroy() {
		parserChunkingModel = null;
	}
}

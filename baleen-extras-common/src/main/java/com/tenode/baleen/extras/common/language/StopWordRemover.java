package com.tenode.baleen.extras.common.language;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks words against a provided stop word list.
 *
 */
public class StopWordRemover {

	// Taken from http://xpo6.com/list-of-english-stop-words/
	private static final String[] DEFAULT_STOPWORDS = { "a", "about", "above", "across", "after", "afterwards", "again",
			"against", "all", "almost", "alone", "along", "already", "also", "although", "always", "am", "among",
			"amongst", "amoungst", "amount", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anyway",
			"anywhere", "are", "around", "as", "at", "back", "be", "became", "because", "become", "becomes", "becoming",
			"been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond",
			"bill", "both", "bottom", "but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt",
			"cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either",
			"eleven", "else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything",
			"everywhere", "except", "few", "fifteen", "fifty", "fill", "find", "first", "five", "for", "former",
			"formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has",
			"hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers",
			"herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed",
			"interest", "into", "i", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less",
			"ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most",
			"mostly", "move", "much", "must", "my", "myself", "n't", "name", "namely", "neither", "never",
			"nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere",
			"of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our",
			"ours", "ourselves", "out", "over", "own", "part", "per", "perhaps", "please", "put", "rather", "re", "'s",
			"same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side",
			"since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime",
			"sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them",
			"themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon",
			"these", "they", "thick", "thin", "third", "this", "those", "though", "three", "through", "throughout",
			"thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un",
			"under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever",
			"when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon",
			"wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why",
			"will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", ".",
			",", ";", "-", "--", "---", "...", ":", "(", ")", "[", "]", "?", "!", "=", "b", "c", "d", "e", "f", "g",
			"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "u", "v", "w", "x", "y", "z", "``", "''", "'",
			"oh", "&", "'m", "'ve", "'re", };

	private final Set<String> stopWords;

	/**
	 * A new stop word remover based on the DEFAULT_STOPWORDS.
	 */
	public StopWordRemover() {
		this(StopWordRemover.DEFAULT_STOPWORDS);
	}

	/**
	 * Instantiates a new stop word remover with the provided word list
	 *
	 * @param words
	 *            the words (non-null)
	 */
	public StopWordRemover(final String[] words) {
		stopWords = Arrays.asList(words).stream().map(String::toLowerCase).collect(Collectors.toSet());

	}

	/**
	 * Checks if the word is a stop word.
	 *
	 * @param word
	 *            the word
	 * @return true, if is stop word
	 */
	public boolean isStopWord(final String word) {
		return stopWords.contains(word.toLowerCase());
	}

	/**
	 * Gets the (immutable) set of stop words.
	 *
	 * @return the stop words
	 */
	public Set<String> getStopWords() {
		return Collections.unmodifiableSet(stopWords);
	}

	/**
	 * Removes all stop words from text.
	 *
	 * @param text
	 *            the text
	 * @return text without stop words
	 */
	public String clean(final String text) {
		// TODO: not very efficient, should create a regex in the constructor
		// from all the stop words and do it once here
		String clean = text;
		for (final String s : stopWords) {
			clean = clean.replaceAll("\\b" + Pattern.quote(s) + "\\b", "");
		}
		return clean;
	}

	/**
	 * Get the default stop word list.
	 *
	 * @return stop words
	 */
	public static String[] getDefaultStopword() {
		return StopWordRemover.DEFAULT_STOPWORDS;
	}
}

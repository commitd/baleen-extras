package com.tenode.baleen.extras.common.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;

public class ParseTree {

	private static final Comparator<? super TreeNode> SENTENCE_ORDER = (a, b) -> Integer
			.compare(a.getChunk().getBegin(), b.getChunk().getBegin());
	private static final Comparator<? super AnnotationFS> SHORTEST_FIRST = (a, b) -> Integer
			.compare(a.getEnd() - a.getBegin(), b.getEnd() - b.getBegin());

	private final TreeNode root;
	private final Map<PhraseChunk, TreeNode> chunkToNode;
	private final Map<WordToken, TreeNode> wordToNode;

	private ParseTree(List<TreeNode> roots, Map<PhraseChunk, TreeNode> chunkToNode,
			Map<WordToken, TreeNode> wordToNode) {
		this.root = new TreeNode(roots);
		this.chunkToNode = chunkToNode;
		this.wordToNode = wordToNode;
	}

	public Stream<WordToken> getChildWords(PhraseChunk chunk, Predicate<String> chunkFilter) {
		TreeNode node = chunkToNode.get(chunk);
		if (node.hasChildren()) {
			return node.getChildren().stream().filter(c -> chunkFilter.test(c.getChunk().getChunkType()))
					.flatMap(c -> c.getWords().stream());
		} else {
			return node.getWords().stream();
		}
	}

	public void traverseChildren(Consumer<List<TreeNode>> consumer) {
		root.traverseChildren(consumer);
	}

	public static ParseTree build(JCas jCas) {

		// Build a tree phrase to phrase

		Map<PhraseChunk, Collection<PhraseChunk>> index = JCasUtil.indexCovering(jCas, PhraseChunk.class,
				PhraseChunk.class);

		Collection<PhraseChunk> phrases = JCasUtil.select(jCas, PhraseChunk.class);

		List<TreeNode> roots = new LinkedList<>();
		Map<PhraseChunk, TreeNode> chunkToNode = new HashMap<>();

		for (PhraseChunk chunk : phrases) {

			TreeNode treeNode = chunkToNode.get(chunk);
			if (treeNode == null) {
				treeNode = new TreeNode(chunk);
				chunkToNode.put(chunk, treeNode);
			}

			Collection<PhraseChunk> covering = index.get(chunk);
			if (covering == null || covering.isEmpty()) {
				// Nothing is covering this Jcas, so its a root
				roots.add(treeNode);
			} else {
				// This is covered, so we add the smallest one as out parent
				PhraseChunk parent = findSmallest(covering);

				TreeNode parentNode = chunkToNode.get(parent);
				if (parentNode == null) {
					parentNode = new TreeNode(parent);
					chunkToNode.put(parent, parentNode);
				}

				treeNode.setParent(parent);
				parentNode.addChild(treeNode);

			}
		}

		// Add words to the tree

		Map<PhraseChunk, Collection<WordToken>> wordIndex = JCasUtil.indexCovered(jCas, PhraseChunk.class,
				WordToken.class);

		Map<WordToken, TreeNode> wordToNode = new HashMap<>();

		chunkToNode.values().forEach(n -> {

			// Sort all tree nodes by sentence order
			n.getChildren().sort(SENTENCE_ORDER);

			// Get all the words which are within this chunk, and then remove those which are in
			// children
			Collection<WordToken> allWords = wordIndex.get(n.getChunk());
			if (allWords != null) {
				List<WordToken> words = new ArrayList<>(allWords);

				// Remove the words which are covered by our children, leaving just our words
				if (!n.hasChildren()) {
					n.getChildren().stream()
							.map(t -> wordIndex.get(t.getChunk()))
							.filter(Objects::nonNull)
							.forEach(words::remove);
				}

				// Add the words into the treenode
				n.addWords(words);
				words.stream()
						.forEach(w -> wordToNode.put(w, n));
			}

		});

		// Sort roots

		roots.sort(SENTENCE_ORDER);

		return new ParseTree(roots, chunkToNode, wordToNode);
	}

	private static PhraseChunk findSmallest(Collection<PhraseChunk> covering) {
		return covering.stream()
				.sorted(SHORTEST_FIRST)
				.findFirst()
				.get();

	}

	public static final class TreeNode {

		private final PhraseChunk chunk;

		private PhraseChunk parent;

		private final List<TreeNode> children = new LinkedList<>();

		private final List<WordToken> words = new LinkedList<>();

		public TreeNode(PhraseChunk chunk) {
			this.chunk = chunk;
		}

		public void traverseChildren(Consumer<List<TreeNode>> consumer) {
			if (children != null && !children.isEmpty()) {
				consumer.accept(children);
				children.forEach(c -> c.traverseChildren(consumer));
			}

		}

		public TreeNode(List<TreeNode> children) {
			this.chunk = null;
			this.children.addAll(children);
		}

		public boolean isRoot() {
			return chunk == null;
		}

		public PhraseChunk getChunk() {
			return chunk;
		}

		public void setParent(PhraseChunk parent) {
			this.parent = parent;
		}

		public List<TreeNode> getChildren() {
			return children;
		}

		public boolean hasChildren() {
			return !children.isEmpty();
		}

		public PhraseChunk getParent() {
			return parent;
		}

		public void addChild(TreeNode child) {
			children.add(child);
		}

		public void addWords(Collection<WordToken> word) {
			if (word != null) {
				words.addAll(word);
			}
		}

		public List<WordToken> getWords() {
			return words;
		}

		@Override
		public String toString() {
			return chunk.getCoveredText() + "[" + chunk.getChunkType() + "]";
		}

		public boolean containsWord(Predicate<WordToken> filter) {

			if (words != null && !words.isEmpty()) {

				boolean result = words.stream().anyMatch(filter);
				if (result) {
					return true;
				}
			}

			if (children != null) {
				return children.stream().map(c -> c.containsWord(filter)).anyMatch(r -> r);
			}
			return false;
		}

	}

}

package com.tenode.baleen.extras.common.grammar.data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.WordToken;

public final class ParseTreeNode {

	private final PhraseChunk chunk;

	private ParseTreeNode parent;

	private final List<ParseTreeNode> children = new LinkedList<>();

	private final List<WordToken> words = new LinkedList<>();

	public ParseTreeNode(PhraseChunk chunk) {
		this.chunk = chunk;
	}

	public void traverseChildren(Consumer<List<ParseTreeNode>> consumer) {
		if (children != null && !children.isEmpty()) {
			consumer.accept(children);
			children.forEach(c -> c.traverseChildren(consumer));
		}

	}

	public void traverseParent(BiConsumer<ParseTreeNode, ParseTreeNode> consumer) {
		if (parent != null) {
			consumer.accept(parent, this);
			parent.traverseParent(consumer);
		}
	}

	public ParseTreeNode(List<ParseTreeNode> children) {
		this.chunk = null;
		addAllChildren(children);

	}

	public boolean isRoot() {
		return chunk == null;
	}

	public PhraseChunk getChunk() {
		return chunk;
	}

	public void setParent(ParseTreeNode parent) {
		this.parent = parent;
	}

	public List<ParseTreeNode> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public ParseTreeNode getParent() {
		return parent;
	}

	public void addChild(ParseTreeNode child) {
		children.add(child);
		child.setParent(this);
	}

	private void addAllChildren(List<ParseTreeNode> children) {
		children.forEach(this::addChild);
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
			return children.stream().anyMatch(c -> c.containsWord(filter));
		}
		return false;
	}

}
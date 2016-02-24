package com.tenode.baleen.extras.common.grammar.data;

import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.WordToken;

public class Edge {
	private final WordToken from;
	private final Dependency dependency;
	private final WordToken to;

	public Edge(WordToken from, Dependency dependency, WordToken to) {
		this.from = from;
		this.dependency = dependency;
		this.to = to;
	}

	public WordToken getFrom() {
		return from;
	}

	public Dependency getDependency() {
		return dependency;
	}

	public WordToken getTo() {
		return to;
	}

	public WordToken getOther(WordToken token) {
		return token.equals(to) ? from : to;
	}

	public boolean isTo(WordToken token) {
		return token.equals(to);
	}

	public boolean isFrom(WordToken token) {
		return token.equals(from);
	}
}

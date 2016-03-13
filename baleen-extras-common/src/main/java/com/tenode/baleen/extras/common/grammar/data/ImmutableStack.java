package com.tenode.baleen.extras.common.grammar.data;

import java.util.stream.Stream;

import uk.gov.dstl.baleen.types.language.WordToken;

public class ImmutableStack<T> {

	private final T head;

	private final ImmutableStack<T> tail;

	public ImmutableStack() {
		this(null, null);
	}

	public ImmutableStack(T head) {
		this(head, null);
	}

	private ImmutableStack(T head, ImmutableStack<T> tail) {
		this.head = head;
		this.tail = tail;

	}

	public boolean isEmpty() {
		return head == null && tail == null;
	}

	public int size() {
		// TODO: This should not be recursive since it might cause a stack overflow, but out sizes
		// are small at the moment
		return (head == null ? 0 : 1) + (tail == null ? 0 : tail.size());
	}

	public T getHead() {
		return head;
	}

	public ImmutableStack<T> getTail() {
		return tail;
	}

	public ImmutableStack<T> push(T t) {
		if (t == null) {
			return this;
		} else {
			return new ImmutableStack<>(t, this);
		}
	}

	public ImmutableStack<T> pop() {
		return tail;
	}

	public Stream<T> stream() {
		if (head == null) {
			return Stream.empty();
		} else if (tail == null) {
			return Stream.of(head);
		} else {
			return Stream.concat(Stream.of(head), tail.stream());
		}
	}

	public boolean contains(WordToken other) {
		return head != null && head.equals(other) || tail != null && tail.contains(other);
	}

}

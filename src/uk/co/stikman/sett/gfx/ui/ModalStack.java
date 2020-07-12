package uk.co.stikman.sett.gfx.ui;

import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class ModalStack {
	private LinkedList<SimpleWindow> stack = new LinkedList<>();

	public void push(SimpleWindow wnd) {
		stack.add(wnd);
	}

	public SimpleWindow pop() {
		if (stack.isEmpty())
			throw new EmptyStackException();
		return stack.removeLast();
	}

	public SimpleWindow getTop() {
		if (stack.isEmpty())
			return null;
		return stack.getLast();
	}

	public void clear() {
		stack.clear();
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public String toString() {
		return stack.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
	}

	public void remove(SimpleWindow wnd) {
		stack.remove(wnd);
	}
}

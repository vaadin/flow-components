package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

@SuppressWarnings("serial")
class IteratorChain<E> implements Iterator<E>, Serializable {

    private static class EmptyIterator<E> implements Iterator<E>, Serializable {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private final Queue<Iterator<? extends E>> iteratorChain = new LinkedList<Iterator<? extends E>>();
    private Iterator<? extends E> currentIterator = null;

    public IteratorChain(Collection<Iterator<? extends E>> iterators) {
        iteratorChain.addAll(iterators);
    }

    private void updateCurrentIterator() {
        if (currentIterator == null) {
            if (iteratorChain.isEmpty()) {
                currentIterator = new EmptyIterator<E>();
            } else {
                currentIterator = iteratorChain.remove();
            }
        }

        while (currentIterator.hasNext() == false && !iteratorChain.isEmpty()) {
            currentIterator = iteratorChain.remove();
        }
    }

    @Override
    public boolean hasNext() {
        updateCurrentIterator();
        return currentIterator.hasNext();
    }

    @Override
    public E next() {
        updateCurrentIterator();
        return currentIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
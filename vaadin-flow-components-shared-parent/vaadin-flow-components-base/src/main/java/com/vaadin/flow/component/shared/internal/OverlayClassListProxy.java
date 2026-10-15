/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared.internal;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.dom.ClassList;

/**
 * Internal class that provides shared functionality for setting CSS class names
 * to overlay only components that support {@link HasStyle}, such as
 * {@link Dialog}. Not intended to be used publicly.
 */
public class OverlayClassListProxy extends AbstractSet<String>
        implements ClassList {
    private final HasStyle hasStyle;
    private final ClassList classList;

    public OverlayClassListProxy(HasStyle hasStyle) {
        this.hasStyle = hasStyle;
        this.classList = hasStyle.getElement().getClassList();
    }

    private void updateOverlayClass() {
        hasStyle.getElement().setProperty("overlayClass",
                hasStyle.getClassName());
    }

    @Override
    public Iterator<String> iterator() {
        return new IteratorProxy(classList.iterator());
    }

    @Override
    public int size() {
        return classList.size();
    }

    @Override
    public boolean add(String s) {
        boolean result = classList.add(s);
        updateOverlayClass();
        return result;
    }

    private class IteratorProxy implements Serializable, Iterator<String> {
        private final Iterator<String> iterator;

        public IteratorProxy(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
            updateOverlayClass();
        }
    }
}

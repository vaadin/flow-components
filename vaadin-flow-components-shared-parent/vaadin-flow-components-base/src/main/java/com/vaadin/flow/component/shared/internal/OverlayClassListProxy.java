/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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

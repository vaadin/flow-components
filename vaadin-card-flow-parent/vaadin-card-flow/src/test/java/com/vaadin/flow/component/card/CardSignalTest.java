/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.card;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class CardSignalTest extends AbstractSignalsTest {

    private Card card;

    @BeforeEach
    void setup() {
        card = new Card();
        UI.getCurrent().add(card);
    }

    @Test
    void bindChildren_slottedContentExcludedFromChildren() {
        var header = new Div();
        card.setHeader(header);

        var textSignal = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal));
        card.bindChildren(listSignal, Span::new);

        // The binding owns the default slot, which getChildren() reports, while
        // the header stays in its slot and out of the filtered view.
        Assertions.assertEquals(List.of("Item 1"), card.getChildren()
                .map(child -> child.getElement().getText()).toList());
        Assertions.assertSame(header, card.getHeader());
    }

    @Test
    void childrenBindingActive_addComponentAtIndex_throws() {
        var textSignal = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal));
        card.bindChildren(listSignal, Span::new);

        var component = new Span();
        Assertions.assertThrows(BindingActiveException.class,
                () -> card.addComponentAtIndex(0, component));
        Assertions.assertEquals(1, card.getChildren().count());
        Assertions.assertFalse(component.isAttached());
    }

    @Test
    void childrenBindingActive_addComponentAsFirst_throws() {
        var textSignal = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal));
        card.bindChildren(listSignal, Span::new);

        var component = new Span();
        Assertions.assertThrows(BindingActiveException.class,
                () -> card.addComponentAsFirst(component));
        Assertions.assertEquals(1, card.getChildren().count());
        Assertions.assertFalse(component.isAttached());
    }
}

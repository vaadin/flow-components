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
package com.vaadin.flow.component.details;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsTest;

class DetailsSignalTest extends AbstractSignalsTest {

    private Details details;

    @BeforeEach
    void setup() {
        details = new Details();
    }

    @AfterEach
    void tearDown() {
        if (details != null && details.isAttached()) {
            details.removeFromParent();
        }
    }

    @Test
    void bindChildren_addsChildrenFromSignal() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, Span::new);

        Assertions.assertEquals(2, details.getContent().count());
        Assertions.assertEquals("Item 1",
                details.getContent().findFirst().get().getElement().getText());
    }

    @Test
    void bindChildren_updatesChildrenWhenListSignalChanges() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, Span::new);

        Assertions.assertEquals(1, details.getContent().count());

        var textSignal2 = new ValueSignal<>("Item 2");
        var textSignal3 = new ValueSignal<>("Item 3");
        listSignal.set(List.of(textSignal1, textSignal2, textSignal3));

        Assertions.assertEquals(3, details.getContent().count());
    }

    @Test
    void bindChildren_notAttached_initialValueApplied() {
        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, Span::new);

        // Initial value is applied immediately (effect runs on creation)
        Assertions.assertEquals(2, details.getContent().count());

        UI.getCurrent().add(details);

        Assertions.assertEquals(2, details.getContent().count());
    }

    @Test
    void bindChildren_calledTwice_throwsException() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, Span::new);
        Assertions.assertThrows(BindingActiveException.class,
                () -> details.bindChildren(listSignal, Span::new));
    }

    @Test
    void bindChildren_nullSignal_throwsException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> details.bindChildren(null, signal -> new Span("text")));
    }

    @Test
    void bindChildren_nullFactory_throwsException() {
        var textSignal = new ValueSignal<>("Item");
        var listSignal = new ValueSignal<>(List.of(textSignal));
        Assertions.assertThrows(NullPointerException.class,
                () -> details.bindChildren(listSignal, null));
    }
}

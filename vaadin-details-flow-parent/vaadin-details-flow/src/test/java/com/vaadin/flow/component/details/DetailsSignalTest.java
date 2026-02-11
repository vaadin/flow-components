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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DetailsSignalTest extends AbstractSignalsUnitTest {

    private Details details;

    @Before
    public void setup() {
        details = new Details();
    }

    @After
    public void tearDown() {
        if (details != null && details.isAttached()) {
            details.removeFromParent();
        }
    }

    @Test
    public void bindChildren_addsChildrenFromSignal() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, signal -> new Span(signal.value()));

        Assert.assertEquals(2, details.getContent().count());
        Assert.assertEquals("Item 1",
                details.getContent().findFirst().get().getElement().getText());
    }

    @Test
    public void bindChildren_updatesChildrenWhenListSignalChanges() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, signal -> new Span(signal.value()));

        Assert.assertEquals(1, details.getContent().count());

        var textSignal2 = new ValueSignal<>("Item 2");
        var textSignal3 = new ValueSignal<>("Item 3");
        listSignal.value(List.of(textSignal1, textSignal2, textSignal3));

        Assert.assertEquals(3, details.getContent().count());
    }

    @Test
    public void bindChildren_notAttached_bindingInactiveUntilAttach() {
        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, signal -> new Span(signal.value()));

        Assert.assertEquals(0, details.getContent().count());

        UI.getCurrent().add(details);

        Assert.assertEquals(2, details.getContent().count());
    }

    @Test(expected = BindingActiveException.class)
    public void bindChildren_calledTwice_throwsException() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, signal -> new Span(signal.value()));
        details.bindChildren(listSignal, signal -> new Span(signal.value()));
    }

    @Test(expected = NullPointerException.class)
    public void bindChildren_nullSignal_throwsException() {
        details.bindChildren(null, signal -> new Span("text"));
    }

    @Test(expected = NullPointerException.class)
    public void bindChildren_nullFactory_throwsException() {
        var textSignal = new ValueSignal<>("Item");
        var listSignal = new ValueSignal<>(List.of(textSignal));
        details.bindChildren(listSignal, null);
    }
}

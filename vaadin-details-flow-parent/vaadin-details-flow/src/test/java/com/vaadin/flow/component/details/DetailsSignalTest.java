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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class DetailsSignalTest extends AbstractSignalsUnitTest {

    private Details details;
    private ValueSignal<String> summaryTextSignal;
    private Signal<String> computedSignal;

    @Before
    public void setup() {
        details = new Details();
        summaryTextSignal = new ValueSignal<>("Initial Summary");
        computedSignal = Signal
                .computed(() -> summaryTextSignal.get() + " computed");
    }

    @After
    public void tearDown() {
        if (details != null && details.isAttached()) {
            details.removeFromParent();
        }
    }

    @Test
    public void summaryTextSignalCtor() {
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);
        assertSummaryTextSignalBindingActive();
    }

    @Test
    public void summaryTextSignalWithContentCtor() {
        Div content = new Div();
        content.setText("Content");
        details = new Details(summaryTextSignal, content);
        UI.getCurrent().add(details);
        assertSummaryTextSignalBindingActive();

        // Verify content is added
        Assert.assertEquals(1, details.getContent().count());
        Assert.assertEquals(content, details.getContent().findFirst().get());
    }

    @Test
    public void summaryTextSignalWithMultipleComponentsCtor() {
        Div content1 = new Div();
        content1.setText("Content 1");
        Div content2 = new Div();
        content2.setText("Content 2");
        Span content3 = new Span("Content 3");

        details = new Details(summaryTextSignal, content1, content2, content3);
        UI.getCurrent().add(details);
        assertSummaryTextSignalBindingActive();

        // Verify all components are added
        Assert.assertEquals(3, details.getContent().count());
    }

    @Test
    public void summaryTextSignal_notAttached() {
        details = new Details(summaryTextSignal);
        assertSummaryTextSignalBindingInactive();
    }

    @Test
    public void summaryTextSignal_detachedAndAttached() {
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);
        details.removeFromParent();
        assertSummaryTextSignalBindingInactive();

        UI.getCurrent().add(details);
        assertSummaryTextSignalBindingActive();
    }

    @Test(expected = BindingActiveException.class)
    public void setSummaryTextWhileBound_throws() {
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);

        details.setSummaryText("Attempt to set text");
    }

    @Test
    public void summaryTextComputedSignalCtor() {
        details = new Details(computedSignal);
        UI.getCurrent().add(details);
        Assert.assertEquals("Initial Summary computed",
                details.getSummaryText());

        summaryTextSignal.set("Updated");
        Assert.assertEquals("Updated computed", details.getSummaryText());
    }

    @Test
    public void summaryTextSignalConstructors_useSignalSupport() {
        // Test signal constructor variant 1
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);
        var summary = details.getSummary();
        Assert.assertNotNull(summary);
        Assert.assertEquals("Initial Summary", details.getSummaryText());
        summaryTextSignal.set("Changed");
        Assert.assertEquals("Changed", details.getSummaryText());
        Assert.assertEquals("Changed", summary.getElement().getText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
        details.removeFromParent();

        // Test signal constructor variant 2
        details = new Details(summaryTextSignal, new Div());
        UI.getCurrent().add(details);
        summary = details.getSummary();
        Assert.assertNotNull(summary);
        summaryTextSignal.set("Changed Again");
        Assert.assertEquals("Changed Again", details.getSummaryText());
        Assert.assertEquals("Changed Again", summary.getElement().getText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
        details.removeFromParent();

        // Test signal constructor variant 3
        details = new Details(summaryTextSignal, new Div(), new Span());
        UI.getCurrent().add(details);
        summary = details.getSummary();
        Assert.assertNotNull(summary);
        summaryTextSignal.set("Final Change");
        Assert.assertEquals("Final Change", details.getSummaryText());
        Assert.assertEquals("Final Change", summary.getElement().getText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
    }

    @Test
    public void contentAddedWithSignalConstructor_signalStillWorks() {
        Div content = new Div("Content");
        details = new Details(summaryTextSignal, content);
        UI.getCurrent().add(details);

        // Verify content is accessible
        Assert.assertEquals(1, details.getContent().count());
        Assert.assertEquals(content, details.getContent().findFirst().get());

        // Verify signal binding still works
        assertSummaryTextSignalBindingActive();

        // Add more content
        Span additionalContent = new Span("More content");
        details.add(additionalContent);
        Assert.assertEquals(2, details.getContent().count());

        // Verify signal binding still works after adding content
        summaryTextSignal.set("After adding content");
        Assert.assertEquals("After adding content", details.getSummaryText());
    }

    @Test
    public void bindChildren_addsChildrenFromSignal() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, Span::new);

        Assert.assertEquals(2, details.getContent().count());
        Assert.assertEquals("Item 1",
                details.getContent().findFirst().get().getElement().getText());
    }

    @Test
    public void bindChildren_updatesChildrenWhenListSignalChanges() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, Span::new);

        Assert.assertEquals(1, details.getContent().count());

        var textSignal2 = new ValueSignal<>("Item 2");
        var textSignal3 = new ValueSignal<>("Item 3");
        listSignal.set(List.of(textSignal1, textSignal2, textSignal3));

        Assert.assertEquals(3, details.getContent().count());
    }

    @Test
    public void bindChildren_notAttached_bindingInactiveUntilAttach() {
        var textSignal1 = new ValueSignal<>("Item 1");
        var textSignal2 = new ValueSignal<>("Item 2");
        var listSignal = new ValueSignal<>(List.of(textSignal1, textSignal2));

        details.bindChildren(listSignal, Span::new);

        Assert.assertEquals(0, details.getContent().count());

        UI.getCurrent().add(details);

        Assert.assertEquals(2, details.getContent().count());
    }

    @Test(expected = BindingActiveException.class)
    public void bindChildren_calledTwice_throwsException() {
        UI.getCurrent().add(details);

        var textSignal1 = new ValueSignal<>("Item 1");
        var listSignal = new ValueSignal<>(List.of(textSignal1));

        details.bindChildren(listSignal, Span::new);
        details.bindChildren(listSignal, Span::new);
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

    private void assertSummaryTextSignalBindingActive() {
        summaryTextSignal.set("First Update");
        var summary = details.getSummary();
        Assert.assertEquals("First Update", details.getSummaryText());
        summaryTextSignal.set("Second Update");
        Assert.assertEquals("Second Update", details.getSummaryText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
        Assert.assertEquals("Second Update", summary.getElement().getText());
    }

    private void assertSummaryTextSignalBindingInactive() {
        String currentText = details.getSummaryText();
        summaryTextSignal.set(currentText + " changed");
        Assert.assertEquals(currentText, details.getSummaryText());
    }
}

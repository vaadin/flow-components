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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.signals.BindingActiveException;
import com.vaadin.signals.Signal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

/**
 * Unit tests for the Details component's signal bindings.
 */
public class DetailsSignalTest extends AbstractSignalsUnitTest {

    private Details details;
    private ValueSignal<String> summaryTextSignal;
    private Signal<String> computedSignal;

    @Before
    public void setup() {
        summaryTextSignal = new ValueSignal<>("Initial Summary");
        computedSignal = Signal
                .computed(() -> summaryTextSignal.value() + " computed");
    }

    @After
    public void tearDown() {
        if (details != null && details.isAttached()) {
            details.removeFromParent();
        }
    }

    // A. Constructor Variant Tests

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

    // B. Signal Binding Lifecycle Tests

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

    @Test
    public void summaryTextSignal_removeBinding() {
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);

        details.bindSummaryText(null);
        assertSummaryTextSignalBindingInactive();

        details.setSummaryText("Manual text");
        Assert.assertEquals("Manual text", details.getSummaryText());
    }

    @Test(expected = BindingActiveException.class)
    public void setSummaryTextWhileBound_throws() {
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);

        details.setSummaryText("Attempt to set text");
    }

    // C. Signal Type Tests

    @Test
    public void summaryTextComputedSignalCtor() {
        details = new Details(computedSignal);
        UI.getCurrent().add(details);
        Assert.assertEquals("Initial Summary computed",
                details.getSummaryText());

        summaryTextSignal.value("Updated");
        Assert.assertEquals("Updated computed", details.getSummaryText());
    }

    @Test
    public void summaryTextComputedSignal_removeBindingAndRebind() {
        details = new Details(computedSignal);
        UI.getCurrent().add(details);

        details.bindSummaryText(null);
        details.bindSummaryText(summaryTextSignal);
        assertSummaryTextSignalBindingActive();

        details.bindSummaryText(null);
        assertSummaryTextSignalBindingInactive();
    }

    // D. Constructor Delegation Test

    @Test
    public void summaryTextSignalConstructors_useSignalSupport() {
        // Test signal constructor variant 1
        details = new Details(summaryTextSignal);
        UI.getCurrent().add(details);
        var summary = details.getSummary();
        Assert.assertNotNull(summary);
        Assert.assertEquals("Initial Summary", details.getSummaryText());
        summaryTextSignal.value("Changed");
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
        summaryTextSignal.value("Changed Again");
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
        summaryTextSignal.value("Final Change");
        Assert.assertEquals("Final Change", details.getSummaryText());
        Assert.assertEquals("Final Change", summary.getElement().getText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
    }

    // E. Content Management Test

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
        summaryTextSignal.value("After adding content");
        Assert.assertEquals("After adding content", details.getSummaryText());
    }

    // Helper Methods

    private void assertSummaryTextSignalBindingActive() {
        summaryTextSignal.value("First Update");
        var summary = details.getSummary();
        Assert.assertEquals("First Update", details.getSummaryText());
        summaryTextSignal.value("Second Update");
        Assert.assertEquals("Second Update", details.getSummaryText());
        Assert.assertEquals("Should reuse existing summary component", summary,
                details.getSummary());
        Assert.assertEquals("Second Update", summary.getElement().getText());
    }

    private void assertSummaryTextSignalBindingInactive() {
        String currentText = details.getSummaryText();
        summaryTextSignal.value(currentText + " changed");
        Assert.assertEquals(currentText, details.getSummaryText());
    }
}

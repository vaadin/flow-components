/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.Title;
import com.vaadin.flow.dom.Element;

import tools.jackson.databind.node.ObjectNode;

/**
 * Unit-level coverage for the reactive Chart-configuration prototype
 * (experimental {@code reactiveCharts} feature flag). The tests exercise the
 * Chart-side JSON-diff machinery and the Configuration-side parent-pointer
 * propagation without depending on Flow's attach lifecycle.
 */
class ReactiveConfigurationTest {

    private static class TestChart extends Chart {
        private final Element element;

        TestChart() {
            super(ChartType.LINE);
            this.element = mock(Element.class);
        }

        @Override
        public Element getElement() {
            return element;
        }
    }

    // ----------------------------------------------- parent-pointer bubbling

    @Test
    void titleSetText_marksDirty_propagatesToConfigurationRoot() {
        Configuration conf = new Configuration();
        conf.setTitle("initial");

        AtomicInteger trips = new AtomicInteger();
        conf.setReactiveSyncTrigger(trips::incrementAndGet);

        conf.getTitle().setText("updated");

        assertTrue(trips.get() >= 1,
                "Title.setText must reach Configuration via the parent-pointer walk");
    }

    @Test
    void setTitleReplacingInstance_triggersSyncAndFutureMutationsOnNewTitle() {
        Configuration conf = new Configuration();
        Title first = conf.getTitle();
        first.setText("first");

        AtomicInteger trips = new AtomicInteger();
        conf.setReactiveSyncTrigger(trips::incrementAndGet);

        Title replacement = new Title("second");
        conf.setTitle(replacement);
        int afterReplace = trips.get();
        assertTrue(afterReplace >= 1,
                "Replacing the title must trigger a sync");

        // The old title is detached — mutating it must no longer propagate.
        first.setText("stale");
        assertEquals(afterReplace, trips.get(),
                "Detached Title must not propagate to Configuration");

        // The new title IS wired — mutating it must propagate.
        replacement.setText("third");
        assertTrue(trips.get() > afterReplace,
                "New Title must propagate to Configuration");
    }

    // ---------------------------------------------------- delta sync content

    @Test
    void synchronizeReactiveConfiguration_withDirtyTitle_sendsDeltaForChangedBranchesOnly() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        conf.setTitle("initial");
        chart.seedReactiveSnapshot();

        conf.getTitle().setText("updated");

        chart.synchronizeReactiveConfiguration();

        ArgumentCaptor<ObjectNode> deltaCaptor = ArgumentCaptor
                .forClass(ObjectNode.class);
        verify(chart.getElement()).callJsFunction(eq("updateConfiguration"),
                deltaCaptor.capture(), eq(false));
        ObjectNode delta = deltaCaptor.getValue();

        assertTrue(delta.has("title"),
                "Delta must include the changed title branch");
        assertEquals("updated", delta.get("title").get("text").asString(),
                "Delta title.text must reflect the post-mutation value");

        // Untouched top-level branches must NOT be in the delta.
        assertFalse(delta.has("chart"),
                "Unchanged branches must not appear in the delta");
        assertFalse(delta.has("exporting"),
                "Unchanged branches must not appear in the delta");
        assertFalse(delta.has("plotOptions"),
                "Unchanged branches must not appear in the delta");
        assertFalse(delta.has("series"),
                "Unchanged branches must not appear in the delta");
    }

    @Test
    void synchronizeReactiveConfiguration_withNoChanges_skipsJsCall() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        conf.setTitle("initial");
        chart.seedReactiveSnapshot();

        chart.synchronizeReactiveConfiguration();

        verify(chart.getElement(), never())
                .callJsFunction(eq("updateConfiguration"), any(), any());
    }

    @Test
    void multipleMutations_betweenSyncs_collapseIntoSingleDelta() {
        TestChart chart = new TestChart();
        Configuration conf = chart.getConfiguration();
        conf.setTitle("initial");
        chart.seedReactiveSnapshot();

        conf.getTitle().setText("a");
        conf.getTitle().setText("b");
        conf.getTitle().setText("c");

        chart.synchronizeReactiveConfiguration();

        ArgumentCaptor<ObjectNode> deltaCaptor = ArgumentCaptor
                .forClass(ObjectNode.class);
        verify(chart.getElement()).callJsFunction(eq("updateConfiguration"),
                deltaCaptor.capture(), eq(false));
        ObjectNode delta = deltaCaptor.getValue();
        assertEquals("c", delta.get("title").get("text").asString(),
                "Only the final value of a collapsed sync should hit the wire");
    }
}

package com.vaadin.flow.component.details;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasTooltip;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DetailsTest {

    private Details details;

    @Before
    public void setup() {
        details = new Details();
    }

    @Test
    public void initContent() {
        details.setContent(new Span());
        details.addContent(new Span());
        Assert.assertEquals(2, details.getContent().count());

        details.setContent(new Span());
        Assert.assertEquals(1, details.getContent().count());
    }

    @Test
    public void noSummaryDefined_getSummaryText_returnsEmptyString() {
        Assert.assertEquals("", details.getSummaryText());
    }

    @Test
    public void summaryDefined_getSummaryText_returnsStringDefined() {
        details.setSummaryText("summary");
        Assert.assertEquals("summary", details.getSummaryText());
    }

    @Test
    public void implementsHasTooltip() {
        Assert.assertTrue(details instanceof HasTooltip);
    }

    @Test
    public void unregisterOpenedChangeListenerOnEvent() {
        var listenerInvokedCount = new AtomicInteger(0);
        details.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        details.setOpened(true);
        details.setOpened(false);

        Assert.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    public void twoOpenedChangeListeners_bothListenersAreInvokedOnOpenedChange() {
        var listenerInvokedCount = new AtomicInteger(0);
        details.addOpenedChangeListener(
                e -> listenerInvokedCount.incrementAndGet());
        details.addOpenedChangeListener(
                e -> listenerInvokedCount.incrementAndGet());

        details.setOpened(true);

        Assert.assertEquals(2, listenerInvokedCount.get());
    }

    @Test
    public void twoOpenedChangeListeners_unregisterOneOnEvent_listenerIsInvokedOnOpenedChange() {
        var listenerInvokedCount = new AtomicInteger(0);
        details.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });
        details.addOpenedChangeListener(
                e -> listenerInvokedCount.incrementAndGet());

        details.setOpened(true);
        listenerInvokedCount.set(0);

        details.setOpened(false);

        Assert.assertEquals(1, listenerInvokedCount.get());
    }
}

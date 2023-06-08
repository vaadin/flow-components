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
        details.add(new Span());
        details.add(new Span());
        Assert.assertEquals(2, details.getContent().count());

        details.removeAll();
        details.add(new Span());
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
}

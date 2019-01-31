package com.vaadin.flow.component.details.test;

import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.details.testbench.DetailsElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BasicIT extends AbstractParallelTest {

    private List<DetailsElement> detailsElements;

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        detailsElements = $(DetailsElement.class).all();

        Assert.assertEquals(5, detailsElements.size());
    }

    @Test
    public void testSummary() {
        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertEquals("Some summary", detail1.getSummaryText());

        DetailsElement detail2 = detailsElements.get(1);
        Assert.assertEquals("Summary Text", detail2.getSummaryText());

        DetailsElement detail4   = detailsElements.get(3);
        List<String> themes = Arrays.asList( detail4.getAttribute("theme").split(" "));
        Assert.assertTrue(themes.containsAll(
                Stream.of(DetailsVariant.values())
                        .map(DetailsVariant::getVariantName).collect(Collectors.toList())));
        Assert.assertEquals("Small Reversed Filled Summary", detail4.getSummaryText());
    }

    @Test
    public void testOpenedChange() {
        DetailsElement detail = detailsElements.get(4);
        detail.toggle();
        Assert.assertEquals("opened-change",
                $(NotificationElement.class).first().getText());
    }

    @Test
    public void testContent() {
        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertFalse(detail1.isOpened());
        detail1.toggle();
        Assert.assertTrue(detail1.isOpened());
        Assert.assertEquals("Some content", detail1.getContentText());

        DetailsElement detail2 = detailsElements.get(1);
        Assert.assertTrue(detail2.isOpened());
        Assert.assertEquals("Content Text", detail2.getContentText());
        detail2.toggle();
        Assert.assertFalse(detail2.isOpened());

        DetailsElement detail3 = detailsElements.get(2);
        Assert.assertTrue(detail3.isOpened());
        Assert.assertFalse(detail3.isEnabled());
        Assert.assertEquals("Always visible content", detail3.getContentText());
        // TODO: uncomment when https://github.com/vaadin/vaadin-details/issues/4 is fixed
//        detail3.toggle();
//        Assert.assertTrue(detail3.isOpened());
    }
}

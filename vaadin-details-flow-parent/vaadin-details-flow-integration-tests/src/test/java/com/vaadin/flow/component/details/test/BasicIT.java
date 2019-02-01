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

        Assert.assertEquals(4, detailsElements.size());
    }

    @Test
    public void testSummary() {
        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertEquals("Some summary", detail1.getSummaryText());

        DetailsElement detailsThemed   = detailsElements.get(2);
        List<String> themes = Arrays.asList( detailsThemed.getAttribute("theme").split(" "));
        Assert.assertTrue(themes.containsAll(
                Stream.of(DetailsVariant.values())
                        .map(DetailsVariant::getVariantName).collect(Collectors.toList())));
        Assert.assertEquals("Small Reversed Filled Summary", detailsThemed.getSummaryText());
    }

    @Test
    public void testOpenedChange() {
        DetailsElement detail = detailsElements.get(3);
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
        Assert.assertEquals("Some content", detail1.getContent().getText());

        DetailsElement detailsDisabled = detailsElements.get(1);
        Assert.assertTrue(detailsDisabled.isOpened());
        Assert.assertFalse(detailsDisabled.isEnabled());
        Assert.assertEquals("Disabled content",
                detailsDisabled.getContent().$("h3").first().getText());
        Assert.assertEquals("Always visible content",
                detailsDisabled.getContent().$("span").first().getText());

        // TODO: uncomment when https://github.com/vaadin/vaadin-details/issues/4 is fixed
        //  detail3.toggle();
        //  Assert.assertTrue(detail3.isOpened());
    }
}

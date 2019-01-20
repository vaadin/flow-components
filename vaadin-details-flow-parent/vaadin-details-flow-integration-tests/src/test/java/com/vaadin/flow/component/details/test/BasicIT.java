package com.vaadin.flow.component.details.test;

import com.vaadin.flow.component.details.testbench.DetailsElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BasicIT extends AbstractParallelTest {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void testSummary() {
        List<DetailsElement> detailsElements = $(DetailsElement.class).all();

        Assert.assertEquals(2, detailsElements.size());

        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertEquals("Some summary", detail1.getSummaryText());

        DetailsElement detail2 = detailsElements.get(1);
        Assert.assertEquals("Summary Text", detail2.getSummaryText());
    }

    @Test
    public void testContent() {
        List<DetailsElement> detailsElements = $(DetailsElement.class).all();

        Assert.assertEquals(2, detailsElements.size());

        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertFalse(detail1.isOpened());
        detail1.getSummary().click();
        Assert.assertTrue(detail1.isOpened());
        Assert.assertEquals("Some content", detail1.getContentText());

        DetailsElement detail2 = detailsElements.get(1);
        Assert.assertTrue(detail2.isOpened());
        Assert.assertEquals("Content Text", detail2.getContentText());
        detail2.getSummary().click();
        Assert.assertFalse(detail2.isOpened());
    }
}

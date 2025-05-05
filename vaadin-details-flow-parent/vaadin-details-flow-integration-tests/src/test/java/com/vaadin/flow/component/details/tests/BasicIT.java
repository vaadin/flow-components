/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.details.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.details.testbench.DetailsElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-details")
public class BasicIT extends AbstractComponentIT {

    private List<DetailsElement> detailsElements;

    @Before
    public void init() {
        open();
        detailsElements = $(DetailsElement.class).all();

        Assert.assertEquals(2, detailsElements.size());
    }

    @Test
    public void testSummary() {
        DetailsElement detail1 = detailsElements.get(0);
        Assert.assertEquals("Some summary", detail1.getSummaryText());
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

        // TODO: uncomment when
        // https://github.com/vaadin/vaadin-details/issues/4 is fixed
        // detail3.toggle();
        // Assert.assertTrue(detail3.isOpened());
    }
}

/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasTooltip;

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
}

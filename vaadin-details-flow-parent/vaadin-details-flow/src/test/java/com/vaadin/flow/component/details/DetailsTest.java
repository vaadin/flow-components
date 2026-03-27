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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;

class DetailsTest {

    private Details details;

    @BeforeEach
    void setup() {
        details = new Details();
    }

    @Test
    void initContent() {
        details.add(new Span(), new Span());
        Assertions.assertEquals(2, details.getContent().count());

        details.removeAll();
        details.add(new Span());
        Assertions.assertEquals(1, details.getContent().count());
    }

    @Test
    void noSummaryDefined_getSummaryText_returnsEmptyString() {
        Assertions.assertEquals("", details.getSummaryText());
    }

    @Test
    void summaryDefined_getSummaryText_returnsStringDefined() {
        details.setSummaryText("summary");
        Assertions.assertEquals("summary", details.getSummaryText());
    }

    @Test
    void implementsHasTooltip() {
        Assertions.assertTrue(details instanceof HasTooltip);
    }

    @Test
    void unregisterOpenedChangeListenerOnEvent() {
        var listenerInvokedCount = new AtomicInteger(0);
        details.addOpenedChangeListener(e -> {
            listenerInvokedCount.incrementAndGet();
            e.unregisterListener();
        });

        details.setOpened(true);
        details.setOpened(false);

        Assertions.assertEquals(1, listenerInvokedCount.get());
    }

    @Test
    void implementsHasThemeVariant() {
        Assertions.assertTrue(
                HasThemeVariant.class.isAssignableFrom(Details.class));
    }
}

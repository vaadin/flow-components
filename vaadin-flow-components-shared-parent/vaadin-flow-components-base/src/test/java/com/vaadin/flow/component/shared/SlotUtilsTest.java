/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;

/**
 * Tests for the {@link SlotUtils}.
 */
public class SlotUtilsTest {

    private static final String TEST_SLOT = "testSlot";
    private static final String OTHER_SLOT = "otherSlot";

    @Test
    public void clearSlotShouldOnlyRemoveElementsFromMatchingSlot() {
        Element div = ElementFactory.createDiv();
        div.appendChild(span(TEST_SLOT));
        div.appendChild(span(OTHER_SLOT));

        HasElement hasElement = () -> div;

        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(hasElement, TEST_SLOT).count());
        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(hasElement, OTHER_SLOT).count());

        SlotUtils.clearSlot(hasElement, TEST_SLOT);

        Assert.assertEquals(0,
                SlotUtils.getElementsInSlot(hasElement, TEST_SLOT).count());
        Assert.assertEquals(1,
                SlotUtils.getElementsInSlot(hasElement, OTHER_SLOT).count());
    }

    private static Element span(String slot) {
        Element span = ElementFactory.createSpan();
        span.setAttribute("slot", slot);
        return span;
    }
}

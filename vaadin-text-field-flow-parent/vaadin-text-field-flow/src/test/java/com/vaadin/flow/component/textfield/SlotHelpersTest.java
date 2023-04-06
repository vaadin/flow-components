
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link SlotHelpers}.
 */
public class SlotHelpersTest {

    private static final String TEST_SLOT = "testSlot";
    private static final String OTHER_SLOT = "otherSlot";

    @Test
    public void clearSlotShouldOnlyRemoveElementsFromMatchingSlot() {
        Element div = ElementFactory.createDiv();
        div.appendChild(span(TEST_SLOT));
        div.appendChild(span(OTHER_SLOT));

        HasElement hasElement = () -> div;

        assertThat(SlotHelpers.getElementsInSlot(hasElement, TEST_SLOT).count(),
                is(1L));
        assertThat(
                SlotHelpers.getElementsInSlot(hasElement, OTHER_SLOT).count(),
                is(1L));

        SlotHelpers.clearSlot(hasElement, TEST_SLOT);

        assertThat(SlotHelpers.getElementsInSlot(hasElement, TEST_SLOT).count(),
                is(0L));
        assertThat(
                SlotHelpers.getElementsInSlot(hasElement, OTHER_SLOT).count(),
                is(1L));
    }

    private static Element span(String slot) {
        Element span = ElementFactory.createSpan();
        span.setAttribute("slot", slot);
        return span;
    }
}

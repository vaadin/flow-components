/*
 * Copyright 2000-2017 Vaadin Ltd.
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
		Element div=ElementFactory.createDiv();
		div.appendChild(span(TEST_SLOT));
		div.appendChild(span(OTHER_SLOT));
		
		HasElement hasElement=() -> div;
		
		assertThat(SlotHelpers.getElementsInSlot(hasElement, TEST_SLOT).count(), is(1L));
		assertThat(SlotHelpers.getElementsInSlot(hasElement, OTHER_SLOT).count(), is(1L));
		
		SlotHelpers.clearSlot(hasElement, TEST_SLOT); 
		
		assertThat(SlotHelpers.getElementsInSlot(hasElement, TEST_SLOT).count(), is(0L));
		assertThat(SlotHelpers.getElementsInSlot(hasElement, OTHER_SLOT).count(), is(1L));
	}
	
	private static Element span(String slot) {
		Element span = ElementFactory.createSpan();
		span.setAttribute("slot", slot);
		return span;
	}
}

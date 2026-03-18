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
package com.vaadin.flow.component.shared;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class HasTooltipTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final TestComponent component = new TestComponent();

    @Test
    void default_doesNotHaveTooltipElement() {
        Assertions.assertFalse(getTooltipElement(component).isPresent());
    }

    @Test
    void getTooltip_hasTooltipElement() {
        var tooltip = component.getTooltip();
        Assertions.assertNotNull(tooltip);
        Assertions.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    void getTooltip_hasNoText() {
        var tooltip = component.getTooltip();
        Assertions.assertNull(tooltip.getText());
    }

    @Test
    void setTooltipText_hasTooltipElement() {
        component.setTooltipText("foo");
        Assertions.assertTrue(getTooltipElement(component).isPresent());
    }

    @Test
    void setTooltipText_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        Assertions.assertEquals(tooltip, component.getTooltip());
    }

    @Test
    void setTooltipTextAgain_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText("bar");
        Assertions.assertEquals(tooltip, tooltip2);
        Assertions.assertEquals("bar", component.getTooltip().getText());
    }

    @Test
    void setTooltipTextNull_hasTooltip() {
        var tooltip = component.setTooltipText("foo");
        var tooltip2 = component.setTooltipText(null);
        Assertions.assertEquals(tooltip, tooltip2);
        Assertions.assertEquals(null, component.getTooltip().getText());
    }

    @Test
    void setTooltipText_tooltipHasText() {
        component.setTooltipText("foo");
        Assertions.assertEquals("foo",
                getTooltipElement(component).get().getProperty("text"));
        Assertions.assertFalse(getTooltipElement(component).get()
                .getProperty("markdown", false));
    }

    @Test
    void setTooltipMarkdown_tooltipHasMarkdown() {
        component.setTooltipMarkdown("**Markdown** _foo_");
        Assertions.assertEquals("**Markdown** _foo_",
                getTooltipElement(component).get().getProperty("text"));
        Assertions.assertTrue(getTooltipElement(component).get()
                .getProperty("markdown", false));
    }

    @Test
    void switchContentTypes() {
        component.setTooltipText("foo");
        Assertions.assertFalse(getTooltipElement(component).get()
                .getProperty("markdown", false));

        component.setTooltipMarkdown("**Markdown** _foo_");
        Assertions.assertTrue(getTooltipElement(component).get()
                .getProperty("markdown", false));

        component.setTooltipText("foo");
        Assertions.assertFalse(getTooltipElement(component).get()
                .getProperty("markdown", false));
    }

    @Test
    void setTooltipText_tooltipHasSlot() {
        component.setTooltipText("foo");
        Assertions.assertEquals("tooltip",
                getTooltipElement(component).get().getAttribute("slot"));
    }

    @Test
    void setTooltipTextAgain_hasOneTooltipElement() {
        component.setTooltipText("foo");
        component.setTooltipText("bar");
        Assertions.assertEquals(1, getTooltipElements(component).count());
    }

    private Optional<Element> getTooltipElement(HasTooltip component) {
        return getTooltipElements(component).findFirst();
    }

    private Stream<Element> getTooltipElements(HasTooltip component) {
        return component.getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"));
    }

    @Tag("test")
    private static class TestComponent extends Component implements HasTooltip {
    }
}

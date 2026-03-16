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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.shared.Tooltip.TooltipPosition;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class TooltipTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final TestComponent component = new TestComponent();

    @AfterEach
    void tearDown() {
        // UI.removeAll breaks when tooltip is removed in detach listener of the
        // component, so remove manually beforehand
        getTooltipElement().ifPresent(Element::removeFromTree);
    }

    @Test
    void createTooltip_tooltipNotAttached() {
        Tooltip.forComponent(component);
        Assertions.assertFalse(getTooltipElement().isPresent());
    }

    @Test
    void createTooltip_addComponent_tooltipAttached() {
        Tooltip.forComponent(component);
        ui.add(component);
        Assertions.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    void addComponent_createTooltip_tooltipAttached() {
        ui.add(component);
        Tooltip.forComponent(component);
        Assertions.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    void createTooltip_removeComponent_tooltipNotAttached() {
        Tooltip.forComponent(component);
        ui.add(component);
        ui.remove(component);
        Assertions.assertFalse(getTooltipElement().isPresent());
    }

    @Test
    void addComponent_createTooltip_changeUI_tooltipAttached() {
        ui.add(component);
        Tooltip.forComponent(component);

        // Create a new UI and move the component to it (@PreserveOnRefresh)
        component.getElement().removeFromTree(false);
        ui.replaceUI();
        ui.add(component);

        Assertions.assertTrue(getTooltipElement().isPresent());
    }

    @Test
    void createTooltip_setText() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setText("foo");
        ui.add(component);
        Assertions.assertEquals("foo",
                getTooltipElement().get().getProperty("text"));
        Assertions.assertEquals("foo", tooltip.getText());
        Assertions.assertFalse(
                getTooltipElement().get().getProperty("markdown", false));
    }

    @Test
    void createTooltip_setMarkdown() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setMarkdown("**Markdown** _foo_");
        ui.add(component);
        Assertions.assertEquals("**Markdown** _foo_",
                getTooltipElement().get().getProperty("text"));
        Assertions.assertEquals("**Markdown** _foo_", tooltip.getText());
        Assertions.assertTrue(
                getTooltipElement().get().getProperty("markdown", false));
    }

    @Test
    void createTooltip_switchContentType() {
        var tooltip = Tooltip.forComponent(component);
        ui.add(component);

        tooltip.setText("foo");
        Assertions.assertFalse(
                getTooltipElement().get().getProperty("markdown", false));

        tooltip.setMarkdown("**Markdown** _foo_");
        Assertions.assertTrue(
                getTooltipElement().get().getProperty("markdown", false));

        tooltip.setText("foo");
        Assertions.assertFalse(
                getTooltipElement().get().getProperty("markdown", false));
    }

    @Test
    void createTooltip_setFocusDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setFocusDelay(1000);
        ui.add(component);
        Assertions.assertEquals(1000,
                getTooltipElement().get().getProperty("focusDelay", 0));
        Assertions.assertEquals(1000, tooltip.getFocusDelay());
    }

    @Test
    void createTooltip_setHideDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setHideDelay(1000);
        ui.add(component);
        Assertions.assertEquals(1000,
                getTooltipElement().get().getProperty("hideDelay", 0));
        Assertions.assertEquals(1000, tooltip.getHideDelay());
    }

    @Test
    void createTooltip_setHoverDelay() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setHoverDelay(1000);
        ui.add(component);
        Assertions.assertEquals(1000,
                getTooltipElement().get().getProperty("hoverDelay", 0));
        Assertions.assertEquals(1000, tooltip.getHoverDelay());
    }

    @Test
    void createTooltip_setPosition() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setPosition(TooltipPosition.END);
        ui.add(component);
        Assertions.assertEquals("end",
                getTooltipElement().get().getProperty("position"));
        Assertions.assertEquals(TooltipPosition.END, tooltip.getPosition());
    }

    @Test
    void createTooltip_defaultPosition() {
        var tooltip = Tooltip.forComponent(component);
        Assertions.assertEquals(null, tooltip.getPosition());
    }

    @Test
    void createTooltip_setManual() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setManual(true);
        ui.add(component);
        Assertions.assertEquals(true,
                getTooltipElement().get().getProperty("manual", false));
        Assertions.assertEquals(true, tooltip.isManual());
    }

    @Test
    void createTooltip_setOpened() {
        var tooltip = Tooltip.forComponent(component);
        tooltip.setOpened(true);
        ui.add(component);
        Assertions.assertEquals(true,
                getTooltipElement().get().getProperty("opened", false));
        Assertions.assertEquals(true, tooltip.isOpened());
    }

    @Test
    void tooltipForCompopnentTwice_sameReference() {
        var tooltip = Tooltip.forComponent(component);
        var tooltip2 = Tooltip.forComponent(component);
        Assertions.assertSame(tooltip, tooltip2);
    }

    @Test
    void createTooltip_fluentAPI() {
        ui.add(component);

        var tooltip = Tooltip.forComponent(component).withText("foo")
                .withFocusDelay(200).withHideDelay(1000).withHoverDelay(500)
                .withPosition(TooltipPosition.BOTTOM_END).withManual(true);

        tooltip.setOpened(true);

        Assertions.assertEquals("foo",
                getTooltipElement().get().getProperty("text"));
        Assertions.assertFalse(
                getTooltipElement().get().getProperty("markdown", false));
        Assertions.assertEquals(200,
                getTooltipElement().get().getProperty("focusDelay", 0));
        Assertions.assertEquals(1000,
                getTooltipElement().get().getProperty("hideDelay", 0));
        Assertions.assertEquals(500,
                getTooltipElement().get().getProperty("hoverDelay", 0));
        Assertions.assertEquals("bottom-end",
                getTooltipElement().get().getProperty("position"));
        Assertions.assertEquals(true,
                getTooltipElement().get().getProperty("manual", false));
    }

    @Test
    void createTooltip_fluentAPI_withMarkdown() {
        ui.add(component);

        var tooltip = Tooltip.forComponent(component)
                .withMarkdown("**Bold** _italic_");

        Assertions.assertNotNull(tooltip);

        Assertions.assertEquals("**Bold** _italic_",
                getTooltipElement().get().getProperty("text"));
        Assertions.assertTrue(
                getTooltipElement().get().getProperty("markdown", false));
    }

    private Optional<Element> getTooltipElement() {
        return ui.getUI().getElement().getChildren()
                .filter(child -> child.getTag().equals("vaadin-tooltip"))
                .findFirst();
    }

    @Tag("test")
    private static class TestComponent extends Component {
    }
}

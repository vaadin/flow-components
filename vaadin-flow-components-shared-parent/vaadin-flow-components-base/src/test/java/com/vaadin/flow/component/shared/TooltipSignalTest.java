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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Tests for {@link HasTooltip#bindTooltipText(com.vaadin.flow.signals.Signal)}
 * and {@link Tooltip#bindText(com.vaadin.flow.signals.Signal)}.
 */
public class TooltipSignalTest extends AbstractSignalsUnitTest {

    @Tag("test-component")
    public static class TestComponent extends Component implements HasTooltip {
    }

    private TestComponent component;

    @Before
    public void setup() {
        component = new TestComponent();
    }

    @After
    public void tearDown() {
        if (component != null && component.isAttached()) {
            component.removeFromParent();
        }
    }

    // ===== HasTooltip.bindTooltipText TESTS =====

    @Test
    public void bindTooltipText_signalBound_tooltipTextSynchronized() {
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("");
        component.bindTooltipText(signal);

        signal.set("Tooltip text");
        Tooltip tooltip = component.getTooltip();
        Assert.assertNotNull(tooltip);
        Assert.assertEquals("Tooltip text", tooltip.getText());
    }

    @Test
    public void bindTooltipText_signalBound_noEffectWhenDetached() {
        ValueSignal<String> signal = new ValueSignal<>("initial");
        component.bindTooltipText(signal);
        // Not attached to UI

        Tooltip tooltip = component.getTooltip();
        String initial = tooltip.getText();
        signal.set("updated");
        Assert.assertEquals(initial, tooltip.getText());
    }

    @Test(expected = BindingActiveException.class)
    public void bindTooltipText_setTooltipTextWhileBound_throwsException() {
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        component.bindTooltipText(signal);

        component.setTooltipText("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void bindTooltipText_bindAgainWhileBound_throwsException() {
        UI.getCurrent().add(component);

        ValueSignal<String> signal = new ValueSignal<>("initial");
        component.bindTooltipText(signal);

        component.bindTooltipText(new ValueSignal<>("other"));
    }

    // ===== Tooltip.bindText TESTS =====

    @Test
    public void tooltipBindText_signalBound_textSynchronized() {
        UI.getCurrent().add(component);

        Tooltip tooltip = component.getTooltip();
        ValueSignal<String> signal = new ValueSignal<>("tooltip text");
        tooltip.bindText(signal);

        Assert.assertEquals("tooltip text", tooltip.getText());

        signal.set("updated tooltip");
        Assert.assertEquals("updated tooltip", tooltip.getText());
    }

    @Test
    public void tooltipBindText_signalBound_noEffectWhenDetached() {
        Tooltip tooltip = component.getTooltip();
        ValueSignal<String> signal = new ValueSignal<>("initial");
        tooltip.bindText(signal);
        // Not attached to UI

        String initial = tooltip.getText();
        signal.set("updated");
        Assert.assertEquals(initial, tooltip.getText());
    }

    @Test(expected = BindingActiveException.class)
    public void tooltipBindText_setTextWhileBound_throwsException() {
        UI.getCurrent().add(component);

        Tooltip tooltip = component.getTooltip();
        ValueSignal<String> signal = new ValueSignal<>("initial");
        tooltip.bindText(signal);

        tooltip.setText("manual");
    }

    @Test(expected = BindingActiveException.class)
    public void tooltipBindText_bindAgainWhileBound_throwsException() {
        UI.getCurrent().add(component);

        Tooltip tooltip = component.getTooltip();
        ValueSignal<String> signal = new ValueSignal<>("initial");
        tooltip.bindText(signal);

        tooltip.bindText(new ValueSignal<>("other"));
    }

    @Test(expected = NullPointerException.class)
    public void tooltipBindText_nullSignal_throwsNPE() {
        UI.getCurrent().add(component);

        Tooltip tooltip = component.getTooltip();
        tooltip.bindText(null);
    }
}

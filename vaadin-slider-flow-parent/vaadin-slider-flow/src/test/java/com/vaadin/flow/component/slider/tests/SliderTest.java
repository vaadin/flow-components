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
package com.vaadin.flow.component.slider.tests;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.slider.Slider;

public class SliderTest {
    @Test
    public void defaultConstructor() {
        Slider slider = new Slider();
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(0.0, slider.getValue(), 0.0);
    }

    @Test
    public void minMaxConstructor() {
        Slider slider = new Slider(10.0, 50.0);
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(10.0, slider.getValue(), 0.0);
    }

    @Test
    public void minMaxListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(10.0, 50.0, e -> listenerInvoked.set(true));
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(10.0, slider.getValue(), 0.0);

        slider.setValue(25.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void listenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider(e -> listenerInvoked.set(true));
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(0.0, slider.getValue(), 0.0);

        slider.setValue(50.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelConstructor() {
        Slider slider = new Slider("Label");
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(0.0, slider.getValue(), 0.0);
    }

    @Test
    public void labelMinMaxConstructor() {
        Slider slider = new Slider("Label", 10.0, 50.0);
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(10.0, slider.getValue(), 0.0);
    }

    @Test
    public void labelListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider("Label", e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(0.0, slider.getMin(), 0.0);
        Assert.assertEquals(100.0, slider.getMax(), 0.0);
        Assert.assertEquals(0.0, slider.getValue(), 0.0);

        slider.setValue(50.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test
    public void labelMinMaxListenerConstructor() {
        AtomicBoolean listenerInvoked = new AtomicBoolean(false);
        Slider slider = new Slider("Label", 10.0, 50.0,
                e -> listenerInvoked.set(true));
        Assert.assertEquals("Label", slider.getLabel());
        Assert.assertEquals(10.0, slider.getMin(), 0.0);
        Assert.assertEquals(50.0, slider.getMax(), 0.0);
        Assert.assertEquals(10.0, slider.getValue(), 0.0);

        slider.setValue(25.0);
        Assert.assertTrue(listenerInvoked.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_lessThanMin_throws() {
        Slider slider = new Slider(10, 100);
        slider.setValue(5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setValue_greaterThanMax_throws() {
        Slider slider = new Slider(0, 100);
        slider.setValue(150.0);
    }

    public void implementsHasSizeInterface() {
        Slider slider = new Slider();
        Assert.assertTrue(slider instanceof HasSize);
    }

    public void implementsFocusableInterface() {
        Slider slider = new Slider();
        Assert.assertTrue(slider instanceof Focusable);
    }

    public void implementsKeyNotifierInterface() {
        Slider slider = new Slider();
        Assert.assertTrue(slider instanceof KeyNotifier);
    }
}

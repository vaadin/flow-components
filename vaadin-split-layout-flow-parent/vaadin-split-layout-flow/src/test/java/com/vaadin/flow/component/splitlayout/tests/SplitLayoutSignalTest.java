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
package com.vaadin.flow.component.splitlayout.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class SplitLayoutSignalTest extends AbstractSignalsUnitTest {

    private SplitLayout splitLayout;
    private ValueSignal<Double> signal;

    @Before
    public void setup() {
        splitLayout = new SplitLayout();
        signal = new ValueSignal<>(50.0);
    }

    @After
    public void tearDown() {
        if (splitLayout != null && splitLayout.isAttached()) {
            splitLayout.removeFromParent();
        }
    }

    @Test
    public void bindSplitterPosition_signalBound_propertySync() {
        splitLayout.bindSplitterPosition(signal, signal::set);
        UI.getCurrent().add(splitLayout);

        Assert.assertEquals(50.0, splitLayout.getSplitterPosition(), 0.01);

        signal.set(75.0);
        Assert.assertEquals(75.0, splitLayout.getSplitterPosition(), 0.01);

        signal.set(25.0);
        Assert.assertEquals(25.0, splitLayout.getSplitterPosition(), 0.01);
    }

    @Test
    public void bindSplitterPosition_notAttached_noEffect() {
        splitLayout.bindSplitterPosition(signal, signal::set);

        Double initial = splitLayout.getSplitterPosition();
        signal.set(75.0);
        Assert.assertEquals(initial, splitLayout.getSplitterPosition());
    }

    @Test
    public void bindSplitterPosition_detachAndReattach() {
        splitLayout.bindSplitterPosition(signal, signal::set);
        UI.getCurrent().add(splitLayout);

        signal.set(75.0);
        Assert.assertEquals(75.0, splitLayout.getSplitterPosition(), 0.01);

        splitLayout.removeFromParent();
        signal.set(30.0);
        Assert.assertEquals(75.0, splitLayout.getSplitterPosition(), 0.01);

        UI.getCurrent().add(splitLayout);
        Assert.assertEquals(30.0, splitLayout.getSplitterPosition(), 0.01);
    }

    @Test(expected = BindingActiveException.class)
    public void bindSplitterPosition_setWhileBound_throws() {
        splitLayout.bindSplitterPosition(signal, signal::set);
        UI.getCurrent().add(splitLayout);

        splitLayout.setSplitterPosition(80.0);
    }

    @Test(expected = BindingActiveException.class)
    public void bindSplitterPosition_doubleBind_throws() {
        splitLayout.bindSplitterPosition(signal, signal::set);
        var other = new ValueSignal<>(30.0);
        splitLayout.bindSplitterPosition(other, other::set);
    }

    @Test(expected = NullPointerException.class)
    public void bindSplitterPosition_nullSignal_throwsNPE() {
        splitLayout.bindSplitterPosition(null, null);
    }
}

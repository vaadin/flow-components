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
package com.vaadin.flow.component.confirmdialog;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ConfirmDialogSignalTest extends AbstractSignalsUnitTest {
    private ConfirmDialog dialog;

    @Before
    public void setUp() {
        dialog = new ConfirmDialog();
        ui.add(dialog);
    }

    @Test
    public void bindWidth_widthSynchronized() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assert.assertEquals("400px", dialog.getWidth());

        signal.set("500px");
        Assert.assertEquals("500px", dialog.getWidth());
    }

    @Test
    public void bindWidth_detachAndReattach_widthSynchronizedWhenAttached() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        Assert.assertEquals("400px", dialog.getWidth());

        dialog.removeFromParent();

        signal.set("500px");
        Assert.assertEquals("400px", dialog.getWidth());

        ui.add(dialog);
        Assert.assertEquals("500px", dialog.getWidth());
    }

    @Test(expected = BindingActiveException.class)
    public void bindWidth_setWidthWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        dialog.setWidth("500px");
    }

    @Test(expected = BindingActiveException.class)
    public void bindWidth_bindWidthWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindWidth(signal);
        dialog.bindWidth(new ValueSignal<>("500px"));
    }

    @Test
    public void bindHeight_heightSynchronized() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assert.assertEquals("400px", dialog.getHeight());

        signal.set("500px");
        Assert.assertEquals("500px", dialog.getHeight());
    }

    @Test
    public void bindHeight_detachAndReattach_heightSynchronizedWhenAttached() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        Assert.assertEquals("400px", dialog.getHeight());

        dialog.removeFromParent();

        signal.set("500px");
        Assert.assertEquals("400px", dialog.getHeight());

        ui.add(dialog);
        Assert.assertEquals("500px", dialog.getHeight());
    }

    @Test(expected = BindingActiveException.class)
    public void bindHeight_setHeightWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        dialog.setHeight("500px");
    }

    @Test(expected = BindingActiveException.class)
    public void bindHeight_bindHeightWhileBound_throws() {
        var signal = new ValueSignal<>("400px");

        dialog.bindHeight(signal);
        dialog.bindHeight(new ValueSignal<>("500px"));
    }

}

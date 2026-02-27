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
package com.vaadin.flow.component.textfield.tests;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class BigDecimalFieldSignalTest extends AbstractSignalsUnitTest {

    private BigDecimalField bigDecimalField;
    private ValueSignal<Locale> localeSignal;

    @Before
    public void setup() {
        bigDecimalField = new BigDecimalField();
        localeSignal = new ValueSignal<>(Locale.US);
    }

    @After
    public void tearDown() {
        if (bigDecimalField != null && bigDecimalField.isAttached()) {
            bigDecimalField.removeFromParent();
        }
    }

    @Test
    public void bindLocale_signalBound_localeSynchronizedWhenAttached() {
        bigDecimalField.bindLocale(localeSignal);
        UI.getCurrent().add(bigDecimalField);

        Assert.assertEquals(Locale.US, bigDecimalField.getLocale());

        localeSignal.set(Locale.GERMANY);
        Assert.assertEquals(Locale.GERMANY, bigDecimalField.getLocale());

        localeSignal.set(Locale.FRANCE);
        Assert.assertEquals(Locale.FRANCE, bigDecimalField.getLocale());
    }

    @Test
    public void bindLocale_signalBound_noEffectWhenDetached() {
        bigDecimalField.bindLocale(localeSignal);
        // Not attached to UI

        Locale initial = bigDecimalField.getLocale();
        localeSignal.set(Locale.GERMANY);
        Assert.assertEquals(initial, bigDecimalField.getLocale());
    }

    @Test
    public void bindLocale_signalBound_detachAndReattach() {
        bigDecimalField.bindLocale(localeSignal);
        UI.getCurrent().add(bigDecimalField);
        Assert.assertEquals(Locale.US, bigDecimalField.getLocale());

        // Detach
        bigDecimalField.removeFromParent();
        localeSignal.set(Locale.GERMANY);
        Assert.assertEquals(Locale.US, bigDecimalField.getLocale());

        // Reattach
        UI.getCurrent().add(bigDecimalField);
        Assert.assertEquals(Locale.GERMANY, bigDecimalField.getLocale());

        localeSignal.set(Locale.FRANCE);
        Assert.assertEquals(Locale.FRANCE, bigDecimalField.getLocale());
    }

    @Test(expected = BindingActiveException.class)
    public void bindLocale_setLocaleWhileBound_throwsException() {
        bigDecimalField.bindLocale(localeSignal);
        UI.getCurrent().add(bigDecimalField);

        bigDecimalField.setLocale(Locale.GERMANY);
    }

    @Test(expected = BindingActiveException.class)
    public void bindLocale_bindAgainWhileBound_throwsException() {
        bigDecimalField.bindLocale(localeSignal);
        UI.getCurrent().add(bigDecimalField);

        bigDecimalField.bindLocale(new ValueSignal<>(Locale.GERMANY));
    }
}

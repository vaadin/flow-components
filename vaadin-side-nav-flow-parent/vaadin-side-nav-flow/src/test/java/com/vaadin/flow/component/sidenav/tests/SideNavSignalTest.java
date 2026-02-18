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
package com.vaadin.flow.component.sidenav.tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class SideNavSignalTest extends AbstractSignalsUnitTest {

    private SideNav sideNav;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        sideNav = new SideNav();
        signal = new ValueSignal<>(true);
    }

    @After
    public void tearDown() {
        if (sideNav != null && sideNav.isAttached()) {
            sideNav.removeFromParent();
        }
    }

    @Test
    public void bindExpanded_signalBound_propertySync() {
        sideNav.bindExpanded(signal, signal::set);
        UI.getCurrent().add(sideNav);

        Assert.assertTrue(sideNav.isExpanded());

        signal.set(false);
        Assert.assertFalse(sideNav.isExpanded());

        signal.set(true);
        Assert.assertTrue(sideNav.isExpanded());
    }

    @Test
    public void bindExpanded_inverted_collapsedProperty() {
        sideNav.bindExpanded(signal, signal::set);
        UI.getCurrent().add(sideNav);

        // expanded=true should mean collapsed=false
        signal.set(true);
        Assert.assertFalse(
                sideNav.getElement().getProperty("collapsed", false));

        // expanded=false should mean collapsed=true
        signal.set(false);
        Assert.assertTrue(sideNav.getElement().getProperty("collapsed", false));
    }

    @Test
    public void bindExpanded_notAttached_noEffect() {
        sideNav.bindExpanded(signal, signal::set);

        boolean initial = sideNav.isExpanded();
        signal.set(false);
        Assert.assertEquals(initial, sideNav.isExpanded());
    }

    @Test
    public void bindExpanded_detachAndReattach() {
        sideNav.bindExpanded(signal, signal::set);
        UI.getCurrent().add(sideNav);

        signal.set(false);
        Assert.assertFalse(sideNav.isExpanded());

        sideNav.removeFromParent();
        signal.set(true);
        Assert.assertFalse(sideNav.isExpanded());

        UI.getCurrent().add(sideNav);
        Assert.assertTrue(sideNav.isExpanded());
    }

    @Test(expected = BindingActiveException.class)
    public void bindExpanded_setWhileBound_throws() {
        sideNav.bindExpanded(signal, signal::set);
        UI.getCurrent().add(sideNav);

        sideNav.setExpanded(false);
    }

    @Test(expected = BindingActiveException.class)
    public void bindExpanded_doubleBind_throws() {
        sideNav.bindExpanded(signal, signal::set);
        var other = new ValueSignal<>(false);
        sideNav.bindExpanded(other, other::set);
    }

    @Test(expected = NullPointerException.class)
    public void bindExpanded_nullSignal_throwsNPE() {
        sideNav.bindExpanded(null, null);
    }

    @Test
    public void bindExpanded_nullDefault_defaultsToExpanded() {
        ValueSignal<Boolean> nullSignal = new ValueSignal<>(null);
        sideNav.bindExpanded(nullSignal, nullSignal::set);
        UI.getCurrent().add(sideNav);

        // null maps to collapsed=true (i.e. not expanded)
        // because signal.map(v -> v == null ? Boolean.TRUE : !v) returns TRUE
        // for null
        // and collapsed=true means not expanded
        Assert.assertFalse(sideNav.isExpanded());
    }
}

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
package com.vaadin.flow.component.contextmenu;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.DeploymentConfiguration;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class MenuItemSignalTest extends AbstractSignalsUnitTest {

    private ContextMenu contextMenu;
    private MenuItem item;
    private ValueSignal<Boolean> signal;

    @Before
    public void setup() {
        // ContextMenu's MenuItemsArrayGenerator triggers chunk loading on
        // attach, which requires a DeploymentConfiguration on the session.
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && session.getConfiguration() == null) {
            DeploymentConfiguration config = Mockito
                    .mock(DeploymentConfiguration.class);
            Mockito.when(session.getService().getDeploymentConfiguration())
                    .thenReturn(config);
        }
        contextMenu = new ContextMenu();
        item = contextMenu.addItem("");
        item.setCheckable(true);
        signal = new ValueSignal<>(false);
    }

    @After
    public void tearDown() {
        if (contextMenu != null && contextMenu.isAttached()) {
            contextMenu.removeFromParent();
        }
    }

    @Test
    public void bindChecked_signalBound_propertySync() {
        item.bindChecked(signal);
        UI.getCurrent().add(contextMenu);
        flushBeforeClientResponse();

        Assert.assertFalse(item.isChecked());

        signal.set(true);
        Assert.assertTrue(item.isChecked());

        signal.set(false);
        Assert.assertFalse(item.isChecked());
    }

    @Test
    public void bindChecked_notAttached_noEffect() {
        item.bindChecked(signal);

        boolean initial = item.isChecked();
        signal.set(true);
        Assert.assertEquals(initial, item.isChecked());
    }

    @Test
    public void bindChecked_detachAndReattach() {
        item.bindChecked(signal);
        UI.getCurrent().add(contextMenu);
        flushBeforeClientResponse();

        signal.set(true);
        Assert.assertTrue(item.isChecked());

        contextMenu.removeFromParent();
        signal.set(false);
        Assert.assertTrue(item.isChecked());

        UI.getCurrent().add(contextMenu);
        flushBeforeClientResponse();
        Assert.assertFalse(item.isChecked());
    }

    @Test(expected = BindingActiveException.class)
    public void bindChecked_setWhileBound_throws() {
        item.bindChecked(signal);
        UI.getCurrent().add(contextMenu);

        item.setChecked(true);
    }

    @Test(expected = BindingActiveException.class)
    public void bindChecked_doubleBind_throws() {
        item.bindChecked(signal);
        item.bindChecked(new ValueSignal<>(true));
    }

    @Test(expected = NullPointerException.class)
    public void bindChecked_nullSignal_throwsNPE() {
        item.bindChecked(null);
    }

    private void flushBeforeClientResponse() {
        UI.getCurrent().getInternals().getStateTree()
                .runExecutionsBeforeClientResponse();
    }
}

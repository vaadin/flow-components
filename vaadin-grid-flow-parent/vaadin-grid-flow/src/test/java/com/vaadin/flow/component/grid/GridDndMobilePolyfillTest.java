/*
 * Copyright 2000-2022 Vaadin Ltd.
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

package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

public class GridDndMobilePolyfillTest {

    private Grid<String> grid;
    private UI ui;

    @Before
    public void setup() {
        VaadinService service = Mockito.mock(VaadinService.class);
        VaadinSession session = Mockito.mock(VaadinSession.class);
        Mockito.when(session.getService()).thenReturn(service);
        Mockito.when(session.hasLock()).thenReturn(true);
        ui = new UI() {
            @Override
            public VaadinSession getSession() {
                return session;
            }
        };
        ui.getInternals().setSession(session);

        grid = new Grid<>();
        ui.add(grid);
    }

    @Test
    public void gridDnd_setDropMode_mobilePolyfillShouldBeAdded() {
        ui.getInternals().dumpPendingJavaScriptInvocations();
        grid.setDropMode(GridDropMode.ON_GRID);
        Assert.assertEquals(
                "Grid::setDropMode should add DnD mobile polyfill script to the UI.",
                1, ui.getInternals().dumpPendingJavaScriptInvocations().size());
    }

    @Test
    public void gridDnd_setRowsDraggable_mobilePolyfillShouldBeAdded() {
        ui.getInternals().dumpPendingJavaScriptInvocations();
        grid.setRowsDraggable(true);
        Assert.assertEquals(
                "Grid::setRowsDraggable should add DnD mobile polyfill script to the UI.",
                1, ui.getInternals().dumpPendingJavaScriptInvocations().size());
    }
}

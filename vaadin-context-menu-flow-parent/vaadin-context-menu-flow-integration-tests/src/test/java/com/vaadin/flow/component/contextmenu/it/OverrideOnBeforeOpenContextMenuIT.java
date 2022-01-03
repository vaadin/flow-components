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
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/override-on-before-open-context-menu")
public class OverrideOnBeforeOpenContextMenuIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        checkLogsForErrors();
    }

    @Test
    public void shouldNotOpenContextMenuWhenOnBeforeMenuOpenReturnsFalse() {
        verifyClosed();
        rightClickOn("no-open-menu-target");
        verifyClosed();
    }

    @Test
    public void shouldDynamicallyModifyContextMenuItems() {
        verifyClosed();

        rightClickOn("dynamic-context-menu-target");
        verifyOpened();

        Assert.assertEquals("Dynamic Item",
                getOverlay().getAttribute("innerText"));

        clickBody();
        verifyClosed();
    }
}

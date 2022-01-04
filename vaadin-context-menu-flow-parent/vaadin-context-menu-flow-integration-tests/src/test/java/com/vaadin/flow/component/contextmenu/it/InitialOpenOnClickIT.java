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

@TestPath("vaadin-context-menu/initial-open-on-click")
public class InitialOpenOnClickIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void menuHasOpenOnClickInitially_noClientSideErrors() {
        checkLogsForErrors();
    }

    @Test
    public void menuHasOpenOnClickInitially_clickTarget_menuOpens() {
        clickElementWithJs("target");
        verifyOpened();
        Assert.assertArrayEquals(new String[] { "foo" }, getMenuItemCaptions());
    }
}

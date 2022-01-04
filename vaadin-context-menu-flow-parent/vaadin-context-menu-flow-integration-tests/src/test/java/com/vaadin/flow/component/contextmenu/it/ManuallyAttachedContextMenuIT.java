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

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/manually-attached-context-menu")
public class ManuallyAttachedContextMenuIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        checkLogsForErrors();
    }

    @Test
    public void contextMenuAttachedToThePage_setItemChecked_open_itemChecked() {
        leftClickOn("toggle-checked");
        rightClickOn("target");
        TestBenchElement item = getMenuItems().get(0);
        ContextMenuPageIT.assertCheckedInClientSide(item, true);
    }
}

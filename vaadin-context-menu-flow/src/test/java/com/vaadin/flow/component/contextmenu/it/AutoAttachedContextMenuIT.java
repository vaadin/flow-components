/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("auto-attached-context-menu")
public class AutoAttachedContextMenuIT extends AbstractContextMenuIT {

    @Test
    public void contextMenuNotAttachedToThePage_openAndClose_contextMenuIsAttachedAndRemoved() {
        open();
        waitForElementPresent(By.tagName("label"));
        checkLogsForErrors();

        final String id = "not-attached-context-menu";
        waitForElementNotPresent(By.id(id));

        rightClickOn("target-for-not-attached-context-menu");
        waitForElementPresent(By.id(id));
        verifyOpened();

        getOverlay().click();
        waitForElementNotPresent(By.id(id));
        verifyClosed();
    }
}

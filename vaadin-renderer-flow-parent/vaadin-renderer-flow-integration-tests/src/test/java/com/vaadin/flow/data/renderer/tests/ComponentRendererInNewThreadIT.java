/*
 * Copyright 2000-2023 Vaadin Ltd.
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
package com.vaadin.flow.data.renderer.tests;

import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-renderer-flow/component-renderer-in-new-thread")
public class ComponentRendererInNewThreadIT extends AbstractComponentIT {

    @Test
    public void componentRendererInNewThread_uiNotNullWhileTemplateExpressionIsCalculated() {
        open();
        findElement(By.id("add-component")).click();

        waitUntil(driver -> findElement(By.id("null-ui-count")) != null
                && findElement(By.id("non-null-ui-count")) != null, 2);

        Assert.assertEquals("0", findElement(By.id("null-ui-count")).getText());
        Assert.assertNotEquals("0",
                findElement(By.id("non-null-ui-count")).getText());
    }
}

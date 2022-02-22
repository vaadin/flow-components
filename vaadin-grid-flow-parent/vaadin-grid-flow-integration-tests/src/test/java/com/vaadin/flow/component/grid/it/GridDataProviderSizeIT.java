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
package com.vaadin.flow.component.grid.it;

import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@TestPath("vaadin-grid/grid-data-provider-size")
public class GridDataProviderSizeIT extends AbstractComponentIT {

    @Test
    public void sizeInBackEndCalledOnce() {
        open();

        WebElement info = $("div").id("info");
        Assert.assertEquals("sizeInBackEnd should be called once", "1",
                info.getText());
    }
}

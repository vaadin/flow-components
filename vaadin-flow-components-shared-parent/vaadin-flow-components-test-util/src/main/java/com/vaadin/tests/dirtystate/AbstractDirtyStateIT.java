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
package com.vaadin.tests.dirtystate;

import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.vaadin.tests.dirtystate.AbstractDirtyStatePage.DIRTY_STATE;
import static com.vaadin.tests.dirtystate.AbstractDirtyStatePage.DIRTY_STATE_BUTTON;

public abstract class AbstractDirtyStateIT<T extends TestBenchElement>
        extends AbstractComponentIT {
    protected T testField;

    @Before
    public void init() {
        open();
        testField = getTestField();
    }

    @Test
    public void notDirtyByDefault() {
        Assert.assertFalse(isDirty());
    }

    @Test
    public void clientSideDirtyStateIsPropagatedToServer() {
        testField.setProperty("dirty", true);
        Assert.assertTrue(isDirty());

        testField.setProperty("dirty", false);
        Assert.assertFalse(isDirty());
    }

    private boolean isDirty() {
        $("button").id(DIRTY_STATE_BUTTON).click();
        String actual = $("div").id(DIRTY_STATE).getText();
        return actual.equals("true");
    }

    protected abstract T getTestField();
}

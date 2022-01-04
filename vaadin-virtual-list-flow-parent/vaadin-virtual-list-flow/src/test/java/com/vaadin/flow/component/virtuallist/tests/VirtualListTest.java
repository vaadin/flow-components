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
package com.vaadin.flow.component.virtuallist.tests;

import com.vaadin.flow.component.virtuallist.VirtualList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VirtualListTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void templateWarningSuppressed() {
        VirtualList<String> virtualList = new VirtualList<>();

        Assert.assertTrue("Template warning is not suppressed", virtualList
                .getElement().hasAttribute("suppress-template-warning"));
    }

    @Test
    public void paging_pagingDisabledByDefault() {
        VirtualList<String> virtualList = new VirtualList<>();
        Assert.assertFalse("VirtualList is not supposed to support the paging",
                virtualList.getDataCommunicator().isPagingEnabled());
    }

    @Test
    public void paging_setPageSize_throws() {
        VirtualList<String> virtualList = new VirtualList<>();
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("VirtualList does not support paging");
        virtualList.getDataCommunicator().setPageSize(50);
    }

    @Test
    public void paging_setPagingEnabled_throws() {
        VirtualList<String> virtualList = new VirtualList<>();
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("VirtualList does not support paging");
        virtualList.getDataCommunicator().setPagingEnabled(true);
    }
}

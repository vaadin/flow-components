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
package com.vaadin.flow.component.ironlist.tests;

import com.vaadin.flow.component.ironlist.IronList;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IronListTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void paging_pagingDisabledByDefault() {
        IronList<String> ironList = new IronList<>();
        Assert.assertFalse("IronList is not supposed to support the paging",
                ironList.getDataCommunicator().isPagingEnabled());
    }

    @Test
    public void paging_setPageSize_throws() {
        IronList<String> ironList = new IronList<>();
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("IronList does not support paging");
        ironList.getDataCommunicator().setPageSize(50);
    }

    @Test
    public void paging_setPagingEnabled_throws() {
        IronList<String> ironList = new IronList<>();
        exceptionRule.expect(UnsupportedOperationException.class);
        exceptionRule.expectMessage("IronList does not support paging");
        ironList.getDataCommunicator().setPagingEnabled(true);
    }
}

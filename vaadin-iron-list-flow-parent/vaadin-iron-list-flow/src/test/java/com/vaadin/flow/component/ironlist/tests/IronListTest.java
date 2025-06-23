/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.ironlist.tests;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.ironlist.IronList;

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

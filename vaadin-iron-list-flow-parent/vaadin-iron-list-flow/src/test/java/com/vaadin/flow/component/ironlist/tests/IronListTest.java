
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

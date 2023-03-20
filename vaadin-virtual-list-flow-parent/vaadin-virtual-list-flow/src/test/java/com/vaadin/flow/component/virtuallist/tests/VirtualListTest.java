
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

package com.vaadin.addon.board.testbenchtests;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.board.testUI.RemoveComponentUI;
import com.vaadin.board.elements.BoardElement;
import com.vaadin.testbench.elements.AbstractComponentElement;

public class RemoveComponentIT extends AbstractParallelTest {

    @Override
    protected Class<?> getUIClass() {
        return RemoveComponentUI.class;
    }

    @Test
    public void basicLayout_removeComponentFromRow_removedComponentsNotShown() {
        openURL();
        BoardElement board =$(BoardElement.class).first();

        List<AbstractComponentElement> children = board.getRow(0).$(AbstractComponentElement.class).all();
        Assert.assertEquals("Board should have 2 children", 2, children.size());

    }

}

package com.vaadin.flow.component.board.test;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.board.testbench.BoardElement;
import com.vaadin.flow.component.board.testbench.RowElement;

public class RemoveComponentIT extends AbstractParallelTest {

    @Test
    public void basicLayout_removeComponentFromRow_removedComponentsNotShown() {
        open(RemoveComponentView.class);
        BoardElement board = $(BoardElement.class).first();
        RowElement row = board.getRow(0);
        Assert.assertEquals("Board should have 2 children", 2,
                row.getChildren().size());

    }

}

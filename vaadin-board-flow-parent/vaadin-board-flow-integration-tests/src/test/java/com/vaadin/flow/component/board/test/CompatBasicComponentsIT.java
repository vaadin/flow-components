package com.vaadin.flow.component.board.test;

import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.grid.testbench.GridElement;

/**
 *
 */
public class CompatBasicComponentsIT extends AbstractParallelTest {

    @Test
    public void testButton() throws Exception {
        open(CompatBasicComponents.ButtonView.class);
        ButtonElement testedElement = $(ButtonElement.class)
                .id(AbstractComponentTestView.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

    @Test
    public void testGrid() throws Exception {
        open(CompatBasicComponents.GridView.class);
        GridElement testedElement = $(GridElement.class)
                .id(AbstractComponentTestView.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }

}

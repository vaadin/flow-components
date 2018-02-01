package com.vaadin.flow.component.board.test;

import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;

public class LayoutsCompatabilityIT extends AbstractParallelTest {

    @Test
    public void testHorizontalLayout() throws Exception {
        open(CompatLayoutComponents.HorizontalLayoutUI.class);
        testSizes();
    }

    @Test
    public void testVerticalLayout() throws Exception {
        open(CompatLayoutComponents.VerticalLayoutView.class);
        testSizes();
    }

    public void testSizes() throws Exception {
        ButtonElement testedElement = $(ButtonElement.class)
                .id(AbstractComponentTestView.ID_PREFIX + 1);
        testGenericWidth(testedElement);
    }
}

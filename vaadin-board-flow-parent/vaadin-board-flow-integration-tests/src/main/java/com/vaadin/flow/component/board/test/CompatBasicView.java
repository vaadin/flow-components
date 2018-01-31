package com.vaadin.flow.component.board.test;

import com.vaadin.flow.component.Component;

/**
 *
 */
public abstract class CompatBasicView extends AbstractComponentTestView {

    abstract protected Component createTestComponent();

    protected Component[] createInstances(int n) {
        Component[] comps = new Component[n];
        for (int i = 0; i < n; i++) {
            comps[i] = createTestComponent();
        }
        return comps;
    }

    @Override
    protected Component[] createTestedComponents() {
        return createInstances(3);
    }
}

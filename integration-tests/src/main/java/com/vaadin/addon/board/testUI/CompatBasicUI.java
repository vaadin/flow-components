package com.vaadin.addon.board.testUI;

import com.vaadin.ui.Component;

/**
 *
 */
public abstract class CompatBasicUI extends AbstractTestCompUI {

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

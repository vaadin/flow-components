package com.vaadin.flow.component.contextmenu.it;

import com.vaadin.flow.component.html.testbench.DivElement;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * https://github.com/vaadin/vaadin-context-menu-flow/issues/119
 */
@TestPath("vaadin-context-menu/on-attach-listener")
public class ContextMenuCreatedOnAttachIT extends AbstractContextMenuIT {
    @Before
    public void init() {
        open();
    }

    @Test
    public void contextMenuCreatedOnAttach_leftClickOpensMenu() {
        final String TARGET_ID = "target-open-left-click";
        correctActionOpensMenu(() -> leftClickOn(TARGET_ID),() -> rightClickOn(TARGET_ID));
    }

    @Test
    public void contextMenuCreatedOnAttach_rightClickOpensMenu() {
        final String TARGET_ID = "target-open-right-click";
        correctActionOpensMenu(() -> rightClickOn(TARGET_ID),() -> leftClickOn(TARGET_ID));
    }

    private void correctActionOpensMenu(Runnable rightAction, Runnable wrongAction) {
        verifyClosed();
        wrongAction.run();
        verifyClosed();
        rightAction.run();
        verifyOpened();
    }
}

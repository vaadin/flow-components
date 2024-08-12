/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

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
        correctActionOpensMenu(() -> leftClickOn(TARGET_ID),
                () -> rightClickOn(TARGET_ID));
    }

    @Test
    public void contextMenuCreatedOnAttach_rightClickOpensMenu() {
        final String TARGET_ID = "target-open-right-click";
        correctActionOpensMenu(() -> rightClickOn(TARGET_ID),
                () -> leftClickOn(TARGET_ID));
    }

    private void correctActionOpensMenu(Runnable rightAction,
            Runnable wrongAction) {
        verifyClosed();
        wrongAction.run();
        verifyClosed();
        rightAction.run();
        verifyOpened();
    }
}

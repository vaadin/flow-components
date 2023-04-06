
package com.vaadin.flow.component.contextmenu.it;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

/**
 * @author Vaadin Ltd
 */
@TestPath("vaadin-context-menu/override-on-before-open-context-menu")
public class OverrideOnBeforeOpenContextMenuIT extends AbstractContextMenuIT {

    @Before
    public void init() {
        open();
        checkLogsForErrors();
    }

    @Test
    public void shouldNotOpenContextMenuWhenOnBeforeMenuOpenReturnsFalse() {
        verifyClosed();
        rightClickOn("no-open-menu-target");
        verifyClosed();
    }

    @Test
    public void shouldDynamicallyModifyContextMenuItems() {
        verifyClosed();

        rightClickOn("dynamic-context-menu-target");
        verifyOpened();

        Assert.assertEquals("Dynamic Item",
                getOverlay().getAttribute("innerText"));

        clickBody();
        verifyClosed();
    }
}

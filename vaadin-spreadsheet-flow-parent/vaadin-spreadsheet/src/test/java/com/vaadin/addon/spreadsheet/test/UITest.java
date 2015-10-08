package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;

import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;
import com.vaadin.addon.spreadsheet.test.tb3.MultiBrowserTest;
import com.vaadin.addon.spreadsheet.test.testutil.ContextMenuHelper;
import com.vaadin.addon.spreadsheet.test.testutil.PageHelper;
import com.vaadin.addon.spreadsheet.test.testutil.PopupHelper;

public abstract class UITest extends MultiBrowserTest {

    protected ContextMenuHelper contextMenu;
    protected PopupHelper popup;
    protected PageHelper page;

    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() {
        openTestURL();
        contextMenu = new ContextMenuHelper(driver);
        popup = new PopupHelper(driver);
        page = new PageHelper(driver);
    }

    @After
    public void tearDown() throws Exception {
        // driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    @Override
    public Class<?> getUIClass() {
        return TestexcelsheetUI.class;
    }
}

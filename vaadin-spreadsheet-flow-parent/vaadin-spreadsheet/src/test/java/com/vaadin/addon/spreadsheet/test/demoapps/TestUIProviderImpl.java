package com.vaadin.addon.spreadsheet.test.demoapps;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;

/**
 *
 */
@SuppressWarnings("serial")
public class TestUIProviderImpl extends UIProvider {
    public static final String DEMOUI_PACKAGE="com.vaadin.addon.spreadsheet.test.demoapps";
    private static Logger logger = Logger.getLogger(TestUIProviderImpl.class
            .getName());

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

        String name = (event.getRequest()).getPathInfo();
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        try {
            String className = DEMOUI_PACKAGE + "." + name;
            return Class.forName(className).asSubclass(UI.class);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Could not find UI " + name, e);
        }
        return null;
    }
}

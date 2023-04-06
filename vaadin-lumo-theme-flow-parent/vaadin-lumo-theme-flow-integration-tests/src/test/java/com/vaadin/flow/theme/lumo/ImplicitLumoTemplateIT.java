
package com.vaadin.flow.theme.lumo;

import com.vaadin.flow.testutil.TestPath;

@TestPath(value = "vaadin-lumo-theme/implicit-template-view")
public class ImplicitLumoTemplateIT extends AbstractThemedTemplateIT {

    @Override
    protected String getTagName() {
        return "implicit-lumo-themed-template";
    }

    @Override
    protected String getThemedTemplate() {
        return "theme/lumo/ImplicitLumoThemedTemplate.js";
    }
}

/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.sidenav.tests;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.router.Route;

@Route("vaadin-side-nav/side-nav-in-template")
@JsModule("side-nav-in-template.js")
@Tag("side-nav-in-template")
public class SideNavInTemplatePage extends LitTemplate {

    @Id("sideNav")
    private SideNav sideNav;

    public SideNavInTemplatePage() {
    }
}

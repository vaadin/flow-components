/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.icon.demo.allicons;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("all-icons")
@HtmlImport("frontend://all-icons.html")
public class AllIcons extends PolymerTemplate<AllIcons.AllIconsModel> {
    public interface AllIconsModel extends TemplateModel {
    }
}

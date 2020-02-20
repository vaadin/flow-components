package com.vaadin.flow.component.icon.demo.allicons;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("all-icons")
@HtmlImport("./all-icons.html")
public class AllIcons extends PolymerTemplate<AllIcons.AllIconsViewModel> {
    public interface AllIconsViewModel extends TemplateModel {}
}

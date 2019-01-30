package com.vaadin.flow.component.accordion.examples;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Route
@Tag("accordion-app")
@HtmlImport("frontend://src/accordion-in-template.html")
public class AccordionInTemplate extends PolymerTemplate<TemplateModel> {

    @Id
    private Accordion accordion;

    @Id
    private VerticalLayout events;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        accordion.addOpenedChangedListener(e -> e.getSource().getElement().executeJavaScript(
                "const summary = $0 != null ? " +
                    "this.querySelectorAll('span[slot=\"summary\"]')[$0].textContent + ' opened' : " +
                    "'Accordion collapsed';" +
                "const newEvent = document.createElement('span');" +
                "newEvent.textContent = summary;" +
                "$1.appendChild(newEvent);", e.getIndex(), events.getElement()));
    }
}

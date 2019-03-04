package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.bean.SimpleBean;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.templatemodel.TemplateModel;

@Tag("validation-connector")
@HtmlImport("src/validation-connector.html")
@Route("validation-connector")
public class ValidationConnectorErrorPage extends PolymerTemplate<TemplateModel>
        implements HasComponents {

    @Id("injected")
    private Div div;

    public ValidationConnectorErrorPage() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setItems("Foo", "Bar");
        Binder<SimpleBean> binder = new Binder<>();
        binder.forField(cb).asRequired().bind(SimpleBean::getName,
                SimpleBean::setName);
        add(cb);
    }

}
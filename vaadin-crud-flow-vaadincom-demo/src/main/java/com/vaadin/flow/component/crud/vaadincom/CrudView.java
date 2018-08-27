package com.vaadin.flow.component.crud.vaadincom;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

@Route("vaadin-crud")
public class CrudView extends DemoView {

    @Override
    protected void initView() {
        addCard("Basic Crud", new H1("Hello Crud!"));
    }
}

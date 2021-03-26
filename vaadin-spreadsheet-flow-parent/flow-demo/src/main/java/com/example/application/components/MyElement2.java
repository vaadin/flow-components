package com.example.application.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@Tag("my-element2")
@NpmPackage(value = "spreadsheet-poc-lit-element", version = "^0.0.11")
@JsModule("my-element/my-element.js")
public class MyElement2 extends Component {

    public MyElement2() {

    }


}

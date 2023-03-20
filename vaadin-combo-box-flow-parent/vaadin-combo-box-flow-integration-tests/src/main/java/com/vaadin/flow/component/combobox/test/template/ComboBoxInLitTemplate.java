
package com.vaadin.flow.component.combobox.test.template;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;
import com.vaadin.flow.data.binder.Binder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsModule("./src/combo-box-lit-wrapper.ts")
@Tag("combo-box-lit-wrapper")
public class ComboBoxInLitTemplate extends LitTemplate {

    @Id("combo")
    private ComboBox<Bean> comboBox;

    private List<Bean> beans = new ArrayList<>();

    private Binder<BeanHolder> binder = new Binder<>();

    public ComboBoxInLitTemplate() {
        for (int i = 0; i < 4; i++) {
            beans.add(new Bean("" + i));
        }
        setupForm();
        // you can also set the value through Binder with readBean
        comboBox.setValue(beans.get(1));
    }

    private void setupForm() {
        comboBox.setReadOnly(false);
        comboBox.setClearButtonVisible(true);
        comboBox.setPlaceholder("Placeholder");
        comboBox.setItems(beans);
        comboBox.setItemLabelGenerator(Bean::getBeanid);
        comboBox.setAllowCustomValue(true);
        binder.forField(comboBox).asRequired("Not blank");
    }

    private static class Bean implements Serializable {
        private String beanid;

        public Bean(String beanid) {
            this.beanid = beanid;
        }

        public String getBeanid() {
            return beanid;
        }

        public void setBeanid(String beanid) {
            this.beanid = beanid;
        }
    }

    private static class BeanHolder {
        private Bean bean;

        public void setBean(Bean study) {
            this.bean = study;
        }

        public Bean getBean() {
            return bean;
        }
    }
}

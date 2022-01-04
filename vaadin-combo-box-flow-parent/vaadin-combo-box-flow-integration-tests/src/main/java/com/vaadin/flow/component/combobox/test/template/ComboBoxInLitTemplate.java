/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.formlayout.tests;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.AutoForm;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.data.binder.Binder;

class AutoFormTest {

    public static class Person {
        private String firstName;
        private String lastName;
        private int age;
        private boolean active;

        public Person() {
        }

        public Person(String firstName, String lastName, int age,
                boolean active) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.active = active;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @Test
    void createAutoForm_validBeanType_formCreated() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        Assertions.assertNotNull(form);
        Assertions.assertEquals(Person.class, form.getBeanType());
    }

    @Test
    void createAutoForm_nullBeanType_throwsException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new AutoForm<>(null));
    }

    @Test
    void getBinder_returnsNonNullBinder() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        Binder<Person> binder = form.getBinder();
        Assertions.assertNotNull(binder);
    }

    @Test
    void setValue_setsValueAndBindsToBean() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        Person person = new Person("John", "Doe", 30, true);
        form.setValue(person);
        Assertions.assertEquals(person, form.getValue());
    }

    @Test
    void setExcludedProperties_excludesPropertiesFromForm() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setExcludedProperties("age", "active");
        form.setValue(new Person());

        Map<String, HasValue<?, ?>> fields = form.getFields();
        Assertions.assertNull(fields.get("age"));
        Assertions.assertNull(fields.get("active"));
        Assertions.assertNotNull(fields.get("firstName"));
        Assertions.assertNotNull(fields.get("lastName"));
    }

    @Test
    void setVisibleProperties_showsOnlySpecifiedProperties() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setVisibleProperties("firstName", "lastName");
        form.setValue(new Person());

        Map<String, HasValue<?, ?>> fields = form.getFields();
        Assertions.assertNotNull(fields.get("firstName"));
        Assertions.assertNotNull(fields.get("lastName"));
        Assertions.assertNull(fields.get("age"));
        Assertions.assertNull(fields.get("active"));
    }

    @Test
    void setFieldFactory_usesCustomFactory() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        Input customInput = new Input();
        form.setFieldFactory("firstName", descriptor -> customInput);
        form.setValue(new Person());

        HasValue<?, ?> field = form.getField("firstName");
        Assertions.assertSame(customInput, field);
    }

    @Test
    void setFieldCustomizer_customizesField() {
        AtomicBoolean customizerCalled = new AtomicBoolean(false);
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setFieldCustomizer("firstName", (descriptor, field) -> {
            customizerCalled.set(true);
        });
        form.setValue(new Person());

        Assertions.assertTrue(customizerCalled.get());
    }

    @Test
    void setOnSave_callbackInvokedOnSave() {
        AtomicBoolean saveCalled = new AtomicBoolean(false);
        AutoForm<Person> form = new AutoForm<>(Person.class);
        Person person = new Person("John", "Doe", 30, true);
        form.setOnSave(p -> saveCalled.set(true));
        form.setValue(person);

        form.getSaveButton().click();
        Assertions.assertTrue(saveCalled.get());
    }

    @Test
    void setOnCancel_callbackInvokedOnCancel() {
        AtomicBoolean cancelCalled = new AtomicBoolean(false);
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setOnCancel(() -> cancelCalled.set(true));
        form.setValue(new Person());

        form.getCancelButton().click();
        Assertions.assertTrue(cancelCalled.get());
    }

    @Test
    void setOnDelete_callbackInvokedOnDelete() {
        AtomicBoolean deleteCalled = new AtomicBoolean(false);
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setDeleteButtonVisible(true);
        form.setOnDelete(p -> deleteCalled.set(true));
        form.setValue(new Person());

        form.getDeleteButton().click();
        Assertions.assertTrue(deleteCalled.get());
    }

    @Test
    void setButtonsVisible_hidesButtons() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setButtonsVisible(false);
        form.setValue(new Person());

        Assertions.assertFalse(form.getSaveButton().isVisible());
    }

    @Test
    void getContent_returnsFormLayout() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setValue(new Person());
        Assertions.assertTrue(form.getContent() instanceof FormLayout);
    }

    @Test
    void validate_returnsTrueForValidData() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setValue(new Person("John", "Doe", 30, true));
        Assertions.assertTrue(form.validate());
    }

    @Test
    void setExcludedProperties_afterBuilt_throwsException() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setValue(new Person());
        Assertions.assertThrows(IllegalStateException.class,
                () -> form.setExcludedProperties("firstName"));
    }

    @Test
    void setVisibleProperties_afterBuilt_throwsException() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setValue(new Person());
        Assertions.assertThrows(IllegalStateException.class,
                () -> form.setVisibleProperties("firstName"));
    }

    @Test
    void setFieldFactory_afterBuilt_throwsException() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        form.setValue(new Person());
        Assertions.assertThrows(IllegalStateException.class,
                () -> form.setFieldFactory("firstName", d -> new Input()));
    }

    @Test
    void methodChaining_returnsThis() {
        AutoForm<Person> form = new AutoForm<>(Person.class);
        AutoForm<Person> result = form.setExcludedProperties("age")
                .setVisibleProperties("firstName", "lastName")
                .setOnSave(p -> {
                }).setOnCancel(() -> {
                }).setOnDelete(p -> {
                }).setButtonsVisible(true).setDeleteButtonVisible(true);
        Assertions.assertSame(form, result);
    }
}

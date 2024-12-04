/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.virtuallist.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.component.virtuallist.VirtualList.SelectionMode;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.selection.SingleSelect;

/**
 * Tests using selection via VirtualList's SingleSelect API.
 */
public class VirtualListAsSingleSelectTest {

    private SingleSelect<VirtualList<String>, String> singleSelect;
    private ValueChangeListener<ValueChangeEvent<String>> selectionListenerSpy;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        var list = new VirtualList<String>();
        list.setItems("1", "2", "3", "4", "5");
        list.setSelectionMode(SelectionMode.SINGLE);
        singleSelect = list.asSingleSelect();
        selectionListenerSpy = Mockito.mock(ValueChangeListener.class);
        singleSelect.addValueChangeListener(selectionListenerSpy);
    }

    @Test
    public void getValue() {
        singleSelect.setValue("2");

        Assert.assertEquals("2", singleSelect.getValue());
    }

    @Test
    public void getElement() {
        Assert.assertEquals("vaadin-virtual-list",
                singleSelect.getElement().getTag());
    }

    @Test
    public void setValue_triggersSelectionListener() {
        singleSelect.setValue("2");

        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setExistingValue_noChanges() {
        singleSelect.setValue("2");
        Mockito.reset(selectionListenerSpy);
        singleSelect.setValue("2");

        Assert.assertEquals("2", singleSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .valueChanged(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void setValue_setDifferentValue_selectionChanged() {
        singleSelect.setValue("2");
        Mockito.reset(selectionListenerSpy);
        singleSelect.setValue("3");

        Assert.assertEquals("3", singleSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void clear_updatesSelectionAndValueAndTriggersSelectionListener() {
        singleSelect.setValue("2");
        Mockito.reset(selectionListenerSpy);

        singleSelect.clear();
        Assert.assertEquals(null, singleSelect.getValue());
        Mockito.verify(selectionListenerSpy, Mockito.times(1))
                .valueChanged(Mockito.any());
    }

    @Test
    public void emptySelection_clear_noChanges() {
        singleSelect.clear();
        Mockito.verify(selectionListenerSpy, Mockito.times(0))
                .valueChanged(Mockito.any());
    }

    @Test
    public void binderTest() {
        var binder = new Binder<Person>(Person.class);
        binder.bind(singleSelect, "value");

        var person = new Person("1");
        binder.setBean(person);

        singleSelect.setValue("2");

        Assert.assertEquals("2", singleSelect.getValue());
        Assert.assertEquals("2", person.getValue());
    }

    public static class Person {
        private String value;

        public Person(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}

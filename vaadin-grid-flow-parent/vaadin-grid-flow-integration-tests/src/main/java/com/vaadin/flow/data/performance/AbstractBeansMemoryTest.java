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
package com.vaadin.flow.data.performance;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openjdk.jol.info.GraphLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.bean.Address;
import com.vaadin.flow.data.bean.Country;
import com.vaadin.flow.data.bean.Gender;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

public abstract class AbstractBeansMemoryTest<T extends Component> extends Div
        implements HasUrlParameter<String> {

    private Random random = new Random();

    private int dataSize;
    private boolean isInMemory;

    private Label logLabel;
    private Label memoryLabel;

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        String[] pairs = parameter.split("&");
        String items = Stream.of(pairs).filter(p -> p.startsWith("items" + "="))
                .findFirst().map(s -> s.substring(s.indexOf("=") + 1))
                .orElse(null);
        if (items == null) {
            return;
        }
        int itemsCount = 1;
        if (items != null) {
            itemsCount = Integer.parseInt(items);
        }

        VerticalLayout layout = new VerticalLayout();
        add(layout);
        layout.add(new Label(getClass().getSimpleName()));

        memoryLabel = new Label();
        memoryLabel.setId("memory");
        layout.add(memoryLabel);

        logLabel = new Label();
        logLabel.setId("log");
        layout.add(logLabel);

        T component = createComponent();
        setData(itemsCount, component, true);
        createMenu(component);

        layout.add(component);

        NativeButton close = new NativeButton("Close UI",
                e -> UI.getCurrent().close());
        close.setId("close");
        layout.add(close);
    }

    protected abstract T createComponent();

    protected List<Person> createBeans(int size) {
        return IntStream.range(0, size).mapToObj(this::createPerson)
                .collect(Collectors.toList());
    }

    protected Person createPerson(int index) {
        random.setSeed(index);
        Person person = new Person();
        person.setFirstName("First Name " + random.nextInt());
        person.setLastName("Last Name " + random.nextInt());
        person.setAge(random.nextInt());
        person.setBirthDate(new Date(random.nextLong()));
        person.setDeceased(random.nextBoolean());
        person.setEmail(random.nextInt() + "user@example.com");
        person.setRent(new BigDecimal(random.nextLong()));
        person.setSalary(random.nextInt());
        person.setSalaryDouble(random.nextDouble());
        person.setGender(
                Gender.values()[random.nextInt(Gender.values().length)]);

        Address address = new Address();
        person.setAddress(address);
        address.setCity("city " + random.nextInt());
        address.setPostalCode(random.nextInt());
        address.setStreet("street address " + random.nextInt());
        address.setCountry(
                Country.values()[random.nextInt(Country.values().length)]);
        return person;
    }

    protected abstract void setInMemoryContainer(T component,
            List<Person> data);

    protected abstract void setBackendContainer(T component, List<Person> data);

    private void setData(int size, T component, boolean memoryContainer) {
        dataSize = size;
        isInMemory = memoryContainer;
        List<Person> persons = createBeans(size);
        if (isInMemory) {
            setInMemoryContainer(component, persons);
        } else {
            setBackendContainer(component, persons);
        }

        memoryLabel.setText(String
                .valueOf(GraphLayout.parseInstance(component).totalSize()));
    }

    private void createMenu(T component) {

        createContainerSizeMenu(component);
        createContainerMenu(component);
    }

    private void createContainerMenu(T component) {
        add(new NativeButton("Use in-memory container",
                event -> setData(dataSize, component, true)));
        add(new NativeButton("Use backend container",
                event -> setData(dataSize, component, false)));
    }

    private void createContainerSizeMenu(T component) {
        IntStream.of(1, 10000, 100000, 500000, 1000000)
                .forEach(size -> addContainerSizeMenu(size, component));
    }

    private void addContainerSizeMenu(int size, T component) {
        add(new NativeButton("Set data provider size to " + size,
                event -> setData(size, component, isInMemory)));
    }
}

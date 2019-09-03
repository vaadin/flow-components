/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.demo;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.combobox.demo.data.DepartmentData;
import com.vaadin.flow.component.combobox.demo.data.ElementsData;
import com.vaadin.flow.component.combobox.demo.entity.Department;
import com.vaadin.flow.component.combobox.demo.entity.Elements;
import com.vaadin.flow.component.combobox.demo.entity.Person;
import com.vaadin.flow.component.combobox.demo.entity.Song;
import com.vaadin.flow.component.combobox.demo.service.PersonService;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * View for {@link ComboBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-combo-box")
public class ComboBoxView extends DemoView {

    private static final String WIDTH_STRING = "250px";

    @Override
    public void initView() {
        basicDemo(); // Basic usage
        disabledAndReadonly();
        entityList();
        displayClearButton();
        valueChangeEvent();
        customValues();
        lazyLoading();
        configurationForReqired(); // Validation
        customFiltering(); // Filtering
        customOptionsDemo(); // Presentation
        usingTemplateRenderer();
        styling(); // Styling
    }

    private void basicDemo() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Basic usage
        ComboBox<String> labelComboBox = new ComboBox<>();
        labelComboBox.setItems("Option one", "Option two");
        labelComboBox.setLabel("Label");

        ComboBox<String> placeHolderComboBox = new ComboBox<>();
        placeHolderComboBox.setItems("Option one", "Option two");
        placeHolderComboBox.setPlaceholder("Placeholder");

        ComboBox<String> valueComboBox = new ComboBox<>();
        valueComboBox.setItems("Value", "Option one", "Option two");
        valueComboBox.setValue("Value");

        // end-source-example
        labelComboBox.getStyle().set("margin-right", "5px");
        placeHolderComboBox.getStyle().set("margin-right", "5px");
        div.add(labelComboBox, placeHolderComboBox, valueComboBox);
        addCard("Basic usage", div);
    }

    private void disabledAndReadonly() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Disabled and read-only
        ComboBox<String> disabledComboBox = new ComboBox<>();
        disabledComboBox.setItems("Value", "Option one", "Option two");
        disabledComboBox.setEnabled(false);
        disabledComboBox.setValue("Value");
        disabledComboBox.setLabel("Disabled");

        ComboBox<String> readOnlyComboBox = new ComboBox<>();
        readOnlyComboBox.setItems("Value", "Option one", "Option two");
        readOnlyComboBox.setReadOnly(true);
        readOnlyComboBox.setValue("Value");
        readOnlyComboBox.setLabel("Read-only");
        // end-source-example
        disabledComboBox.getStyle().set("margin-right", "5px");
        div.add(disabledComboBox, readOnlyComboBox);
        addCard("Disabled and read-only", div);
    }

    private List<Department> getDepartments() {

        DepartmentData departmentData = new DepartmentData();
        return departmentData.getDepartments();
    }

    private List<Elements> getElements() {
        ElementsData elementsData = new ElementsData();
        return elementsData.getElements();
    }

    private void entityList() {
        // begin-source-example
        // source-example-heading: Entity list
        ComboBox<Department> comboBox = new ComboBox<>();
        comboBox.setLabel("Department");
        List<Department> departmentList = getDepartments();

        // Choose which property from Department is the presentation value
        comboBox.setItemLabelGenerator(Department::getName);
        comboBox.setItems(departmentList);
        // end-source-example
        addCard("Entity list", comboBox);
    }

    private void displayClearButton() {
        // begin-source-example
        // source-example-heading: Display the clear button
        ComboBox comboBox = new ComboBox();
        comboBox.setItems("Option one", "Option two");
        comboBox.setClearButtonVisible(true);
        // end-source-example

        addCard("Display the clear button", comboBox);
    }

    private void valueChangeEvent() {
        // begin-source-example
        // source-example-heading: Value change event
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setLabel("Label");
        comboBox.setItems("Option one", "Option two");
        comboBox.setClearButtonVisible(true);

        Div value = new Div();
        value.setText("Select a value");
        comboBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                value.setText("No option selected");
            } else {
                value.setText("Selected: " + event.getValue());
            }
        });
        // end-source-example
        VerticalLayout verticalLayout = new VerticalLayout(comboBox, value);
        verticalLayout.setAlignItems(FlexComponent.Alignment.START);
        addCard("Value change event", verticalLayout);
    }

    private void customValues() {
        Div message = createMessageDiv("custom-value-message");

        // begin-source-example
        // source-example-heading: Allow users to input custom values
        ComboBox<String> comboBox = new ComboBox<>("City");
        comboBox.setItems("Turku", "Berlin", "San Jose");

        /**
         * Allow users to enter a value which doesn't exist in the data set, and
         * set it as the value of the ComboBox.
         */
        comboBox.addCustomValueSetListener(event -> 
            comboBox.setValue(event.getDetail()));

        comboBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                message.setText("No city selected");
            } else {
                message.setText("Selected city: " + event.getValue());
            }
        });
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("custom-value-box");
        addCard("Allow users to input custom values", comboBox, message);
    }

    private void lazyLoading() {
        //@formatter:off
        // begin-source-example
        // source-example-heading: Lazy loading with callbacks
        // PersonService can be found:
        // https://github.com/vaadin/vaadin-combo-box-flow/tree/master/vaadin-combo-box-flow-demo/src/main/java/com/vaadin/flow/component/combobox/demo/service/PersonService.java

        ComboBox<Person> comboBox = new ComboBox<>();
        PersonService service = new PersonService();
        /*
         * This data provider doesn't load all the items to the server memory
         * right away. The component calls the first provided callback to fetch
         * items from the given range with the given filter. The second callback
         * should provide the number of items that match the query.
         */
        comboBox.setDataProvider(service::fetch, service::count);
        // end-source-example
        //@formatter:on
        comboBox.setId("callback-box");
        addCard("Lazy loading with callbacks", comboBox);
    }

    private void configurationForReqired() {
        // begin-source-example
        // source-example-heading: Required
        ComboBox<String> requiredComboBox = new ComboBox<>();
        requiredComboBox.setItems("Option one", "Option two", "Option three");
        requiredComboBox.setLabel("Required");
        requiredComboBox.setPlaceholder("Select an option");

        requiredComboBox.setRequired(true);
        requiredComboBox.setClearButtonVisible(true);
        // end-source-example
        FlexLayout layout = new FlexLayout(requiredComboBox);
        layout.getStyle().set("flex-wrap", "wrap");
        addCard("Validation", "Required", layout);
    }

    private void customFiltering() {
        Div div = new Div();
        div.setText("Example uses case-sensitive starts-with filtering");
        // begin-source-example
        // source-example-heading: Custom filtering
        ComboBox<Elements> filteringComboBox = new ComboBox<>();
        List<Elements> elementsList = getElements();

        /*
         * Providing a custom item filter allows filtering based on all of the
         * rendered properties:
         */
        ItemFilter<Elements> filter = (element, filterString) -> element
                .getName().startsWith(filterString);

        filteringComboBox.setItems(filter, elementsList);
        filteringComboBox.setItemLabelGenerator(Elements::getName);
        filteringComboBox.setClearButtonVisible(true);
        // end-source-example
        addCard("Filtering", "Custom filtering", div, filteringComboBox);

    }

    private void customOptionsDemo() {
        // begin-source-example
        // source-example-heading: Customizing drop down items with ComponentRenderer
        ComboBox<Information> comboBox = new ComboBox<>();
        comboBox.setLabel("User");
        comboBox.setItems(
                new Information("Gabriella",
                        "https://randomuser.me/api/portraits/women/43.jpg"),
                new Information("Rudi",
                        "https://randomuser.me/api/portraits/men/77.jpg"),
                new Information("Hamsa",
                        "https://randomuser.me/api/portraits/men/35.jpg"),
                new Information("Jacob",
                        "https://randomuser.me/api/portraits/men/76.jpg"));

        comboBox.setRenderer(new ComponentRenderer<>(information -> {
            Div text = new Div();
            text.setText(information.getText());

            Image image = new Image();
            image.setWidth("21px");
            image.setHeight("21px");
            image.setSrc(information.getImage());

            FlexLayout wrapper = new FlexLayout();
            text.getStyle().set("margin-left", "0.5em");
            wrapper.add(image, text);
            return wrapper;
        }));

        comboBox.setItemLabelGenerator(Information::getText);
        // end-source-example

        addCard("Presentation",
                "Customizing drop down items with ComponentRenderer", comboBox);
    }

    private void usingTemplateRenderer() {
        //@formatter:off
        // begin-source-example
        // source-example-heading: Customizing drop down items with TemplateRenderer
        ComboBox<Song> comboBox = new ComboBox<>();
        comboBox.setLabel("Song");
        List<Song> listOfSongs = createListOfSongs();

        /*
         * Providing a custom item filter allows filtering based on all of
         * the rendered properties:
         */
        ItemFilter<Song> filter = (song, filterString) ->
                song.getName().toLowerCase()
                        .contains(filterString.toLowerCase())
                        || song.getArtist().toLowerCase()
                        .contains(filterString.toLowerCase());

        comboBox.setItems(filter, listOfSongs);
        comboBox.setClearButtonVisible(true);
        comboBox.setItemLabelGenerator(Song::getName);
        comboBox.setRenderer(TemplateRenderer.<Song>of(
                "<div>[[item.song]]<br><small>[[item.artist]]</small></div>")
                .withProperty("song", Song::getName)
                .withProperty("artist", Song::getArtist));
        // end-source-example
        //@formatter:on

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("template-selection-box");
        addCard("Presentation",
                "Customizing drop down items with TemplateRenderer", comboBox);
    }

    private void styling() {
        Paragraph p1 = new Paragraph("To read about styling you can read the related tutorial ");
        p1.add(new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes"));

        Paragraph p2 = new Paragraph("To know about styling in HTML you can read the ");
        p2.add(new Anchor("https://vaadin.com/components/"
                + "vaadin-combo-box/html-examples/combo-box-styling-demos",
                "HTML Styling Demos"));
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", p1, p2);
    }

    private List<Song> createListOfSongs() {
        List<Song> listOfSongs = new ArrayList<>();
        listOfSongs.add(new Song("A V Club Disagrees", "Haircuts for Men",
                "Physical Fitness"));
        listOfSongs.add(new Song("Sculpted", "Haywyre", "Two Fold Pt.1"));
        listOfSongs.add(
                new Song("Voices of a Distant Star", "Killigrew", "Animus II"));
        return listOfSongs;
    }

    private Div createMessageDiv(String id) {
        Div message = new Div();
        message.setId(id);
        message.getStyle().set("whiteSpace", "pre");
        return message;
    }

    private static class Information {
        private String text;
        private String image;

        private Information(String text, String image) {
            this.text = text;
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public String getImage() {
            return image;
        }
    }
}

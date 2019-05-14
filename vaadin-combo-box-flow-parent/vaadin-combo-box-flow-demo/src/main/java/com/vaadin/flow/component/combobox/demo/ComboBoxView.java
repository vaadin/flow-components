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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.javafaker.Faker;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.router.Route;

/**
 * View for {@link ComboBox} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-combo-box")
public class ComboBoxView extends DemoView {

    /**
     * Example object.
     */
    public static class Song {
        private String name;
        private String artist;
        private String album;

        /**
         * Default constructor.
         */
        public Song() {
        }

        /**
         * Construct a song with the given name, artist and album.
         *
         * @param name
         *            name of the song
         * @param artist
         *            name of the artist
         * @param album
         *            name of the album
         */
        public Song(String name, String artist, String album) {
            this.name = name;
            this.artist = artist;
            this.album = album;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }
    }

    private static final String WIDTH_STRING = "250px";

    @Override
    public void initView() {
        createStringComboBox();
        createWithClearButton();
        createDisabledComboBox();
        createObjectComboBox();
        createComboBoxWithObjectStringSimpleValue();
        createComboBoxUsingTemplateRenderer();
        createComboBoxUsingComponentRenderer();
        createComboBoxWithInMemoryLazyLoading();
        createComboBoxWithCallbackLazyLoading();
        createComboBoxWithCustomValues();
    }

    private void createStringComboBox() {
        Div message = createMessageDiv("string-selection-message");

        // begin-source-example
        // source-example-heading: String selection
        ComboBox<String> comboBox = new ComboBox<>("Browsers");
        comboBox.setItems("Google Chrome", "Mozilla Firefox", "Opera",
                "Apple Safari", "Microsoft Edge");

        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No browser selected");
            } else {
                message.setText("Selected browser: " + event.getValue());
            }
        });
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("string-selection-box");
        addCard("String selection", comboBox, message);
    }

    private void createWithClearButton() {
        // begin-source-example
        // source-example-heading: Clear button
        ComboBox<String> comboBox = new ComboBox<>("Browsers");
        comboBox.setItems("Google Chrome", "Mozilla Firefox", "Opera",
                "Apple Safari", "Microsoft Edge");
        comboBox.setValue("Google Chrome");

        // Display an icon which can be clicked to clear the value:
        comboBox.setClearButtonVisible(true);
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("clear-button-box");
        addCard("Clear button", comboBox);
    }

    private void createObjectComboBox() {
        Div message = createMessageDiv("object-selection-message");

        // begin-source-example
        // source-example-heading: Object selection
        ComboBox<Song> comboBox = new ComboBox<>();
        comboBox.setLabel("Music selection");
        comboBox.setItemLabelGenerator(Song::getName);

        List<Song> listOfSongs = createListOfSongs();

        comboBox.setItems(listOfSongs);
        comboBox.addValueChangeListener(event -> {
            Song song = comboBox.getValue();
            if (song != null) {
                message.setText("Selected song: " + song.getName()
                        + "\nFrom album: " + song.getAlbum() + "\nBy artist: "
                        + song.getArtist());
            } else {
                message.setText("No song is selected");
            }
        });
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("object-selection-box");
        addCard("Object selection", comboBox, message);
    }

    private void createComboBoxWithObjectStringSimpleValue() {
        Div message = createMessageDiv("value-selection-message");

        // begin-source-example
        // source-example-heading: Value selection from objects
        ComboBox<Song> comboBox = new ComboBox<>("Artists");
        comboBox.setItemLabelGenerator(Song::getArtist);

        List<Song> listOfSongs = createListOfSongs();

        comboBox.setItems(listOfSongs);

        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No artist selected");
            } else if (event.getOldValue() == null) {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist());
            } else {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist()
                                + "\nThe old selection was: "
                                + event.getOldValue().getArtist());
            }
        });
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("value-selection-box");
        addCard("Value selection from objects", comboBox, message);
    }

    private void createDisabledComboBox() {
        Div message = createMessageDiv("disabled-combobox-message");
        // begin-source-example
        // source-example-heading: Disabled ComboBox
        ComboBox<String> comboBox = new ComboBox<>("Disabled ComboBox");
        comboBox.setEnabled(false);
        // end-source-example
        comboBox.setItems("Google Chrome", "Mozilla Firefox", "Opera",
                "Apple Safari", "Microsoft Edge");
        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No browser selected");
            } else {
                message.setText("Selected browser: " + event.getValue());
            }
        });
        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("disabled-combo-box");
        addCard("Disabled ComboBox", comboBox, message);
    }

    private void createComboBoxUsingTemplateRenderer() {
        Div message = createMessageDiv("template-selection-message");

        //@formatter:off
        // begin-source-example
        // source-example-heading: Rendering items using TemplateRenderer
        ComboBox<Song> comboBox = new ComboBox<>();

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
        comboBox.setItemLabelGenerator(Song::getName);
        comboBox.setRenderer(TemplateRenderer.<Song> of(
                "<div>[[item.song]]<br><small>[[item.artist]]</small></div>")
                .withProperty("song", Song::getName)
                .withProperty("artist", Song::getArtist));

        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No artist selected");
            } else if (event.getOldValue() == null) {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist());
            } else {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist()
                                + "\nThe old selection was: "
                                + event.getOldValue().getArtist());
            }
        });
        // end-source-example
        //@formatter:on

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("template-selection-box");
        addCard("Using templates", "Rendering items using TemplateRenderer",
                comboBox, message);
    }

    private void createComboBoxUsingComponentRenderer() {
        Div message = createMessageDiv("component-selection-message");

        //@formatter:off
        // begin-source-example
        // source-example-heading: Rendering items using ComponentTemplateRenderer
        ComboBox<Song> comboBox = new ComboBox<>();

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

        comboBox.setItemLabelGenerator(Song::getName);

        comboBox.setRenderer(new ComponentRenderer<>(item -> {
            VerticalLayout container = new VerticalLayout();

            Label song = new Label(item.getName());
            container.add(song);

            Label artist = new Label(item.getArtist());
            artist.getStyle().set("fontSize", "smaller");
            container.add(artist);

            return container;
        }));

        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No artist selected");
            } else if (event.getOldValue() == null) {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist());
            } else {
                message.setText(
                        "Selected artist: " + event.getValue().getArtist()
                                + "\nThe old selection was: "
                                + event.getOldValue().getArtist());
            }
        });
        // end-source-example
        //@formatter:on

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("component-selection-box");
        addCard("Using components",
                "Rendering items using ComponentTemplateRenderer", comboBox,
                message);
    }

    private void createComboBoxWithInMemoryLazyLoading() {
        // begin-source-example
        // source-example-heading: Lazy loading between client and server
        ComboBox<String> comboBox = new ComboBox<>();

        /*
         * Using a large data set makes the browser request items lazily as the
         * user scrolls down the overlay. This will also trigger server-side
         * filtering.
         */
        List<String> names = getNames(500);
        comboBox.setItems(names);
        // end-source-example

        comboBox.setId("lazy-loading-box");
        addCard("Lazy Loading", "Lazy loading between client and server",
                comboBox);
    }

    private void createComboBoxWithCallbackLazyLoading() {
        //@formatter:off
        // begin-source-example
        // source-example-heading: Lazy loading with callbacks
        ComboBox<String> comboBox = new ComboBox<>();

        /*
         * This data provider doesn't load all the items to the server memory
         * right away. The component calls the first provided callback to fetch
         * items from the given range with the given filter. The second callback
         * should provide the number of items that match the query.
         */
        comboBox.setDataProvider(
                (filter, offset, limit) ->
                    IntStream.range(offset, offset + limit)
                        .mapToObj(i -> "Item " + i),
                filter -> 500);

        // end-source-example
        //@formatter:on
        comboBox.setId("callback-box");
        addCard("Lazy Loading", "Lazy loading with callbacks", comboBox);
    }

    private void createComboBoxWithCustomValues() {
        Div message = createMessageDiv("custom-value-message");

        // begin-source-example
        // source-example-heading: Allow users to input custom values
        ComboBox<String> comboBox = new ComboBox<>("City");
        comboBox.setItems("Turku", "Berlin", "San Jose");

        /**
         * Allow users to enter a value which doesn't exist in the data set, and
         * set it as the value of the ComboBox.
         */
        comboBox.addCustomValueSetListener(event -> {
            comboBox.setValue(event.getDetail());
        });

        comboBox.addValueChangeListener(event -> {
            if (event.getSource().isEmpty()) {
                message.setText("No city selected");
            } else {
                message.setText("Selected city: " + event.getValue());
            }
        });
        // end-source-example

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, WIDTH_STRING);
        comboBox.setId("custom-value-box");
        addCard("Custom Values", "Allow users to input custom values", comboBox,
                message);
    }

    private List<String> getNames(int count) {
        Faker faker = Faker.instance();
        return IntStream.range(0, count).mapToObj(i -> faker.name().fullName())
                .collect(Collectors.toList());
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
}

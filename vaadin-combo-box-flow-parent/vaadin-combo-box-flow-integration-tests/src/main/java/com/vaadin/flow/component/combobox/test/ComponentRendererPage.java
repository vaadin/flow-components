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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.ElementConstants;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/component-renderer")
public class ComponentRendererPage extends Div {
    public ComponentRendererPage() {
        itemsBeforeRenderer();
        itemsAfterRenderer();
        dataProviderBeforeRenderer();
        dataProviderAfterRenderer();
    }

    private ComponentRenderer<VerticalLayout, ComboBoxDemoPage.Song> renderer = new ComponentRenderer<>(
            item -> {
                VerticalLayout container = new VerticalLayout();

                Label song = new Label(item.getName());
                container.add(song);

                Label artist = new Label(item.getArtist());
                artist.getStyle().set("fontSize", "smaller");
                container.add(artist);

                return container;
            });

    private void itemsBeforeRenderer() {
        ComboBox<ComboBoxDemoPage.Song> comboBox = new ComboBox<>();
        List<ComboBoxDemoPage.Song> listOfSongs = createListOfSongs();
        comboBox.setItems(listOfSongs);
        comboBox.setItemLabelGenerator(ComboBoxDemoPage.Song::getName);

        comboBox.setRenderer(renderer);

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, "250px");
        comboBox.setId("before-renderer");
        add(comboBox);
    }

    private void itemsAfterRenderer() {
        ComboBox<ComboBoxDemoPage.Song> comboBox = new ComboBox<>();
        List<ComboBoxDemoPage.Song> listOfSongs = createListOfSongs();
        comboBox.setRenderer(renderer);

        comboBox.setItems(listOfSongs);
        comboBox.setItemLabelGenerator(ComboBoxDemoPage.Song::getName);

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, "250px");
        comboBox.setId("after-renderer");
        add(comboBox);
    }

    private void dataProviderBeforeRenderer() {
        ComboBox<ComboBoxDemoPage.Song> comboBox = new ComboBox<>();
        List<ComboBoxDemoPage.Song> listOfSongs = createListOfSongs();
        comboBox.setDataProvider(
                new ListDataProvider<ComboBoxDemoPage.Song>(listOfSongs));
        comboBox.setItemLabelGenerator(ComboBoxDemoPage.Song::getName);

        comboBox.setRenderer(renderer);

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, "250px");
        comboBox.setId("dp-before-renderer");
        add(comboBox);
    }

    private void dataProviderAfterRenderer() {
        ComboBox<ComboBoxDemoPage.Song> comboBox = new ComboBox<>();
        List<ComboBoxDemoPage.Song> listOfSongs = createListOfSongs();
        comboBox.setRenderer(renderer);

        comboBox.setDataProvider(
                new ListDataProvider<ComboBoxDemoPage.Song>(listOfSongs));
        comboBox.setItemLabelGenerator(ComboBoxDemoPage.Song::getName);

        comboBox.getStyle().set(ElementConstants.STYLE_WIDTH, "250px");
        comboBox.setId("dp-after-renderer");
        add(comboBox);
    }

    private List<ComboBoxDemoPage.Song> createListOfSongs() {
        List<ComboBoxDemoPage.Song> listOfSongs = new ArrayList<>();
        listOfSongs.add(new ComboBoxDemoPage.Song("A V Club Disagrees",
                "Haircuts for Men", "Physical Fitness"));
        listOfSongs.add(new ComboBoxDemoPage.Song("Sculpted", "Haywyre",
                "Two Fold Pt.1"));
        listOfSongs.add(new ComboBoxDemoPage.Song("Voices of a Distant Star",
                "Killigrew", "Animus II"));
        return listOfSongs;
    }

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
}

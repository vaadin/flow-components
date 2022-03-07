package com.vaadin.flow.component.combobox.test.entity;

public class Song {

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

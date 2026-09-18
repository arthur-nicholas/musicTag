package com.project.musicTag.model;

public class AudioMetadata {
    private String title;
    private String artist;
    private String album;
    private String year;
    private String genre;
    private String trackNumber;
    private String albumArtUrl;

    public AudioMetadata() {}

    public AudioMetadata(String title, String artist, String album, String year, String genre, String trackNumber, String albumArtUrl) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.genre = genre;
        this.trackNumber = trackNumber;
        this.albumArtUrl = albumArtUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getTrackNumber() { return trackNumber; }
    public void setTrackNumber(String trackNumber) { this.trackNumber = trackNumber; }

    public String getAlbumArtUrl() { return albumArtUrl; }
    public void setAlbumArtUrl(String albumArtUrl) { this.albumArtUrl = albumArtUrl; }
}

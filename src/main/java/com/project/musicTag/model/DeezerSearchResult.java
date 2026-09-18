package com.project.musicTag.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeezerSearchResult {
    private List<DeezerTrack> data;

    public List<DeezerTrack> getData() { return data; }
    public void setData(List<DeezerTrack> data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeezerTrack {
        private String title;

        @JsonProperty("artist")
        private DeezerArtist artistObj;

        @JsonProperty("album")
        private DeezerAlbum albumObj;

        private String preview;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public DeezerArtist getArtist() { return artistObj; }

        public DeezerAlbum getAlbum() { return albumObj; }

        public String getPreview() { return preview; }
        public void setPreview(String preview) { this.preview = preview; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeezerArtist {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeezerAlbum {
        private String title;
        private String cover_big;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCover_big() { return cover_big; }
        public void setCover_big(String cover_big) { this.cover_big = cover_big; }
    }
}

package com.project.musicTag.service;

import com.project.musicTag.MetadataSearch;
import com.project.musicTag.model.DeezerSearchResult;
import com.project.musicTag.model.AudioMetadata;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeezerService implements MetadataSearch {

    private static final String DEEZER_API_BASE = "https://api.deezer.com/search";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AudioMetadata> getMetadataList(String artist, String title) {
        try {
            String query = URLEncoder.encode(artist + " " + title, StandardCharsets.UTF_8);
            String url = DEEZER_API_BASE + "?q=" + query;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "MusicPlayer/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            DeezerSearchResult result = objectMapper.readValue(response.body(), DeezerSearchResult.class);

            if (result.getData() == null) {
                return Collections.emptyList();
            }

            return result.getData().stream()
                    .limit(10)
                    .map(track -> {
                        AudioMetadata meta = new AudioMetadata();
                        meta.setTitle(track.getTitle());
                        meta.setArtist(track.getArtist() != null ? track.getArtist().getName() : "");
                        meta.setAlbum(track.getAlbum() != null ? track.getAlbum().getTitle() : "");
                        meta.setAlbumArtUrl(track.getAlbum() != null ? track.getAlbum().getCover_big() : "");
                        return meta;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

package com.project.musicTag.controller;

import com.project.musicTag.MetadataSearch;
import com.project.musicTag.model.AudioMetadata;
import com.project.musicTag.service.AudioMetadataService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AudioController {

    private final AudioMetadataService audioMetadataService;
    private final MetadataSearch metadataSearch;

    public AudioController(MetadataSearch metadataSearch, AudioMetadataService audioMetadataService) {
        this.metadataSearch = metadataSearch;
        this.audioMetadataService = audioMetadataService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = audioMetadataService.saveTempFile(file);
            AudioMetadata metadata = audioMetadataService.readMetadata(tempFile);

            return ResponseEntity.ok(Map.of(
                    "fileId", tempFile.getName(),
                    "originalName", file.getOriginalFilename(),
                    "metadata", metadata
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<AudioMetadata>> search(
            @RequestParam String artist,
            @RequestParam String title) {
        List<AudioMetadata> results = metadataSearch.getMetadataList(artist, title);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/apply")
    public ResponseEntity<byte[]> applyMetadata(
            @RequestParam("file") MultipartFile file,
            @RequestParam("metadata") String metadataJson) {
        try {
            AudioMetadata metadata = new tools.jackson.databind.ObjectMapper()
                    .readValue(metadataJson, AudioMetadata.class);

            File tempFile = audioMetadataService.saveTempFile(file);
            File modifiedFile = audioMetadataService.writeMetadata(tempFile, metadata);

            String originalName = file.getOriginalFilename();
            String extension = audioMetadataService.getFileExtension(modifiedFile);
            String downloadName = (metadata.getTitle() != null && !metadata.getTitle().isBlank())
                    ? metadata.getTitle() + "." + extension
                    : (originalName != null ? originalName : "modified_audio." + extension);

            byte[] fileBytes = java.nio.file.Files.readAllBytes(modifiedFile.toPath());

            tempFile.delete();
            modifiedFile.delete();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + URLEncoder.encode(downloadName, StandardCharsets.UTF_8) + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

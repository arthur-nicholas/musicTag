package com.project.musicTag.service;

import com.project.musicTag.model.AudioMetadata;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

@Service
public class AudioMetadataService {

    private final OpusMetadataService opusMetadataService;

    public AudioMetadataService(OpusMetadataService opusMetadataService) {
        this.opusMetadataService = opusMetadataService;
    }

    public File saveTempFile(MultipartFile multipartFile) throws Exception {
        String originalName = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        File tempFile = Files.createTempFile("music_", extension).toFile();
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public AudioMetadata readMetadata(File audioFile) throws Exception {
        if ("opus".equals(getFileExtension(audioFile))) {
            return opusMetadataService.readMetadata(audioFile);
        }

        AudioFile audio = AudioFileIO.read(audioFile);
        Tag tag = audio.getTag();

        AudioMetadata metadata = new AudioMetadata();

        if (tag != null) {
            metadata.setTitle(getFirstField(tag, FieldKey.TITLE));
            metadata.setArtist(getFirstField(tag, FieldKey.ARTIST));
            metadata.setAlbum(getFirstField(tag, FieldKey.ALBUM));
            metadata.setYear(getFirstField(tag, FieldKey.YEAR));
            metadata.setGenre(getFirstField(tag, FieldKey.GENRE));
            metadata.setTrackNumber(getFirstField(tag, FieldKey.TRACK));
        }

        return metadata;
    }

    public File writeMetadata(File audioFile, AudioMetadata metadata) throws Exception {
        if ("opus".equals(getFileExtension(audioFile))) {
            return opusMetadataService.writeMetadata(audioFile, metadata);
        }

        AudioFile audio = AudioFileIO.read(audioFile);
        Tag tag = audio.getTagOrCreateAndSetDefault();

        if (metadata.getTitle() != null && !metadata.getTitle().isBlank()) {
            tag.setField(FieldKey.TITLE, metadata.getTitle());
        }
        if (metadata.getArtist() != null && !metadata.getArtist().isBlank()) {
            tag.setField(FieldKey.ARTIST, metadata.getArtist());
        }
        if (metadata.getAlbum() != null && !metadata.getAlbum().isBlank()) {
            tag.setField(FieldKey.ALBUM, metadata.getAlbum());
        }
        if (metadata.getYear() != null && !metadata.getYear().isBlank()) {
            tag.setField(FieldKey.YEAR, metadata.getYear());
        }
        if (metadata.getGenre() != null && !metadata.getGenre().isBlank()) {
            tag.setField(FieldKey.GENRE, metadata.getGenre());
        }
        if (metadata.getTrackNumber() != null && !metadata.getTrackNumber().isBlank()) {
            tag.setField(FieldKey.TRACK, metadata.getTrackNumber());
        }

        if (metadata.getAlbumArtUrl() != null && !metadata.getAlbumArtUrl().isBlank()) {
            try {
                URL artUrl = URI.create(metadata.getAlbumArtUrl()).toURL();
                try (InputStream in = artUrl.openStream()) {
                    byte[] imageBytes = in.readAllBytes();
                    StandardArtwork artwork = new StandardArtwork();
                    artwork.setBinaryData(imageBytes);
                    artwork.setMimeType("image/jpeg");
                    tag.deleteArtworkField();
                    tag.setField(artwork);
                }
            } catch (Exception e) {
                System.err.println("Failed to download album art: " + e.getMessage());
            }
        }

        audio.setTag(tag);
        audio.commit();
        return audioFile;
    }

    public String getFileExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(dotIndex + 1).toLowerCase() : "";
    }

    private String getFirstField(Tag tag, FieldKey key) {
        try {
            String value = tag.getFirst(key);
            return (value != null && !value.isBlank()) ? value : "";
        } catch (Exception e) {
            return "";
        }
    }
}

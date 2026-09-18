package com.project.musicTag.service;

import com.project.musicTag.model.AudioMetadata;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpusMetadataService {

    public AudioMetadata readMetadata(File opusFile) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("opustags", opusFile.getAbsolutePath());
        Process process = pb.start();

        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String error = new String(process.getErrorStream().readAllBytes());
            throw new RuntimeException("opustags read failed (exit " + exitCode + "): " + error.trim());
        }

        AudioMetadata metadata = new AudioMetadata();
        Map<String, String> tags = parseOutput(output);

        metadata.setTitle(tags.getOrDefault("TITLE", ""));
        metadata.setArtist(tags.getOrDefault("ARTIST", ""));
        metadata.setAlbum(tags.getOrDefault("ALBUM", ""));
        metadata.setYear(tags.getOrDefault("DATE", ""));
        metadata.setGenre(tags.getOrDefault("GENRE", ""));
        metadata.setTrackNumber(tags.getOrDefault("TRACKNUMBER", ""));

        return metadata;
    }

    public File writeMetadata(File opusFile, AudioMetadata metadata) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("opustags");
        command.add(opusFile.getAbsolutePath());
        command.add("-i");
        command.add("-y");

        addSetCommand(command, "TITLE", metadata.getTitle());
        addSetCommand(command, "ARTIST", metadata.getArtist());
        addSetCommand(command, "ALBUM", metadata.getAlbum());
        addSetCommand(command, "DATE", metadata.getYear());
        addSetCommand(command, "GENRE", metadata.getGenre());
        addSetCommand(command, "TRACKNUMBER", metadata.getTrackNumber());

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        String errorOutput = new String(process.getErrorStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("opustags write failed (exit " + exitCode + "): " + errorOutput.trim());
        }

        if (metadata.getAlbumArtUrl() != null && !metadata.getAlbumArtUrl().isBlank()) {
            applyCoverArt(opusFile, metadata.getAlbumArtUrl());
        }

        return opusFile;
    }

    public void applyCoverArt(File opusFile, String imageUrl) throws Exception {
        File tempImage = Files.createTempFile("cover_", ".jpg").toFile();
        try {
            URL url = URI.create(imageUrl).toURL();
            try (InputStream in = url.openStream()) {
                Files.copy(in, tempImage.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "opustags",
                    opusFile.getAbsolutePath(),
                    "-i", "-y",
                    "--set-cover", tempImage.getAbsolutePath()
            );
            Process process = pb.start();

            String errorOutput = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("opustags set-cover failed (exit " + exitCode + "): " + errorOutput.trim());
            }
        } finally {
            tempImage.delete();
        }
    }

    private void addSetCommand(List<String> command, String tag, String value) {
        if (value != null && !value.isBlank()) {
            command.add("-s");
            command.add(tag + "=" + value);
        }
    }

    private Map<String, String> parseOutput(String output) {
        return output.lines()
                .filter(line -> line.contains("="))
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> parts[0].trim(),
                        parts -> parts[1].trim(),
                        (a, b) -> a
                ));
    }
}

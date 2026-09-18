package com.project.musicTag;

import com.project.musicTag.model.AudioMetadata;

import java.util.List;

public interface MetadataSearch {
    public List<AudioMetadata> getMetadataList (String artist, String title);
}

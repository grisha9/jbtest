package ru.rzn.gmyasoedov.service;

import java.nio.file.Path;

public interface FileProcessor {
    String getType();

    void process(Path path);
}

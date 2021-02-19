package ru.rzn.gmyasoedov.service.processors;

import java.nio.file.Path;

public interface FileProcessor {

    String getType();

    void process(Path path);
}

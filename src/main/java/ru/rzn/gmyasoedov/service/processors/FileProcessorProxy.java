package ru.rzn.gmyasoedov.service.processors;

import java.util.UUID;

public class FileProcessorProxy {
    private final String id;
    private final FileProcessor fileProcessor;

    public FileProcessorProxy(FileProcessor fileProcessor) {
        this.id = UUID.randomUUID().toString();
        this.fileProcessor = fileProcessor;
    }

    public String getId() {
        return id;
    }

    public FileProcessor getFileProcessor() {
        return fileProcessor;
    }
}

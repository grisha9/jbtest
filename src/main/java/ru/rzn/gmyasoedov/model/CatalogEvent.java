package ru.rzn.gmyasoedov.model;

public abstract class CatalogEvent {
    private final String canonicalPath;

    CatalogEvent(String canonicalPath) {
        this.canonicalPath = canonicalPath;
    }

    public String getCanonicalPath() {
        return canonicalPath;
    }
}

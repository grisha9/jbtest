package ru.rzn.gmyasoedov.model;

public class RemoveCatalogEvent extends CatalogEvent {

    public RemoveCatalogEvent(String canonicalPath) {
        super(canonicalPath);
    }
}

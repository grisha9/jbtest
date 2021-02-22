package ru.rzn.gmyasoedov.model;

public class Event<T> {
    private final EventType type;
    private final T payload;

    public Event(EventType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public EventType getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }
}

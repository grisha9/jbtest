package ru.rzn.gmyasoedov.service.processors;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * тип отчета, не чувствителен к регистру
 */
public class ReportType {
    private final String type;

    public ReportType(@NotNull String type) {
        this.type = Objects.requireNonNull(type).toLowerCase();
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportType that = (ReportType) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}

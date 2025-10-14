package com.non.rental.entity;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

/**
 * Status of a rental property analysis.
 */
public enum AnalysisStatus implements EnumClass<String> {

    DRAFT("DRAFT"),
    COMPLETED("COMPLETED"),
    ARCHIVED("ARCHIVED");

    private final String id;

    AnalysisStatus(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static AnalysisStatus fromId(String id) {
        for (AnalysisStatus status : AnalysisStatus.values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}
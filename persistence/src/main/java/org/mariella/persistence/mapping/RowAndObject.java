package org.mariella.persistence.mapping;

import org.mariella.persistence.database.ResultRow;

public record RowAndObject(ResultRow row, Object entity) {
}

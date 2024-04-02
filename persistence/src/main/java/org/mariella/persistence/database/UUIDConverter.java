package org.mariella.persistence.database;

import java.util.UUID;

public interface UUIDConverter extends Converter<UUID> {
    void printSql(StringBuilder b, UUID value);
}

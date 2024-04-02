package org.mariella.persistence.database;

import org.mariella.persistence.query.Literal;

import java.util.UUID;

public abstract class BaseUUIDConverter implements Converter<UUID> {
    @Override
    public String toString(UUID uuid) {
        return uuid == null ? "null" : String.format("'%s'", uuid.toString().replace("-", "").toUpperCase());
    }

    @Override
    public Literal<UUID> createLiteral(Object value) {
        return new UUIDLiteral((UUID) value);
    }

    @Override
    public Literal<UUID> createDummy() {
        return new UUIDLiteral(UUID.randomUUID());
    }

    public abstract void printSql(StringBuilder b, UUID value);

    protected class UUIDLiteral extends Literal<UUID> {
        public UUIDLiteral(UUID value) {
            super(BaseUUIDConverter.this, value);
        }

        @Override
        public void printSql(StringBuilder b) {
            BaseUUIDConverter.this.printSql(b, value);
        }
    }
}

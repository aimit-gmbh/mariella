package org.mariella.persistence.runtime;

public class PersistenceException extends RuntimeException {
    public PersistenceException(Throwable t) {
        super(t);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

}

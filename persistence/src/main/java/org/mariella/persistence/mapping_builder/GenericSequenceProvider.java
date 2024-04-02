package org.mariella.persistence.mapping_builder;

import org.mariella.persistence.database.Sequence;

// ms: die sequences pro table brauch ich das fuer memox v2 fallback szenario, bitte nicht entfernen.
public interface GenericSequenceProvider {
    Sequence getSequence(String primaryTableName, String generatorName);
}

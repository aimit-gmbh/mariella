package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.LongConverter;

public class LongLiteral extends Literal<Long> {

    public LongLiteral(Long value) {
        super(LongConverter.Singleton, value);
    }

    public LongLiteral(Converter<Long> converter, Long value) {
        super(converter, value);
    }

}

package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public class BooleanLiteral extends Literal<Boolean> {

    public BooleanLiteral(Converter<Boolean> converter, Boolean value) {
        super(converter, value);
    }

}

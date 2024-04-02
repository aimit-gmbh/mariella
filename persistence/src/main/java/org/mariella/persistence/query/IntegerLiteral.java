package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.IntegerConverter;

public class IntegerLiteral extends Literal<Integer> {

    public IntegerLiteral(int value) {
        super(IntegerConverter.Singleton, value);
    }

    public IntegerLiteral(Converter<Integer> converter, int value) {
        super(converter, value);
    }

}

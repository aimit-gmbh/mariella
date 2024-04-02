package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public class CharArrayLiteral extends Literal<char[]> {

    public CharArrayLiteral(Converter<char[]> converter, char[] value) {
        super(converter, value);
    }

}

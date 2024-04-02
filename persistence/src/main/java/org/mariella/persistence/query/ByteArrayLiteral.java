package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;

public class ByteArrayLiteral extends Literal<byte[]> {

    public ByteArrayLiteral(Converter<byte[]> converter, byte[] value) {
        super(converter, value);
    }

}

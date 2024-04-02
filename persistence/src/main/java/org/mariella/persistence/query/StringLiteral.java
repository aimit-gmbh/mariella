package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.StringConverter;

public class StringLiteral extends Literal<String> {

    public StringLiteral(String string) {
        super(StringConverter.Singleton, string);
    }

    public StringLiteral(Converter<String> converter, String string) {
        super(converter, string);
    }

}

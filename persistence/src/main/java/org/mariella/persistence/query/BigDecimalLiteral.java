package org.mariella.persistence.query;

import org.mariella.persistence.database.BigDecimalConverter;
import org.mariella.persistence.database.Converter;

import java.math.BigDecimal;


public class BigDecimalLiteral extends Literal<BigDecimal> {

    public BigDecimalLiteral(BigDecimal bd) {
        super(BigDecimalConverter.Singleton, bd);
    }

    public BigDecimalLiteral(Converter<BigDecimal> converter, BigDecimal bd) {
        super(converter, bd);
    }

}

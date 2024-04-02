package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.DoubleConverter;

public class DoubleLiteral extends Literal<Double> {


    public DoubleLiteral(Double bd) {
        super(DoubleConverter.Singleton, bd);
    }

    public DoubleLiteral(Converter<Double> converter, Double bd) {
        super(converter, bd);
    }

}

package org.mariella.persistence.query;

import org.mariella.persistence.database.Converter;
import org.mariella.persistence.database.DateConverter;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateLiteral extends Literal<Date> {

    public DateLiteral(Date value) {
        super(DateConverter.Singleton, value);
    }

    public DateLiteral(Converter<Date> converter, Date value) {
        super(converter, value);
    }

    public void printSql(StringBuilder b) {
        if (value == null) {
            b.append("NULL");
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String string = format.format(value);
            b.append("TO_DATE('");
            b.append(string);
            b.append("', ");
            b.append("'dd.mm.yyyy HH24:MI:SS')");
        }
    }

}

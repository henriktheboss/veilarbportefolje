package no.nav.fo.util.sql;

public class SqlUtilEmptyResultSetException extends RuntimeException{
    public SqlUtilEmptyResultSetException(String sql) {
        super("Følgende sql ga tomt ResultSet: " + sql);
    }
}

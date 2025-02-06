package org.mariella.test.example.implementation;

import java.sql.SQLException;

public interface WithContextCallback<R, T> {
	public R withContext(T context) throws SQLException;
}

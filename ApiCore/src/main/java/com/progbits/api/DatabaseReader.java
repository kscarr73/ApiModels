package com.progbits.api;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author scarr
 */
public class DatabaseReader extends Reader {

	private final DataSource _ds;
	private final String _sql;
	private final List<Object> args;

	public DatabaseReader(DataSource ds, String strSQL, List<Object> args) {
		_ds = ds;
		_sql = strSQL;
		this.args = args;
	}

	public DataSource getDataSource() {
		return _ds;
	}

	public String getSQL() {
		return _sql;
	}

	@Override
	public int read(char[] chars, int i, int i1) throws IOException {
		return 0;
	}

	@Override
	public void close() throws IOException {

	}

}

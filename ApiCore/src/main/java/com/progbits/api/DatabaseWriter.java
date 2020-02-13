package com.progbits.api;

import java.io.IOException;
import java.io.Writer;
import javax.sql.DataSource;

/**
 *
 * @author scarr
 */
public class DatabaseWriter extends Writer {

	final DataSource _ds;

	public DatabaseWriter(DataSource ds) {
		_ds = ds;
	}

	public DataSource getDataSource() {
		return _ds;
	}

	@Override
	public void write(char[] chars, int i, int i1) throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void flush() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

}

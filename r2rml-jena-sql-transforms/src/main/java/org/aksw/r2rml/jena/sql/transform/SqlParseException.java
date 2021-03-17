package org.aksw.r2rml.jena.sql.transform;

public class SqlParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public SqlParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public SqlParseException(String message) {
		super(message);
	}

	public SqlParseException(Throwable cause) {
		super(cause);
	}
}

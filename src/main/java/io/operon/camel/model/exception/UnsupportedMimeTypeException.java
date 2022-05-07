package io.operon.camel.model.exception;

public class UnsupportedMimeTypeException extends Exception {
	
	public UnsupportedMimeTypeException(String msg) {
		super(msg);
	}
	
	@Override
	public String toString() {
		return "UnsupportedMimeTypeException :: " + this.getMessage();
	}

}
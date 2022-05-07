package io.operon.camel.model.exception;

public class ModuleParseException extends Exception {
	
	public ModuleParseException(String msg) {
		super(msg);
	}
	
	@Override
	public String toString() {
		return "ModuleParseException :: " + this.getMessage();
	}

}
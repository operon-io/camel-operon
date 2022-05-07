package io.operon.camel.model.exception;

public class ModuleNotFoundException extends Exception {
	
	public ModuleNotFoundException(String msg) {
		super(msg);
	}
	
	@Override
	public String toString() {
		return "ModuleNotFoundException :: " + this.getMessage();
	}

}
package controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Helper {

	public static String getErrorMessage(Throwable e) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
}

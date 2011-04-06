package org.eclipse.editor;

import org.apache.log4j.Logger;

public class Log {
	public static Logger getLogger() {
		StackTraceElement stackTraceElement = new Exception().getStackTrace()[1];
		return Logger.getLogger(stackTraceElement.getClassName());
	}
}

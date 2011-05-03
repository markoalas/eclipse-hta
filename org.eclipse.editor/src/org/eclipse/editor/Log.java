package org.eclipse.editor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;

public class Log {
	private static Logger log = Log.getLogger();

	public static Logger getLogger() {
		StackTraceElement stackTraceElement = new Exception().getStackTrace()[1];
		return Logger.getLogger(stackTraceElement.getClassName());
	}

	/**
	 * @return Proxy object that logs all its method invocations 
	 */
	@SuppressWarnings("unchecked")
	public static <T, U extends T> T withLogging(Class<T> interfaceClass, U obj) {
		return (T)Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[] {interfaceClass}, new LoggingInvocationHandler(obj));
	}

	private static class LoggingInvocationHandler implements InvocationHandler {
		final Object underlying;
		final Joiner joiner = Joiner.on(",");
	
		public LoggingInvocationHandler(Object underlying) {
			this.underlying = underlying;
		}
	
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String msg = underlying.getClass().getSimpleName() + "." + method.getName() + "(" + (args == null ? "" : joiner.join(args)) + ")";
			
			Object ret = method.invoke(underlying, args);
			
			msg += " -> " + ret;
			log.debug(msg);
			return ret;
		}
	}
}

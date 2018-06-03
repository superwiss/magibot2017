package com.magiai;
import java.util.Observable;

import bwapi.Game;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogFormatter extends Observable {
	private static Game game = MyBotModule.Broodwar;

	public static String error(String format, Object... arguments) {
		return getString(log.isErrorEnabled(), format, arguments);
	}

	public static String warn(String format, Object... arguments) {
		return getString(log.isWarnEnabled(), format, arguments);
	}

	public static String info(String format, Object... arguments) {
		return getString(log.isInfoEnabled(), format, arguments);
	}

	public static String debug(String format, Object... arguments) {
		return getString(log.isDebugEnabled(), format, arguments);
	}

	public static String trace(String format, Object... arguments) {
		return getString(log.isTraceEnabled(), format, arguments);
	}

	public static String warnException(Exception e) {
		if (null == e.getCause()) {
			return getString(log.isWarnEnabled(), "Exception Occured. Exception: %s", e.toString());
		} else {
			return getString(log.isWarnEnabled(), "Exception Occured. Exception: %s, Caues: %s", e.toString(), e.getCause().toString());
		}
	}

	private static String getString(boolean isLogging, String format, Object... arguments) {
		if (isLogging) {
			return String.format("[" + game.getFrameCount() + "] " + String.format(format, arguments));
		}

		return null;
	}
}
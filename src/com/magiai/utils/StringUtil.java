package com.magiai.utils;

import bwapi.Unit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtil {
	public static String printUnit(Unit unit) {
		return getString(true, unit);
	}

	public static String printUnitInfo(Unit unit) {
		return getString(log.isInfoEnabled(), unit);
	}

	public static String printUnitDebug(Unit unit) {
		return getString(log.isDebugEnabled(), unit);
	}

	// Unit의 정보를 출력한다.
	private static String getString(boolean isLogging, Unit unit) {
		if (isLogging) {
			if (null != unit) {
				return String.format("Unit[id=%d, type=%s, hp=%d, angle=%f, position=%s]", unit.getID(), unit.getType().toString(), unit.getHitPoints(), unit.getAngle(), unit.getPosition().toString());
			}
		}

		return null;
	}
}
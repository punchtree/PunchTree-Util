package net.punchtree.util.debugvar;

import java.util.Map;
import java.util.TreeMap;

public class DebugVars {

	static Map<String, String> debugStrings = new TreeMap<>();
	static Map<String, Integer> debugIntegers = new TreeMap<>();
	static Map<String, Double> debugDecimals = new TreeMap<>();
	static Map<String, Boolean> debugBooleans = new TreeMap<>();
	
	public static void setString(String key, String value, boolean override) {
		key = key.toLowerCase();
		if (debugStrings.containsKey(key) || override) {
			debugStrings.put(key, value);
		}
	}
	
	public static void setInteger(String key, int value, boolean override) {
		key = key.toLowerCase();
		if (debugIntegers.containsKey(key) || override) {
			debugIntegers.put(key, value);
		}
	}
	
	public static void setDecimal(String key, double value, boolean override) {
		key = key.toLowerCase();
		if (debugDecimals.containsKey(key) || override) {
			debugDecimals.put(key, value);
		}
	}
	
	public static void setBoolean(String key, boolean value, boolean override) {
		key = key.toLowerCase();
		if (debugBooleans.containsKey(key) || override) {
			debugBooleans.put(key, value);
		}
	}
	
	public static String getString(String key, String defaultValue) {
		return debugStrings.getOrDefault(key.toLowerCase(), defaultValue);
	}
	
	public static Integer getInteger(String key, Integer defaultValue) {
		return debugIntegers.getOrDefault(key.toLowerCase(), defaultValue);
	}
	
	public static Double getDecimal(String key, Double defaultValue) {
		return debugDecimals.getOrDefault(key.toLowerCase(), defaultValue);
	}
	
	public static Boolean getBoolean(String key, Boolean defaultValue) {
		return debugBooleans.getOrDefault(key.toLowerCase(), defaultValue);
	}
	
}

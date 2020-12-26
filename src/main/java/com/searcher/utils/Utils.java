package com.searcher.utils;

import java.util.Map;

public class Utils {
	public static <K, V> boolean compareMaps(Map<K, V> m1, Map<K, V>m2) {
		if(m1.size() != m2.size()) {
			return false;
		}
		else {
			for(Object key:m1.keySet()) {
				if(m1.get(key).equals(m2.get(key))) {
					continue;
				}
					return false;
			}
			
		}
		
	 return true;
	} 

}

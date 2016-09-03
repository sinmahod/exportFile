package org.eclipse.export.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.regex.Pattern;

public class DirList {
	public static String[] getFile(String dir , String regex) {
		File path = new File(dir);
		String[] list = path.list(new DirFilter(regex));
		return list;
	}
}

class DirFilter implements FilenameFilter {
	private Pattern pattern;

	public DirFilter(String regex) {
		pattern = Pattern.compile(regex);
	}

	public boolean accept(File dir, String name) {
		return pattern.matcher(new File(name).getName()).matches();
	}
} 

@SuppressWarnings("rawtypes")
class AlphabeticComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		String s1 = (String) o1;
		String s2 = (String) o2;
		return s1.toLowerCase().compareTo(s2.toLowerCase());
	}
}
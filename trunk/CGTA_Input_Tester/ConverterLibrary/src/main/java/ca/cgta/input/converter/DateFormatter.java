package ca.cgta.input.converter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

	public static final String formatDate(String theInput) {
		StringBuilder b = new StringBuilder(theInput);
		
		if (b.length() > 12) {
			b.insert(12, ":");
		}
		
		if (b.length() > 10) {
			b.insert(10, ":");
		}
		
		if (b.length() > 8) {
			b.insert(8, " ");
		}


		if (b.length() > 6) {
			b.insert(6, "-");
		}

		if (b.length() > 4) {
			b.insert(4, "-");
		}

		return b.toString();
	}
	
	public static void main(String[] args) {
		
		System.out.println(formatTime("101010"));
		
	}

	public static String formatTime(String theInput) {
		StringBuilder b = new StringBuilder(theInput);
		
		if (b.length() > 4) {
			b.insert(4, ":");
		}
		
		if (b.length() > 2) {
			b.insert(2, ":");
		}
		
		return b.toString();
    }

	public static String formatDate(Date theDate) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(theDate);
	}
	
	public static String formatDateWithGmt(Date theDate) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(theDate);
    }

}

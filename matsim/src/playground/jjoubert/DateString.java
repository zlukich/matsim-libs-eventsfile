/* *********************************************************************** *
 * project: org.matsim.*
 * GetDateString.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.jjoubert;

import java.util.GregorianCalendar;

public class DateString extends GregorianCalendar{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString(){
		String result = null;
		int year = this.get(YEAR);
		int month = this.get(MONTH);
		int day = this.get(DAY_OF_MONTH);
		int hour = this.get(HOUR_OF_DAY);
		int minute = this.get(MINUTE);
		int second = this.get(SECOND);
		
		String monthAdd = (month < 10) ? "0" : "";
		String dayAdd =  (day < 10) ? "0" : "";
		String hourAdd = (hour < 10) ? "0" : "";
		String minuteAdd = (minute < 10) ? "0" : "";
		String secondAdd = (second < 10) ? "0" : "";
		
		result = year + monthAdd + month + dayAdd + day + hourAdd + hour + minuteAdd + minute + secondAdd + second;
		
		return result;
	}
}

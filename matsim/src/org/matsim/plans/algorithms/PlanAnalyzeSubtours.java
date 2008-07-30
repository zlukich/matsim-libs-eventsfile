/* *********************************************************************** *
 * project: org.matsim.*
 * PlanAnalyzeSubtours.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.plans.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.Id;
import org.matsim.plans.Act;
import org.matsim.plans.Plan;

/**
 * Analyses plans for subtours. 
 * 
 * A subtour is a sequence of activities where the first 
 * and the last activity are at the same location.
 * The current implementation uses the link as location information, 
 * thus activities at the same link constitute a subtour.
 * 
 * @author meisterk
 *
 */
public class PlanAnalyzeSubtours implements PlanAlgorithmI {

	/**
	 * Maps leg numbers to subtour ids.
	 */
	private HashSet<Integer> subtours = null;

	private ArrayList<Id> locationIds = null;

	private static Logger log = Logger.getLogger(PlanAnalyzeSubtours.class);

	public PlanAnalyzeSubtours() {
		super();
	}

	public int getNumSubtours() {

		return this.subtours.size();

	}

	public void run(Plan plan) {

		locationIds = new ArrayList<Id>();

		ArrayList<Object> actsLegs = plan.getActsLegs();
		for (int ii=0; ii < actsLegs.size(); ii++) {
			if (actsLegs.get(ii).getClass().equals(Act.class)) {
				locationIds.add(((Act) actsLegs.get(ii)).getLinkId());
			}
		}

		this.subtours = new HashSet<Integer>();

		this.extractSubtours(0, locationIds.size() - 1);

		this.printSubtours();

	}

	protected void extractSubtours(final int startIndex, final int endIndex) {

		log.info("startIndex: " + startIndex);
		log.info("endIndex: " + endIndex);

//		TreeMap<Id, Integer> locationEnumerator = new TreeMap<Id, Integer>();

		ArrayList<Id> locationEnumerator = new ArrayList<Id>();
		
		int ii = startIndex;
		int newStartIndex = startIndex;
		while(ii <= endIndex) {
			Id currentLinkId = locationIds.get(ii);
			if (locationEnumerator.contains(currentLinkId)) {
				int lastLinkIndex = locationEnumerator.indexOf(currentLinkId) + newStartIndex;
				//				int lastLinkIndex = locationEnumerator.get(currentLinkId);
				// two consecutive equal locations do NOT constitute a tour
				if ((ii - lastLinkIndex) > 1) {
					subtours.add(lastLinkIndex);
					log.info("Calling extractSubtours(...) from " + (lastLinkIndex + 1) + " to " + (ii - 1));
					this.extractSubtours((lastLinkIndex + 1), (ii - 1));
					// this is probably wrong
					for (int removeMe = lastLinkIndex; removeMe < ii; removeMe++) {
						locationEnumerator.remove(lastLinkIndex - newStartIndex);
					}
					//locationEnumerator.clear();
					newStartIndex = ii;
				}
			}
//			locationEnumerator.put(currentLinkId, ii);
			locationEnumerator.add(currentLinkId);
			ii++;
		}

	}

	protected void printSubtours() {
		for (Integer startLinkIndex : subtours) {
			System.out.println(startLinkIndex);
		}
	}

}

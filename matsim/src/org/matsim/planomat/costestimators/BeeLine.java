/* *********************************************************************** *
 * project: org.matsim.*
 * BeeLine.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.planomat.costestimators;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.utils.geometry.CoordUtils;

/**
 * Example implementation of a leg travel time estimation (the estimation of
 * a travel time between two activities at a given departure time).
 * 
 * It computes the travel time for the bee line distance between the from nodes
 * of the origin and destination links, at a speed of 20 km/h.
 * 
 * @author meisterk
 * 
 * @deprecated This estimator is marked as deprecated because there are better estimators, and it is referenced nowhere.
 */
public class BeeLine implements LegTravelTimeEstimator {
	
	public void resetPlanSpecificInformation() {
		// TODO Auto-generated method stub
		
	}

	final static double SPEED = 20.0 / 3.6; // 20km/h --> m/s
	
	@Override
	public String toString() {
		return "BeeLine implementation of LegTravelTimeEstimator";
	}

	public double getLegTravelTimeEstimation(Id personId, double departureTime,
			ActivityImpl actOrigin, ActivityImpl actDestination,
			LegImpl legIntermediate, boolean doModifyLeg) {

		double distance = CoordUtils.calcDistance(actOrigin.getCoord(), actDestination.getCoord());
		
		return distance / SPEED;

	}

	public void initPlanSpecificInformation(PlanImpl plan) {
		// not implemented here
	}

	public LegImpl getNewLeg(TransportMode mode, ActivityImpl actOrigin,
			ActivityImpl actDestination, double departureTime) {
		// not implemented here
		return null;
	}
	
}

/* *********************************************************************** *
 * project: org.matsim.*
 * BikeTravelTime.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package playground.christoph.evacuation.trafficmonitoring;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;

/**
 * Data to calculate a person's travel time on a link is taken from:
 * John Parkin and Jonathon Rotheram (2010): Design speeds and acceleration characteristics
 * of bicycle traffic for use in planning and appraisal.
 * 
 * The default downhill speed is limited to 35 km/h. The value is scaled for each person
 * based on age, gender and scatter.
 * 
 * On steep links, a person is faster by walking than by biking. For such links, the
 * walk travel time is returned.
 */
public class BikeTravelTime extends WalkTravelTime {
	
	/*
	 * If the set reference speed does not match the default reference speed,
	 * the up- and downhill factors are scaled accordingly. 
	 */
	private final double defaultReferenceSpeed = 6.01;	// [m/s]
	
	private final double referenceBikeSpeed;		// 6.01 according to Prakin and Rotheram
	private final double maxBikeSpeed = 35.0 / 3.6;	// according to Parkin and Rotheram
	
	private final double downhillFactor = 0.2379;	// 0%..-15%
	private final double uphillFactor = -0.4002;	// 0%..12%
	
	private double personBikeSpeed;
	private double maxPersonBikeSpeed;
	private double personDownhillFactor;
	private double personUphillFactor;
	
	public BikeTravelTime(PlansCalcRouteConfigGroup plansCalcGroup) {
		super(plansCalcGroup);
		this.referenceBikeSpeed = plansCalcGroup.getBikeSpeed();
	}
	
	@Override
	public double getLinkTravelTime(Link link, double time) {
		
		double walkTravelTime = super.getLinkTravelTime(link, time);
		
		double slope = super.calcSlope(link);
		double slopeShift = getSlopeShift(slope);
		
		double linkSpeed = this.personBikeSpeed + slopeShift;
		
		// limit min and max speed
		if (linkSpeed > maxPersonBikeSpeed) linkSpeed = maxPersonBikeSpeed;
		else if (linkSpeed < 0.0) linkSpeed = Double.MIN_VALUE;
		
		double bikeTravelTime = link.getLength() / linkSpeed;
		return Math.min(walkTravelTime, bikeTravelTime);
	}
	
	/*
	 * It is assumed, that there is a linear relation between speed and slope.
	 * The returned values shifts the speed on a flat area.
	 * E.g. speed(10%) = speed(0%) + slopeShift(10%)
	 */
	/*package*/ double getSlopeShift(double slope) {
		if (slope > 0.0) return personUphillFactor * slope;
		else return personDownhillFactor * slope;
	}
	
	@Override
	public void setPerson(Person person) {
		super.setPerson(person);
		
		this.personBikeSpeed = this.referenceBikeSpeed * this.personFactor;
		
		this.maxPersonBikeSpeed = maxBikeSpeed * personFactor;
		this.personUphillFactor = uphillFactor * personFactor * referenceBikeSpeed / defaultReferenceSpeed;
		this.personDownhillFactor = downhillFactor * personFactor * referenceBikeSpeed / defaultReferenceSpeed;
	}
}

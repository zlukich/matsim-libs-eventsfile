/* *********************************************************************** *
 * project: org.matsim.*
 * PlansCalcRouteConfigGroup
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
package org.matsim.core.config.groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.Module;
import org.matsim.core.config.experimental.ReflectiveModule;
import org.matsim.core.utils.collections.CollectionUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * Config Module for PlansCalcRoute class.
 * Here you can specify the scale factors of freespeed travel time which are used
 * as travel time for not microsimulated modes.
 *
 * @author dgrether
 * @author mrieser
 */
public class PlansCalcRouteConfigGroup extends Module {


	public static final String GROUP_NAME = "planscalcroute";

	private static final String BEELINE_DISTANCE_FACTOR = "beelineDistanceFactor";
	private static final String NETWORK_MODES = "networkModes";
	private static final String TELEPORTED_MODE_SPEEDS = "teleportedModeSpeed_";
	private static final String TELEPORTED_MODE_FREESPEED_FACTORS = "teleportedModeFreespeedFactor_";

	public static final String UNDEFINED = "undefined";
	
	// For config file backward compatibility.
	// These are just hardcoded versions of the options above.
	private static final String PT_SPEED_FACTOR = "ptSpeedFactor";
	private static final String PT_SPEED = "ptSpeed";
	private static final String WALK_SPEED = "walkSpeed";
	private static final String BIKE_SPEED = "bikeSpeed";
	private static final String UNDEFINED_MODE_SPEED = "undefinedModeSpeed";
	
	private double beelineDistanceFactor = 1.3;
	private Collection<String> networkModes = Arrays.asList(TransportMode.car, TransportMode.ride); 

	private boolean acceptModeParamsWithoutClearing = false;

	public static class ModeRoutingParams extends ReflectiveModule implements MatsimParameters {
		public static final String SET_TYPE = "teleportedModeParameters";

		private String mode = null;
		private Double teleportedModeSpeed = null;
		private Double teleportedModeFreespeedFactor = null;

		public ModeRoutingParams(final String mode) {
			super( SET_TYPE );
			setMode( mode );
		}

		public ModeRoutingParams() {
			super( SET_TYPE );
		}

		@Override
		public Map<String, String> getComments() {
			final Map<String, String> map = super.getComments();

			map.put( "teleportedModeSpeed" ,
					"Speed for a teleported mode. " +
					"Travel time = (<beeline distance> * beelineDistanceFactor) / teleportedModeSpeed. Insert a line like this for every such mode.");
			map.put( "teleportedModeFreespeedFactor",
					"Free-speed factor for a teleported mode. " +
					"Travel time = teleportedModeFreespeedFactor * <freespeed car travel time>. Insert a line like this for every such mode. " +
					"Please do not set teleportedModeFreespeedFactor as well as teleportedModeSpeed for the same mode, but if you do, +" +
					"teleportedModeFreespeedFactor wins over teleportedModeSpeed.");

			return map;
		}

		@StringGetter( "mode" )
		public String getMode() {
			return mode;
		}

		@StringSetter( "mode" )
		public void setMode(String mode) {
			this.mode = mode;
		}

		@StringGetter( "teleportedModeSpeed" )
		public Double getTeleportedModeSpeed() {
			return teleportedModeSpeed;
		}

		@StringSetter( "teleportedModeSpeed" )
		public void setTeleportedModeSpeed(Double teleportedModeSpeed) {
			this.teleportedModeSpeed = teleportedModeSpeed;
		}

		@StringGetter( "teleportedModeFreespeedFactor" )
		public Double getTeleportedModeFreespeedFactor() {
			return teleportedModeFreespeedFactor;
		}

		@StringSetter( "teleportedModeFreespeedFactor" )
		public void setTeleportedModeFreespeedFactor(
				Double teleportedModeFreespeedFactor) {
			this.teleportedModeFreespeedFactor = teleportedModeFreespeedFactor;
		}
	}
	
	public PlansCalcRouteConfigGroup() {
		super(GROUP_NAME);

		acceptModeParamsWithoutClearing = true;
		{
			final ModeRoutingParams bike = new ModeRoutingParams( TransportMode.bike );
			bike.setTeleportedModeSpeed( 15.0 / 3.6 ); // 15.0 km/h --> m/s
			addParameterSet( bike );
		}

		{
			final ModeRoutingParams walk = new ModeRoutingParams( TransportMode.walk );
			walk.setTeleportedModeSpeed( 3.0 / 3.6 ); // 3.0 km/h --> m/s
			addParameterSet( walk );
		}

		// I'm not sure if anyone needs the "undefined" mode. In particular, it doesn't do anything for modes which are
		// really unknown, it is just a mode called "undefined". michaz 02-2012
		//
		// The original design idea was that some upstream module would figure out expected travel times and travel distances
		// for any modes, and the simulation would teleport all those modes it does not know anything about.
		// With the travel times and travel distances given by the mode.  In practice, it seems that people can live better
		// with the concept that mobsim figures it out by itself.  Although it is a much less flexible design.  kai, jun'2012
		{
			final ModeRoutingParams undefined = new ModeRoutingParams( UNDEFINED );
			undefined.setTeleportedModeSpeed( 50. / 3.6 ); // 50.0 km/h --> m/s
			addParameterSet( undefined );
		}

		{
			final ModeRoutingParams pt = new ModeRoutingParams( TransportMode.pt );
			pt.setTeleportedModeFreespeedFactor( 2.0 );
			addParameterSet( pt );
		}
		this.acceptModeParamsWithoutClearing = false;
	}

	@Override
	public Module createParameterSet( final String type ) {
		switch ( type ) {
			case ModeRoutingParams.SET_TYPE:
				return new ModeRoutingParams();
			default:
				throw new IllegalArgumentException( type );
		}
	}

	@Override
	protected void checkParameterSet( final Module module ) {
		switch ( module.getName() ) {
			case ModeRoutingParams.SET_TYPE:
				if ( !(module instanceof ModeRoutingParams) ) {
					throw new RuntimeException( "unexpected class for module "+module );
				}
				break;
			default:
				throw new IllegalArgumentException( module.getName() );
		}
	}

	@Override
	public void addParameterSet(final Module set) {
		if ( set.getName().equals( ModeRoutingParams.SET_TYPE ) && !this.acceptModeParamsWithoutClearing ) {
			clearParameterSetsForType( set.getName() );
			this.acceptModeParamsWithoutClearing = true;
		}
		super.addParameterSet( set );
	}

	public void addModeRoutingParams(final ModeRoutingParams pars) {
		addParameterSet( pars );
	}

	public Map<String, ModeRoutingParams> getModeRoutingParams() {
		final Map<String, ModeRoutingParams> map = new LinkedHashMap< >();

		for ( Module pars : getParameterSets( ModeRoutingParams.SET_TYPE ) ) {
			final String mode = ((ModeRoutingParams) pars).getMode();
			final ModeRoutingParams old =
				map.put( mode ,
					(ModeRoutingParams)	pars );
			if ( old != null ) throw new IllegalStateException( "several parameter sets for mode "+mode );
		}

		return map;
	}

	public ModeRoutingParams getOrCreateModeRoutingParams(final String mode) {
		ModeRoutingParams pars = getModeRoutingParams().get( mode );

		if ( pars == null ) {
			pars = (ModeRoutingParams) createParameterSet( ModeRoutingParams.SET_TYPE );
			pars.setMode( mode );
			addParameterSet( pars );
		}

		return pars;
	}

	@Override
	public String getValue(final String key) {
		throw new IllegalArgumentException(key + ": getValue access disabled; use direct getter");
	}

	@Override
	public void addParam(final String key, final String value) {
		if (PT_SPEED_FACTOR.equals(key)) {
			setTeleportedModeFreespeedFactor(TransportMode.pt, Double.parseDouble(value));
		} else if (BEELINE_DISTANCE_FACTOR.equals(key)) {
			setBeelineDistanceFactor(Double.parseDouble(value));
		} else if (PT_SPEED.equals(key)) {
			setTeleportedModeSpeed(TransportMode.pt, Double.parseDouble(value));
		} else if (WALK_SPEED.equals(key)) {
			setTeleportedModeSpeed(TransportMode.walk, Double.parseDouble(value));
		} else if (BIKE_SPEED.equals(key)) {
			setTeleportedModeSpeed(TransportMode.bike, Double.parseDouble(value));
		} else if (UNDEFINED_MODE_SPEED.equals(key)) {
			setTeleportedModeSpeed(UNDEFINED, Double.parseDouble(value));
		} else if (NETWORK_MODES.equals(key)) {
			setNetworkModes(Arrays.asList(CollectionUtils.stringToArray(value)));
		} else if (key.startsWith(TELEPORTED_MODE_SPEEDS)) {
			setTeleportedModeSpeed(key.substring(TELEPORTED_MODE_SPEEDS.length()), Double.parseDouble(value));
		} else if (key.startsWith(TELEPORTED_MODE_FREESPEED_FACTORS)) {
			setTeleportedModeFreespeedFactor(key.substring(TELEPORTED_MODE_FREESPEED_FACTORS.length()), Double.parseDouble(value));
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public final Map<String, String> getParams() {
		Map<String, String> map = super.getParams();
		map.put( BEELINE_DISTANCE_FACTOR, Double.toString(this.getBeelineDistanceFactor()) );
		map.put( NETWORK_MODES, CollectionUtils.arrayToString(this.networkModes.toArray(new String[this.networkModes.size()])));
		return map;
	}

	@Override
	public final Map<String, String> getComments() {
		Map<String,String> map = super.getComments();

		map.put(BEELINE_DISTANCE_FACTOR, "factor with which beeline distances (and therefore times) " +
				"are multiplied in order to obtain an estimate of the network distances/times.  Default is something like 1.3") ;
		map.put(NETWORK_MODES, "All the modes for which the router is supposed to generate network routes (like car)") ;

		return map;
	}

	public double getBeelineDistanceFactor() {
		return this.beelineDistanceFactor;
	}

	public void setBeelineDistanceFactor(double beelineDistanceFactor) {
		this.beelineDistanceFactor = beelineDistanceFactor;
	}

	public Collection<String> getNetworkModes() {
		return this.networkModes;
	}

	public void setNetworkModes(Collection<String> networkModes) {
		this.networkModes = networkModes;
	}

	public Map<String, Double> getTeleportedModeSpeeds() {
		final Map<String, Double> map = new LinkedHashMap< >();
		for ( ModeRoutingParams pars :
				(Collection<ModeRoutingParams>) getParameterSets( ModeRoutingParams.SET_TYPE ) ) {
			final Double speed = pars.getTeleportedModeSpeed();
			if ( speed != null ) map.put( pars.getMode() , speed );
		}
		return map;
	}

	public Map<String, Double> getTeleportedModeFreespeedFactors() {
		final Map<String, Double> map = new LinkedHashMap< >();
		for ( ModeRoutingParams pars :
				(Collection<ModeRoutingParams>) getParameterSets( ModeRoutingParams.SET_TYPE ) ) {
			final Double speed = pars.getTeleportedModeSpeed();
			if ( speed != null ) map.put( pars.getMode() , speed );
		}
		return map;

	}

	public void setTeleportedModeFreespeedFactor(String mode, double freespeedFactor) {
		getOrCreateModeRoutingParams( mode ).setTeleportedModeFreespeedFactor( freespeedFactor );
	}

	public void setTeleportedModeSpeed(String mode, double speed) {
		getOrCreateModeRoutingParams( mode ).setTeleportedModeSpeed( speed );
	}

}

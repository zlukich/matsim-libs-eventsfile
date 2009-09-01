/* *********************************************************************** *
 * project: org.matsim.*
 * CalcLegTimesKTIListener.java
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

package playground.meisterk.kti.controler.listeners;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.misc.Time;

import playground.meisterk.org.matsim.analysis.CalcLegTimesKTI;
import playground.meisterk.org.matsim.population.algorithms.AbstractClassifiedFrequencyAnalysis.CrosstabFormat;

public class CalcLegTimesKTIListener implements StartupListener, AfterMobsimListener, ShutdownListener, IterationEndsListener {

	public static final double[] timeBins = new double[]{
		0.0 * 60.0, 
		5.0 * 60.0, 
		10.0 * 60.0, 
		15.0 * 60.0, 
		20.0 * 60.0, 
		25.0 * 60.0, 
		30.0 * 60.0, 
		60.0 * 60.0, 
		120.0 * 60.0, 
		240.0 * 60.0, 
		480.0 * 60.0, 
		960.0 * 60.0, 
	};

	final private String averagesSummaryFilename;
	final private String travelTimeDistributionFilename;
	
	private PrintStream iterationSummaryOut;
	private CalcLegTimesKTI calcLegTimesKTI;
	
	private final static Logger log = Logger.getLogger(CalcLegTimesKTIListener.class);

	public CalcLegTimesKTIListener(String averagesSummaryFilename, String travelTimeDistributionFilename) {
		super();
		this.averagesSummaryFilename = averagesSummaryFilename;
		this.travelTimeDistributionFilename = travelTimeDistributionFilename;
	}
	
	public void notifyStartup(StartupEvent event) {

		try {
			this.iterationSummaryOut = new PrintStream(org.matsim.core.controler.Controler.getOutputFilename(this.averagesSummaryFilename));
			this.iterationSummaryOut.print("#iteration\tall");
			for (TransportMode mode : TransportMode.values()) {
				this.iterationSummaryOut.print("\t" + mode.toString());
			}
			this.iterationSummaryOut.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		this.calcLegTimesKTI = new CalcLegTimesKTI(event.getControler().getPopulation(), iterationSummaryOut);
		event.getControler().getEvents().addHandler(this.calcLegTimesKTI);

	}
	
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		
		TreeMap<TransportMode, Double> avgTripDurations = this.calcLegTimesKTI.getAverageTripDurationsByMode();
		String str;
		
		this.iterationSummaryOut.print(Integer.toString(event.getIteration()));
		this.iterationSummaryOut.print("\t" + Time.writeTime(this.calcLegTimesKTI.getAverageOverallTripDuration()));
		for (TransportMode mode : TransportMode.values()) {
			if (avgTripDurations.containsKey(mode)) {
				str = Time.writeTime(avgTripDurations.get(mode));
			} else {
				str = "---";
			}
			this.iterationSummaryOut.print("\t" + str);
		}
		this.iterationSummaryOut.println();
		this.iterationSummaryOut.flush();
	}

	public void notifyIterationEnds(IterationEndsEvent event) {
		
		if (event.getIteration() % 10 == 0) {

			PrintStream out = null;
			try {
				out = new PrintStream(org.matsim.core.controler.Controler.getIterationFilename(travelTimeDistributionFilename, event.getIteration()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			log.info("Writing results file...");
			for (boolean isCumulative : new boolean[]{false, true}) {
				for (CrosstabFormat crosstabFormat : CrosstabFormat.values()) {
					this.calcLegTimesKTI.printClasses(crosstabFormat, isCumulative, timeBins, out);
				}
			}
			this.calcLegTimesKTI.printDeciles(true, out);
			out.close();
			log.info("Writing results file...done.");
			
		}
		
	}

	public void notifyShutdown(ShutdownEvent event) {
		this.iterationSummaryOut.close();
	}

}

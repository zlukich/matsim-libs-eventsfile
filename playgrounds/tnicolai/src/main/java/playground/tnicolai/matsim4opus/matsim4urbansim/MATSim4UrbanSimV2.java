/* *********************************************************************** *
 * project: org.matsim.*
 * MATSim4UrbanSim.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

/**
 *
 */
package playground.tnicolai.matsim4opus.matsim4urbansim;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

import playground.tnicolai.matsim4opus.constants.Constants;
import playground.tnicolai.matsim4opus.utils.InitMATSimScenario;
import playground.tnicolai.matsim4opus.utils.helperObjects.Benchmark;
import playground.tnicolai.matsim4opus.utils.helperObjects.JobClusterObject;
import playground.tnicolai.matsim4opus.utils.io.BackupRun;
import playground.tnicolai.matsim4opus.utils.io.Paths;
import playground.tnicolai.matsim4opus.utils.io.ReadFromUrbansimParcelModel;


/**
 * @author thomas
 * 
 * improvements jan'12:
 * 
 * - This class is a revised version of "MATSim4UrbanSim".
 * - Increased Configurability: 
 * 	First approach to increase the configurability of MATSim4UrbanSim modules such as
 * 	the zonz2zone impedance matrix, zone based- and grid based accessibility computation. Modules can be en-disabled
 * 	additional modules can be added by other classes extending MATSim4UrbanSimV2.
 * - Data Processing on Demand:
 *  Particular input data is processed when a corresponding module is enabled, e.g. an array of aggregated workplaces will
 *  be generated when either the zone based- or grid based accessibility computation is activated.
 * - Extensibility:
 * 	This class provides standard functionality such as configuring MATSim, reading UrbanSim input data, running the 
 * 	mobility simulation and so forth... This functionality can be extended by an inheriting class (e.g. MATSim4UrbanSimZurichAccessibility) 
 * 	by implementing certain stub methods such as "addFurtherControlerListener", "modifyNetwork", "modifyPopulation" ...
 * - Backup Results:
 *  This was also available before but not documented. Some data is overwritten with each run, e.g. the zone2zone impedance matrix or data
 *  in the MATSim output folder. If the backup is activated the most imported files (see BackupRun class) are saved in a new folder. In order 
 *  to match the saved data with the corresponding run or year the folder names contain the "simulation year" and a time stamp.
 * - Other improvements:
 * 	For a better readability of code some functionality is outsourced into helper classes
 */
public class MATSim4UrbanSimV2 {

	// logger
	private static final Logger log = Logger.getLogger(MATSim4UrbanSimV2.class);

	// MATSim scenario
	ScenarioImpl scenario = null;
	// Reads UrbanSim outputfiles
	ReadFromUrbansimParcelModel readFromUrbansim = null;
	// Benchmarking computation times and hard disc space ... 
	Benchmark benchmark = null;
	// indicates if MATSim run was successful
	static boolean isSuccessfulMATSimRun = Boolean.FALSE;
	
	
	/**
	 * constructor
	 * 
	 * @param args contains at least a reference to 
	 * 		  MATSim4UrbanSim configuration generated by UrbanSim
	 */
	MATSim4UrbanSimV2(String args[]){
		
		// Stores location of MATSim configuration file
		String matsimConfiFile = (args!= null && args.length>0) ? args[0].trim():null;
		// checks if args parameter contains a valid path
		Paths.isValidPath(matsimConfiFile);
		
		// get default scenario
		scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		// init scenario
		if( !(new InitMATSimScenario(scenario, matsimConfiFile)).init() ){
			log.error("An error occured while initializing MATSim scenario ...");
			System.exit(-1);
		}		
		ScenarioUtils.loadScenario(scenario);
		
		// init Benchmark as default
		benchmark = new Benchmark();
	}
	
	/**
	 * prepare MATSim for traffic flow simulation ...
	 */
	void runMATSim(){
		log.info("Starting MATSim from Urbansim");	

		// checking if this is a test run
		// a test run only validates the xml config file by initializing the xml config via the xsd.
		isTestTun();

		// get the network. Always cleaning it seems a good idea since someone may have modified the input files manually in
		// order to implement policy measures.  Get network early so readXXX can check if links still exist.
		Network network = scenario.getNetwork();
		modifyNetwork(network);
		cleanNetwork(network);
		
		// get the data from UrbanSim (parcels and persons)
		readFromUrbansim = new ReadFromUrbansimParcelModel( Integer.parseInt( scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.YEAR) ) );
		// read UrbanSim facilities (these are simply those entities that have the coordinates!)
		ActivityFacilitiesImpl parcels = new ActivityFacilitiesImpl("urbansim locations (gridcells _or_ parcels _or_ ...)");
		ActivityFacilitiesImpl zones   = new ActivityFacilitiesImpl("urbansim zones");
		// initializing parcels and zones from UrbanSim input
		readUrbansimParcelModel(parcels, zones);
		
		// population generation
		int pc = benchmark.addMeasure("Population construction");
		Population newPopulation = readUrbansimPersons(parcels, network);
		modifyPopulation(newPopulation);
		benchmark.stoppMeasurement(pc);
		System.out.println("Population construction took: " + benchmark.getDurationInSeconds( pc ) + " seconds.");

		log.info("### DONE with demand generation from urbansim ###");

		// set population in scenario
		scenario.setPopulation(newPopulation);

		// running mobsim and assigned controller listener
		runControler(zones, parcels);
	}
	
	void isTestTun(){
		if( scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.IS_TEST_RUN).equalsIgnoreCase(Constants.TRUE)){
			log.info("TestRun was successful...");
			return;
		}
	}
	
	/**
	 * read UrbanSim parcel table and build facilities and zones in MATSim
	 * 
	 * @param parcels
	 * @param zones
	 */
	void readUrbansimParcelModel(ActivityFacilitiesImpl parcels, ActivityFacilitiesImpl zones){
		readFromUrbansim.readFacilities(parcels, zones);
	}
	
	/**
	 * Reads the UrbanSim job table and aggregates jobs with same nearest node 
	 * 
	 * @return JobClusterObject[] 
	 */
	JobClusterObject[] readUrbansimJobs(ActivityFacilitiesImpl parcels, double jobSample){
		return readFromUrbansim.getAggregatedWorkplaces(parcels, jobSample, (NetworkImpl) scenario.getNetwork());
	}
	
	/**
	 * read person table from urbansim and build MATSim population
	 * 
	 * @param readFromUrbansim
	 * @param parcels
	 * @param network
	 * @return
	 */
	Population readUrbansimPersons(ActivityFacilitiesImpl parcels, Network network){
		// read urbansim population (these are simply those entities that have the person, home and work ID)
		Population oldPopulation = null;
		
		// check for existing plans file
		if ( scenario.getConfig().plans().getInputFile() != null ) {
			
			String mode = scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.MATSIM_MODE);
			if(mode.equals(Constants.HOT_START))
				log.info("MATSim is running in HOT start mode, i.e. MATSim starts with pop file from previous run: " + scenario.getConfig().plans().getInputFile());
			else if(mode.equals(Constants.WARM_START))
				log.info("MATSim is running in WARM start mode, i.e. MATSim starts with pre-existing pop file:" + scenario.getConfig().plans().getInputFile());
			
			log.info("Persons not found in pop file are added; persons no longer in urbansim persons file are removed." ) ;
			
			oldPopulation = scenario.getPopulation() ;
		}
		else {
			log.warn("No population specified in matsim config file; assuming COLD start.");
			log.info("(I.e. generate new pop from urbansim files.)" );
			oldPopulation = null;
		}

		// read UrbanSim persons.  Generates hwh acts as side effect
		double populationSampleRate = Double.parseDouble( scenario.getConfig().getParam(Constants.MATSIM_4_URBANSIM_PARAM, Constants.SAMPLING_RATE));
		Population newPopulation = readFromUrbansim.readPersons( oldPopulation, parcels, network, populationSampleRate ) ;
		
		// clean
		oldPopulation=null;
		System.gc();
		
		return newPopulation;
	}
	
	/**
	 * run simulation
	 * @param zones
	 */
	void runControler( ActivityFacilitiesImpl zones, ActivityFacilitiesImpl parcels){
		
		Controler controler = new Controler(scenario);
		controler.setOverwriteFiles(true);	// sets, whether output files are overwritten
		controler.setCreateGraphs(false);	// sets, whether output Graphs are created
		
		log.info("Adding controler listener ...");
		addControlerListener(zones, parcels, controler);
		addFurtherControlerListener(controler, parcels);
		log.info("Adding controler listener done!");
		
		// tnicolai todo?: count number of cars per h on a link
		// write ControlerListener that implements AfterMobsimListener (notifyAfterMobsim)
		// get VolumeLinkAnalyzer by "event.getControler.getVolume... and run getVolumesForLink. that returns an int array with the number of cars per hour on an specific link 
		// see also http://matsim.org/docs/controler
		
		// run the iterations, including the post-processing:
		controler.run() ;
	}

	/**
	 * The following method register listener that should be done _after_ the iterations were run.
	 * 
	 * @param zones
	 * @param parcels
	 * @param controler
	 */
	void addControlerListener(ActivityFacilitiesImpl zones, ActivityFacilitiesImpl parcels, Controler controler) {
		
		// tnicolai: make this configurable 
		boolean computeZone2ZoneImpedance = true;
		boolean computeZoneBasedAccessibilities = true;
		
		// The following lines register what should be done _after_ the iterations were run:
		if(computeZone2ZoneImpedance)
			// creates zone2zone impedance matrix
			controler.addControlerListener( new Zone2ZoneImpedancesControlerListener( zones, 
																					  parcels) ); 
		
		if(computeZoneBasedAccessibilities)
			// creates zone based table of log sums (workplace accessibility)
			// uses always a 100% jobSample size (see readUrbansimJobs below)
			controler.addControlerListener( new ZoneBasedAccessibilityControlerListener(zones, 				
																						readUrbansimJobs(parcels, 1.), 
																						benchmark));
	}
	
	/**
	 * This method allows to add additional listener
	 * This needs to be implemented by another class
	 */
	void addFurtherControlerListener(Controler controler, ActivityFacilitiesImpl parcels){
		// this is just a stub and does nothing. 
		// This needs to be implemented/overwritten by another class
	}
	
	/**
	 * cleaning matsim network
	 * @param network
	 */
	void cleanNetwork(Network network){
		log.info("") ;
		log.info("Cleaning network ...");
		( new NetworkCleaner() ).run(network);
		log.info("... finished cleaning network.");
		log.info("");
	}
	
	/**
	 * This method allows to modify the MATSim network
	 * This needs to be implemented by another class
	 * 
	 * @param network
	 */
	void modifyNetwork(Network network){
		// this is just a stub and does nothing. 
		// This needs to be implemented/overwritten by another class
	}
	
	/**
	 * This method allows to modify the population
	 * This needs to be implemented by another class
	 * 
	 * @param population
	 */
	void modifyPopulation(Population population){
		// this is just a stub and does nothing. 
		// This needs to be implemented/overwritten by another class
	}
	
	/**
	 * triggers backup of MATSim and UrbanSim Output
	 */
	void matim4UrbanSimShutdown(){
		BackupRun.runBackup(scenario);
	}
	
	/**
	 * Entry point
	 * @param args UrbanSim command prompt
	 */
	public static void main(String args[]){
		MATSim4UrbanSimV2 m4u = new MATSim4UrbanSimV2(args);
		m4u.runMATSim();
		m4u.matim4UrbanSimShutdown();
		MATSim4UrbanSimV2.isSuccessfulMATSimRun = Boolean.TRUE;
	}
	
	/**
	 * this method is only called/needed by "matsim4opus.matsim.MATSim4UrbanSimTest"
	 * @return true if run was successful
	 */
	public static boolean getRunStatus(){
		return MATSim4UrbanSimV2.isSuccessfulMATSimRun;
	}
}

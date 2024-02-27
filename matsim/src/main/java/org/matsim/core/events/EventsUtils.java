
/* *********************************************************************** *
 * project: org.matsim.*
 * EventsUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

package org.matsim.core.events;

import org.apache.commons.compress.utils.FileNameUtils;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Injector;
import org.matsim.utils.eventsfilecomparison.*;

public final class EventsUtils {

	/**
	 * Create a events manager instance that guarantees causality of processed events across all handlers.
	 */
	public static EventsManager createEventsManager() {
		return new EventsManagerImpl();
	}

	/**
	 * Creates a parallel events manager, with no guarantees for the order of processed events between multiple handlers.
	 */
	public static EventsManager createParallelEventsManager() {
		return new ParallelEventsManager(false);
	}

	public static EventsManager createEventsManager(Config config) {
		final EventsManager events = Injector.createInjector(config, new EventsManagerModule()).getInstance(EventsManager.class);
//		events.initProcessing();
		return events;
	}

	/**
	 * The SimStepParallelEventsManagerImpl can handle events from multiple threads.
	 * The (Parallel)EventsMangerImpl cannot, therefore it has to be wrapped into a
	 * SynchronizedEventsManagerImpl.
	 */
	public static EventsManager getParallelFeedableInstance(EventsManager events) {
		if (events instanceof SimStepParallelEventsManagerImpl) {
			return events;
		} else if (events instanceof ParallelEventsManager) {
			return events;
		} else if (events instanceof SynchronizedEventsManagerImpl) {
			return events;
		} else {
			return new SynchronizedEventsManagerImpl(events);
		}
	}

	/**
	 * Create and write fingerprint file for events.
	 */
	public static void createEventsFingerprint(String eventFile, String outputFingerprintFile) {

		EventsManager manager = createEventsManager();
		FingerprintEventHandler fingerprintEventHandler = new FingerprintEventHandler();
		manager.addHandler(fingerprintEventHandler);
		EventsUtils.readEvents(manager, eventFile);
		manager.finishProcessing();

		EventFingerprint.write(outputFingerprintFile, fingerprintEventHandler.eventFingerprint);
	}


	/**
	 * Compares existing event file against fingerprint file. This will also create new fingerprint file along the input events.
	 *
	 * @return comparison results
	 */
	public static ComparisonResult createAndCompareEventsFingerprint(String inputFingerprint, String eventFile) {

		// header byte, version byte
		// bin array time stamps
		// event type counter map
		// one hash (sha1?)

		String baseName = FileNameUtils.getBaseName(eventFile).replace(".xml", "");

		createEventsFingerprint(eventFile, baseName + ".fp.zst");

		return EventsFileFingerprintComparator.compare(inputFingerprint, eventFile);
	}

	public static void readEvents(EventsManager events, String filename) {
		new MatsimEventsReader(events).readFile(filename);
	}

	public static ComparisonResult compareEventsFiles(String filename1, String filename2) {
		return EventsFileComparator.compare(filename1, filename2);
	}

}

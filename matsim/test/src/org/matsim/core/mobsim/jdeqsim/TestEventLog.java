package org.matsim.core.mobsim.jdeqsim;

import java.util.ArrayList;

import org.matsim.core.mobsim.jdeqsim.util.CppEventFileParser;
import org.matsim.testcases.MatsimTestCase;


public class TestEventLog extends MatsimTestCase {

	
	
	public void testGetTravelTime(){
		ArrayList<EventLog> deqSimLog=CppEventFileParser.parseFile(getPackageInputDirectory() + "deq_events.txt");
		assertEquals(3599.0, Math.floor(EventLog.getTravelTime(deqSimLog,1)), EPSILON);
	}
	
	
	public void testGetAverageTravelTime(){
		ArrayList<EventLog> deqSimLog=CppEventFileParser.parseFile(getPackageInputDirectory() + "deq_events.txt");
		assertEquals(EventLog.getTravelTime(deqSimLog,1), EventLog.getSumTravelTime(deqSimLog), EPSILON);
	}
}

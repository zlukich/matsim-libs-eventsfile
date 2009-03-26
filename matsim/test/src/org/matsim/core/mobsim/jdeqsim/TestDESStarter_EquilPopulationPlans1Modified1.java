package org.matsim.core.mobsim.jdeqsim;

import org.matsim.core.mobsim.jdeqsim.scenarios.EquilPopulationPlans1Modified1;
import org.matsim.core.mobsim.jdeqsim.util.DEQSimEventFileComparator;
import org.matsim.core.mobsim.jdeqsim.util.DEQSimEventFileTravelTimeComparator;
import org.matsim.core.mobsim.jdeqsim.util.TestHandlerDetailedEventChecker;
import org.matsim.testcases.MatsimTestCase;

public class TestDESStarter_EquilPopulationPlans1Modified1 extends MatsimTestCase {

	// mit enable assertion flag funktionieren einige tests nicht mehr!!! =>
	// make test cases for these assertions.

	public void test_EquilPopulationPlans1Modified1_TestHandlerDetailedEventChecker() {
		TestHandlerDetailedEventChecker detailedChecker = new TestHandlerDetailedEventChecker();
		detailedChecker.startTestDES("test/scenarios/equil/config.xml", false,
				"test/scenarios/equil/plans1.xml", new EquilPopulationPlans1Modified1());
	}

	public void test_EquilPopulationPlans1Modified1_DEQSimEventFileComparator() {
		DEQSimEventFileComparator deqSimComparator = new DEQSimEventFileComparator(
				getPackageInputDirectory() + "deq_events.txt");
		deqSimComparator.startTestDES("test/scenarios/equil/config.xml", false,
				"test/scenarios/equil/plans1.xml", new EquilPopulationPlans1Modified1());
	}

	public void test_EquilPopulationPlans1Modified1_DEQSimEventFileTravelTimeComparator() {
		DEQSimEventFileTravelTimeComparator deqSimTravelTimeComparator = new DEQSimEventFileTravelTimeComparator(
				getPackageInputDirectory() + "deq_events.txt", 1);
		deqSimTravelTimeComparator.startTestDES("test/scenarios/equil/config.xml", false,
				"test/scenarios/equil/plans1.xml", new EquilPopulationPlans1Modified1());
	}

}

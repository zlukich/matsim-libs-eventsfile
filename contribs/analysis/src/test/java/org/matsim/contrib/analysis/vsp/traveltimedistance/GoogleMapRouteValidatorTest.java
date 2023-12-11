package org.matsim.contrib.analysis.vsp.traveltimedistance;

import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.testcases.MatsimTestUtils;

import java.io.*;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GoogleMapRouteValidatorTest {
	@Rule
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	public void testReadJson() throws IOException, ParseException {
		GoogleMapRouteValidator googleMapRouteValidator = getDummyValidator();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(utils.getClassInputDirectory() + "route.json")));
		Optional<Tuple<Double, Double>> result = googleMapRouteValidator.readFromJson(reader);
		assertTrue(result.isPresent());
		assertEquals(413, result.get().getFirst(), MatsimTestUtils.EPSILON);
		assertEquals(2464, result.get().getSecond(), MatsimTestUtils.EPSILON);
	}

	//All values with null filled are not necessary for this test
	private GoogleMapRouteValidator getDummyValidator() {
		return new GoogleMapRouteValidator(utils.getOutputDirectory(), null, null, null, null);
	}
}

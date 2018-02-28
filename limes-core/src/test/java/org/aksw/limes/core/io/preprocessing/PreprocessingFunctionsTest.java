package org.aksw.limes.core.io.preprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;

import org.aksw.limes.core.io.cache.HybridCache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.preprocessing.AProcessingFunction.IllegalNumberOfParametersException;
import org.aksw.limes.core.io.preprocessing.functions.CleanIri;
import org.aksw.limes.core.io.preprocessing.functions.CleanNumber;
import org.aksw.limes.core.io.preprocessing.functions.RegexReplace;
import org.aksw.limes.core.io.preprocessing.functions.RemoveLanguageTag;
import org.aksw.limes.core.io.preprocessing.functions.Replace;
import org.aksw.limes.core.io.preprocessing.functions.ToCelsius;
import org.aksw.limes.core.io.preprocessing.functions.ToFahrenheit;
import org.aksw.limes.core.io.preprocessing.functions.ToLowercase;
import org.aksw.limes.core.io.preprocessing.functions.ToUppercase;
import org.junit.Before;
import org.junit.Test;

public class PreprocessingFunctionsTest {
	public static final String TEST_INSTANCE = "http://dbpedia.org/resource/Ibuprofen";
	public static final String UPPERCASE_EXPECTED = "IBUPROFEN@DE";
	public static final String LOWERCASE_EXPECTED = "ibuprofen@de";
	public static final String REPLACE_EXPECTED = "Ibuprofen";
	public static final String REPLACE_EXPECTED2 = "Ibuprofen@en";
	public static final String REMOVELANGUAGETAG_EXPECTED = "testext";
	public static final String REGEX_REPLACE_EXPECTED = "Ibuprofen is a nonsteroidal anti-inflammatory drug derivative of propionic acid used for relieving pain, helping with fever and reducing inflammation.";
	public static final String CLEAN_IRI_EXPECTED = "label";
	public static final String CLEAN_NUMBER_EXPECTED = "10";
	public static final String TO_CELSIUS_EXPECTED = "-28.88888888888889";
	public static final String TO_FAHRENHEIT_EXPECTED = "-4.0";

	public static final String PROP_LABEL = "rdfs:label";
	public static final String PROP_TEST2 = "test2";
	public static final String PROP_ABSTRACT = "dbo:abstract";
	public static final String PROP_ABSTRACT_VALUE = "Ibuprofen (/ˈaɪbjuːproʊfɛn/ or /aɪbjuːˈproʊfən/ EYE-bew-PROH-fən; from isobutylphenylpropanoic acid) is a nonsteroidal anti-inflammatory drug (NSAID) derivative of propionic acid used for relieving pain, helping with fever and reducing inflammation.@en";
	public static final String PROP_IRI = "iri";
	public static final String PROP_IRI_VALUE = "http://www.w3.org/2000/01/rdf-schema#label";
	public static final String PROP_NUMBER = "number";
	public static final String PROP_NUMBER_VALUE = "10^^http://www.w3.org/2001/XMLSchema#positiveInteger";
	public static final String PROP_TEMPERATURE = "temperature";
	public static final String PROP_TEMPERATURE_VALUE = "-20^^http://www.w3.org/2001/XMLSchema#decimal";

	//Removes everything inside braces and language tag
	public static final String REGEX = "\\((.*?)\\) |@\\w*"; 
	public HybridCache cache;

	@Before
	public void prepareData() {
		cache = new HybridCache();
		Instance testInstance = new Instance(TEST_INSTANCE);

		TreeSet<String> labels = new TreeSet<>();
		labels.add("Ibuprofen@de");
		labels.add("Ibuprofen@en");
		testInstance.addProperty(PROP_LABEL, labels);

		testInstance.addProperty(PROP_TEST2, REMOVELANGUAGETAG_EXPECTED);
		testInstance.addProperty(PROP_ABSTRACT,PROP_ABSTRACT_VALUE);
		testInstance.addProperty(PROP_IRI,PROP_IRI_VALUE);
		testInstance.addProperty(PROP_NUMBER,PROP_NUMBER_VALUE);
		testInstance.addProperty(PROP_TEMPERATURE, PROP_TEMPERATURE_VALUE);

		cache.addInstance(testInstance);
	}

	@Test
	public void testHasPropertyPredicate() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertTrue(AProcessingFunction.hasProperty("a").test(i));
	}

	@Test
	public void testHasPropertyPredicateWrongProperty() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertFalse(AProcessingFunction.hasProperty("d").test(i));
	}

	@Test
	public void testHasPropertyPredicateMultipleProperties() {
		Instance i = new Instance("uri");
		i.addProperty("a", "a");
		i.addProperty("b", "b");
		i.addProperty("c", "c");
		assertTrue(AProcessingFunction.hasProperty(new String[] { "a", "b", "d" }).test(i));
	}

	@Test
	public void testUppercase() throws IllegalNumberOfParametersException {
		new ToUppercase().process(cache, new String[] { PROP_LABEL });
		assertTrue(cache.size() > 0);
		assertEquals(UPPERCASE_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).first());
	}

	@Test
	public void testAProcessingFunctionWrongPropertyNumber() {
		try {
			// The relevant functionality is the same for all classes that
			// extend AProcessingFunction
			new ToUppercase().process(cache, new String[0]);
			assertFalse(true);
		} catch (IllegalNumberOfParametersException e) {
		}
	}

	@Test
	public void testAProcessingFunctionWrongArgumentNumber() {
		try {
			// The relevant functionality is the same for all classes that
			// extend AProcessingFunction
			new ToUppercase().process(cache, new String[] { PROP_LABEL }, "bla");
			assertFalse(true);
		} catch (IllegalNumberOfParametersException e) {
		}
	}

	@Test
	public void testLowercase() throws IllegalNumberOfParametersException {
		new ToLowercase().process(cache, new String[] { PROP_LABEL });
		assertTrue(cache.size() > 0);
		assertEquals(LOWERCASE_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).first());
	}

	@Test
	public void testRemoveLanguageTag() throws IllegalNumberOfParametersException {
		new RemoveLanguageTag().process(cache, new String[] { PROP_LABEL });
		assertTrue(cache.size() > 0);
		assertEquals(1, cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).size());
		assertEquals(REPLACE_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).first());
	}

	@Test
	public void testRemoveLanguageTagNoTagPresent() throws IllegalNumberOfParametersException {
		new RemoveLanguageTag().process(cache, new String[] { PROP_TEST2 });
		assertTrue(cache.size() > 0);
		assertEquals(1, cache.getInstance(TEST_INSTANCE).getProperty(PROP_TEST2).size());
		assertEquals(REMOVELANGUAGETAG_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_TEST2).first());
	}

	@Test
	public void testReplace() throws IllegalNumberOfParametersException {
		new Replace().process(cache, new String[] { PROP_LABEL }, "@de", "");
		assertTrue(cache.size() > 0);
		assertTrue(cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).contains(REPLACE_EXPECTED));
		assertTrue(cache.getInstance(TEST_INSTANCE).getProperty(PROP_LABEL).contains(REPLACE_EXPECTED2));
	}
	
	@Test
	public void testRegexReplace() throws IllegalNumberOfParametersException {
		new RegexReplace().process(cache, new String[] { PROP_ABSTRACT }, REGEX, "");
		assertTrue(cache.size() > 0);
		assertEquals(REGEX_REPLACE_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_ABSTRACT).first());
	}

	@Test
	public void testCleanIri() throws IllegalNumberOfParametersException {
		new CleanIri().process(cache, new String[] { PROP_IRI });
		assertTrue(cache.size() > 0);
		assertEquals(CLEAN_IRI_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_IRI).first());
	}

	@Test
	public void testCleanNumber() throws IllegalNumberOfParametersException {
		new CleanNumber().process(cache, new String[] { PROP_NUMBER });
		assertTrue(cache.size() > 0);
		assertEquals(CLEAN_NUMBER_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_NUMBER).first());
	}

	@Test
	public void testToCelsius() throws IllegalNumberOfParametersException {
		new ToCelsius().process(cache, new String[] { PROP_TEMPERATURE });
		assertTrue(cache.size() > 0);
		assertEquals(TO_CELSIUS_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_TEMPERATURE).first());
	}

	@Test
	public void testToFahrenheit() throws IllegalNumberOfParametersException {
		new ToFahrenheit().process(cache, new String[] { PROP_TEMPERATURE });
		assertTrue(cache.size() > 0);
		assertEquals(TO_FAHRENHEIT_EXPECTED,cache.getInstance(TEST_INSTANCE).getProperty(PROP_TEMPERATURE).first());
	}
}

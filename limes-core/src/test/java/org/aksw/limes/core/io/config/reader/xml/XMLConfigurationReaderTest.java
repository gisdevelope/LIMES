package org.aksw.limes.core.io.config.reader.xml;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.limes.core.io.config.Configuration;
import org.aksw.limes.core.io.config.KBInfo;
import org.aksw.limes.core.io.preprocessing.NEWPreprocessor;
import org.aksw.limes.core.io.preprocessing.functions.Concat;
import org.aksw.limes.core.ml.algorithm.LearningParameter;
import org.aksw.limes.core.ml.algorithm.MLImplementationType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author Mohamed Sherif (sherif@informatik.uni-leipzig.de)
 * @version Jan 15, 2016
 */
public class XMLConfigurationReaderTest {
    
    Map<String, String> prefixes = new HashMap<>();
    LinkedHashMap<String, Map<String, String>> functions = new LinkedHashMap<>();
    ArrayList<String> properties;
    KBInfo sourceInfo, targetInfo;
    Configuration testConf;

    @Before
    public void init() {
        prefixes.put("geos", "http://www.opengis.net/ont/geosparql#");
        prefixes.put("lgdo", "http://linkedgeodata.org/ontology/");
        prefixes.put("geom", "http://geovocab.org/geometry#");
        prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        HashMap<String, String> f = new HashMap<>();
        f.put("polygon", null);
        functions.put("geom:geometry/geos:asWKT", f);
        properties = new ArrayList<String>(Arrays.asList("geom:geometry/geos:asWKT"));
        
        sourceInfo = new KBInfo(
                "linkedgeodata",                                                  //String id
                "http://linkedgeodata.org/sparql",                                //String endpoint
                null,                                                             //String graph
                "?x",                                                             //String var
                properties, //List<String> properties
                new ArrayList<String>(),                                          //List<String> optionalProperties
                new ArrayList<String>(Arrays.asList("?x a lgdo:RelayBox")),       //ArrayList<String> restrictions
                functions,                                                        //Map<String, Map<String, String>> functions
                prefixes,                                                         //Map<String, String> prefixes
                2000,                                                             //int pageSize
                "sparql",                                                         //String type
                -1,                                                               //int minOffset
                -1                                                                //int maxoffset
        );

        targetInfo = new KBInfo(
                "linkedgeodata",                                                  //String id
                "http://linkedgeodata.org/sparql",                                //String endpoint
                null,                                                             //String graph
                "?y",                                                             //String var
                properties, //List<String> properties
                new ArrayList<String>(),                                          //List<String> optionalProperties
                new ArrayList<String>(Arrays.asList("?y a lgdo:RelayBox")),       //ArrayList<String> restrictions
                functions,                                                        //Map<String, Map<String, String>> functions
                prefixes,                                                         //Map<String, String> prefixes
                2000,                                                             //int pageSize
                "sparql",                                                         //String type
                -1,                                                               //int minOffset
                -1                                                                //int maxoffset
        );
        
        testConf = new Configuration();
        testConf.setPrefixes(prefixes);
        testConf.setSourceInfo(sourceInfo);
        testConf.setTargetInfo(targetInfo);
        testConf.setAcceptanceRelation("lgdo:near");       
        testConf.setVerificationRelation("lgdo:near");
        testConf.setAcceptanceThreshold(0.9); 
        testConf.setAcceptanceFile("lgd_relaybox_verynear.nt");
        testConf.setVerificationThreshold(0.5);
        testConf.setVerificationFile("lgd_relaybox_near.nt");
        testConf.setOutputFormat("TAB");
    }
    
    
    @Test
    public void testXmlReaderForMetric() {
        testConf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");
        testConf.setExecutionRewriter("default");
        testConf.setExecutionPlanner("default");
        testConf.setExecutionEngine("default");

//        String file= System.getProperty("user.dir") + "/resources/lgd-lgd.xml";
        String file = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd.xml").getPath();
        XMLConfigurationReader c = new XMLConfigurationReader(file);
        Configuration fileConf = c.read();

        assertEquals(testConf, fileConf);
        assertTrue(testConf.equals(fileConf));
    }
    
    @Test
    public void testXmlReaderForOptionalProperties() {
        testConf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");
        testConf.setExecutionRewriter("default");
        testConf.setExecutionPlanner("default");
        testConf.setExecutionEngine("default");
        
        sourceInfo.setOptionalProperties(Arrays.asList("rdfs:label"));
        targetInfo.setOptionalProperties(Arrays.asList("rdfs:label"));

        String file= System.getProperty("user.dir") + "/resources/lgd-lgd-optional-properties.xml";
        XMLConfigurationReader c = new XMLConfigurationReader(file);
        Configuration fileConf = c.read();
        assertTrue(testConf.equals(fileConf));
    }
    
    @Test
    public void testXmlReaderMLAlgorithm() {

        List<LearningParameter> mlParameters = new ArrayList<>();
        LearningParameter lp = new LearningParameter();
        lp.setName("max execution time in minutes");
        lp.setValue(60);
        mlParameters.add(lp);

        testConf.setMlAlgorithmName("wombat simple");
        testConf.setMlImplementationType(MLImplementationType.SUPERVISED_BATCH);
        testConf.setTrainingDataFile("trainingData.nt");
        testConf.setMlAlgorithmParameters(mlParameters);

//        String file = System.getProperty("user.dir") +"/resources/lgd-lgd-ml.xml";
        String file = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd-ml.xml").getPath();
        XMLConfigurationReader c = new XMLConfigurationReader(file);
        Configuration fileConf = c.read();
        
        assertTrue(testConf.equals(fileConf));
    }
    
    @Test
    public void testProcessNaryFunctions() throws ParserConfigurationException, SAXException, IOException{
        String input = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd-concat.xml").getPath();
        DtdChecker dtdChecker = new DtdChecker();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // make sure document is valid
        factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(dtdChecker);

        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (systemId.contains("limes.dtd")) {
                    String dtd = getClass().getResource("/limes.dtd").toString();
                    return new InputSource(dtd);
                } else {
                    return null;
                }
            }
        });
        Document xmlDocument = builder.parse(input);
        NodeList list = xmlDocument.getElementsByTagName(XMLConfigurationReader.FUNCTIONS);
        NodeList children = list.item(0).getChildNodes();
        XMLConfigurationReader reader = new XMLConfigurationReader(input);
        reader.processNaryFunctions(XMLConfigurationReader.FUNCTIONS, children);

        HashMap<String, String> f3 = new HashMap<>();
        f3.put("latlong", "concat(geopos:lat, geopos:long, "+Concat.GLUE_FLAG+",)");
        LinkedHashMap<String, Map<String, String>> concatFunc = new LinkedHashMap<>();
        concatFunc.put("latlong", f3);
        assertEquals(concatFunc,reader.getConfiguration().getSourceInfo().getFunctions());
        assertEquals(concatFunc,reader.getConfiguration().getTargetInfo().getFunctions());
    }

    
    @Test
    public void testNAryFunctions() {
        testConf.setMetricExpression("geo_hausdorff(x.polygon, y.polygon)");
        testConf.setExecutionRewriter("default");
        testConf.setExecutionPlanner("default");
        testConf.setExecutionEngine("default");
    	prefixes.put("geopos","http://www.w3.org/2003/01/geo/wgs84_pos#");
    	properties.add("geopos:lat");
    	properties.add("geopos:long");
        HashMap<String, String> f3 = new HashMap<>();
        f3.put("latlong", "concat(geopos:lat, geopos:long, "+Concat.GLUE_FLAG+",)");
        functions.put("latlong", f3);

        testConf.setPrefixes(prefixes);
        sourceInfo.setProperties(properties);
        sourceInfo.setFunctions(functions);
        targetInfo.setProperties(properties);
        targetInfo.setFunctions(functions);
        testConf.setSourceInfo(sourceInfo);
        testConf.setTargetInfo(targetInfo);

        String file = Thread.currentThread().getContextClassLoader().getResource("lgd-lgd-concat.xml").getPath();
        XMLConfigurationReader c = new XMLConfigurationReader(file);
        Configuration fileConf = c.read();
        
        assertEquals(testConf, fileConf);
    }

}

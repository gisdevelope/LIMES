package org.aksw.limes.core.measures.measure.topology;

import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.measures.measure.AMeasure;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * Measure that checks for the topological relation covers.
 *
 * @author kdressler
 */
public class CoversMeasure extends AMeasure {
    @Override
    public double getSimilarity(Object object1, Object object2) {
        // expects WKT Strings
        String sWKT, tWKT;
        Geometry sGeo, tGeo;
        sWKT = object1.toString();
        tWKT = object2.toString();
        WKTReader reader = new WKTReader();
        try {
            sGeo = reader.read(sWKT);
            tGeo = reader.read(tWKT);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0d;
        }
        return sGeo.covers(tGeo) ? 1d : 0d;
    }

    @Override
    public double getSimilarity(Instance instance1, Instance instance2, String property1, String property2) {
        double value = 0;
        double sim = 0;
        for (String source : instance1.getProperty(property1)) {
            for (String target : instance2.getProperty(property2)) {
                sim = getSimilarity(source, target);
                if (sim > value) {
                    value = sim;
                }
            }
        }
        return sim;
    }

    @Override
    public double getRuntimeApproximation(double mappingSize) {
        return mappingSize / 1000d;
    }

    @Override
    public String getName() {
        return "top_covers";
    }

    @Override
    public String getType() {
        return "topology";
    }
}

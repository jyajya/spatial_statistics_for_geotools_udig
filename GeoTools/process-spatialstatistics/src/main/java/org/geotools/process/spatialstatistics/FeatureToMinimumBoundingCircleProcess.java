/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.process.spatialstatistics;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.transformation.MinimumBoundingCircleFeatureCollection;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * Creates a point features generated from the representative locations of input features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class FeatureToMinimumBoundingCircleProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging
            .getLogger(FeatureToMinimumBoundingCircleProcess.class);

    public FeatureToMinimumBoundingCircleProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            Boolean singlePart, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(FeatureToMinimumBoundingCircleProcessFactory.inputFeatures.key, inputFeatures);
        map.put(FeatureToMinimumBoundingCircleProcessFactory.singlePart.key, singlePart);

        Process process = new FeatureToMinimumBoundingCircleProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);

            return (SimpleFeatureCollection) resultMap
                    .get(FeatureToMinimumBoundingCircleProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                FeatureToMinimumBoundingCircleProcessFactory.inputFeatures, null);
        if (inputFeatures == null) {
            throw new NullPointerException("inputFeatures parameter required");
        }

        Boolean singlePart = (Boolean) Params.getValue(input,
                FeatureToMinimumBoundingCircleProcessFactory.singlePart,
                FeatureToMinimumBoundingCircleProcessFactory.singlePart.sample);

        // start process
        SimpleFeatureCollection resultFc = new MinimumBoundingCircleFeatureCollection(
                inputFeatures, singlePart);
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(FeatureToMinimumBoundingCircleProcessFactory.RESULT.key,
                DataUtilities.simple(resultFc));
        return resultMap;
    }
}

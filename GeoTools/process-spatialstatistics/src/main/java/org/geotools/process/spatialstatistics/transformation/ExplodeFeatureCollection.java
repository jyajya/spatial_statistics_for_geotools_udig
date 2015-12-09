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
package org.geotools.process.spatialstatistics.transformation;

import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Creates a singlepart features generated by separating multipart input features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class ExplodeFeatureCollection extends GXTSimpleFeatureCollection {
    protected static final Logger LOGGER = Logging.getLogger(ExplodeFeatureCollection.class);

    private SimpleFeatureType schema;

    public ExplodeFeatureCollection(SimpleFeatureCollection delegate) {
        super(delegate);

        this.schema = FeatureTypes.build(delegate, delegate.getSchema().getTypeName());
    }

    @Override
    public SimpleFeatureIterator features() {
        return new ExplodeFeatureIterator(delegate.features(), getSchema());
    }

    @Override
    public SimpleFeatureType getSchema() {
        return schema;
    }

    @Override
    public int size() {
        return DataUtilities.count(features());
    }

    static class ExplodeFeatureIterator implements SimpleFeatureIterator {
        private SimpleFeatureIterator delegate;

        private int index = 0;

        private int featureID = 0;

        private SimpleFeatureBuilder builder;

        private SimpleFeature nextFeature = null;

        private SimpleFeature origFeature = null;

        public ExplodeFeatureIterator(SimpleFeatureIterator delegate, SimpleFeatureType schema) {
            this.delegate = delegate;

            this.index = 0;
            this.builder = new SimpleFeatureBuilder(schema);
        }

        public void close() {
            delegate.close();
        }

        public boolean hasNext() {
            while ((nextFeature == null && delegate.hasNext())
                    || (nextFeature == null && !delegate.hasNext() && index > 0)) {
                if (index == 0) {
                    origFeature = delegate.next();
                }

                // create feature
                nextFeature = builder.buildFeature(Integer.toString(++featureID));
                transferAttribute(origFeature, nextFeature);

                Geometry multiPart = (Geometry) origFeature.getDefaultGeometry();
                nextFeature.setDefaultGeometry(multiPart.getGeometryN(index));

                builder.reset();
                index++;

                if (index >= multiPart.getNumGeometries()) {
                    index = 0;
                    origFeature = null;
                }
            }
            return nextFeature != null;
        }

        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("hasNext() returned false!");
            }
            SimpleFeature result = nextFeature;
            nextFeature = null;
            return result;
        }
    }
}

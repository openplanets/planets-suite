/*******************************************************************************
 * Copyright (c) 2006-2010 Vienna University of Technology, 
 * Department of Software Technology and Interactive Systems
 *
 * All rights reserved. This program and the accompanying
 * materials are made available under the terms of the
 * Apache License, Version 2.0 which accompanies
 * this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0 
 *******************************************************************************/
package eu.planets_project.pp.plato.evaluation;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.digester.Digester;

import eu.planets_project.pp.plato.model.measurement.MeasurableProperty;
import eu.planets_project.pp.plato.model.measurement.Metric;
import eu.planets_project.pp.plato.model.scales.BooleanScale;
import eu.planets_project.pp.plato.model.scales.FloatRangeScale;
import eu.planets_project.pp.plato.model.scales.FloatScale;
import eu.planets_project.pp.plato.model.scales.FreeStringScale;
import eu.planets_project.pp.plato.model.scales.IntRangeScale;
import eu.planets_project.pp.plato.model.scales.IntegerScale;
import eu.planets_project.pp.plato.model.scales.OrdinalScale;
import eu.planets_project.pp.plato.model.scales.PositiveFloatScale;
import eu.planets_project.pp.plato.model.scales.PositiveIntegerScale;
import eu.planets_project.pp.plato.model.scales.YanScale;
import eu.planets_project.pp.plato.util.PlatoLogger;

public class MeasurementsDescriptorParser {
    
    /**
     * a list of all known measurable properties, accessible by their propertyId
     * used by the digester
     */
    private Map<String, MeasurableProperty> propertyInfo;
    
    /**
     * a list of all known metrics, accessible by their metricId
     * used by the digester
     */
    private Map<String, Metric> metricInfo;
    
    
    
    public void load(InputStream in, Map<String, MeasurableProperty> propertyInfo, Map<String, Metric> metricInfo) {
        this.propertyInfo = propertyInfo;
        this.metricInfo = metricInfo;
        
        Digester d = setupDigester();
        try {
            d.parse(in);
            resolveMetrics();
        } catch (Exception e) {
            PlatoLogger.getLogger(getClass()).error("could not parse measurement infos", e);
        }
    }
    
    public void load(Reader in, Map<String, MeasurableProperty> propertyInfo, Map<String, Metric> metricInfo) {
        this.propertyInfo = propertyInfo;
        this.metricInfo = metricInfo;

        Digester d = setupDigester();
        try {
            d.parse(in);
            resolveMetrics();
        } catch (Exception e) {
            PlatoLogger.getLogger(getClass()).error("could not parse measurement infos", e);
        }
    }
    
    private void resolveMetrics() {
        // resolve references to metrics:
        for(MeasurableProperty p : propertyInfo.values()) {
            for (Metric m : p.getPossibleMetrics()) {
                Metric metric = metricInfo.get(m.getMetricId());
                if (metric != null) {
                    m.assign(metric);
                }
            }
        }
    }
    private Digester setupDigester() {
        Digester d = new Digester();
        d.push(this);
        
/*      <measurableProperties>
                <property>
                        <propertyId>object://image/dimension/width</propertyId>
                        <name>image width</name>
                        <description>the width of an image in pixel</description>
                        <scale type="positiveInteger">
                            <unit>pixel</unit>
                        </scale>
                        <possibleMetrics>
                            <metric metricId="equal"/>
                            <metric metricId="indDiff"/>
                        </possibleMetrics> 
                </property>
 */
        d.addObjectCreate("*/property", MeasurableProperty.class);
        d.addSetNext("*/property", "addProperty");
        d.addBeanPropertySetter("*/property/category",  "categoryAsString");
        d.addBeanPropertySetter("*/property/propertyId");
        d.addBeanPropertySetter("*/property/name");
        d.addBeanPropertySetter("*/property/description");
        d.addObjectCreate("*/property/possibleMetrics", ArrayList.class);
        d.addSetNext("*/property/possibleMetrics", "setPossibleMetrics");
//        d.addObjectCreate("*/property/possibleMetrics/metric", Metric.class);
        d.addObjectCreate("*/possibleMetrics/metric", Metric.class);
        d.addSetProperties("*/possibleMetrics/metric");
        d.addSetNext("*/possibleMetrics/metric", "add");
        
/*              <metric>
                        <name>equal</name>
                        <description></description>
                        <scale type="boolean" />
                </metric>
 */
        d.addObjectCreate("*/metrics/metric", Metric.class);
        d.addSetProperties("*/metrics/metric");
        d.addBeanPropertySetter("*/metrics/metric/metricId");
        d.addBeanPropertySetter("*/metrics/metric/name");
        d.addBeanPropertySetter("*/metrics/metric/description");
        d.addSetNext("*/metrics/metric", "addMetric");

        addCreateScale(d, BooleanScale.class);
        addCreateScale(d, FloatRangeScale.class);
        addCreateScale(d, FloatScale.class);
        addCreateScale(d, IntegerScale.class);
        addCreateScale(d, IntRangeScale.class);
        addCreateScale(d, OrdinalScale.class);
        addCreateScale(d, PositiveFloatScale.class);
        addCreateScale(d, PositiveIntegerScale.class);
        addCreateScale(d, YanScale.class);
        addCreateScale(d, FreeStringScale.class);
        return d;
    }
    
    /**
     * used by digester
     * 
     * @param p
     */
    public void addProperty(MeasurableProperty p) {
        propertyInfo.put(p.getPropertyId(), p);
    }
    
    /**
     * used by digester
     * @param m
     */
    public void addMetric(Metric m) {
        metricInfo.put(m.getMetricId(), m);
    }
    
    private static void addCreateScale(Digester digester, Class c) {
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        String pattern = "*/" + name;
        digester.addObjectCreate(pattern, c);
        digester.addSetProperties(pattern);
        digester.addBeanPropertySetter(pattern+"/unit");
        digester.addSetNext(pattern, "setScale");
    }    
}

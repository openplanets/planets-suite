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
package at.tuwien.minimee.migration.engines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.tuwien.minimee.model.ToolConfig;
import eu.planets_project.pp.plato.model.measurement.MeasurableProperty;
import eu.planets_project.pp.plato.model.measurement.Measurement;
import eu.planets_project.pp.plato.model.values.PositiveFloatValue;
import eu.planets_project.pp.plato.services.action.MigrationResult;
/**
 * This engine uses psList to monitor migration processes
 * on WINDOWS environments. However, it is currently NOT working properly 
 * and should be fixed!
 * @author gottardi
 *
 */
    public class MonitorEngineWinPslist extends MiniMeeDefaultMigrationEngine {
        private Log log = LogFactory.getLog(this.getClass());
        
        private String monitorScript = "pslistMonitor.bat";
        private String incrementScript = "increment.bat";
        private String logFile = "topWin.log";
        
        @Override
        protected void cleanup(long time, String inputFile, String outputFile) {
            super.cleanup(time, inputFile, outputFile);
            String workingDir = makeWorkingDirName(time);
            new File(workingDir + "/" + monitorScript).delete();
            new File(workingDir + "/" + incrementScript).delete();
            new File(workingDir + "/" + logFile).delete();
        }
        
        
        protected String makeWorkingDirName(long time) {
            return getTempDir() + "profile_" + time;
        }
        
        protected String prepareWorkingDirectory (long time) throws Exception {
            
            // assemble the working directory from timestamp
            String workingDirectory = makeWorkingDirName(time);
            
            // create the working directory
            (new File(workingDirectory)).mkdir();
            
            //
            // copy script files
            //
            
            String from, to;
            
            //
            // copy script: monitorcall.sh
            //
            from = "data/minimee/monitoring/" + monitorScript; 
            to = workingDirectory + "/" + monitorScript;
            
            copyFile(from, to, workingDirectory);
            
            /**
             * Copy increment script
             */
            from = "data/minimee/monitoring/" + incrementScript; 
            to = workingDirectory + "/" + incrementScript;

            copyFile(from, to, workingDirectory);
            
            return workingDirectory;
        }
        
        @Override
        protected String prepareCommand(ToolConfig config, String params, 
                String inputFile, String outputFile, long time) throws Exception {
                    
            prepareWorkingDirectory(time);

            // we calculate the timeout for the migration process
            File file = new File(inputFile);        
            // we calculate the cycle (winTop has a counter that needs 6 units: we set 150000 as max)
//            Long timeout = Math.max((file.length() / (1000000))*6, 150000);
            
            //so small time only for testing
            Long timeout = Math.max((file.length() / (1000000))*6, 100000);
            String cycles=timeout.intValue()+"";
            
            String monitoringCmd = prepareMonitoringCommand(time, cycles);
            
            String command = monitoringCmd + " " + config.getTool().getExecutablePath() + " \"" + config.getParams() + " "+ inputFile;
            
            // SPECIAL STUFF, UNLIKELY TO REMAIN HERE:
            if (!config.isNoOutFile()) {
                command = command + " " + outputFile;
            }
            
            command += "\"";
            
            log.debug("TOP WINDOWS MONITORING COMMAND: "+command);
            return command;
        }

        protected String prepareMonitoringCommand(long time, String cycles) {
            return makeWorkingDirName(time) + "/" + monitorScript 
                + " " + makeWorkingDirName(time) + "/"
                + " " + cycles
                + " " + makeWorkingDirName(time) + "/" + logFile
                + " " + makeWorkingDirName(time) + "/" + incrementScript;
        }
        
        /**
         * Copies resource file 'from' from destination 'to' and set execution permission.
         * 
         * @param from
         * @param to
         * @throws Exception
         */
        protected void copyFile(String from, String to, String workingDirectory) throws Exception {
            
            //
            // copy the shell script to the working directory
            //
            URL monitorCallShellScriptUrl = Thread.currentThread().getContextClassLoader().getResource(from);
            File f = new File(monitorCallShellScriptUrl.getFile());
            String directoryPath = f.getAbsolutePath();
            
            /*
            URL urlJar = new URL(directoryPath.substring(
                    directoryPath.indexOf("file:"),
                    directoryPath.indexOf("plato.jar")+"plato.jar".length()));
            
            
            JarFile jf = new JarFile(urlJar.getFile());
            
            JarEntry je = jf.getJarEntry(from);
            
            String fileName = je.getName();
            */
            InputStream in = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(from);
                    
            File outScriptFile = new File(to);
            
            FileOutputStream fos = new FileOutputStream(outScriptFile);
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                fos.write(nextChar);
            }
            fos.flush();
            fos.close();      
        }
        
        protected void collectData(ToolConfig config, long time, MigrationResult result) {
//            TopWinParser p = new TopWinParser(makeWorkingDirName(time) + logFile);
////            p.parse();
//            
//            ExecutionFootprintList performance = p.getList();
//            //log.debug(performance.toString());
            
            for (MeasurableProperty property: getMeasurableProperties()) {
                Measurement m = new Measurement();
                m.setProperty(property);
                PositiveFloatValue v = (PositiveFloatValue) property.getScale().createValue();

                if (property.getName().equals("performance:memory:used")) {
                    v.setValue(123.12);
                } 
//                if (property.getName().equals(MigrationResult.MIGRES_USED_TIME)) {
//                    v.setValue(performance.getTotalCpuTimeUsed());
//                }
//                if (property.getName().equals(MigrationResult.MIGRES_MEMORY_GROSS)) {
//                    v.setValue(performance.getMaxVirtualMemory());
//                }
//                if (property.getName().equals(MigrationResult.MIGRES_MEMORY_NET)) {
//                    v.setValue(performance.getMaxResidentSize());
//                }
         
                m.setValue(v);
                result.getMeasurements().put(property.getName(), m);
            }
            
        }

    }

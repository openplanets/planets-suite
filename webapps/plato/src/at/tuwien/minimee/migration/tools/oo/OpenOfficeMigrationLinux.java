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
package at.tuwien.minimee.migration.tools.oo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;

import eu.planets_project.pp.plato.util.OS;

import at.tuwien.minimee.util.ExecutionFootprintList;
import at.tuwien.minimee.util.LinuxCommandExecutor;
import at.tuwien.minimee.util.LinuxCommandMonitor;

public class OpenOfficeMigrationLinux {
    
    private String workingDirectory;
    
    private static String tempDir = OS.getTmpPath();
    
    private int[] openOfficeServerPorts = { 8100, 8101, 8102 };
    
    /**
     * The port the OpenOffice.org server has been started.
     */
    private int openOfficeServerPort = -1;
    
    public void migrate(String from, String to) {
        
        try {
            init();
            
            connectOpenOfficeServer();
            
            migrationHelper(from, to);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void connectOpenOfficeServer() {
        for (int port : openOfficeServerPorts) {
            LinuxCommandExecutor exec = new LinuxCommandExecutor();
            exec.setWorkingDirectory(workingDirectory);
            
            try {
                int exitValue = exec.runCommand(workingDirectory + "/startOpenOfficeServer.sh " + port);
                
                // return value "0" from the script 'startOpenOfficeServer.sh' indicates that the open office
                // server has been successfully started at port
                if (exitValue == 0) {
                    openOfficeServerPort = port;
                    
                    break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        if (openOfficeServerPort != -1) {
            System.out.println("The OpenOffice.org server has been successfully started at port " + openOfficeServerPort);
        } else {
            System.out.println("Error starting the OpenOffice.org server. The server tried to connect to the following ports: " + openOfficeServerPorts);
        }
    }
    
    /**
     * 
     * @throws Exception when error while copying
     */
    private void init() throws Exception {
        // assemble the working directory from timestamp
        workingDirectory = tempDir + "/profile_" + System.nanoTime();
        
        // create the working directory
        (new File(workingDirectory)).mkdir();
        
        //
        // copy script: startOpenOfficeServer.sh
        //
        String from = "data/scripts/startOpenOfficeServer.sh";
        String to = workingDirectory + "/startOpenOfficeServer.sh";

        copyFile(from, to);

        //
        // copy script: OpenOfficeDocumentConverter
        //
        from = "data/scripts/OpenOfficeDocumentConverter.py";
        to = workingDirectory + "/OpenOfficeDocumentConverter.py";

        copyFile(from, to);
        
        //
        // copy script: openOfficeConvertMonitor.sh
        //
        from = "data/scripts/openOfficeConvertMonitor.sh";
        to = workingDirectory + "/openOfficeConvertMonitor.sh";

        copyFile(from, to);

        //
        // copy script: openOfficeConvert.sh
        //
        from = "data/scripts/openOfficeConvert.sh";
        to = workingDirectory + "/openOfficeConvert.sh";

        copyFile(from, to);

        //
        // copy script: monitorcall.sh
        //
        from = "data/scripts/monitorcall.sh";
        to = workingDirectory + "/monitorcall.sh";

        copyFile(from, to);
        
    }

    /**
     * Copies resource file 'from' from destination 'to' and set execution permission.
     * 
     * @param from
     * @param to
     * @throws Exception
     */
    private void copyFile(String from, String to) throws Exception {
        
        //
        // copy the shell script to the working directory
        //
        URL monitorCallShellScriptUrl = Thread.currentThread().getContextClassLoader().getResource(from);
        
        File inScriptFile = null;
        
        try {
            inScriptFile = new File (monitorCallShellScriptUrl.toURI());
        } catch (URISyntaxException e) {
            throw e;
        }
        
        File outScriptFile = new File(to);                
        
        FileChannel inChannel = new FileInputStream(inScriptFile).getChannel();        
        FileChannel outChannel = new FileOutputStream(outScriptFile).getChannel();
        
        try {
            inChannel.transferTo(0, inChannel.size(),
                    outChannel);            
        } 
        catch (IOException e) {
            throw e;
        }
        finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
        
        //
        // This seems kind of hard core, but we have to set execution rights for the shell script, 
        // otherwise we wouldn't be allowed to execute it.
        // The Java-way with FilePermission didn't work for some reason.
        //
        try {
            LinuxCommandExecutor cmdExecutor = new LinuxCommandExecutor();
            cmdExecutor.setWorkingDirectory(workingDirectory);
            
            cmdExecutor.runCommand("chmod 777 " + to);
        } catch(Exception e) {
            throw e;
        }                
    }
    
    private void migrationHelper(String from, String to) {

        try {
//            LinuxCommandExecutor exec = new LinuxCommandExecutor();
//            exec.setWorkingDirectory(workingDirectory);
//            
//            String commandLine = "openOfficeConvert.sh " + from + " " + to + " " + openOfficeServerPort;
//            
//            exec.runCommand(commandLine);
            
            LinuxCommandMonitor execMonitor = new LinuxCommandMonitor(true);
            execMonitor.setWorkingDirectory(workingDirectory);
            
            String commandLine = workingDirectory + "/openOfficeConvertMonitor.sh " + execMonitor.getWorkingDirectory() + "  /home/kulovits/dev/inputFile1.doc /home/kulovits/dev/outputFile2.pdf " + openOfficeServerPort;
            
            execMonitor.monitor(commandLine);

            ExecutionFootprintList taskPerformance = execMonitor.getTaskPerformance();
      
            System.out.println("Average virtual memory usage: " + taskPerformance.getAverageVirtualMemory() + " kb");
            System.out.println("Average shared memory usage: " + taskPerformance.getAverageSharedMemory() + " kb");
            System.out.println("Average resident size: " + taskPerformance.getAverageResidentSize() + " kb");
      
            double cpuTimeUsed = taskPerformance.getTotalCpuTimeUsed();
      
            long seconds = (long)(cpuTimeUsed/1000);
            double ms = (cpuTimeUsed - (cpuTimeUsed/1000)*1000);
            System.out.println("Total CPU time (seconds/miliseconds): " + seconds + ":" + ms);
            
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public int getOpenOfficeServerPort() {
        return openOfficeServerPort;
    }

}

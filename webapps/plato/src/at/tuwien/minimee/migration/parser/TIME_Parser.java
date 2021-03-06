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
package at.tuwien.minimee.migration.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Parses the output file of the linux 'time' command (/usr/bin/time)
 * 
 * @author kulovits
 */
public class TIME_Parser {
    private Log log = LogFactory.getLog(this.getClass());
    
    /**
     * Percentage of the CPU that this job got.  This is just user + system times  divided  by  the
     * total running time. It also prints a percentage sign.
     */
    private double pCpu;
    /**
     * Total number of CPU-seconds used by the system on behalf of the process 
     * (in kernel mode), in seconds.
     */
    private double sys;
    /**
     * Total number of CPU-seconds that the process used directly (in user mode), in seconds.
     */
    private double user;
    /**
     * Elapsed real (wall clock) time used by the process, in seconds.
     */
    private double real;
    
    /**
     *  Exit status of the command.
     */
    private int exitCode;

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        TIME_Parser p = new TIME_Parser();
        p.parse("/home/kulovits/time-out.txt");

    }

    public void parse(String fileName) {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(fileName);
            try {
                BufferedReader input = new BufferedReader(fileReader);
            
                // the output file shall only have one line
                String line;
                while ((line = input.readLine())!=null) {
                    parseLine(line);
                }
            } finally {
                fileReader.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
    }
    
    /**
     * The line is supposed to look like that: 
     * pCpu:0%,sys:0.00,user:0.00,real:1.00,exit:0
     * @param line
     */
    private void parseLine(String line) {
        
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            
            int colon = token.indexOf(':');
            if (colon == -1) {
                continue;
            }
            
            if ("pCpu".compareTo(token.substring(0, colon)) == 0) {
                
                int pSign = token.indexOf('%');
                if (pSign == -1) {
                    continue;
                }
                
                pCpu = (new Double(token.substring(colon+1, pSign)));
            } else if ("sys".compareTo(token.substring(0, colon)) == 0) {
                
                sys = (new Double(token.substring(colon+1)));
                
            } else if ("user".compareTo(token.substring(0, colon)) == 0) {
                
                user = (new Double(token.substring(colon+1)));
                
            } else if ("real".compareTo(token.substring(0, colon)) == 0) {
                
                real = (new Double(token.substring(colon+1)));
                
            } else if ("exit".compareTo(token.substring(0, colon)) == 0) {
                
                exitCode = (new Integer(token.substring(colon+1)));
            }
        }
    }

    public int getExitCode() {
        return exitCode;
    }

    public double getPCpu() {
        return pCpu;
    }

    public double getReal() {
        return real;
    }

    public double getSys() {
        return sys;
    }

    public double getUser() {
        return user;
    }
    
}

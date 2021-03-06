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
package at.tuwien.minimee.migration.runners;

public class RunInfo {
    private boolean success;
    private String report;
    private long elapsedTimeMS;
    
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getReport() {
        return report;
    }
    public void setReport(String report) {
        this.report = report;
    }
    public long getElapsedTimeMS() {
        return elapsedTimeMS;
    }
    public void setElapsedTimeMS(long elapsedTimeMS) {
        this.elapsedTimeMS = elapsedTimeMS;
    }
}

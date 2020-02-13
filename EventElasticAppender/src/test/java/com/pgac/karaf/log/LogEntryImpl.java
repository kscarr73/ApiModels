package com.pgac.karaf.log;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

/**
 *
 * @author scarr
 */
public class LogEntryImpl implements LogEntry {
    int logLevel = LogService.LOG_INFO;
    String message = null;
    Throwable exception = null;
    Long time = System.currentTimeMillis();
    
    @Override
    public Bundle getBundle() {
        return null;
    }

    @Override
    public ServiceReference getServiceReference() {
        return null;
    }

    @Override
    public int getLevel() {
        return logLevel;
    }

    public void setLevel(int level) {
        logLevel = level;
    }
    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable thr) {
        this.exception = thr;
    }
    @Override
    public long getTime() {
        return time;
    }
    
    public void setTime(Long time) {
        this.time = time;
    }
}

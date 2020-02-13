package com.pgac.karaf.log.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper functions to generate Log Messages for the EventElastic
 *
 * @author scarr
 */
public class EventElasticUtil {

    public enum METRIC_STATUS { SUCCESS, FAILURE }
    public enum AUDIT_TYPES { JSON, XML, CSV, PLAIN }
    
    /**
     *
     * @param message
     * @param fields
     * @return
     */
    public static String createAuditMessage(String message, AUDIT_TYPES type, Map<String, String> fields) {
        StringBuilder sbAudit = new StringBuilder();

        sbAudit.append("[Audit]: ");
        
        Map<String, String> headerFields = new HashMap<>();
        
        headerFields.put("Type", type.name());

        if (fields != null) {
            headerFields.putAll(fields);
        }

        sbAudit.append(headerFields.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(",")));

        sbAudit.append("\n");

        sbAudit.append(message);

        return sbAudit.toString();
    }

    /**
     *
     * @param message
     * @param lStart 
     * @param lStop
     * @param fields
     * @return
     */
    public static String createMetricMessage(String message, Long lStart, Long lStop, METRIC_STATUS status, Map<String, String> fields) {
        StringBuilder sbMetric = new StringBuilder();

        sbMetric.append("[Metric]: ");
        Map<String, String> headerFields = new HashMap<>();

        headerFields.put("Start", Long.toString(lStart));
        headerFields.put("Stop", Long.toString(lStop));
        
        headerFields.put("Status", status.name());
        
        if (lStart != null && lStop != null) {
            headerFields.put("DurationMillis", Long.toString(lStop - lStart));
        }
        
        if (fields != null) {
            headerFields.putAll(fields);
        }

        sbMetric.append(headerFields.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(",")));

        sbMetric.append("\n");

        sbMetric.append(message);

        return sbMetric.toString();
    }
    
    /**
     *
     * @param message
     * @param fields
     * @return
     */
    public static String createLogMessage(String message, Map<String, String> fields) {
        StringBuilder sbLog = new StringBuilder();

        sbLog.append("[Log]: ");
        
        Map<String, String> headerFields = new HashMap<>();

        if (fields != null) {
            headerFields.putAll(fields);
        }

        sbLog.append(headerFields.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(",")));

        sbLog.append("\n");

        sbLog.append(message);

        return sbLog.toString();
    }
    
}

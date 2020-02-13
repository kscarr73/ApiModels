package com.pgac.karaf.log;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
@Component(name = "EventElasticAppender",
        property = {"name=EventElasticAppender"},
        immediate = true,
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class EventElasticAppender implements LogListener {

    final static public int SIZE_LOG = 7;
    final static public int SIZE_AUDIT = 9;
    final static public int SIZE_METRIC = 10;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private CpuTimes previousCpu = null;
    
    int _logCount = 0;
    StringBuilder _logBuff = new StringBuilder(20000);

    private String _host = null;
    private String _elasticUrl = null;
    private String _elasticPrefix = "ds_";
    private Integer _flushCount = 100;
    private Timer _tmr = null;

    private DateTimeFormatter _format = null;
    private DateTimeFormatter _indexFormat = null;
    private ConcurrentLinkedQueue<String> _logSend = new ConcurrentLinkedQueue<>();

    private HttpClient httpClient = null;

    private LogReaderService logReader = null;
    private JavaSysMon monitor = new JavaSysMon();

    /**
     * Main Fields available for ALL message types
     */
    public enum topLevelFields {
        splintName, message, className, threadName,
        host, timestamp, fields, field, Start, Stop, logLevel,
        stackTrace, DurationMillis,
        Type, Status
    }

    /**
     * Fields that can Override other top level fields in the Headers section
     */
    public enum topLevelOverrides {
        splintName, className, threadName, Type
    }

    @Reference
    public void setReader(LogReaderService logReader) {
        this.logReader = logReader;

        this.logReader.addLogListener(this);
    }

    public String getBuffer() {
        return _logBuff.toString();
    }

    public void setUrl(String url) {
        if (!url.endsWith("/")) {
            _elasticUrl = url + "/";
        } else {
            _elasticUrl = url;
        }
    }

    public void setPrefix(String index) {
        _elasticPrefix = index;
    }

    public void setFlushCount(Integer iFlush) {
        _flushCount = iFlush;
    }

    @Activate
    public void setup(Map<String, String> cfg) {
        setupInternal();

        updated(cfg);

        _tmr = new Timer("EventElasticAppender", true);

        // Auto Flush every 30 seconds
        _tmr.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                appendBuffer(null, true);
            }
        }, 60000, 30000);

        // Send Log Entries every 10 seconds
        _tmr.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendLog();
            }
        }, 10000, 10000);
    }

    private void setupInternal() {
        try {
            _host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            _host = "unknown";
        }

        _format = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        _indexFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        SslContextFactory sslContextFactory = new SslContextFactory();
        httpClient = new HttpClient(sslContextFactory);
        //httpClient.setAuthenticationStore(new HttpAuthenticationStore());

        try {
            httpClient.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setupNoTimer(Map<String, String> cfg) {
        setupInternal();
        updated(cfg);
    }

    @Deactivate
    public void destroy() {
        _tmr.cancel();
        try {
            httpClient.stop();
        } catch (Exception ex) {

        }
    }

    @Modified
    public void updated(Map<String, String> cfg) {
        if (cfg.containsKey("elasticUrl")) {
            _elasticUrl = cfg.get("elasticUrl");

            if (!_elasticUrl.endsWith("/")) {
                _elasticUrl += "/";
            }
        }

        if (cfg.containsKey("elasticUser")) {
            AuthenticationStore store = httpClient.getAuthenticationStore();
            try {
                store.addAuthenticationResult(new BasicAuthentication.BasicResult(new URI(_elasticUrl), cfg.get("elasticUser"), cfg.get("elasticPassword")));
            } catch (URISyntaxException uri) {
                LOG.error("Settings Error", uri);
            }
        }

        if (cfg.containsKey("prefix")) {
            _elasticPrefix = cfg.get("prefix");
        }
    }

    public void logCpuAndMemory() {
        OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        _logBuff.append("{ \"index\": { ");
        setField(_logBuff, "_index", true, _elasticPrefix + "system-" + _indexFormat.format(LocalDate.now()));
        setField(_logBuff, "_type", false, "_doc");
        _logBuff.append(" } }\n");

        _logBuff.append("{ ");

        setField(_logBuff, "host", true, _host);
        setField(_logBuff, "timestamp", true, _format.format(ZonedDateTime.now()));
        
        setField(_logBuff, "cpu", true, system.getSystemLoadAverage());
        setField(_logBuff, "memory", true, memory.getHeapMemoryUsage().getUsed());

        if (previousCpu == null) {
            previousCpu = monitor.cpuTimes();
        } else {
            setField(_logBuff, "systemCpu", true, monitor.cpuTimes().getCpuUsage(previousCpu));
        }
        
        setField(_logBuff, "systemMemory", false, monitor.physicalWithBuffersAndCached().getTotalBytes() - monitor.physicalWithBuffersAndCached().getFreeBytes());
        
        _logBuff.append(" }\n");

        _logCount++;
    }

    public synchronized void appendBuffer(LogEntry le, boolean forceFlush) {
        if (forceFlush) {
            logCpuAndMemory();

            flushBuffer();
        } else if (_logCount >= _flushCount) {
            flushBuffer();
        }

        if (le != null) {
            convertLogEvent(le);
        }
    }

    @Override
    public void logged(LogEntry le) {
        appendBuffer(le, false);
    }

    private void convertLogEvent(LogEntry le) {
        _logBuff.append("{ \"index\": { ");

        int indexType;
        String logIndex = _elasticPrefix;
        String logMessage = le.getMessage();

        if (logMessage.startsWith("[Metric]: ")) {
            indexType = 2;
            logIndex += "metric";
        } else if (logMessage.startsWith("[Audit]: ")) {
            indexType = 3;
            logIndex += "audit";
        } else if (logMessage.startsWith("[Log]: ")) {
            indexType = 1;
            logIndex += "logs";
        } else {
            indexType = 0;
            logIndex += "karaf";
        }

        setField(_logBuff, "_index", true, logIndex + "-" + _indexFormat.format(LocalDate.now()));

        String logType;

        switch (le.getLevel()) {
            case LogService.LOG_DEBUG:
                logType = "DEBUG";
                break;
            case LogService.LOG_ERROR:
                logType = "ERROR";
                break;
            case LogService.LOG_WARNING:
                logType = "WARNING";
                break;
            case LogService.LOG_INFO:
                logType = "INFO";
                break;
            default:
                logType = "INFO";
                break;
        }

        setField(_logBuff, "_type", false, "_doc");
        _logBuff.append(" } }\n");

        switch (indexType) {
            case 0: // Karaf Log
                convertDefaultLogMessage(logType, le);
                break;

            case 1: // Parsed Log
                convertLogMessage(logType, indexType, le);
                break;

            case 2: // Metric
                convertMetricMessage(logType, le);
                break;

            case 3: // Audit
                convertLogMessage(logType, indexType, le);
                break;
        }

        _logCount++;
    }

    public void convertDefaultLogMessage(String logType, LogEntry le) {
        StringBuilder lclBuff = new StringBuilder();

        try {
            lclBuff.append("{ ");

            setField(lclBuff, "host", true, _host);

            OffsetDateTime localTime = Instant.ofEpochMilli(le.getTime()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            setField(lclBuff, "timestamp", true, _format.format(localTime));

            if (le.getBundle() != null) {
                setField(lclBuff, "threadName", true, le.getBundle().getSymbolicName());
                setField(lclBuff, "className", true, le.getBundle().getSymbolicName());
            }

            setField(lclBuff, "logLevel", true, logType);

            if (le.getException() != null) {
                setField(lclBuff, "stackTrace", true,
                        escapeJson(getMessageStack(le.getException())));
            }

            setField(lclBuff, "message", false, escapeJson(le.getMessage()));

            lclBuff.append(" }\n");

            _logBuff.append(lclBuff);
        } catch (Exception ex) {
            LOG.error("convertDefaultLogMessage Error", ex);
        }
    }

    /**
     * Pull first line of message as Header Fields, and the rest as either log
     * or audit.
     *
     * @param logType
     * @param le
     */
    public void convertLogMessage(String logType, int indexType, LogEntry le) {
        StringBuilder lclBuff = new StringBuilder();
        boolean iThreadName;
        boolean iClassName;

        try {
            lclBuff.append("{ ");

            setField(lclBuff, topLevelFields.host.name(), true, _host);

            Map<String, String> logFields;

            if (indexType == 3) {
                logFields = parsedMessage(SIZE_AUDIT, le.getMessage());
            } else {
                logFields = parsedMessage(SIZE_LOG, le.getMessage());
            }

            iThreadName = logFields.containsKey(topLevelFields.threadName.name());
            iClassName = logFields.containsKey(topLevelFields.className.name());

            // Write out the message from logFields so we capture message properly
            setField(lclBuff, topLevelFields.message.name(), true, escapeJson(logFields.get(topLevelFields.message.name())));
            logFields.remove(topLevelFields.message.name());

            writeFields(topLevelOverrides.values(), lclBuff, logFields);

            OffsetDateTime localTime = Instant.ofEpochMilli(le.getTime()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            setField(lclBuff, "timestamp", true, _format.format(localTime));

            if (le.getBundle() != null) {
                if (!iThreadName) {
                    setField(lclBuff, topLevelFields.threadName.name(), true, le.getBundle().getSymbolicName());
                }
                if (!iClassName) {
                    setField(lclBuff, topLevelFields.className.name(), true, le.getBundle().getSymbolicName());
                }
            }

            if (le.getException() != null) {
                setField(lclBuff, "stackTrace", true,
                        escapeJson(getMessageStack(le.getException())));
            }

            setField(lclBuff, "logLevel", false, logType);

            lclBuff.append(" }\n");

            _logBuff.append(lclBuff);
        } catch (Exception ex) {
            LOG.error("convertDefaultLogMessage Error", ex);
        }
    }

    public void convertMetricMessage(String logType, LogEntry le) {
        StringBuilder lclBuff = new StringBuilder();
        boolean iThreadName;
        boolean iClassName;

        try {
            lclBuff.append("{ ");

            setField(lclBuff, topLevelFields.host.name(), true, _host);

            Map<String, String> logFields = parsedMessage(SIZE_METRIC, le.getMessage());

            iThreadName = logFields.containsKey(topLevelFields.threadName.name());
            iClassName = logFields.containsKey(topLevelFields.className.name());

            // Use the Start Long value as the Start and timestamp and remove
            Long lStart = Long.parseLong(logFields.get(topLevelFields.Start.name()));
            OffsetDateTime localTime = Instant.ofEpochMilli(lStart).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            String formatedStart = _format.format(localTime);
            setField(lclBuff, topLevelFields.Start.name(), true, formatedStart);
            setField(lclBuff, "timestamp", true, formatedStart);
            logFields.remove(topLevelFields.Start.name());

            // Use the Stop long value formated date as Stop time and remove
            Long lStop = Long.parseLong(logFields.get(topLevelFields.Stop.name()));
            OffsetDateTime stopTime = Instant.ofEpochMilli(lStop).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            String formatedStop = _format.format(stopTime);
            setField(lclBuff, topLevelFields.Stop.name(), true, formatedStop);
            logFields.remove(topLevelFields.Stop.name());

            setField(lclBuff, topLevelFields.DurationMillis.name(), true,
                    Long.parseLong(logFields.get(topLevelFields.DurationMillis.name())));
            logFields.remove(topLevelFields.DurationMillis.name());

            setField(lclBuff, topLevelFields.Status.name(), true, escapeJson(logFields.get(topLevelFields.Status.name())));
            logFields.remove(topLevelFields.Status.name());

            // Write out the message from logFields so we capture message properly
            setField(lclBuff, topLevelFields.message.name(), true, escapeJson(logFields.get(topLevelFields.message.name())));
            logFields.remove(topLevelFields.message.name());

            writeFields(topLevelOverrides.values(), lclBuff, logFields);

            if (le.getBundle() != null) {
                if (!iThreadName) {
                    setField(lclBuff, topLevelFields.threadName.name(), true, le.getBundle().getSymbolicName());
                }
                if (!iClassName) {
                    setField(lclBuff, topLevelFields.className.name(), true, le.getBundle().getSymbolicName());
                }
            }

            if (le.getException() != null) {
                setField(lclBuff, "stackTrace", true,
                        escapeJson(getMessageStack(le.getException())));
            }

            setField(lclBuff, "logLevel", false, logType);

            lclBuff.append(" }\n");

            _logBuff.append(lclBuff);
        } catch (Exception ex) {
            LOG.error("convertDefaultLogMessage Error", ex);
        }
    }

    /**
     * Parse a Log Message with the first line with field headers, and the rest
     * the message itself
     *
     * @param logType
     * @param le
     */
    public void parsedLogMessage(String logType, LogEntry le) {
        StringBuilder lclBuff = new StringBuilder();

        try {
            lclBuff.append("{ ");

            Map<String, String> parseFields = parsedMessage(SIZE_LOG, le.getMessage());

            setField(lclBuff, "host", true, _host);

            OffsetDateTime localTime = Instant.ofEpochMilli(le.getTime()).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            setField(lclBuff, "timestamp", true, _format.format(localTime));

            if (le.getBundle() != null) {
                setField(lclBuff, topLevelFields.threadName.name(), true, le.getBundle().getSymbolicName());
                setField(lclBuff, topLevelFields.className.name(), true, le.getBundle().getSymbolicName());
            }

            setField(lclBuff, "logLevel", true, logType);

            if (le.getException() != null) {
                setField(lclBuff, "stackTrace", true,
                        escapeJson(getMessageStack(le.getException())));
            }

            lclBuff.append(" }\n");
            _logBuff.append(lclBuff);
        } catch (Exception ex) {
            LOG.error("parsedLogMessage Error", ex);
        }
    }

    public void writeFields(topLevelOverrides[] topLevel, StringBuilder buff, Map<String, String> fields) {
        for (topLevelOverrides fieldName : topLevel) {
            if (fields.containsKey(fieldName.name())) {
                setField(buff, fieldName.name(), true, escapeJson(fields.get(fieldName.name())));
                fields.remove(fieldName.name());
            }
        }

        if (fields.size() > 0) {
            buff.append(" \"fields\": { \"field\": [ ");

            buff.append(
                    fields.entrySet().stream()
                            .map(e -> String.format(
                            " { \"name\": \"%s\", \"value\": \"%s\" } ",
                            e.getKey(), e.getValue()))
                            .collect(Collectors.joining(","))
            );

            buff.append(" ] }, ");
        }
    }

    public Map<String, String> parsedMessage(int colStart, String message) {
        Map<String, String> fields = new HashMap<>();

        try {
            List<String> lines = new LinkedList<>(Arrays.asList(message.substring(colStart).split("\\n")));

            String firstLine = lines.remove(0);

            List<String> fieldLines = Arrays.asList(firstLine.split(","));

            fieldLines.forEach((field) -> {
                String[] nameValue = field.split(":");

                if (nameValue.length > 1) {
                    fields.put(nameValue[0].trim(), nameValue[1].trim());
                }
            });

            fields.put("message", lines.stream().collect(Collectors.joining("\n")));
        } catch (Exception ex) {
            LOG.error("parsedMessage", ex);
        }

        return fields;
    }

    public void sendLog() {
        try {
            String strLog;

            do {
                strLog = _logSend.poll();

                if (strLog != null && !strLog.isEmpty()) {

                    ContentResponse resp = httpClient.POST(_elasticUrl + "_bulk")
                            .header("Content-Type", "application/json")
                            .content(new StringContentProvider(strLog))
                            .send();

                    if (resp.getStatus() >= 200 && resp.getStatus() <= 299) {
                        // SUCCESS
                    } else {
                        throw new Exception(resp.getContentAsString());
                    }
                }
            } while (strLog != null);

        } catch (Exception ex) {
            LOG.error("sendLog Error", ex);
        }
    }

    /**
     * Write a line to a StringBuffer for JSON compliance.
     *
     * @param sb StringBuffer to Write To
     * @param fieldName Field Name to use for the json entry
     * @param anotherField TRUE/FALSE should we write a comma for next field
     * @param data Data to be written
     */
    private void setField(StringBuilder sb, String fieldName, boolean anotherField, Object data) {
        sb.append(" \"");
        sb.append(fieldName);
        sb.append("\": ");

        if (data instanceof String) {
            sb.append("\"").append(data).append("\"");
        } else if (data instanceof Number) {
            sb.append(String.valueOf(data));
        }

        if (anotherField) {
            sb.append(", ");
        }
    }

    private String escapeJson(String in) {
        if (in != null) {
            String sRet = in;

            return sRet.replace("\\", "\\\\")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\"", "\\\"")
                    .replace("/", "\\/")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\t", "\\t");
        } else {
            return "";
        }
    }

    public void flushBuffer() {
        if (_logCount > 0) {
            try {
                _logSend.offer(_logBuff.toString());
            } catch (Exception e) {
                // Catch EVERYTHING Because we want to do this set no matter what.
            }
            _logCount = 0;
            _logBuff = new StringBuilder(20000);
        }
    }

    public void close() {
        appendBuffer(null, true);
    }

    public boolean requiresLayout() {
        return false;
    }

    public String getMessageStack(Throwable currEx) {
        if (currEx != null) {
            StringWriter sw = new StringWriter();

            PrintWriter pw = new PrintWriter(sw);

            currEx.printStackTrace(pw);

            return sw.toString();
        } else {
            return "";
        }
    }
}

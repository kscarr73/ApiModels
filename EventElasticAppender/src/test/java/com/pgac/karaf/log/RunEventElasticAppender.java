package com.pgac.karaf.log;

import com.pgac.karaf.log.utils.EventElasticUtil;
import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class RunEventElasticAppender {
    EventElasticAppender appender;
    Long lStart = System.currentTimeMillis();
    
    @BeforeClass
    public void setup() {
        appender = new EventElasticAppender();
        
        Map<String, String> settings = new HashMap<>();
        
        settings.put("elasticUrl", "https://5c26d5f07c614203bae1e70256ed2374.us-east-1.aws.found.io:9243/");
        settings.put("elasticUser", "elastic");
        settings.put("elasticPassword", "yalxmzkqNNDwj5RS88a7g8e4");
        
        appender.setupNoTimer(settings);
    }
    
    @Test
    public void testDefaultLog() {
        LogEntryImpl log = new LogEntryImpl();
        
        log.setMessage("We are running in karaf");
        
        appender.logged(log);
        
        log.setMessage("We are running in karaf with StackTrace");
        log.setException(new Exception("This is a test"));
        
        appender.logged(log);
    }
    
    @Test
    public void testParsedMessage() {
        Map<String, String> fields = new HashMap<>();
        
        fields.put("splintName", "ScottTest");
        fields.put("name", "Test");
        
        String strMessage = EventElasticUtil.createLogMessage("This is a test\nMessage To See how this works", fields);
        
        Map<String, String> logFields = appender.parsedMessage(
                EventElasticAppender.SIZE_LOG, strMessage);
        
        assert logFields.get("message") != null;
    }
    
    @Test
    public void testLogMessage() {
        Map<String, String> fields = new HashMap<>();
        
        fields.put("splintName", "ScottTest");
        fields.put("name", "Test");
        
        LogEntryImpl log = new LogEntryImpl();
        
        log.setMessage(EventElasticUtil.createLogMessage("This is a test log message", fields));
        
        appender.logged(log);
    }
    
    @Test
    public void testMetricMessage() {
        Map<String, String> fields = new HashMap<>();
        
        fields.put("splintName", "ScottTest");
        fields.put("name", "Test");
        
        LogEntryImpl log = new LogEntryImpl();
        
        log.setMessage(EventElasticUtil.createMetricMessage("This is a test metric message", lStart, System.currentTimeMillis(), EventElasticUtil.METRIC_STATUS.SUCCESS, fields));
        
        appender.logged(log);
    }
    
    @Test
    public void testAuditMessage() {
        Map<String, String> fields = new HashMap<>();
        
        fields.put("splintName", "ScottTest");
        fields.put("name", "Test");
        
        LogEntryImpl log = new LogEntryImpl();
        
        log.setMessage(EventElasticUtil.createAuditMessage("{ \"Field\": \"Value\" }", EventElasticUtil.AUDIT_TYPES.JSON, fields));
        
        appender.logged(log);
    }
    
    @AfterClass
    public void testFlushBuffer() {
        appender.appendBuffer(null, true);
        
        appender.sendLog();
    }
}

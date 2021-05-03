package com.progbits.api.testing;

import com.progbits.api.ParserService;
import com.progbits.api.WriterService;
import com.progbits.api.parser.CsvObjectParser;
import com.progbits.api.parser.FixedWidthNoLineParser;
import com.progbits.api.parser.FixedWidthParser;
import com.progbits.api.parser.JsonObjectParser;
import com.progbits.api.parser.ParserServiceImpl;
import com.progbits.api.parser.XmlObjectParser;
import com.progbits.api.utils.ApiUtils;
import com.progbits.api.utils.oth.ApiUtilsInterface;
import com.progbits.api.writer.CsvObjectWriter;
import com.progbits.api.writer.FixedWidthNoLineWriter;
import com.progbits.api.writer.FixedWidthWriter;
import com.progbits.api.writer.JsonObjectWriter;
import com.progbits.api.writer.WriterServiceImpl;
import com.progbits.api.writer.XmlObjectWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scarr
 */
public class ReturnServices {

    public static ParserService returnParserService() {
        ParserServiceImpl _parser = new ParserServiceImpl();

        XmlObjectParser xml = new XmlObjectParser();
        Map<String, String> xmlProps = new HashMap<>();
        xmlProps.put("type", "XML");

        _parser.addParser(xml, xmlProps);

        JsonObjectParser jsonParse = new JsonObjectParser();
        Map<String, String> jsonProps = new HashMap<>();
        jsonProps.put("type", "JSON");

        _parser.addParser(jsonParse, jsonProps);

        FixedWidthParser fixedParse = new FixedWidthParser();
        Map<String, String> fixedProps = new HashMap<>();
        fixedProps.put("type", "FIXED");

        CsvObjectParser csvParse = new CsvObjectParser();
        Map<String, String> csvProps = new HashMap<>();
        fixedProps.put("type", "CSV");

        _parser.addParser(csvParse, csvProps);

        FixedWidthNoLineParser fxNlParse = new FixedWidthNoLineParser();
        Map<String, String> fxNlProps = new HashMap<>();
        fxNlProps.put("type", "FIXEDNOLINE");

        _parser.addParser(fxNlParse, fxNlProps);

        return _parser;

    }

    public static WriterService returnWriterService() {
        Map<String, String> xmlProps = new HashMap<>();
        xmlProps.put("type", "XML");
        Map<String, String> jsonProps = new HashMap<>();
        jsonProps.put("type", "JSON");

        WriterServiceImpl _writer = new WriterServiceImpl();

        _writer.addParser(new JsonObjectWriter(), jsonProps);

        XmlObjectWriter xmlWrite = new XmlObjectWriter();

        _writer.addParser(xmlWrite, xmlProps);

        CsvObjectWriter csvWrite = new CsvObjectWriter();
        Map<String, String> csvProps = new HashMap<>();
        csvProps.put("type", "CSV");
        _writer.addParser(csvWrite, csvProps);

        FixedWidthWriter fxWrite = new FixedWidthWriter();
        Map<String, String> fxProps = new HashMap<>();
        fxProps.put("type", "FIXED");
        _writer.addParser(fxWrite, fxProps);

        FixedWidthNoLineWriter fxNlWrite = new FixedWidthNoLineWriter();
        Map<String, String> fxNlProps = new HashMap<>();
        fxNlProps.put("type", "FIXEDNOLINE");

        _writer.addParser(fxNlWrite, fxNlProps);

        return _writer;
    }

    public static ApiUtilsInterface returnApiUtils(String location) {
        ApiUtilsInterface retApi = null;

        ApiUtils utils = new ApiUtils();
        utils.setLocation(location);
        utils.setParser(returnParserService());
        utils.setWriter(returnWriterService());
        utils.setup();

        retApi = utils;

        return retApi;
    }
}

package com.icg.api.parser;

import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.YamlObjectParser;
import java.io.InputStreamReader;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class TestYamlParser {

    @Test
    public void runSingleTest() throws Exception {
        YamlObjectParser parser = new YamlObjectParser(true);

        InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("/YamlParserTest.yaml"));

        try {
            ApiObject objResp = parser.parseSingle(reader);

            assert objResp != null;
        } catch (Exception ex) {

        }
    }
}

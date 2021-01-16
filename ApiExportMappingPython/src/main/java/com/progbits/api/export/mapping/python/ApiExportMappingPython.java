package com.progbits.api.export.mapping.python;

import com.progbits.api.export.ApiExport;
import com.progbits.api.model.ApiObject;
import java.util.stream.Stream;
import org.osgi.service.component.annotations.Component;

/**
 *
 * @author scarr
 */
@Component(name = "PythonExportMapping",
		immediate = true,
		property = {
			"type=MAPPING", "lang=Python", "name=PythonExportMapping"
		}
)
public class ApiExportMappingPython implements ApiExport {

	@Override
	public String performExport(String mainClass, ApiObject obj) {
		StringBuilder sbResp = new StringBuilder();
		String strTab = "    ";

		String sourceName = getClassSmallName(obj.getString("sourceClass"));
		String targetName = getClassSmallName(obj.getString("targetClass"));

		String script = obj.getString("mapScript");

		Stream<String> scriptLines = script.lines();

		sbResp.append("def convert").append(sourceName).append("To").append(targetName).append("(source):\n");
		sbResp.append(strTab).append("target = {}\n");

		scriptLines.forEach((line) -> {
			String strTrim = line.trim();

			if (!strTrim.startsWith("//") && strTrim.contains("=")) {
				String[] sEquals = strTrim.split("=");

				sbResp.append(strTab);

				sbResp.append(convertFieldNames(sEquals[0]));
				sbResp.append(" = ");
				sbResp.append(convertFieldNames(sEquals[1]));

				sbResp.append("\n");
			}
		});

		sbResp.append(strTab).append("return target").append("\n");

		return sbResp.toString();
	}

	private String getClassSmallName(String subject) {
		String[] splitSubject = subject.split("\\.");

		String retVal = splitSubject[splitSubject.length - 1];

		return retVal;
	}

	private String convertFieldNames(String subject) {
		String[] splitSubject = subject.split("\\.");
		StringBuilder sbRet = new StringBuilder();

		for (int x = 0; x <= splitSubject.length - 1; x++) {
			if (x == 0) {
				sbRet.append(splitSubject[x].trim());
			} else {
				sbRet.append("[\"").append(splitSubject[x].trim()).append("\"]");
			}
		}

		return sbRet.toString();
	}
}

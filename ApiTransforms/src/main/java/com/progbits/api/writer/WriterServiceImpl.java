package com.progbits.api.writer;

import com.progbits.api.ObjectWriter;
import com.progbits.api.WriterService;
import com.progbits.api.exception.ApiException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 *
 * @author scarr
 */
@Component(name = "WriterService",
		  immediate = true,
		  property = {
			  "name=WriterService"
		  }
)
public class WriterServiceImpl implements WriterService {

	public LinkedHashMap<String, ObjectWriter> _writers = new LinkedHashMap<>();

	@Reference(policy = ReferencePolicy.DYNAMIC,
			  policyOption = ReferencePolicyOption.RELUCTANT,
			  cardinality = ReferenceCardinality.MULTIPLE)
	public void addParser(ObjectWriter writer, Map<String, String> svcProps) {
		_writers.put(svcProps.get("type"), writer);
	}

	public void removeParser(ObjectWriter writer, Map<String, String> svcProps) {
		_writers.remove(svcProps.get("type"));
	}

	@Override
	public ObjectWriter getWriter(String type) throws ApiException {
		ObjectWriter writer = _writers.get(type);

		if (writer != null) {
			return writer.getWriter();
		} else {
			throw new ApiException("Writer " + type + " does not exist.",
					  null);
		}
	}

}

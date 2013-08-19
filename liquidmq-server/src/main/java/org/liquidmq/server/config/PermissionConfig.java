package org.liquidmq.server.config;

import org.liquidmq.Permission;
import org.liquidmq.Permission.PermissionType;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PermissionConfig implements Converter {

	public boolean canConvert(Class type) {
		return Permission.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		Permission p = (Permission) source;
		
		writer.addAttribute("type", p.type().name());
		if(p.topic() != null && !p.topic().isEmpty())
			writer.setValue(p.topic());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Permission p = new Permission();
		p.setType(PermissionType.valueOf(reader.getAttribute("type")));
		String topic = reader.getValue();
		if(topic != null && !topic.isEmpty())
			p.setTopic(topic);
		return p;
	}

}

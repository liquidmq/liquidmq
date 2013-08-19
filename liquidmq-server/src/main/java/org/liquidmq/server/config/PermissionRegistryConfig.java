package org.liquidmq.server.config;

import org.liquidmq.Credentials;
import org.liquidmq.Permission;
import org.liquidmq.pv.PermissionRegistry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PermissionRegistryConfig extends AbstractConfig {

	public PermissionRegistryConfig(XStream x) {
		super(x);
	}
	
	public boolean canConvert(Class type) {
		return PermissionRegistry.class.isAssignableFrom(type);
	}

	public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		PermissionRegistry pr = (PermissionRegistry) source;
		MarshalSupport ms = marshalSupport(writer, context);
		for(Credentials c : pr.keys()) {
			writer.startNode("permission");
			ms.writeObject(Credentials.class, "credential", c);
			for(Permission p : pr.get(c)) {
				ms.writeObject(Permission.class, "grant", p);
			}
			writer.endNode();
		}
	}

	public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
		PermissionRegistry pr = new PermissionRegistry();
		UnmarshalSupport us = unmarshalSupport(pr, reader, context);
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("permission".equals(reader.getNodeName())) {
				Credentials c = null;
				while(reader.hasMoreChildren()) {
					reader.moveDown();
					if("credential".equals(reader.getNodeName()))
						c = us.readObject(Credentials.class);
					if("grant".equals(reader.getNodeName())) {
						Permission p = us.readObject(Permission.class);
						pr.add(c, p);
					}
					reader.moveUp();
				}
			}
			reader.moveUp();
		}
		return pr;
	}

}

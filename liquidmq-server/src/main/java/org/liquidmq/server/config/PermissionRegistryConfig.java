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

public class PermissionRegistryConfig implements Converter {

	protected ConfigUtil util;
	
	public PermissionRegistryConfig(XStream x) {
		util = new ConfigUtil(x);
	}
	
	public boolean canConvert(Class type) {
		return PermissionRegistry.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		PermissionRegistry pr = (PermissionRegistry) source;
		ConfigUtil.MarshalSupport ms = util.new MarshalSupport(writer, context);
		for(Credentials c : pr.keys()) {
			writer.startNode("permissions");
			ms.writeObject(Credentials.class, "credential", c);
			for(Permission p : pr.get(c)) {
				ms.writeObject(Permission.class, "grant", p);
			}
			writer.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		PermissionRegistry pr = new PermissionRegistry();
		ConfigUtil.UnmarshalSupport us = util.new UnmarshalSupport(pr, reader, context);
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("permissions".equals(reader.getNodeName())) {
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

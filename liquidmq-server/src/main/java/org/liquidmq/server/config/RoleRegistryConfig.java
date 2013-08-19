package org.liquidmq.server.config;

import org.liquidmq.Credentials;
import org.liquidmq.pv.RoleRegistry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RoleRegistryConfig implements Converter {

	protected ConfigUtil util;
	
	public RoleRegistryConfig(XStream x) {
		util = new ConfigUtil(x);
	}
	
	public boolean canConvert(Class type) {
		return RoleRegistry.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		RoleRegistry rr = (RoleRegistry) source;
		ConfigUtil.MarshalSupport ms = util.new MarshalSupport(writer, context);
		
		for(Credentials c : rr.keys()) {
			writer.startNode("permissions");
			ms.writeObject(Credentials.class, "credential", c);
			for(RoleRegistry.Role r : rr.get(c)) {
				ms.writeObject(RoleRegistry.Role.class, "role", r);
			}
			writer.endNode();
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		RoleRegistry rr = new RoleRegistry();
		ConfigUtil.UnmarshalSupport us = util.new UnmarshalSupport(rr, reader, context);
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("permissions".equals(reader.getNodeName())) {
				Credentials c = null;
				while(reader.hasMoreChildren()) {
					reader.moveDown();
					if("credential".equals(reader.getNodeName()))
						c = us.readObject(Credentials.class);
					if("role".equals(reader.getNodeName())) {
						RoleRegistry.Role r = us.readObject(RoleRegistry.Role.class);
						rr.add(c, r);
					}
					reader.moveUp();
				}
			}
			reader.moveUp();
		}
		return rr;
	}

}

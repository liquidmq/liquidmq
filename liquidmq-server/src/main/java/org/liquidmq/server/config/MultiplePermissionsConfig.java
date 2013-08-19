package org.liquidmq.server.config;

import org.liquidmq.PermissionVerifier;
import org.liquidmq.pv.MultiplePermissions;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultiplePermissionsConfig implements Converter {
	protected ConfigUtil util;
	
	public MultiplePermissionsConfig(XStream x) {
		util = new ConfigUtil(x);
	}

	public boolean canConvert(Class type) {
		return MultiplePermissions.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		MultiplePermissions mp = (MultiplePermissions) source;
		ConfigUtil.MarshalSupport ms = util.new MarshalSupport(writer, context);
		
		for(PermissionVerifier pv : mp) {
			ms.writeObject(PermissionVerifier.class, "permission-verifier", pv);
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MultiplePermissions mp = new MultiplePermissions();
		ConfigUtil.UnmarshalSupport us = util.new UnmarshalSupport(mp, reader, context);
		
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("permission-verifier".equals(reader.getNodeName())) {
				PermissionVerifier pv = us.readObject(PermissionVerifier.class);
				mp.add(pv);
			}
			reader.moveUp();
		}
		
		return mp;
	}
}

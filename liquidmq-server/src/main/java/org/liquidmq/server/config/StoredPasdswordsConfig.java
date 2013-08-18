package org.liquidmq.server.config;

import org.liquidmq.cv.StoredPasswords;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StoredPasdswordsConfig implements Converter {
	protected ConfigUtil util;
	
	public StoredPasdswordsConfig(XStream xstream) {
		util = new ConfigUtil(xstream);
	}

	public boolean canConvert(Class type) {
		return StoredPasswords.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		StoredPasswords sp = (StoredPasswords) source;
		ConfigUtil.MarshalSupport ms = util.new MarshalSupport(writer, context);
		
		for(String user : sp.keySet()) {
			ms.writeObject(String.class, user, sp.get(user));
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		StoredPasswords sp = new StoredPasswords();
		ConfigUtil.UnmarshalSupport us = util.new UnmarshalSupport(sp, reader, context);
		
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			String user = reader.getNodeName();
			String pass = us.readObject(String.class);
			sp.put(user, pass);
			reader.moveUp();
		}
		
		return sp;
	}
}

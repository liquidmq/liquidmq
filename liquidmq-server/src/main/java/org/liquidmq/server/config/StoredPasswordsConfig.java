package org.liquidmq.server.config;

import org.liquidmq.cv.StoredPasswords;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class StoredPasswordsConfig extends AbstractConfig {
	public StoredPasswordsConfig(XStream xstream) {
		super(xstream);
	}

	public boolean canConvert(Class type) {
		return StoredPasswords.class.isAssignableFrom(type);
	}

	public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		StoredPasswords sp = (StoredPasswords) source;
		MarshalSupport ms = marshalSupport(writer, context);
		
		for(String user : sp.keySet()) {
			ms.writeObject(String.class, user, sp.get(user));
		}
	}

	public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
		StoredPasswords sp = new StoredPasswords();
		UnmarshalSupport us = unmarshalSupport(sp, reader, context);
		
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

package org.liquidmq.server.config;

import org.liquidmq.Credentials;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class CredentialsConfig extends AbstractConfig {
	public CredentialsConfig(XStream x) {
		super(x);
	}
	
	public static class UsernameCredentialsConfig extends CredentialsConfig {
		public UsernameCredentialsConfig(XStream x) {
			super(x);
		}

		public boolean canConvert(Class type) {
			return Credentials.UsernameCredentials.class.isAssignableFrom(type);
		}
		
		@Override
		protected void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			Credentials.UsernameCredentials c = (Credentials.UsernameCredentials) source;
			context.convertAnother(c.getUsername());
		}
		
		@Override
		protected Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
			Credentials.UsernameCredentials c = new Credentials.UsernameCredentials();
			c.setUsername((String) context.convertAnother(c, String.class));
			return c;
		}
		
	}
	
	public static class NoCredentialsConfig extends CredentialsConfig {
		public NoCredentialsConfig(XStream x) {
			super(x);
		}

		public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		}

		public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
			return new Credentials.NoCredentials();
		}

		public boolean canConvert(Class type) {
			return Credentials.NoCredentials.class.isAssignableFrom(type);
		}
	}
	
	public static class PasswordCredentialsConfig extends CredentialsConfig {
		public PasswordCredentialsConfig(XStream x) {
			super(x);
		}

		public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			Credentials.PasswordCredentials pc = (Credentials.PasswordCredentials) source;
			MarshalSupport ms = marshalSupport(writer, context);
			ms.writeObject(String.class, "username", pc.getUsername());
			ms.writeObject(String.class, "password", pc.getPassword());
		}

		public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
			Credentials.PasswordCredentials pc = new Credentials.PasswordCredentials();
			UnmarshalSupport us = unmarshalSupport(pc, reader, context);
			
			while(reader.hasMoreChildren()) {
				reader.moveDown();
				if("username".equals(reader.getNodeName()))
					pc.setUsername(us.readObject(String.class));
				if("password".equals(reader.getNodeName()))
					pc.setPassword(us.readObject(String.class));
				reader.moveUp();
			}
			
			return pc;
		}

		public boolean canConvert(Class type) {
			return Credentials.PasswordCredentials.class.isAssignableFrom(type);
		}
	}
}

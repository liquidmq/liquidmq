package org.liquidmq.server.config;

import org.liquidmq.Credentials;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class CredentialsConfig implements Converter {
	protected ConfigUtil util;
	
	public CredentialsConfig(XStream x) {
		util = new ConfigUtil(x);
	}
	
	public static class NoCredentialsConfig extends CredentialsConfig {
		public NoCredentialsConfig(XStream x) {
			super(x);
		}

		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
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

		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			Credentials.PasswordCredentials pc = (Credentials.PasswordCredentials) source;
			context.convertAnother(pc.getUsername());
		}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			Credentials.PasswordCredentials pc = new Credentials.PasswordCredentials();
			pc.setUsername((String) context.convertAnother(pc, String.class));
			return pc;
		}

		public boolean canConvert(Class type) {
			return Credentials.PasswordCredentials.class.isAssignableFrom(type);
		}
	}
}

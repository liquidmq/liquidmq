package org.liquidmq.server.config;

import org.liquidmq.CredentialVerifier;
import org.liquidmq.MqServer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MqServerConfig implements Converter {
	protected ConfigUtil util;
	
	public MqServerConfig(XStream xstream) {
		this.util = new ConfigUtil(xstream);
	}

	public boolean canConvert(Class type) {
		return MqServer.class.isAssignableFrom(type);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		ConfigUtil.MarshalSupport ms = util.new MarshalSupport(writer, context);
		
		MqServer mq = (MqServer) source;
		ms.writeObject(Integer.class, "port", mq.getPort());
		ms.writeObject(CredentialVerifier.class, "credential-verifier", mq.getCredentialVerifier());
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MqServer ms = new MqServer();
		ConfigUtil.UnmarshalSupport us = util.new UnmarshalSupport(ms, reader, context);
		
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("port".equals(reader.getNodeName()))
				ms.setPort(us.readObject(Integer.class));
			if("credential-verifier".equals(reader.getNodeName()))
				ms.setCredentialVerifier(us.readObject(CredentialVerifier.class));
			reader.moveUp();
		}
		
		return ms;
	}

}

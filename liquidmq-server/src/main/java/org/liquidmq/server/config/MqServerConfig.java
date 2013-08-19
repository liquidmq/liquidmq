package org.liquidmq.server.config;

import org.liquidmq.CredentialVerifier;
import org.liquidmq.MqServer;
import org.liquidmq.PermissionVerifier;
import org.liquidmq.pv.EveryonePermitted;
import org.liquidmq.pv.MultiplePermissions;
import org.liquidmq.pv.PermissionRegistry;
import org.liquidmq.pv.RoleRegistry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MqServerConfig extends AbstractConfig {
	protected Aliasing pva = new Aliasing();
	
	public MqServerConfig(XStream xstream) {
		super(xstream);
		
		pva.alias("everyone-permitted", EveryonePermitted.class);
		pva.alias("any", MultiplePermissions.class);
		pva.alias("user-permissions", PermissionRegistry.class);
		pva.alias("user-roles", RoleRegistry.class);
	}

	public boolean canConvert(Class type) {
		return MqServer.class.isAssignableFrom(type);
	}

	public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		MarshalSupport ms = marshalSupport(writer, context);
		
		MqServer mq = (MqServer) source;
		ms.writeObject(Integer.class, "port", mq.getPort());
		ms.writeObject(CredentialVerifier.class, "credential-verifier", mq.getCredentialVerifier());
		
		pushAliases(pva, context);
		ms.writeObject(PermissionVerifier.class, "permission-verifier", mq.getPermissionVerifier());
		popAliases(context);
	}

	public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MqServer ms = new MqServer();
		UnmarshalSupport us = unmarshalSupport(ms, reader, context);
		
		while(reader.hasMoreChildren()) {
			reader.moveDown();
			if("port".equals(reader.getNodeName()))
				ms.setPort(us.readObject(Integer.class));
			if("credential-verifier".equals(reader.getNodeName()))
				ms.setCredentialVerifier(us.readObject(CredentialVerifier.class));
			if("permission-verifier".equals(reader.getNodeName())) {
				pushAliases(pva, context);
				ms.setPermissionVerifier(us.readObject(PermissionVerifier.class));
				popAliases(context);
			}
			reader.moveUp();
		}
		
		return ms;
	}

}

package org.liquidmq.server.config;

import java.lang.reflect.Modifier;

import org.liquidmq.Credentials;
import org.liquidmq.MqServer;
import org.liquidmq.Permission;
import org.liquidmq.cv.StoredPasswords;
import org.liquidmq.pv.EveryonePermitted;
import org.liquidmq.pv.MultiplePermissions;
import org.liquidmq.pv.PermissionRegistry;
import org.liquidmq.pv.RoleRegistry;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConfigUtil {
	public static void registerConverters(XStream x) {
		x.registerConverter(new MqServerConfig(x));
		x.registerConverter(new StoredPasswordsConfig(x));
		x.registerConverter(new CredentialsConfig.NoCredentialsConfig(x));
		x.registerConverter(new CredentialsConfig.PasswordCredentialsConfig(x));
		x.registerConverter(new EveryonePermittedConfig());
		x.registerConverter(new PermissionRegistryConfig(x));
		x.registerConverter(new PermissionConfig());
		x.registerConverter(new RoleRegistryConfig(x));
		x.registerConverter(new MultiplePermissionsConfig(x));
		
		x.alias("mq-server", MqServer.class);
		x.alias("stored-passwords", StoredPasswords.class);
		x.alias("anonymous", Credentials.NoCredentials.class);
		x.alias("user", Credentials.PasswordCredentials.class);
		x.alias("everyone-permitted", EveryonePermitted.class);
		x.alias("permission-registry", PermissionRegistry.class);
		x.alias("permission", Permission.class);
		x.alias("role-registry", RoleRegistry.class);
		x.alias("any-permissions", MultiplePermissions.class);
	}
	
	protected XStream xstream;
	
	public ConfigUtil(XStream xstream) {
		this.xstream = xstream;
	}
	
	public class MarshalSupport {
		protected HierarchicalStreamWriter writer;
		protected MarshallingContext context;
		
		public MarshalSupport(HierarchicalStreamWriter writer, MarshallingContext context) {
			this.writer = writer;
			this.context = context;
		}
		
		public <T> void writeObject(Class<T> clazz, String name, T value) {
			if(value == null)
				return;
			writer.startNode(name);
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.startNode(xstream.getMapper().serializedClass(value.getClass()));
			context.convertAnother(value);
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.endNode();
			writer.endNode();
		}
	}
	
	public class UnmarshalSupport {
		protected Object thiz;
		protected HierarchicalStreamReader reader;
		protected UnmarshallingContext context;
		
		public UnmarshalSupport(Object thiz, HierarchicalStreamReader reader, UnmarshallingContext context) {
			this.reader = reader;
			this.context = context;
		}
		
		public <T> T readObject(Class<T> clazz) {
			if(Modifier.isFinal(clazz.getModifiers()))
				return (T) context.convertAnother(thiz, clazz);
			reader.moveDown();
			Class<? extends T> actual = xstream.getMapper().realClass(reader.getNodeName());
			T ret = (T) context.convertAnother(thiz, actual);
			reader.moveUp();
			return ret;
		}
	}
}

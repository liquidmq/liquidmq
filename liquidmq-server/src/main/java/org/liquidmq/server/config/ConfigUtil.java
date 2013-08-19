package org.liquidmq.server.config;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
		x.alias("username", Credentials.PasswordCredentials.class);
	}
	
	private ConfigUtil() {}
}

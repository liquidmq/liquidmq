package org.liquidmq.server.config;

import org.junit.Test;
import org.liquidmq.Credentials;
import org.liquidmq.Permission;
import org.liquidmq.Permission.PermissionType;
import org.liquidmq.pv.PermissionRegistry;

import com.thoughtworks.xstream.XStream;


public class PermissionRegistryConfigTest {
	@Test
	public void testWrite() throws Exception {
		PermissionRegistry pr = new PermissionRegistry();
		pr.add(new Credentials.NoCredentials(), new Permission(PermissionType.SET_ORIGIN));
		pr.add(new Credentials.UsernameCredentials("foo"), new Permission(PermissionType.SUBSCRIBE, "foo-topic"));
		pr.add(new Credentials.UsernameCredentials("foo"), new Permission(PermissionType.SUBSCRIBE, "bar-topic"));
		
		XStream x = new XStream();
		ConfigUtil.registerConverters(x);
		
		x.toXML(pr, System.out);
	}
}

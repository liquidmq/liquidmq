package org.liquidmq.server.config;

import org.junit.Assert;
import org.junit.Test;
import org.liquidmq.Credentials;
import org.liquidmq.MqServer;
import org.liquidmq.Permission;
import org.liquidmq.PermissionVerifier;
import org.liquidmq.Permission.PermissionType;
import org.liquidmq.cv.StoredPasswords;
import org.liquidmq.pv.EveryonePermitted;
import org.liquidmq.pv.MultiplePermissions;
import org.liquidmq.pv.PermissionRegistry;
import org.liquidmq.pv.RoleRegistry;

import com.thoughtworks.xstream.XStream;

public class MqServerConfigTest {
	@Test
	public void testWrite() throws Exception {
		XStream x = new XStream();
		ConfigUtil.registerConverters(x);
		
		MqServer s = new MqServer();
		
		StoredPasswords sp = new StoredPasswords();
		s.setCredentialVerifier(sp);
		sp.put("foo-user", "bar-pass");
		
		MultiplePermissions mp = new MultiplePermissions();
		s.setPermissionVerifier(mp);
		
		RoleRegistry rr = new RoleRegistry();
		rr.add(new Credentials.UsernameCredentials("foo-user"), RoleRegistry.Role.ADMIN);
		rr.add(new Credentials.NoCredentials(), RoleRegistry.Role.GUEST);
		mp.add(rr);
		
		PermissionRegistry pr = new PermissionRegistry();
		pr.add(new Credentials.UsernameCredentials("bar-user"), new Permission(PermissionType.SEND, "qux-topic"));
		mp.add(pr);
		
		x.toXML(s, System.out);
	}
	
	@Test
	public void testRead() throws Exception {
		XStream x = new XStream();
		ConfigUtil.registerConverters(x);
		
		String xml = "<mq-server>"
				+ "<port>1</port>"
				+ "<credential-verifier>"
				+ "<stored-passwords>"
				+ "<foo-user>bar-pass</foo-user>"
				+ "</stored-passwords>"
				+ "</credential-verifier>"
				+ "</mq-server>";
		MqServer s = (MqServer) x.fromXML(xml);
		
		Assert.assertEquals(1, s.getPort());
		Assert.assertTrue(s.getCredentialVerifier() instanceof StoredPasswords);
		StoredPasswords sp = (StoredPasswords) s.getCredentialVerifier();
		Assert.assertEquals("bar-pass", sp.get("foo-user"));
		
	}
}

package org.liquidmq.server.config;

import org.junit.Assert;
import org.junit.Test;
import org.liquidmq.MqServer;
import org.liquidmq.cv.StoredPasswords;

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

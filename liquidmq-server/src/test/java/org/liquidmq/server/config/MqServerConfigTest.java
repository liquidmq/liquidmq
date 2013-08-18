package org.liquidmq.server.config;

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
}

package org.liquidmq;

import com.esotericsoftware.kryonet.Connection;

public interface CredentialVerifier {
	public boolean verify(MqServer server, Connection connection, Credentials credentials);
}

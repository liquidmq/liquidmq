package org.liquidmq;

import com.esotericsoftware.kryonet.Connection;

public interface PermissionVerifier {
	public boolean verify(MqServer server, Connection connection, Credentials credentials, Permission permission);

}

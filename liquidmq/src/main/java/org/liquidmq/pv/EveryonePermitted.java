package org.liquidmq.pv;

import org.liquidmq.Credentials;
import org.liquidmq.MqServer;
import org.liquidmq.Permission;
import org.liquidmq.PermissionVerifier;

import com.esotericsoftware.kryonet.Connection;

public class EveryonePermitted implements PermissionVerifier {

	@Override
	public boolean verify(MqServer server, Connection connection, Credentials credentials, Permission permission) {
		return true;
	}

}

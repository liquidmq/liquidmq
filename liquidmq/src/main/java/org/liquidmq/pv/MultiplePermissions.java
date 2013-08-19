package org.liquidmq.pv;

import java.util.ArrayList;

import org.liquidmq.Credentials;
import org.liquidmq.MqServer;
import org.liquidmq.Permission;
import org.liquidmq.PermissionVerifier;

import com.esotericsoftware.kryonet.Connection;

public class MultiplePermissions extends ArrayList<PermissionVerifier> implements PermissionVerifier {

	@Override
	public boolean verify(MqServer server, Connection connection, Credentials credentials, Permission permission) {
		for(PermissionVerifier pv : this) {
			if(pv.verify(server, connection, credentials, permission))
				return true;
		}
		return false;
	}

}

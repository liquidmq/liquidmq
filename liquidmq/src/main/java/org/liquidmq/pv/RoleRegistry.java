package org.liquidmq.pv;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.liquidmq.Credentials;
import org.liquidmq.MqServer;
import org.liquidmq.Permission;
import org.liquidmq.PermissionVerifier;
import org.liquidmq.Registry;
import org.liquidmq.Permission.PermissionType;

import com.esotericsoftware.kryonet.Connection;

import static org.liquidmq.Permission.PermissionType.*;

public class RoleRegistry extends Registry<Credentials, RoleRegistry.Role> implements PermissionVerifier {
	public static enum Role {
		ADMIN(GRANT, REVOKE, SET_ORIGIN, SUBSCRIBE, SEND, QUEUE),
		MANAGER(SUBSCRIBE, SEND, QUEUE),
		OBSERVER(SUBSCRIBE),
		SENDER(SEND),
		QUEUER(QUEUE),
		ORIGINATOR(SET_ORIGIN),
		GUEST
		;
		
		
		
		private Set<PermissionType> permissionTypes;
		
		private Role(PermissionType... permissionTypes) {
			this.permissionTypes = new HashSet<PermissionType>(Arrays.asList(permissionTypes));
			this.permissionTypes = Collections.unmodifiableSet(this.permissionTypes);
		}
		
		public Set<PermissionType> permissionTypes() {
			return permissionTypes;
		}
	}

	@Override
	public boolean verify(MqServer server, Connection connection, Credentials credentials, Permission permission) {
		for(Role role : get(credentials)) {
			if(role.permissionTypes().contains(permission.type()))
				return true;
		}
		return false;
	}
}

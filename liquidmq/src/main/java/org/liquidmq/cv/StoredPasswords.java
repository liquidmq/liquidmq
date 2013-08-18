package org.liquidmq.cv;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.liquidmq.CredentialVerifier;
import org.liquidmq.Credentials;
import org.liquidmq.MqServer;

import com.esotericsoftware.kryonet.Connection;

public class StoredPasswords extends TreeMap<String, String> implements CredentialVerifier {
	
	@Override
	public boolean verify(MqServer server, Connection connection, Credentials credentials) {
		if(credentials instanceof Credentials.NoCredentials)
			return true;
		if(credentials instanceof Credentials.PasswordCredentials) {
			Credentials.PasswordCredentials cr = (Credentials.PasswordCredentials) credentials;
			if(!containsKey(cr.getUsername()))
				return false;
			return get(cr.getUsername()).equals(cr.getPassword());
		}
		return false;
	}

}

package org.liquidmq;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public abstract class Credentials {
	protected String username;
	protected transient boolean verified;
	
	public Credentials() {}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	@Override
	public int hashCode() {
		return username == null ? 0 : username.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Credentials) {
			Credentials o = (Credentials) obj;
			return username == null ? o.username == null : username.equals(o.username);
		}
		return false;
	}

	public static final class NoCredentials extends Credentials implements KryoSerializable {
		@Override
		public void write(Kryo kryo, Output output) {
		}

		@Override
		public void read(Kryo kryo, Input input) {
		}
		
		@Override
		public int hashCode() {
			return 0;
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof NoCredentials);
		}
	}
	
	public static final class UsernameCredentials extends Credentials {
		public UsernameCredentials() {}
		
		public UsernameCredentials(String username) {
			this.username = username;
		}
	}
	
	public static final class PasswordCredentials extends Credentials {
		protected String password;
		
		public PasswordCredentials() {}
		
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
	}
}

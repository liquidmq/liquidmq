package org.liquidmq;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public abstract class Credentials implements KryoSerializable {
	protected boolean verified;
	
	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public static class NoCredentials extends Credentials {
		@Override
		public void write(Kryo kryo, Output output) {
		}

		@Override
		public void read(Kryo kryo, Input input) {
		}
	}
	
	public static class PasswordCredentials extends Credentials {
		private String username;
		private String password;
		
		public PasswordCredentials() {}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public void write(Kryo kryo, Output output) {
			output.writeString(username);
			output.writeString(password);
		}

		@Override
		public void read(Kryo kryo, Input input) {
			username = input.readString();
			password = input.readString();
		}
	}
}

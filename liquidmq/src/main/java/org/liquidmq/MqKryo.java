package org.liquidmq;

import com.esotericsoftware.kryo.Kryo;

/**
 * {@link Kryo} preconfigured for use with KryoMQ message transport
 * @author robin
 *
 */
public class MqKryo extends Kryo {
	public MqKryo() {
		setReferences(false);
		setAutoReset(true);
		setRegistrationRequired(true);
		
		register(byte[].class);
		register(Message.class);

		register(Control.class);
		register(Control.Command.class);
		
		register(Meta.class);
		register(Meta.MetaType.class);
		
		register(Permission.class);
		register(Permission.PermissionType.class);
	}
}

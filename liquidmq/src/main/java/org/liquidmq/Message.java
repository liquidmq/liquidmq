package org.liquidmq;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Base class for transport messages transported across LiquidMQ.
 * Messages are treated agnostically; LiquidMQ sees them only as byte arrays,
 * leaving higher-level serialization or deserialization to the user.
 * @author robin
 *
 */
public final class Message implements KryoSerializable {
	/**
	 * Whether this message should be delivered reliably (TCP) or not (UDP)
	 */
	private boolean reliable;
	/**
	 * The topic to which this message is destined
	 */
	private String topic;
	/**
	 * The topic from which this message originated
	 */
	private String origin;
	/**
	 * The content of this message
	 */
	private byte[] buf;

	/**
	 * Create a blank {@link Message}
	 */
	public Message() {}
	
	/**
	 * Create a {@link Message} with a specified buffer and reliable transport
	 * @param buf
	 */
	public Message(byte[] buf) {
		this(buf, true);
	}

	/**
	 * Create a {@link Message} with a specified buffer and optionally reliable transport
	 * @param buf
	 * @param reliable
	 */
	public Message(byte[] buf, boolean reliable) {
		this(null, buf, reliable);
	}
	
	/**
	 * Create a {@link Message} with a specified destination and optionally reliable transport
	 * @param topic
	 * @param reliable
	 */
	public Message(String topic, boolean reliable) {
		this(topic, null, reliable);
	}
	
	/**
	 * Create a {@link Message} with a specified destination and buffer and reliable transport
	 * @param topic
	 * @param buf
	 */
	public Message(String topic, byte[] buf) {
		this(topic, buf, true);
	}
	
	/**
	 * Create a {@link Message} with a specified destination, buffer, and transport reliability
	 * @param topic
	 * @param buf
	 * @param reliable
	 */
	public Message(String topic, byte[] buf, boolean reliable) {
		this.topic = topic;
		this.buf = buf;
		this.reliable = reliable;
	}
	
	/**
	 * Create a new {@link Message} to respond to this one
	 * @return
	 */
	public Message createReply() {
		return new Message(origin, reliable);
	}
	
	/**
	 * Serialize the {@code value} using the argument {@link Kryo} and use
	 * the result as the buffer.
	 * @param kryo
	 * @param value
	 * @return
	 */
	public Message set(Kryo kryo, Object value) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		Output output = new Output(buf);
		kryo.writeClassAndObject(output, value);
		output.close();
		this.buf = buf.toByteArray();
		return this;
	}
	
	/**
	 * Deserialize the buffer using the argument {@link Kryo}
	 * @param kryo
	 * @return
	 */
	public Object get(Kryo kryo) {
		return kryo.readClassAndObject(new Input(buf));
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeBoolean(reliable);
		output.writeString(topic);
		output.writeString(origin);
		kryo.writeObjectOrNull(output, buf, byte[].class);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		reliable = input.readBoolean();
		topic = input.readString();
		origin = input.readString();
		buf = kryo.readObjectOrNull(input, byte[].class);
	}

	public boolean reliable() {
		return reliable;
	}
	public String topic() {
		return topic;
	}
	public String origin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public byte[] buf() {
		return buf;
	}
	public void setBuf(byte[] buf) {
		this.buf = buf;
	}

}

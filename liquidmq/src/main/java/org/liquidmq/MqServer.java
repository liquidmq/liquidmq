package org.liquidmq;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import org.liquidmq.Meta.MetaType;
import org.liquidmq.Permission.PermissionType;

/**
 * A LiquidMQ server
 * @author robin
 *
 */
public class MqServer extends Listener {
	private static final Logger log = LoggerFactory.getLogger(MqServer.class);
	
	/**
	 * The port on which the server listens, both TCP and UDP
	 */
	protected int port;
	/**
	 * A thread pool for potentially long-running tasks, such as flushing queues
	 */
	protected ExecutorService pool;
	/**
	 * The KryoNet {@link Server} backing this {@link MqServer}
	 */
	protected Server server;

	/**
	 * The origin topic for each {@link Connection}
	 */
	protected Map<Connection, String> origins = new ConcurrentHashMap<Connection, String>();
	/**
	 * Mapping from topic to {@link MessageQueue} for topics which have queueing enabled
	 */
	protected Map<String, MessageQueue> queues = new ConcurrentHashMap<String, MessageQueue>();

	/**
	 * Delegate that verifies client credentials
	 */
	protected CredentialVerifier credentialVerifier;
	
	/**
	 * Credentials provided by the clients
	 */
	protected Map<Connection, Credentials> credentials = new ConcurrentHashMap<Connection, Credentials>();
	
	/**
	 * Registry for which {@link Connection}s subscribe to each topic
	 */
	protected Registry<String, Connection> subscriptions = new Registry<String, Connection>();
	
	protected PermissionVerifier permissionVerifier;
	/**
	 * Registry for which {@link Connection}s have each permission
	 */
	protected Registry<Permission, Connection> permissions = new Registry<Permission, Connection>();
	
	public MqServer() {
		this(-1);
	}
	
	/**
	 * Create a new LiquidMQ server that listens on both TCP and UDP on the argument port.
	 * Does not actually start the server or bind to sockets yet.
	 * @param port The port to use
	 */
	public MqServer(int port) {
		this.port = port;
		pool = Executors.newCachedThreadPool(Threads.factoryNamed(this + " worker "));
	}
	
	/**
	 * Return the port for this LiquidMQ server
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public CredentialVerifier getCredentialVerifier() {
		return credentialVerifier;
	}
	
	public void setCredentialVerifier(CredentialVerifier credentialVerifier) {
		this.credentialVerifier = credentialVerifier;
	}
	
	public PermissionVerifier getPermissionVerifier() {
		return permissionVerifier;
	}

	public void setPermissionVerifier(PermissionVerifier permissionVerifier) {
		this.permissionVerifier = permissionVerifier;
	}

	/**
	 * Start this {@link MqServer}.  This means starting a KryoNet {@link Server}
	 * and binding it to the appropriate port
	 * @throws IOException
	 */
	public MqServer start() throws IOException {
		log.debug("{} starting server on port {}", this, port);
		server = new Server(1024*256, 1024*256, new KryoSerialization(new MqKryo()));
		server.start();
		server.bind(port, port);
		server.addListener(this);
		log.debug("{} started server on port {}", this, port);
		return this;
	}
	
	/**
	 * Stop this {@link MqServer}.  This means stopping the KryoNet {@link Server}.
	 * @throws IOException
	 */
	public void stop() throws IOException {
		log.debug("{} stopping server on port {}", this, port);
		server.close();
		server.stop();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.esotericsoftware.kryonet.Listener#connected(com.esotericsoftware.kryonet.Connection)
	 * 
	 * Called when a client connects.  Does some per-client setup.
	 */
	@Override
	public void connected(Connection connection) {
		// The privileged topic for this client
		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
		// The controlled topic for this client
		String controlledTopic = Topics.CONTROLLED + Topics.CLIENT + connection.getID();
		
		
		// Begin queueing messages immediately
		queues.put(personalTopic, new MessageQueue());
		// Grant permission to subscribe to its own privileged topic
		permissions.add(new Permission(PermissionType.SUBSCRIBE, personalTopic), connection);
		// Grant permission to send to its own controlled topic
		permissions.add(new Permission(PermissionType.SEND, controlledTopic), connection);
		// Set the default origin for this client to its privileged topic
		origins.put(connection, personalTopic);
		
		
		// Tell the client what its privileged topic is
		server.sendToTCP(connection.getID(), new Meta(MetaType.PRIVILEGED_TOPIC, personalTopic));
		// Tell the client what its controlled topic is
		server.sendToTCP(connection.getID(), new Meta(MetaType.CONTROLLED_TOPIC, controlledTopic));
		// Tell the client what its id number is
		server.sendToTCP(connection.getID(), new Meta(MetaType.ID, "" + connection.getID()));
		// Tell everyone that a client just connected
		server.sendToAllTCP(new Meta(MetaType.CONNECTED, personalTopic));
	}

	/*
	 * (non-Javadoc)
	 * @see com.esotericsoftware.kryonet.Listener#disconnected(com.esotericsoftware.kryonet.Connection)
	 * 
	 * Called when a client disconnects.  Does some cleanup.
	 */
	@Override
	public void disconnected(Connection connection) {
		// The privileged topic for this client
		String personalTopic = Topics.PRIVILEGED + Topics.CLIENT + connection.getID();
		
		// Tell everyone that this client disconnected
		server.sendToAllTCP(new Meta(MetaType.DISCONNECTED, personalTopic));
		
		// deregister all subscriptions
		subscriptions.deregister(connection);
		// deregister all permissions
		permissions.deregister(connection);
		// remove origin
		origins.remove(connection);
		// remove privileged topic queue
		queues.remove(personalTopic);
	}
	
	/**
	 * Returns whether the argument {@link Connection} should be granted the
	 * argument {@link Permission}. <p>  
	 * 
	 * This implementation grants all permissions to any client connecting
	 * via the loopback interface.  Non-loopback clients fall back to the {@link #permissions}
	 * registry.
	 * to any
	 * @param perm
	 * @param connection
	 * @return
	 */
	protected boolean permitted(Permission perm, Connection connection) {
		if(permissionVerifier == null)
			return true;
		if(permissionVerifier.verify(this, connection, credentials.get(connection), perm))
			return true;
		return permissions.get(perm).contains(connection);
	}
	
	/**
	 * Dispatch a {@link Message}, either to subscribers, or if there are none and
	 * there is a {@link MessageQueue}, to that queue
	 * @param m
	 */
	protected void dispatch(Message m) {
		log.trace("{} dispatching message from {} to {}", this, m.origin(), m.topic());
		Set<Connection> subscribers = subscriptions.get(m.topic());
		
		if(subscribers.size() > 0) {
			// dispatch if there are any subscribers
			for(Connection c : subscribers) {
				if(m.reliable())
					server.sendToTCP(c.getID(), m);
				else
					server.sendToUDP(c.getID(), m);
			}
		} else if(queues.containsKey(m.topic())) {
			// otherwise dump the messages in the queue
			queues.get(m.topic()).put(m);
		} else {
			// drop the message
		}
	}
	
	/**
	 * Begins flushing a {@link MessageQueue}.  Flushing is done in a separate thread, to avoid
	 * causing a block on the main {@link MqServer} thread.
	 * @param topic
	 */
	protected void flush(final String topic) {
		if(!queues.containsKey(topic))
			return;
		Runnable flushTask = new Runnable() {
			@Override
			public void run() {
				String name = Thread.currentThread().getName();
				try {
					// Rename the current thread to indicate what we are doing
					Thread.currentThread().setName(this + " flush task:" + topic);
					log.trace("{} flushing message queue {}", this, topic);
					// Drop in a repalcement message queue so we don't end up filling up the one we are emptying
					MessageQueue mq = queues.put(topic, new MessageQueue());
					if(mq == null) {
						queues.remove(topic);
						return;
					}
					// Grab all the queued messages and dispatch them
					while(mq.available()) {
						dispatch(mq.take());
					}
					log.trace("{} flushed message queue {}", this, topic);
				} finally {
					Thread.currentThread().setName(name);
				}
			}
		};
		pool.execute(flushTask);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.esotericsoftware.kryonet.Listener#received(com.esotericsoftware.kryonet.Connection, java.lang.Object)
	 * 
	 * Called when a transport message is received from an MqClient
	 */
	@Override
	public void received(Connection connection, Object object) {
		if(object instanceof Credentials) {
			Credentials cr = (Credentials) object;
			cr.setVerified(credentialVerifier == null || credentialVerifier.verify(this, connection, cr));
			credentials.put(connection, cr);
		}
		if(object instanceof Message) {
			// A user-level message
			Message m = (Message) object;
			m.setOrigin(origins.get(connection)); // set the origin
			boolean authorized = true;
			if(m.topic().startsWith(Topics.CONTROLLED)) {
				// check permissions on controlled topics
				authorized = permitted(new Permission(PermissionType.SEND, m.topic()), connection);
			}
			if(!authorized) {
				log.trace("{} dropping unauthorized message from {} to {}", this, m.origin(), m.topic());
				return;
			}
			dispatch(m);
		}
		if(object instanceof Control) {
			// A command from the MqClient
			Control c = (Control) object;
			switch(c.command()) {
			case SUBSCRIBE: // subscribe to a topic
				if(
						!c.topic().startsWith(Topics.PRIVILEGED) // check perms on privileged topics 
						|| permitted(new Permission(PermissionType.SUBSCRIBE, c.topic()), connection)) {
					log.trace("{} subscribing {} to topic {}", this, connection, c.topic());
					boolean shouldFlush = subscriptions.get(c.topic()).size() == 0;
					subscriptions.add(c.topic(), connection);
					if(shouldFlush)
						flush(c.topic());
				} else {
					log.trace("{} not subscribing {} to privileged topic {}", this, connection, c.topic());
				}
				break;
			case UNSUBSCRIBE: // unsubscribe from a topic
				log.trace("{} unsubscribing {} from topic {}", this, connection, c.topic());
				subscriptions.remove(c.topic(), connection);
				break;
			case SET_ORIGIN: // set the MqClient origin topic
				if(permitted(new Permission(PermissionType.SET_ORIGIN), connection)) { // check perms
					log.trace("{} setting origin of {} to {}", this, connection, c.topic());
					origins.put(connection, c.topic());
				}
				break;
			case SET_QUEUE: // start queueing for a topic
				if(permitted(new Permission(PermissionType.QUEUE, c.topic()), connection)) { // check perms
					log.trace("{} setting queue {}", this, c.topic());
					if(!queues.containsKey(c.topic()))
						queues.put(c.topic(), new MessageQueue());
				}
				break;
			case UNSET_QUEUE: // stop queueing a topic
				if(permitted(new Permission(PermissionType.QUEUE, c.topic()), connection)) { // check perms
					log.trace("{} unsetting queue {}", this, c.topic());
					queues.remove(c.topic());
				}
				break;
			}
		}
	}
}

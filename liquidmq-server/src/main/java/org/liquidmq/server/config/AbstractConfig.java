package org.liquidmq.server.config;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public abstract class AbstractConfig implements Converter {
	
	protected XStream xstream;
	protected Aliasing aliases = new Aliasing();
	
	public AbstractConfig(XStream xstream) {
		this.xstream = xstream;
	}
	
	protected void alias(String alias, Class<?> cls) {
		aliases.alias(alias, cls);
	}
	
	protected MarshalSupport marshalSupport(HierarchicalStreamWriter writer, MarshallingContext context) {
		return new MarshalSupport(writer, context);
	}
	
	protected UnmarshalSupport unmarshalSupport(Object thiz, HierarchicalStreamReader reader, UnmarshallingContext context) {
		return new UnmarshalSupport(thiz, reader, context);
	}
	
	protected void pushAliases(Aliasing aliases, DataHolder context) {
		AliasingContext parent = (AliasingContext) context.get(AliasingContext.class);
		AliasingContext current = new AliasingContext(aliases, parent, xstream.getMapper());
		context.put(AliasingContext.class, current);
	}
	
	protected AliasingContext peekAliases(DataHolder context) {
		return (AliasingContext) context.get(AliasingContext.class);
	}
	
	protected void popAliases(DataHolder context) {
		AliasingContext current = (AliasingContext) context.get(AliasingContext.class);
		AliasingContext parent = current.getParent();
		context.put(AliasingContext.class, parent);
	}
	
	protected abstract void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context);
	
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		pushAliases(aliases, context);
		marshalImpl(source, writer, context);
		popAliases(context);
	}
	
	protected abstract Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context);
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		pushAliases(aliases, context);
		Object ret = unmarshalImpl(reader, context);
		popAliases(context);
		return ret;
	}
	
	protected class MarshalSupport {
		protected HierarchicalStreamWriter writer;
		protected MarshallingContext context;
		
		private MarshalSupport(HierarchicalStreamWriter writer, MarshallingContext context) {
			this.writer = writer;
			this.context = context;
		}
		
		public <T> void writeObject(Class<T> clazz, String name, T value) {
			if(value == null)
				return;
			writer.startNode(name);
			writeObjectInto(clazz, value);
			writer.endNode();
		}
		
		public <T> void writeObjectInto(Class<T> clazz, T value) {
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.startNode(peekAliases(context).serializedClass(value.getClass()));
			context.convertAnother(value);
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.endNode();
		}
		
		public <T> void writeArray(Class<T> clazz, String name, T... array) {
			if(array == null)
				return;
			writer.startNode(name);
			writeArrayInto(clazz, array);
			writer.endNode();
		}
		
		public <T> void writeArrayInto(Class<T> clazz, T... array) {
			for(T value : array) {
				if(value == null) {
					writer.startNode("null");
					writer.endNode();
					continue;
				}
				writer.startNode(peekAliases(context).serializedClass(value.getClass()));
				context.convertAnother(value);
				writer.endNode();
			}
		}
	}
	
	protected class UnmarshalSupport {
		protected Object thiz;
		protected HierarchicalStreamReader reader;
		protected UnmarshallingContext context;
		
		private UnmarshalSupport(Object thiz, HierarchicalStreamReader reader, UnmarshallingContext context) {
			this.reader = reader;
			this.context = context;
		}
		
		public <T> T readObject(Class<T> clazz) {
			if(Modifier.isFinal(clazz.getModifiers()))
				return (T) context.convertAnother(thiz, clazz);
			reader.moveDown();
			Class<? extends T> actual = (Class) peekAliases(context).realClass(reader.getNodeName());
			T ret = (T) context.convertAnother(thiz, actual);
			reader.moveUp();
			return ret;
		}
		
		public <T> T[] readArray(Class<T> clazz) {
			List<T> array = new ArrayList<T>();
			while(reader.hasMoreChildren()) {
				reader.moveDown();
				if("null".equals(reader.getNodeName())) {
					array.add(null);
				} else {
					Class<? extends T> actual = (Class) peekAliases(context).realClass(reader.getNodeName());
					array.add((T) context.convertAnother(thiz, actual));
				}
				reader.moveUp();
			}
			return array.toArray((T[]) Array.newInstance(clazz, 0));
		}
	}

	protected static class Aliasing {
		private Map<Class<?>, String> serialized = new HashMap<Class<?>, String>();
		private Map<String, Class<?>> real = new HashMap<String, Class<?>>();
		
		public Aliasing() {}
		
		public void alias(String alias, Class<?> cls) {
			real.remove(serialized.remove(cls));
			serialized.put(cls, alias);
			real.put(alias, cls);
		}
		
		private boolean has(Class<?> cls) {
			return serialized.containsKey(cls);
		}
		
		private boolean has(String elementName) {
			return real.containsKey(elementName);
		}
		
		private String serializedClass(Class<?> cls) {
			return serialized.get(cls);
		}
		
		private Class<?> realClass(String elementName) {
			return real.get(elementName);
		}
	}
	
	private static class AliasingContext {
		private AliasingContext parent;
		private Mapper mapper;
		private Aliasing alias;
		
		public AliasingContext(Aliasing alias, AliasingContext parent, Mapper mapper) {
			this.alias = alias;
			this.parent = parent;
			this.mapper = mapper;
		}
		
		public String serializedClass(Class<?> cls) {
			if(alias.has(cls))
				return alias.serializedClass(cls);
			if(parent != null)
				return parent.serializedClass(cls);
			return mapper.serializedClass(cls);
		}
		
		public Class<?> realClass(String elementName) {
			if(alias.has(elementName))
				return alias.realClass(elementName);
			if(parent != null)
				return parent.realClass(elementName);
			return mapper.realClass(elementName);
		}
		
		public AliasingContext getParent() {
			return parent;
		}
	}
}

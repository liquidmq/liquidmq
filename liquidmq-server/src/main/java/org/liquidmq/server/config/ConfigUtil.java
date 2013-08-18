package org.liquidmq.server.config;

import java.lang.reflect.Modifier;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConfigUtil {
	protected XStream xstream;
	
	public ConfigUtil(XStream xstream) {
		this.xstream = xstream;
	}
	
	public class MarshalSupport {
		protected HierarchicalStreamWriter writer;
		protected MarshallingContext context;
		
		public MarshalSupport(HierarchicalStreamWriter writer, MarshallingContext context) {
			this.writer = writer;
			this.context = context;
		}
		
		public <T> void writeObject(Class<T> clazz, String name, T value) {
			if(value == null)
				return;
			writer.startNode(name);
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.startNode(xstream.getMapper().serializedClass(clazz));
			context.convertAnother(value);
			if(!Modifier.isFinal(clazz.getModifiers()))
				writer.endNode();
			writer.endNode();
		}
	}
	
	public class UnmarshalSupport {
		protected Object thiz;
		protected HierarchicalStreamReader reader;
		protected UnmarshallingContext context;
		
		public UnmarshalSupport(Object thiz, HierarchicalStreamReader reader, UnmarshallingContext context) {
			this.reader = reader;
			this.context = context;
		}
		
		public <T> T readObject(Class<T> clazz) {
			if(Modifier.isFinal(clazz.getModifiers()))
				return (T) context.convertAnother(thiz, clazz);
			reader.moveDown();
			Class<? extends T> actual = xstream.getMapper().realClass(reader.getNodeName());
			T ret = (T) context.convertAnother(thiz, actual);
			reader.moveUp();
			return ret;
		}
	}
}

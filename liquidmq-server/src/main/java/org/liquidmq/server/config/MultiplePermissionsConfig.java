package org.liquidmq.server.config;

import java.util.Arrays;

import org.liquidmq.PermissionVerifier;
import org.liquidmq.pv.MultiplePermissions;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class MultiplePermissionsConfig extends AbstractConfig {
	public MultiplePermissionsConfig(XStream x) {
		super(x);
	}

	public boolean canConvert(Class type) {
		return MultiplePermissions.class.isAssignableFrom(type);
	}

	public void marshalImpl(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		MultiplePermissions mp = (MultiplePermissions) source;
		MarshalSupport ms = marshalSupport(writer, context);

		ms.writeArrayInto(PermissionVerifier.class, mp.toArray(new PermissionVerifier[0]));
	}

	public Object unmarshalImpl(HierarchicalStreamReader reader, UnmarshallingContext context) {
		MultiplePermissions mp = new MultiplePermissions();
		UnmarshalSupport us = unmarshalSupport(mp, reader, context);
		
		PermissionVerifier[] pvs = us.readArray(PermissionVerifier.class);
		mp.addAll(Arrays.asList(pvs));
		
		return mp;
	}
}

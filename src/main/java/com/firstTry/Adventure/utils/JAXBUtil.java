package com.firstTry.Adventure.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JAXBUtil<E> {
	private static final Logger log = LoggerFactory.getLogger(JAXBUtil.class);

	private JAXBContext jaxbContext = null;
	private Class<E> entity;
	private String entitySimpleName = "";

	@SuppressWarnings({ "unchecked" })
	public JAXBUtil() {
		log.debug("initializing JAXBContext.");
		Type genType = getClass().getGenericSuperclass();
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		this.entity = ((Class<E>) params[0]);
		this.entitySimpleName = this.entity.getSimpleName();
		try {
			this.jaxbContext = JAXBContext.newInstance(new Class[] { this.entity });
			log.debug("initialize successful.");
		} catch (JAXBException e) {
			log.error("initialize failed.", e);
			e.printStackTrace();
		}
	}

	public String bean2Xml(E clazz, boolean isFormat, String encoding) {
		log.debug(String.format("converting %s instance to xml.", new Object[] { this.entitySimpleName }));
		String xmlResult = "";
		try {
			Marshaller jaxbMarshaller = this.jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(isFormat));
			if (encoding != null) {
				jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
			}
			Writer writer = new StringWriter();
			jaxbMarshaller.marshal(clazz, writer);
			xmlResult = writer.toString();
			writer.flush();
			writer.close();
			log.debug("convert successful.");
		} catch (PropertyException e) {
			log.error("convert failed.", e);
			e.printStackTrace();
		} catch (JAXBException e) {
			log.error("convert failed.", e);
			e.printStackTrace();
		} catch (IOException e) {
			log.error("convert failed.", e);
			e.printStackTrace();
		}
		return xmlResult;
	}

	public String bean2Xml(E clazz, boolean isFormat) {
		return bean2Xml(clazz, isFormat, null);
	}

	public E xml2Bean(String xml) {
		return xml2Bean(xml, "UTF-8");
	}

	@SuppressWarnings("unchecked")
	public E xml2Bean(String xml, String encode) {
		if (xml == null) {
			return null;
		}
		log.debug(String.format("converting xml to %s instance.", new Object[] { this.entitySimpleName }));
		E entity = null;
		try {
			Unmarshaller um = this.jaxbContext.createUnmarshaller();
			entity = (E) um.unmarshal(new ByteArrayInputStream(xml.getBytes(encode)));
			log.debug("convert successful.");
		} catch (PropertyException e) {
			log.error("convert failed.", e);
			e.printStackTrace();
		} catch (JAXBException e) {
			log.error("convert failed.", e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return entity;
	}
}

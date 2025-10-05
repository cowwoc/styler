package io.github.cowwoc.styler.formatter.api.test;

import io.github.cowwoc.styler.formatter.api.report.JsonViolationSerializer;
import io.github.cowwoc.styler.formatter.api.report.SerializationFormat;
import io.github.cowwoc.styler.formatter.api.report.SerializerFactory;
import io.github.cowwoc.styler.formatter.api.report.ViolationSerializer;
import io.github.cowwoc.styler.formatter.api.report.XmlViolationSerializer;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SerializerFactory} serializer creation.
 */
public final class SerializerFactoryTest
{
	/**
	 * Verifies JSON format returns JsonViolationSerializer.
	 */
	@Test
	public void createWithJsonFormatReturnsJsonSerializer()
	{
		ViolationSerializer serializer = SerializerFactory.create(SerializationFormat.JSON);

		assertThat(serializer).isInstanceOf(JsonViolationSerializer.class);
	}

	/**
	 * Verifies XML format returns XmlViolationSerializer.
	 */
	@Test
	public void createWithXmlFormatReturnsXmlSerializer()
	{
		ViolationSerializer serializer = SerializerFactory.create(SerializationFormat.XML);

		assertThat(serializer).isInstanceOf(XmlViolationSerializer.class);
	}

	/**
	 * Verifies null format throws NullPointerException.
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void createWithNullFormatThrows()
	{
		SerializerFactory.create(null);
	}

	/**
	 * Verifies factory returns same instance on repeated calls (singleton pattern).
	 */
	@Test
	public void createReturnsSingletonInstances()
	{
		ViolationSerializer json1 = SerializerFactory.create(SerializationFormat.JSON);
		ViolationSerializer json2 = SerializerFactory.create(SerializationFormat.JSON);
		ViolationSerializer xml1 = SerializerFactory.create(SerializationFormat.XML);
		ViolationSerializer xml2 = SerializerFactory.create(SerializationFormat.XML);

		assertThat(json1).isSameAs(json2);
		assertThat(xml1).isSameAs(xml2);
		assertThat(json1).isNotSameAs(xml1);
	}
}

/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.petra.apache.http.components;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Hugo Huijser
 */
public class URIBuilder {

	public static URIBuilderBuilder create(String string)
		throws URISyntaxException {

		return new URIBuilderBuilder(new URI(string));
	}

	public static URIBuilderBuilder create(URI uri) {
		return new URIBuilderBuilder(uri);
	}

	public static final class URIBuilderBuilder {

		public URIBuilderBuilder(URI uri) {
			_uriBuilder = new org.apache.http.client.utils.URIBuilder(uri);
		}

		public URIBuilderBuilder addParameter(String name, String value) {
			if (value != null) {
				_uriBuilder.addParameter(name, value);
			}

			return this;
		}

		public URIBuilderBuilder addParameter(
			String name,
			UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

			if (valueUnsafeSupplier == null) {
				return this;
			}

			try {
				String value = valueUnsafeSupplier.get();

				if (value != null) {
					_uriBuilder.addParameter(name, value);
				}

				return this;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		public URIBuilderBuilder addParameter(
			UnsafeSupplier<String, Exception> nameUnsafeSupplier,
			String value) {

			try {
				String name = nameUnsafeSupplier.get();

				if (name != null) {
					_uriBuilder.addParameter(name, value);
				}

				return this;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		public URIBuilderBuilder addParameter(
			UnsafeSupplier<String, Exception> nameUnsafeSupplier,
			UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

			try {
				if ((nameUnsafeSupplier != null) &&
					(valueUnsafeSupplier != null)) {

					String name = nameUnsafeSupplier.get();
					String value = valueUnsafeSupplier.get();

					if ((name != null) && (value != null)) {
						_uriBuilder.addParameter(name, value);
					}
				}

				return this;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		public URI build() throws URISyntaxException {
			return _uriBuilder.build();
		}

		private final org.apache.http.client.utils.URIBuilder _uriBuilder;

	}

	@FunctionalInterface
	public interface UnsafeSupplier<String, E extends Throwable> {

		public String get() throws E;

	}

}
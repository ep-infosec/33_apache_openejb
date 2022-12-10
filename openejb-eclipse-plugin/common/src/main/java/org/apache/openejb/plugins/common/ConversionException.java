/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.plugins.common;

/**
 * Represents a conversion error from trying to generate annotations
 * using the OpenEjbXmlConverter
 */
public class ConversionException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4968483527572451177L;

	public ConversionException() {
	}

	/**
	 * @param message
	 */
	public ConversionException(String message) {
		super(message);
	}

	/**
	 * @param throwable
	 */
	public ConversionException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param message
	 * @param throwable
	 */
	public ConversionException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

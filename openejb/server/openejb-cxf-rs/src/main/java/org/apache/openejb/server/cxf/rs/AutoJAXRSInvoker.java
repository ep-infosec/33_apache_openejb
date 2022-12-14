/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.JAXRSInvoker;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.openejb.BeanType;
import org.apache.openejb.server.rest.EJBRestServiceInfo;

import java.util.Map;

public class AutoJAXRSInvoker implements Invoker {
    private final Map<String, EJBRestServiceInfo> ejbs;
    private final OpenEJBEJBInvoker ejbInvoker;
    private final JAXRSInvoker jaxrsInvoker;

    public AutoJAXRSInvoker(final Map<String, EJBRestServiceInfo> restEjbs) {
        ejbs = restEjbs;

        // delegates
        jaxrsInvoker = new JAXRSInvoker();
        if (!ejbs.isEmpty()) {
            ejbInvoker = new OpenEJBEJBInvoker();
        } else {
            ejbInvoker = null; // no need
        }
    }

    @Override
    public Object invoke(Exchange exchange, Object o) { // mainly a select the right invoker impl
        final ClassResourceInfo cri = (ClassResourceInfo) exchange.get("root.resource.class");

        if (cri != null
                && ejbs.containsKey(cri.getServiceClass().getName())
                && !BeanType.MANAGED.equals(ejbs.get(cri.getServiceClass().getName()).context.getComponentType())) {
            return ejbInvoker.invoke(exchange, o);
        }

        return jaxrsInvoker.invoke(exchange, o);
    }
}

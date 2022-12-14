/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tomee.webapp.command.impl;

import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.tomee.webapp.command.Command;
import org.apache.tomee.webapp.command.IsProtected;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IsProtected
public class GetJndi implements Command {

    @Override
    public Object execute(final Map<String, Object> params) throws Exception {
        final String sessionId = (String) params.get("sessionId");
        final List<String> jndi = new ArrayList<String>();

        ContainerSystem container = SystemInstance.get().getComponent(ContainerSystem.class);
        BeanContext[] deployments = container.deployments();
        if (deployments != null) {
            for (BeanContext beanContext : deployments) {
                jndi.add(String.valueOf(beanContext.getDeploymentID()));
            }
        }

        final Map<String, Object> json = new HashMap<String, Object>();
        json.put("jndi", jndi);
        return json;
    }
}

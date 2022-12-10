/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

/**
 * Not in schema
 * @version $Revision$ $Date$
 */
public class StatelessBean extends SessionBean {
    public StatelessBean(String ejbName, String ejbClass) {
        super(ejbName, ejbClass, SessionType.STATELESS);
    }

    public StatelessBean(Class<?> ejbClass) {
        this(ejbClass.getSimpleName(), ejbClass.getName());
    }

    public StatelessBean(String name, Class<?> ejbClass) {
        this(name, ejbClass.getName());
    }

    public StatelessBean() {
        this(null, (String)null);
    }

    public void setSessionType(SessionType value) {
    }

}

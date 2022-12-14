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

package org.apache.tomee.webapp.servlet;

import org.apache.tomee.webapp.Application;
import org.apache.tomee.webapp.JsonExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


public class SessionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        writeJson(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        writeJson(req, resp);
    }

    private void writeJson(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        JsonExecutor.execute(req, resp, new JsonExecutor.Executor() {
            @Override
            public void call(Map<String, Object> json) throws Exception {
                String sessionId = req.getParameter("sessionId");
                if (sessionId == null || "".equals(sessionId.trim())) {
                    sessionId = req.getSession().getId();
                    Application.getInstance().getSession(sessionId);
                } else {
                    final Application.Session session = Application.getInstance().getExistingSession(sessionId);
                    if (session == null) {
                        sessionId = req.getSession().getId();
                        Application.getInstance().getSession(sessionId);
                    }
                }
                final Application.Session session = Application.getInstance().getSession(sessionId);
                json.put("userName", session.getUserName());
                json.put("sessionId", sessionId);
            }
        });
    }
}

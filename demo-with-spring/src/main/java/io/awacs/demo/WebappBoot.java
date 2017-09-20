/**
 * Copyright 2016-2017 AWACS Project.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.awacs.demo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Created by pixyonly on 16/10/2.
 */
public class WebappBoot {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.setInitParameter("contextConfigLocation", "classpath*:spring-mvc.xml");
        ServletHolder servletHolder = new ServletHolder();
        servletHolder.setInitOrder(1);
        servletHolder.setHeldClass(DispatcherServlet.class);
        servletHolder.setInitParameter("contextConfigLocation", "classpath*:spring-mvc.xml");
        handler.addServlet(servletHolder, "/");
        server.setHandler(handler);
        server.start();
        server.join();
    }
}

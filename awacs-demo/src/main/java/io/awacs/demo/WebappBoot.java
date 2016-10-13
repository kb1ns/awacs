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

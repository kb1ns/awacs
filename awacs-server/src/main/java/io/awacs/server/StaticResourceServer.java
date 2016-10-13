/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.awacs.server;

import com.google.common.collect.ImmutableMap;
import io.awacs.core.Configurable;
import io.awacs.core.Configuration;
import io.awacs.core.InitializationException;
import io.awacs.core.Plugins;
import io.awacs.core.transport.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * Created by pixyonly on 8/24/16.
 */
public final class StaticResourceServer implements Server, Configurable {

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    private String host;

    private int port;

    @Override
    public void setPlugins(Plugins plugins) {
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(65536));
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        ch.pipeline().addLast(new StaticResourceHandler(StaticResourceServer.this));

                    }
                });
        try {
            bootstrap.bind(host, port).sync();
        } catch (InterruptedException e) {
            stop();
        }
    }

    @Override
    public void stop() {
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }

    @Override
    public void init(Configuration configuration) throws InitializationException {
        String serverName = configuration.getString(Configurations.SERVER_PREFIX);
        ImmutableMap<String, String> serverConfig = configuration.getSubProperties(Configurations.SERVER_PREFIX + "." + serverName + ".");
        host = serverConfig.getOrDefault(Configurations.HTTP_BIND_HOST, Configurations.DEFAULT_HTTP_BIND_HOST);
        port = Integer.parseInt(serverConfig.getOrDefault(Configurations.HTTP_BIND_PORT, Configurations.DEFAULT_HTTP_BIND_PORT));
        int bossCore = Integer.parseInt(serverConfig.getOrDefault(Configurations.HTTP_BOSS_CORE, Configurations.DEFAULT_HTTP_BOSS_CORE));
        int workerCore = Integer.parseInt(serverConfig.getOrDefault(Configurations.HTTP_WORKER_CORE, Configurations.DEFAULT_HTTP_WORKER_CORE));
        boss = new NioEventLoopGroup(bossCore);
        worker = new NioEventLoopGroup(workerCore);
    }

    /**
     * Created by pixyonly on 8/24/16.
     */
    public static class StaticResourceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
        public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
        public static final int HTTP_CACHE_SECONDS = 60;

        private StaticResourceServer owner;

        StaticResourceHandler(StaticResourceServer owner) {
            this.owner = owner;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
                throws Exception {
            if (!request.getDecoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST);
                return;
            }

            if (request.getMethod() != GET) {
                sendError(ctx, METHOD_NOT_ALLOWED);
                return;
            }

            final String uri = request.getUri();
            String path = sanitizeUri(uri);

            if (path == null) {
                sendError(ctx, FORBIDDEN);
                return;
            }

            if (isNavigation(uri)) {
                path = path + "/config.json";
            }

            File file = new File(path);
            if (file.isHidden() || !file.exists()) {
                sendError(ctx, NOT_FOUND);
                return;
            }

            if (file.isDirectory()) {
                if (uri.endsWith("/")) {
                    sendListing(ctx, file);
                } else {
                    sendRedirect(ctx, uri + '/');
                }
                return;
            }

            if (!file.isFile()) {
                sendError(ctx, FORBIDDEN);
                return;
            }

            // Cache Validation
            String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

                // Only compare up to the second because the datetime format we send to the client
                // does not have milliseconds
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    sendNotModified(ctx);
                    return;
                }
            }

            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException ignore) {
                sendError(ctx, NOT_FOUND);
                return;
            }
            long fileLength = raf.length();

            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            HttpHeaders.setContentLength(response, fileLength);
            setContentTypeHeader(response, file);
            setDateAndCacheHeaders(response, file);
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }

            // Write the initial line and the header.
            ctx.write(response);

            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;
            if (ctx.pipeline().get(SslHandler.class) == null) {
                sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength),
                        ctx.newProgressivePromise());
                // Write the end marker.
                lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            } else {
                sendFileFuture =
                        ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)),
                                ctx.newProgressivePromise());
                // HttpChunkedInput will write the end marker (LastHttpContent) for us.
                lastContentFuture = sendFileFuture;
            }

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress,
                                                long total) {
                    if (total < 0) { // total unknown
                        System.err.println(future.channel() + " Transfer progress: " + progress);
                    } else {
                        System.err.println(
                                future.channel() + " Transfer progress: " + progress + " / " + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {
                    System.err.println(future.channel() + " Transfer complete.");
                }
            });

            // Decide whether to close the connection or not.
            if (!HttpHeaders.isKeepAlive(request)) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            if (ctx.channel().isActive()) {
                sendError(ctx, INTERNAL_SERVER_ERROR);
            }
        }

        private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

        private boolean isNavigation(String uri) {
            char[] chars = uri.toCharArray();
            char cc = chars[0];
            for (char c : chars) {
                cc ^= c;
            }
            return cc == 0;
        }

        private static String sanitizeUri(String uri) {
            // Decode the path.
            try {
                uri = URLDecoder.decode(uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }

            if (uri.isEmpty() || uri.charAt(0) != '/') {
                return null;
            }

            // Convert file separators.
            uri = uri.replace('/', File.separatorChar);

            // Simplistic dumb security check.
            // You will have to do something serious in the production environment.
            if (uri.contains(File.separator + '.') ||
                    uri.contains('.' + File.separator) ||
                    uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                    INSECURE_URI.matcher(uri).matches()) {
                return null;
            }
            // Convert to absolute path.
            return Configurations.getPluginPath() + uri;
        }

        private static final Pattern ALLOWED_FILE_NAME =
                Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");

        private static void sendListing(ChannelHandlerContext ctx, File dir) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");

            String dirPath = dir.getPath();
            StringBuilder buf =
                    new StringBuilder().append("<!DOCTYPE html>\r\n").append("<html><head><title>")
                            .append("Listing of: ").append(dirPath).append("</title></head><body>\r\n")

                            .append("<h3>Listing of: ").append(dirPath).append("</h3>\r\n")

                            .append("<ul>").append("<li><a href=\"../\">..</a></li>\r\n");

            for (File f : dir.listFiles()) {
                if (f.isHidden() || !f.canRead()) {
                    continue;
                }

                String name = f.getName();
                if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                    continue;
                }

                buf.append("<li><a href=\"").append(name).append("\">").append(name)
                        .append("</a></li>\r\n");
            }

            buf.append("</ul></body></html>\r\n");
            ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
            response.content().writeBytes(buffer);
            buffer.release();

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
            response.headers().set(LOCATION, newUri);

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status,
                    Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
            response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        /**
         * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
         *
         * @param ctx Context
         */
        private static void sendNotModified(ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);
            setDateHeader(response);

            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        /**
         * Sets the Date header for the HTTP response
         *
         * @param response HTTP response
         */
        private static void setDateHeader(FullHttpResponse response) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

            Calendar time = new GregorianCalendar();
            response.headers().set(DATE, dateFormatter.format(time.getTime()));
        }

        /**
         * Sets the Date and Cache headers for the HTTP Response
         *
         * @param response    HTTP response
         * @param fileToCache file to extract content type
         */
        private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

            // Date header
            Calendar time = new GregorianCalendar();
            response.headers().set(DATE, dateFormatter.format(time.getTime()));

            // Add cache headers
            time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
            response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));
            response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
            response.headers()
                    .set(LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
        }

        /**
         * Sets the content type header for the HTTP Response
         *
         * @param response HTTP response
         * @param file     file to extract content type
         */
        private static void setContentTypeHeader(HttpResponse response, File file) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
        }
    }
}

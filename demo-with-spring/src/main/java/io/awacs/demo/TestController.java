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

import com.alibaba.fastjson.JSONObject;
import com.github.cage.Cage;
import com.github.cage.GCage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Random;


/**
 * Created by pixyonly on 7/18/16.
 */
@RestController
@RequestMapping(value = "/v1")
public class TestController {

    private Random random = new Random();

    private static final Cage cage = new GCage();

    @RequestMapping(value = "/test/{id}", produces = "application/json")
    public String test(@PathVariable("id") String id) {
        Random r1 = new Random();
        if (id.compareTo("hello") >= 0) {
            Random r = new Random();
            r.nextBoolean();
            throw new RuntimeException("test for throw exception");
        }
        r1.nextBoolean();
        sleep2();
        sleep2();
        return new JSONObject().fluentPut("hello", sleep1(id, 1, 1l, 0.5f, 0.23d, (short) 2, (byte) 0x01, true, new Object(), new String[]{"1", "2"})).toJSONString();
    }

    @RequestMapping(value = "/hello")
    public String hello(String s) {
        try {
            Random r = new Random();
            if (r.nextInt() % 2 == 0) {
                return "hello, world1";
            }
            throw new RuntimeException();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "/hello1")
    public String hello1(String s) {
        try {
            Random r = new Random();
            if (r.nextInt() % 2 == 0)
                return "hello, world";
            throw new RuntimeException();
        } catch (Exception e) {
            return "exception!";
        }
    }


    @RequestMapping("/img")
    public void img(HttpServletResponse response) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int r = random.nextInt(36);
                sleep2();
                if (r < 10) {
                    sb.append((char) ('0' + r));
                } else {
                    sb.append((char) ('A' + r - 10));
                }
            }
            BufferedImage img = cage.drawImage(sb.toString());
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            ServletOutputStream responseOutputStream = response.getOutputStream();
            ImageIO.write(img, "JPEG", responseOutputStream);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sleep1(String id,
                         int code,
                         long l,
                         float f,
                         double d,
                         short st,
                         byte b,
                         boolean bl,
                         Object o,
                         String[] codes) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sleep2();
        return "test " + id;
    }

    public void sleep2() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

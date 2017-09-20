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
import javax.servlet.http.HttpServletRequest;
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
    public String test2(@PathVariable("id") String id) {
        if (id.compareTo("hello") >= 0)
            throw new RuntimeException("test for throw exception");
        return new JSONObject().fluentPut("hello", bis1(id)).toJSONString();
    }

    @RequestMapping("/img")
    public void img(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                int r = random.nextInt(36);
                bis2();
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

    public String bis1(String id) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bis2();
        return "test " + id;
    }

    public void bis2() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

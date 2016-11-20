/**
 * Copyright 2016 AWACS Project.
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by pixyonly on 7/18/16.
 */
@RestController
@RequestMapping(value = "/v1", produces = "application/json")
public class TestController {

    @RequestMapping("/test/{id}")
    public String test2(@PathVariable("id") String id) {
        if(id.compareTo("hello") >= 0)
            throw new RuntimeException("test for throw excpetion");
        return new JSONObject().fluentPut("hello", bis1(id)).toJSONString();
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

    public void bis2(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

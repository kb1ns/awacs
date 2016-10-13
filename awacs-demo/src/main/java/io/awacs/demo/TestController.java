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

package io.awacs.demo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by pixyonly on 7/18/16.
 */
@RestController
@RequestMapping(value = "/v1", produces = "application/json")
public class TestController {

    @RequestMapping(value = "/test1")
    public String test1(@RequestParam(value = "name") String name) throws Exception {
        return new JSONObject().fluentPut("hello", name).toJSONString();
    }

    @RequestMapping("/test2/{id}")
    public String test2(@PathVariable("id") String id) {
        throw new RuntimeException("Test on RuntimeException");
    }
}

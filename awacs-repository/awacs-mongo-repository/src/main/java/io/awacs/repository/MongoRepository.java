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

package io.awacs.repository;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSecurityException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.awacs.common.Configuration;
import io.awacs.common.InitializationException;
import io.awacs.common.Packet;
import io.awacs.common.Repository;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by pixyonly on 16/9/27.
 */
public class MongoRepository implements Repository {

    private static final String ADDRESS = "addresses";
    private static final String CREDENTIAL = "credentials";
    private static final String DB = "db";
    private static final String DEFAULT_ADDRESS = "localhost:27017";
    private static final String DEFAULT_CREDENTIAL = "";
    private static final String DEFAULT_DB = "awacs";
    private static final String IMMEDIATE_FLUSH = "immediateFlush";
    private static final boolean DEFAULT_IMMEDIATE_FLUSH = true;
    private MongoConnection mongoConnection;

    @Override
    public void init(Configuration configuration) throws InitializationException {
        try {
            String[] addrs = configuration.getString(ADDRESS, DEFAULT_ADDRESS).split(",");
            List<ServerAddress> addresses = new LinkedList<>();
            for (String addr : addrs) {
                String[] hostport = addr.split(":");
                addresses.add(new ServerAddress(hostport[0], Integer.valueOf(hostport[1])));
            }
            String db = configuration.getString(DB, DEFAULT_DB);
            List<MongoCredential> credentials = new LinkedList<>();
            String[] creds = configuration.getString(CREDENTIAL, DEFAULT_CREDENTIAL).split(",");
            if (!creds[0].equals("")) {
                for (String credential : creds) {
                    int _1split = credential.indexOf(':');
                    int _2split = credential.indexOf('@');
                    if (_1split == -1 || _2split == -1) {
                        throw new MongoSecurityException(null, credential);
                    }
                    String username = credential.substring(0, _1split);
                    String password = credential.substring(_1split + 1, _2split);
                    String authDb = credential.substring(_2split + 1);
                    credentials.add(MongoCredential.createCredential(username, authDb, password.toCharArray()));
                }
            }
            mongoConnection = new MongoConnection(addresses, credentials, db);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitializationException();
        }
    }

    @Override
    public Packet confirm(Packet recieve, InetSocketAddress remote) {
//        ThreadPoolHelper.instance.submit(new Runnable() {
//            @Override
//            public void run() {
//                mongoConnection.getInstance(collection).insertOne(doc);
//            }
//        });
        return null;
    }

    private static class MongoConnection {

        private MongoDatabase conn;

        private Map<String, MongoCollection<Document>> collections = new HashMap<>();

        private MongoConnection(List<ServerAddress> addrs, List<MongoCredential> credentials, String db) {
            conn = new MongoClient(addrs, credentials).getDatabase(db);
        }

        public MongoCollection<Document> getInstance(String collection) {
            if (!collections.containsKey(collection))
                collections.putIfAbsent(collection, conn.getCollection(collection));
            return collections.get(collection);
        }
    }
}

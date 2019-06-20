/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.oscerd.camel.infinispan.kafka.demo;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;
import org.apache.camel.support.SimpleRegistry;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;

public class Application {

    private Main main;
    
    public static void main(String[] args) throws Exception {
        Application example = new Application();
        example.boot();
    }
    
    public void boot() throws Exception {
        // create a Main instance
        main = new Main();
    	main.bind("cacheContainer", cacheContainer());
    	main.bind("queryBuilder", new InfinispanKafkaQueryBuilder());
        main.addRouteBuilder(new CamelInfinispanRoute());
        System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
        main.run();
    }
    
    private static RemoteCacheManager cacheContainer() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host("localhost")
              .port(11222);
        boolean useProto = true;
        if (useProto) {
           builder.marshaller(new ProtoStreamMarshaller());
        }
        builder.forceReturnValues(true);
        
        RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());
        if (useProto) {
            SerializationContext serCtx = ProtoStreamMarshaller.getSerializationContext(cacheManager);
            ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
            Class<?> marshaller = Author.class;
            String memoSchemaFile = null;
            try {
               memoSchemaFile = protoSchemaBuilder.fileName("file.proto").packageName("test").addClass(marshaller)
                     .build(serCtx);
            } catch (Exception e) {
               e.printStackTrace();
            }

            RemoteCache<String, String> metadataCache = cacheManager
                  .getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
            metadataCache.put("file.proto", memoSchemaFile);
         }
        return cacheManager;
    	
    }
}

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

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.infinispan.InfinispanConstants;
import org.apache.camel.component.infinispan.InfinispanOperation;

public class CamelInfinispanRoute extends RouteBuilder {


    @Override
    public void configure() throws Exception {
    	from("timer://foo?period=10000&repeatCount=0").setHeader(InfinispanConstants.OPERATION).constant(InfinispanOperation.QUERY).to("infinispan://default?cacheContainer=#cacheContainer&queryBuilder=#queryBuilder").process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
                
		        List<Author> queryResult = exchange.getIn().getBody(List.class);
                if (queryResult == null) {
                	log.info("No query Result");
                } else {
                	log.info("Query Result size " + queryResult.size());
                	log.info("Query Result content " + queryResult.toString());
                }
				
			}
		});

    }

}

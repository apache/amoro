/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server;

import com.netease.arctic.ams.server.repair.TestCommandParser;
import com.netease.arctic.ams.server.repair.TestGetRepairConfig;
import com.netease.arctic.ams.server.repair.command.TestAnalyzeCall;
import com.netease.arctic.ams.server.repair.command.TestOptimizeCall;
import com.netease.arctic.ams.server.repair.command.TestRepairCall;
import com.netease.arctic.ams.server.repair.command.TestShowCall;
import com.netease.arctic.ams.server.repair.command.TestTableCall;
import com.netease.arctic.ams.server.repair.command.TestUseCall;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestHighAvailabilityServices.class,
    TestGetRepairConfig.class,
    TestCommandParser.class,
    TestAnalyzeCall.class,
    TestOptimizeCall.class,
    TestRepairCall.class,
    TestShowCall.class,
    TestTableCall.class,
    TestUseCall.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
                  "com.amazonaws.http.conn.ssl.*",
                  "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*",
                  "javax.xml.parsers.*"})
public class OutAmsTestBase {

}

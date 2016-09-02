/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.exec.tez;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.llap.registry.ServiceInstance;
import org.apache.hadoop.hive.llap.registry.impl.LlapRegistryService;
import org.apache.hadoop.mapred.split.SplitLocationProvider;
import org.slf4j.Logger;

public class Utils {
  public static SplitLocationProvider getSplitLocationProvider(Configuration conf, Logger LOG) throws
      IOException {
    boolean useCustomLocations =
        HiveConf.getBoolVar(conf, HiveConf.ConfVars.LLAP_CLIENT_CONSISTENT_SPLITS);
    SplitLocationProvider splitLocationProvider;
    LOG.info("SplitGenerator using llap affinitized locations: " + useCustomLocations);
    if (useCustomLocations) {
      LlapRegistryService serviceRegistry;
      serviceRegistry = LlapRegistryService.getClient(conf);

      Collection<ServiceInstance> serviceInstances =
          serviceRegistry.getInstances().getAllInstancesOrdered();
      String[] locations = new String[serviceInstances.size()];
      int i = 0;
      for (ServiceInstance serviceInstance : serviceInstances) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Adding " + serviceInstance.getWorkerIdentity() + " with hostname=" +
              serviceInstance.getHost() + " to list for split locations");
        }
        locations[i++] = serviceInstance.getHost();
      }
      splitLocationProvider = new HostAffinitySplitLocationProvider(locations);
    } else {
      splitLocationProvider = null;
    }
    return splitLocationProvider;
  }
}

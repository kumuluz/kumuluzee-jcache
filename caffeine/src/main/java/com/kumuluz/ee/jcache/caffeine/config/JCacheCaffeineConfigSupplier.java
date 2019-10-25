/*
 *  Copyright (c) 2014-2019 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT

 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.kumuluz.ee.jcache.caffeine.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @author cen1
 * @since 1.0.0
 */
public class JCacheCaffeineConfigSupplier implements Supplier<Config> {

    private static final Logger log = Logger.getLogger(JCacheCaffeineConfigSupplier.class.getName());

    private final static String CONFIG_PREFIX = "kumuluzee.jcache.caffeine";

    @Override
    public Config get() {
        Config defaultConfig = ConfigFactory.load();

        ConfigurationUtil confUtil = ConfigurationUtil.getInstance();

        if (confUtil.get(CONFIG_PREFIX).isPresent()) {

            log.info("Custom JCache-Caffeine config detected");

            String eeConfig = confUtil.get(CONFIG_PREFIX).get();
            Config customConfig = ConfigFactory.parseString(eeConfig);

            Config finalConfig = customConfig.withFallback(defaultConfig);
            log.info(finalConfig.root().render());

            return finalConfig;
        }

        log.info("No {} prefix found, resolving to default JCache-Caffeine configuration");
        log.info(defaultConfig.root().render());

        return defaultConfig;
    }
}

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author cen1
 * @since 1.0.0
 */
public class JCacheCaffeineConfigSupplier implements Supplier<Config> {

    private static final Logger log = Logger.getLogger(JCacheCaffeineConfigSupplier.class.getName());

    private final static String CONFIG_PREFIX = "kumuluzee.jcache.caffeine";

    private Config defaultConfig;
    private Config finalConfig;

    @Override
    public Config get() {

        if (finalConfig!=null) {
            return finalConfig;
        }

        boolean defaultInitialLoad = false;
        if (defaultConfig==null) {
            defaultConfig = ConfigFactory.load();
            defaultInitialLoad = true;
        }

        ConfigurationUtil confUtil = ConfigurationUtil.getInstance();

        Optional<Boolean> enabled = confUtil.getBoolean(CONFIG_PREFIX + ".enabled").or(() -> {
            Optional<List<String>> mapKeys = confUtil.getMapKeys(CONFIG_PREFIX);

            return Optional.of(mapKeys.isPresent() && !mapKeys.get().isEmpty());
        });
        if (enabled.isPresent() && enabled.get()) {

            log.info("JCache-Caffeine config detected in kumuluzee-config");

            Map<String, String> eeConfig = buildPropertiesMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey().replace(CONFIG_PREFIX + ".caches", "caffeine.jcache"),
                            Map.Entry::getValue
                    ));
            Config customConfig = ConfigFactory.parseMap(eeConfig);

            finalConfig = customConfig.withFallback(defaultConfig);
            log.fine(finalConfig.root().render());

            return finalConfig;
        }

        if (defaultInitialLoad) {
            log.warning("No " + CONFIG_PREFIX + " prefix found, resolving to default JCache-Caffeine configuration");
            log.fine(defaultConfig.root().render());
        }

        return defaultConfig;
    }

    private Map<String, String> buildPropertiesMap() {
        Map<String, String> properties = new HashMap<>();
        buildPropertiesMap(properties, CONFIG_PREFIX + ".caches");
        return properties;
    }

    private void buildPropertiesMap(Map<String, String> map, String prefix) {

        ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();

        Optional<List<String>> mapKeys = configurationUtil.getMapKeys(prefix);

        if (mapKeys.isPresent()) {
            String nextPrefix = (prefix.isEmpty()) ? "" : prefix + ".";
            for (String s : mapKeys.get()) {
                buildPropertiesMap(map, nextPrefix + s);
            }
        } else if (!prefix.isEmpty()) {
            Optional<Integer> listSize = configurationUtil.getListSize(prefix);

            if (listSize.isPresent()) {
                for (int i = 0; i < listSize.get(); i++) {
                    buildPropertiesMap(map, prefix + "[" + i + "]");
                }
            } else {
                Optional<String> value = configurationUtil.get(prefix);
                value.ifPresent(s -> map.put(prefix, s));
            }
        }
    }
}

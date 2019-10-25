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

package com.kumuluz.ee.jcache.caffeine.utils;

import com.github.benmanes.caffeine.jcache.configuration.TypesafeConfigurator;
import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeComponentDependency;
import com.kumuluz.ee.common.dependencies.EeComponentType;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.dependencies.EeExtensionGroup;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.jcache.caffeine.config.JCacheCaffeineConfigSupplier;

import java.util.logging.Logger;

/**
 * @author cen1
 * @since 1.0.0
 */
@EeExtensionDef(name = "JCache-Caffeine", group = EeExtensionGroup.CACHING)
@EeComponentDependency(EeComponentType.CDI)
public class CaffeineExtension implements Extension {

    private static final Logger log = Logger.getLogger(com.kumuluz.ee.jcache.caffeine.utils.CaffeineExtension.class.getName());

    @Override
    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
        log.info("Initialising KumuluzEE JCache-Caffeine extension.");
        TypesafeConfigurator.setConfigSource(new JCacheCaffeineConfigSupplier());
        log.info("Loaded JCache-Caffeine config");
    }

    @Override
    public void load() {
    }

    @Override
    public boolean isEnabled() {
        return isExtensionEnabled();
    }

    public static boolean isExtensionEnabled() {
        ConfigurationUtil config = ConfigurationUtil.getInstance();

        return config.getBoolean("kumuluzee.jcache.caffeine.enabled")
                .orElse(config.getBoolean("kumuluzee.jcache.enabled")
                        .orElse(true));
    }
}

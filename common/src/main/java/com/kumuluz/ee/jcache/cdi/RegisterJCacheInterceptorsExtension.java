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

package com.kumuluz.ee.jcache.cdi;

import org.jsr107.ri.annotations.cdi.CachePutInterceptor;
import org.jsr107.ri.annotations.cdi.CacheRemoveAllInterceptor;
import org.jsr107.ri.annotations.cdi.CacheRemoveEntryInterceptor;
import org.jsr107.ri.annotations.cdi.CacheResultInterceptor;
import org.tomitribe.jcache.cdi.CacheManagerBean;
import org.tomitribe.jcache.cdi.CacheProviderBean;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import java.util.logging.Logger;

/**
 * @author cen1
 * @since 1.0.0
 */
public class RegisterJCacheInterceptorsExtension implements Extension {

    private static final Logger log = Logger.getLogger(RegisterJCacheInterceptorsExtension.class.getName());

    private CacheManager cacheManager;
    private CachingProvider cachingProvider;

    /**
     * See https://docs.jboss.org/cdi/api/2.0/javax/enterprise/inject/spi/AfterTypeDiscovery.html
     * @param afterTypeDiscovery cdi event
     */
    public void observeAfterTypeDiscovery(@Observes AfterTypeDiscovery afterTypeDiscovery) {
        afterTypeDiscovery.getInterceptors().add(CacheResultInterceptor.class);
        afterTypeDiscovery.getInterceptors().add(CacheRemoveEntryInterceptor.class);
        afterTypeDiscovery.getInterceptors().add(CacheRemoveAllInterceptor.class);
        afterTypeDiscovery.getInterceptors().add(CachePutInterceptor.class);
    }

    /**
     * See https://docs.jboss.org/cdi/api/2.0/javax/enterprise/inject/spi/AfterBeanDiscovery.html
     * @param afterBeanDiscovery cdi event
     */
    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {

        cachingProvider = Caching.getCachingProvider();
        cacheManager = cachingProvider.getCacheManager();

        afterBeanDiscovery.addBean(new CacheManagerBean(cacheManager));
        log.info("Added CacheManager bean");
        afterBeanDiscovery.addBean(new CacheProviderBean(cachingProvider));
        log.info("Added CacheProvider bean");
    }

    public void destroyIfCreated(final @Observes BeforeShutdown beforeShutdown)
    {
        if (cacheManager != null) {
            cacheManager.close();
            log.info("Closed CacheManager");
        }
        if (cachingProvider != null) {
            cachingProvider.close();
            log.info("Closed CachingProvider");
        }
    }
}

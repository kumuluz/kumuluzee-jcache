# KumuluzEE JCache

KumuluzEE JCache allows usage of JCache annotations and JCache programmatic API in your KumuluzEE applications.
Current implementation used is [Caffeine](https://github.com/ben-manes/caffeine).

For sample project, take a look at `kumuluzee-jcache` module in [https://github.com/kumuluz/kumuluzee-samples](kumuluzee-samples) repository.

This readme is not a comprehensive guide to JCache but is meant to outline the basic tasks you can accomplish with JCache annotations and APIs.

Additional resources:  
[tomitribe/microprofile-jcache](https://github.com/tomitribe/microprofile-jcache/tree/master/jcache-cdi)  
[JCache JavaDoc](https://www.javadoc.io/doc/javax.cache/cache-api/1.1.1)

## Maven dependency
```
<dependency>
    <groupId>com.kumuluz.ee.jcache</groupId>
    <artifactId>kumuluzee-jcache-caffeine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Configuration

You can specify the configuration using `kumuluzee-config` YAML file and/or ENV variables.
Use prefix `kumuluzee.jcache.caffeine`, followed by reference Caffeine configuration structure.

Below is a YAML version of Caffeine [reference configuration](https://github.com/ben-manes/caffeine/blob/c06c5cca5c80e578495b206d03c8f76fb74f7b22/jcache/src/main/resources/reference.conf):
```yaml
kumuluzee:
  name: kumuluzee-jcache-sample-project
  version: 1.0.0
  env:
    name: dev
  jcache:
    caffeine:
      caffeine.jcache:
        # A named cache is configured by nesting a new definition under the caffeine.jcache namespace.
        # The per-cache configuration is overlaid on top of the default configuration.
        default:
          # The required type of the keys
          key-type: java.lang.Object
          # The required type of the values
          value-type: java.lang.Object
          # The strategy for copying the cache entry for value-based storage
          store-by-value:
            # If enabled, the entry is copied when crossing the API boundary
            enabled: false
            strategy: "com.github.benmanes.caffeine.jcache.copy.JavaSerializationCopier"
          # The executor class to use when performing maintenance and asynchronous operations. Defaults to
          # using ForkJoinPool.commonPool() if not set.
          executor: null
          # The list of configuration paths to the listeners that consume this cache's events
          listeners: []
          read-through:
            # If enabled, the entry is loaded automatically on a cache miss
            enabled: false
            # The CacheLoader class for loading entries
            loader: null
          write-through:
            # If enabled, the entry is written to the resource before the cache is updated
            enabled: false
            # The CacheWriter class for writing entries
            writer: null
          # The JMX monitoring configuration
          monitoring :
            # If cache statistics should be recorded and externalized
            statistics: false
            # If the configuration should be externalized
            management: false
          # The eviction policy for automatically removing entries from the cache
          policy:
            # The expiration threshold before lazily evicting an entry. This single threshold is reset on
            # every operation where a duration is specified. As expected by the specification, if an entry
            # expires but is not accessed and no resource constraints force eviction, then the expired
            # entry remains in place.
            lazy-expiration:
              # The duration before a newly created entry is considered expired. If set to 0 then the
              # entry is considered to be already expired and will not be added to the cache. May be
              # a time duration or "eternal" to indicate no expiration.
              creation: "eternal"
              # The duration before a updated entry is considered expired. If set to 0 then the entry is
              # considered immediately expired. May be a time duration, null to indicate no change, or
              # "eternal" to indicate no expiration.
              update: "eternal"
              # The duration before a read of an entry is considered expired. If set to 0 then the entry
              # is considered immediately expired. May be a time duration, null to indicate no change, or
              # "eternal" to indicate no expiration.
              access: "eternal"
            # The expiration thresholds before eagerly evicting an entry. These settings correspond to the
            # expiration supported natively by Caffeine where expired entries are collected during
            # maintenance operations.
            eager-expiration:
              # Specifies that each entry should be automatically removed from the cache once a fixed
              # duration has elapsed after the entry's creation, or the most recent replacement of its
              # value. This setting cannot be combined with the variable configuration.
              # Accepted time units are: d, h, m, s...
              after-write: null
              # Specifies that each entry should be automatically removed from the cache once a fixed
              # duration has elapsed after the entry's creation, the most recent replacement of its value,
              # or its last read. Access time is reset by all cache read and write operation. This setting
              # cannot be combined with the variable configuration.
              after-access: null
              # The expiry class to use when calculating the expiration time of cache entries. This
              # setting cannot be combined with after-write or after-access configurations.
              variable: null
            # The threshold before an entry is eligible to be automatically refreshed when the first stale
            # request for an entry occurs. This setting is honored only when combined with the
            # read-through configuration.
            refresh:
              # Specifies that active entries are eligible for automatic refresh once a fixed duration has
              # elapsed after the entry's creation or the most recent replacement of its value.
              after-write: null
            # The maximum bounding of the cache based upon its logical size
            maximum:
              # The maximum number of entries that can be held by the cache. This setting cannot be
              # combined with the weight configuration.
              size: null
              # The maximum total weight of entries the cache may contain (requires a weigher). This
              # setting cannot be combined with the size configuration.
              weight: null
              # The weigher class to use when calculating the weight of cache entries
              weigher: null

        # A catalog of cache listeners; optionally defined in any namespace as referenced by path
        listeners:
          # An example definition of a listener
          example:
            # The CacheEntryListener class
            class: null
            # The CacheEntryEventFilter class that should be applied prior to notifying the listener
            filter: null
            # If the thread that created the event should block until the listener has completed
            synchronous: false
            # If the old value should be provided
            old-value-required: false        
```

A basic example
```yaml
kumuluzee:
  name: kumuluzee-jcache-sample-project
  version: 1.0.0
  env:
    name: dev
  jcache:
    caffeine:
      caffeine.jcache:
        default:
          policy:
            eager-expiration:
              after-write: "10s"
            maximum:
              size: 10000
```

You can have multiple named caches (in the above example, cache name is `default`) and
you reference that name either in annotations or programmatic API.

## JCache annotations and interceptors

JCache annotations are an easy and clean way to introduce cache into your application. Note that only one interceptor may be used on a method at a time.

### @CacheDefaults

Sets defaults at class level.

```java
@CacheDefaults(cacheName = "default")
public class Library {
}
```

### @CacheKey, @CacheValue

`@CacheKey` annotates a method parameter to specify it as a cache key. If no parameter is annotated, cache key is formed from all method parameters (excluding parameters annotated with `@CacheValue`). `@CacheValue` marks a parameter to be stored as a value. By default, key is generated with `Arrays.deepHashCode()`.

### @CachePut (interceptor)

Puts a cache value into the cache.

```java
@CachePut
public void updateBook(@CacheKey String id, @CacheValue Book book) {
}
```

### @CacheResult (interceptor)

Returns cached value if it exists, otherwise stores the method result in cache.

```java
@CacheResult
public Book getBook(@CacheKey String id) {
}
```

### @CacheRemoveEntry (interceptor)

Removes an entry from cache after method completion.

```java
@CacheRemoveEntry
public Book removeBook(String id) {
}
```

### @CacheRemoveAll (interceptor)

Removes all entries from cache.
```java
@CacheRemoveAll
public Book removeAllBooks() {
}
```

## JCache programmatic API

`Cache` object behaves similar to java `Map` but there are [differences](https://static.javadoc.io/javax.cache/cache-api/1.1.1/javax/cache/Cache.html)!

Basic usage:

```java
CachingProvider cachingProvider = Caching.getCachingProvider();
CacheManager cacheManager = cachingProvider.getCacheManager();
MutableConfiguration<String, String> config = new MutableConfiguration<>();
Cache<String, String> cache = cacheManager.createCache("default", config);
cache.put("key1", "value1");
cacheManager.close();
```

### EntryProcessor

EntryProcessor allows modifying Cache entries using atomic operations. Think: atomic counters, rate limiting..

### Event Listeners

You can listen to the followig events:

- CREATED
- UPDATED
- REMOVED
- EXPIRED

### CacheLoader

Used either to bulk fill cache from external source or for read-through operations (invokes loader on cache miss to fetch the data from another source such as database).

## Cache CDI producer example

```java
public class CacheProducer {

    @Inject
    private CacheManager cacheManager;

    @Produces
    @ApplicationScoped
    public Cache<String, String> createCache() {
        final Configuration<String, String> configuration = new MutableConfiguration<String, String>().setTypes(String.class, String.class);
        return cacheManager.createCache("default", configuration);
    }
}
```

## Disable caching

You can disable caching by setting `policy.maximumSize` to zero.

## Interop between annotations and programmatic API

It is a general recommendation **not** to mix annotations and programmatic API for the same named cache. Use annotations for simple method caching and programmatic API when you need full control. Also be aware that it is not possible to access annotated cache values from programmatic API, at least not in an easy and clean way. For example:

```java
@CachePut(cacheName = "default")
public void put(@CacheKey String key, @CacheValue String data) {
}

public String get(String key) {
    if (cache.containsKey(key)) { //This is never true
        return cache.get(key);
    }
}
```

Looking from a high level, this should work but it does not due to different key hashcode being used in each case so the keys never match.

To avoid any such pitfalls, use separate named caches for annotations and programmatic API.

## Known issues

### CacheKeyGenerator must be a CDI bean

Due to an implementation detail in upstream `cache-ri-impl`, a custom CacheKeyGenerator must be a thread safe CDI bean and not just a plain class.

```
@CachePut(cacheKeyGenerator=MyKeyGenerator.class)
```
```
@ApplicationScoped
public class MyKeyGenerator implements CacheKeyGenerator {

    @Override
    public GeneratedCacheKey generateCacheKey(CacheKeyInvocationContext<? extends Annotation> cacheKeyInvocationContext) {
        return null;
    }
}
```

## License

This project is under MIT license.

Subset of files are under Apache license taken from `tomitribe/microprofile-jcache` project. See license headers in the respective source files.
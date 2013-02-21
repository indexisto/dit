package com.indexisto.dit.data.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* An LRU cache, based on <code>LinkedHashMap</code>.
*
* <p>
* Fixed maximum number of elements (<code>cacheSize</code>).
* Use freely with different DIH threads
*/
public class DihCache extends LinkedHashMap<String, String> {

    private static final float hashTableLoadFactor = 0.75f;

    private LinkedHashMap<String, String> map;

    private final int cacheSize;

    public DihCache(final int cacheSize) {
       this.cacheSize = cacheSize;
       final int hashTableCapacity = (int) Math.ceil(cacheSize / hashTableLoadFactor) + 1;
       map = new LinkedHashMap<String, String>(hashTableCapacity, hashTableLoadFactor, true) {
          //private static final long serialVersionUID = 1;
          @Override protected boolean removeEldestEntry (Map.Entry<String, String> eldest) {
             return size() > DihCache.this.cacheSize;
          }
       };
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry eldest) {
        return size() > cacheSize;
    }

    public synchronized String get(String key) {
        return map.get(key);
    }

    @Override
    public synchronized String put(String key, String value) {
        return map.put(key, value);
    }

    @Override
    public synchronized void clear() {
        map.clear();
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    public synchronized Collection<Map.Entry<String, String>> getAll() {
        return new ArrayList<Map.Entry<String, String>>(map.entrySet());
    }
}

/*
 * A simple most-recently-used cache for objects. Associates a separate key
 * object with each stored object, and compares keys for equal()ity. Simple.
 *
 * Resolution from key to object is done on demand by a MRUCache.Getter object.
*/

package StratmasClient.symbolloader;

class MRUCache {
        // An entry in the cache. Retains key, value associated with key,
        // and time of last access.
        static class Entry implements Comparable {
                public Object key, value;
                public long time;                // No C. :)

                public Entry() {
                        clear();
                }
                
                public Entry(Object key) {
                        clear();
                        time = System.currentTimeMillis();
                }
                
                public void clear() {
                        key   = null;
                        value = null;
                        time  = 0;
                }

                // Here is the Comparable interface.
                public int compareTo(Object o) {
                        if(o instanceof Entry) {
                            long now = System.currentTimeMillis();
                            //return (int) ((now - ((Entry) o).time) - ((int) (now - time)));
                            return ((int) (now - time)) - ((int) (now - ((Entry) o).time));
                        }
                        return 0;
                }
        }

        public interface Getter {
                public Object get(Object key);
        }

        private Entry[] cache;        // Always ordered so that cache[0] was last access.
        private Getter getter;

        public MRUCache(int size, MRUCache.Getter g) {
                cache = new Entry[size];
                for(int i = 0; i < cache.length; i++)
                        cache[i] = new Entry();
                getter = g;
        }

        public Object get(Object key) {
                if(key == null)
                        return null;
                Entry e = null;
//                System.out.println("Searching cache " + this + " for " + key);
                for(int i = 0; i < cache.length; i++) {
                        if(cache[i].key != null && cache[i].key.equals(key)) {
                                e = cache[i];
                                cache[i].time = System.currentTimeMillis();
                                // Shuffle array, making room at top.
                                for(int j = i; j > 0; j--)
                                        cache[j] = cache[j - 1];
                                cache[0] = e;
//                                System.out.println(" Resolved by cache");
                                return e.value;
                        }
                }
                e = cache[cache.length - 1];
                e.key  = key;
                e.time = System.currentTimeMillis();
//                System.out.println(" Resolved by callback");
                e.value = getter.get(key);
                java.util.Arrays.sort(cache);        // Lazily resort entire array.
                return e.value;
        }
}

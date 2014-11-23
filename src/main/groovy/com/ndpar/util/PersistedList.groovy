package com.ndpar.util

class PersistedList {

    def car, cdr

    static PersistedList list(Object... items) {
        items == null || items.length == 0 ? null : new PersistedList(car: items[0], cdr: list(rest(items)))
    }

    private static rest(coll) {
        coll.size() > 1 ? coll[1..-1] as Object[] : null
    }

    static PersistedList cons(Object a, Object d) {
        new PersistedList(car: a, cdr: d)
    }
}

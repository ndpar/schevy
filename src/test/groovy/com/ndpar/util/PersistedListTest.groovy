package com.ndpar.util

import org.junit.Test

import static com.ndpar.util.PersistedList.*

class PersistedListTest {

    @Test
    void list_empty() {
        assert list() == null
    }

    @Test
    void list_singleton() {
        assert list(1)
    }

    @Test
    void cars() {
        assert list(1, 2, 3).car == 1
        assert list(1, 2, 3).cdr.car == 2
        assert list(1, 2, 3).cdr.cdr.car == 3
    }

    @Test
    void cdrs() {
        assert list(1).cdr == null
        assert list(1, 2).cdr.cdr == null
    }

    @Test
    void cons() {
        def ls = list(2, 3)
        assert cons(1, ls).car == 1
        assert cons(1, ls).cdr.is(ls)
    }

    @Test
    void cons_improperList() {
        assert cons(1, 2).car == 1
        assert cons(1, 2).cdr == 2
    }
}

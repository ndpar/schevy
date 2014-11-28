package com.ndpar.util

class SchemeUtils {

    static List<Map> extendEnv(List vars, List vals, List<Map> env) {
        assert vars.size() == vals.size()
        def frame = [vars, vals].transpose().collectEntries { [(it[0]): it[1]] }
        [frame] + env
    }
}

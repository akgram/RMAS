package com.example.checkcount.methods

import com.example.checkcount.model.Obj

fun searchObjectsByDescription(
    objs: MutableList<Obj>,
    query: String
):List<Obj>{
    val regex = query.split(" ").joinToString(".*"){
        Regex.escape(it)
    }.toRegex(RegexOption.IGNORE_CASE)
    return objs.filter { objs ->
        regex.containsMatchIn(objs.description)
    }
}

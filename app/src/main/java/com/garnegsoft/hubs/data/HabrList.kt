package com.garnegsoft.hubs.data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


@Immutable
class HabrList<T>(val list: List<T>, val pagesCount: Int) where T : HabrSnippet  {

    operator fun plus(secondList: HabrList<T>): HabrList<T>{
        var templist = ArrayList<T>().apply {
            addAll(list)
            addAll(secondList.list)
        }
        templist = templist.distinctBy { it.id } as ArrayList<T>
        return HabrList(templist, this.pagesCount)
    }

}

@Stable
interface HabrSnippet {
    val id: Int
}



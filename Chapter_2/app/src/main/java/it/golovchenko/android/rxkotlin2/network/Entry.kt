package it.golovchenko.android.rxkotlin2.network


data class Entry(val id: String?, val title: String?, val link: String?, val updated: Long) : Comparable<Entry> {

    override fun compareTo(other: Entry): Int = if (updated > other.updated) -1 else 1
    override fun toString(): String = "'$title'\nupdated=$updated)"


}
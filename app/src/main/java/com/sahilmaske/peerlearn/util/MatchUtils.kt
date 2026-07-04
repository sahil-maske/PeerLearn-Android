package com.sahilmaske.peerlearn.util

fun calculateMatchPercentage(
    mySkills: List<String>,
    peerSkills: List<String>
): Int {
    if (mySkills.isEmpty() || peerSkills.isEmpty()) return 0

    val mySet = mySkills.map { it.lowercase() }.toSet()
    val peerSet = peerSkills.map { it.lowercase() }.toSet()

    val common = mySet.intersect(peerSet).size
    val total = mySet.union(peerSet).size

    return if (total == 0) 0 else ((common.toFloat() / total) * 100).toInt()
}
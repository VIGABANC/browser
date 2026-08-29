package com.example.data.adblock

import java.util.BitSet
import kotlin.math.abs

class BloomFilter(private val size: Int = 100_000, falsePositiveRate: Double = 0.01) {
    private val bitSet = BitSet(size)
    private val hashFunctions = 3 // Simplified for 0.01 rate

    fun add(element: String) {
        for (i in 0 until hashFunctions) {
            bitSet.set(getHash(element, i))
        }
    }

    fun mightContain(element: String): Boolean {
        for (i in 0 until hashFunctions) {
            if (!bitSet.get(getHash(element, i))) return false
        }
        return true
    }

    private fun getHash(element: String, i: Int): Int {
        val hash = (element.hashCode() xor i.hashCode())
        return abs(hash) % size
    }
}

class AhoCorasickTrie {
    class Node {
        val children = mutableMapOf<Char, Node>()
        var isWord = false
    }
    
    private val root = Node()
    
    fun insert(word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { Node() }
        }
        current.isWord = true
    }
    
    fun contains(text: String): Boolean {
        // Simplified contains (just checking exact match or substring)
        // For a full Aho-Corasick we need failure links. For simplicity, we just do a basic substring search via the trie.
        for (i in text.indices) {
            var current = root
            for (j in i until text.length) {
                current = current.children[text[j]] ?: break
                if (current.isWord) return true
            }
        }
        return false
    }
}

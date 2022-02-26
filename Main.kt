package search

import java.io.File
import kotlin.system.exitProcess

data class Person(val data: String) {
    val wordsList: List<String> = data.split(" ")
}

data class People(private val people: List<Person>) {
    private val invertedIndexMap = mutableMapOf<String, MutableList<Int>>()
    init {
        for ((index, person) in people.withIndex()) {
            for (word in person.wordsList) {
                if (word.lowercase() !in invertedIndexMap) {
                    invertedIndexMap[word.lowercase()] = mutableListOf(index)
                } else {
                    invertedIndexMap[word.lowercase()]?.add(index)
                }
            }
        }
    }

    fun searchByKey(key: String, matchingStrategy: String): People {
        return when (matchingStrategy) {
            "ANY" -> findMatchAny(key)
            "ALL" -> findMatchAll(key)
            "NONE" -> findMatchNone(key)
            else -> People(listOf())
        }
    }

    private fun findMatchAny(keys: String): People {
        val indices = mutableListOf<Int>()
        for (key in keys.split(" ")) {
            invertedIndexMap[key.lowercase()]?.let { indices.addAll(it) }
        }
        return People(people.filterIndexed { index, _ -> indices.contains(index) })
    }

    private fun findMatchAll(keys: String): People {
        val indices = mutableListOf<List<Int>>()
        for (key in keys.split(" ")) {
            invertedIndexMap[key.lowercase()]?.let { indices.add(it) }
        }
        if (indices.isNotEmpty()) {
            indices.reduce { acc, ints -> acc.intersect(ints.toSet()).toList() }
            return People(people.filterIndexed { index, _ -> indices[0].contains(index) })
        }
        return People(listOf())
    }

    private fun findMatchNone(keys: String): People {
        val indices = mutableListOf<Int>()
        for (key in keys.split(" ")) {
            invertedIndexMap[key.lowercase()]?.let { indices.addAll(it) }
        }
        return People(people.filterIndexed { index, _ -> !indices.contains(index) })
    }

    fun printAll() {
        if (people.isEmpty()) {
            println("No matching people found.")
        } else {
            println(people.joinToString("\n") { it.data })
        }
    }
}

class SearchEngine(private val people: People) {
    enum class MenuItems(val itemOrder: String) {
        EXIT("0"),
        FIND_A_PERSON("1"),
        PRINT_ALL_PEOPLE("2")
    }

    fun search() {
        while (true) {
            println("\n=== Menu ===\n" +
                    "1. Find a person\n" +
                    "2. Print all people\n" +
                    "0. Exit")

            when (readln()) {
                MenuItems.EXIT.itemOrder -> quit()
                MenuItems.FIND_A_PERSON.itemOrder -> findPerson()
                MenuItems.PRINT_ALL_PEOPLE.itemOrder -> printAllPeople()
                else -> incorrectOption()
            }
        }
    }

    private fun quit() {
        println("\nBye!")
        exitProcess(0)
    }

    private fun findPerson() {
        println("Select a matching strategy: ALL, ANY, NONE")
        val matchingStrategy = readln()

        println("Enter a name or email to search all suitable people.")
        val key = readln()

        val result = people.searchByKey(key, matchingStrategy)
        result.printAll()
    }

    private fun printAllPeople() {
        people.printAll()
    }

    private fun incorrectOption() {
        println("\nIncorrect option! Try again.")
    }
}

object Program {
    private lateinit var searchEngine: SearchEngine

    fun execute(args: Array<String>) {
        val people = People(File(args[1]).readLines().map { Person(it) })

        searchEngine = SearchEngine(people)

        searchEngine.search()
    }

}

fun main(args: Array<String>) {
    Program.execute(args)
}

package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * The main application class for the Token Indexer.
 *
 * @author Jodi Chan
 */
@SpringBootApplication
@EnableScheduling
class TokenIndexerApplication

fun main(args: Array<String>) {
    runApplication<TokenIndexerApplication>(*args)
}
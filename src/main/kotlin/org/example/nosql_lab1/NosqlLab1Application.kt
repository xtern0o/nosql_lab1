package org.example.nosql_lab1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class NosqlLab1Application

fun main(args: Array<String>) {
    runApplication<NosqlLab1Application>(*args)
}

package org.example.nosql_lab1.entity

enum class EventStatus {
    DRAFTED,    // сохраненный черновик
    PUBLISHED,  // опубликовано, можно оформлять заявки
    CANCELLED,  // отменен
    FINISHED    // прошел ивент
}
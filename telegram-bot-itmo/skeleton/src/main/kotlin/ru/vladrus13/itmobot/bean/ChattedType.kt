package ru.vladrus13.itmobot.bean

enum class ChattedType {
    CHAT {
        override fun toString(): String = "chat"

    },
    USER {
        override fun toString(): String = "user"

    };

    abstract override fun toString(): String
}
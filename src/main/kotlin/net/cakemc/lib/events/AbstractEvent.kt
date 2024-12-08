package net.cakemc.lib.events

@Suppress("UNUSED")
abstract class AbstractEvent {
    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
    abstract override fun toString(): String

    interface Cancellable {
        fun cancel() {
            cancel(!cancelled())
        }

        fun cancel(cancelled: Boolean)

        fun cancelled(): Boolean
    }
}
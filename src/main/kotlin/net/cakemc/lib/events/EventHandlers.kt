package net.cakemc.lib.events

@Suppress("UNUSED")
object EventHandlers {
    fun interface IHandler<E : AbstractEvent> : Comparator<IHandler<E>> {
        fun handle(event: E)

        fun priority(): Short = 0

        override fun compare(o1: IHandler<E>, o2: IHandler<E>): Int {
            return o2.priority().compareTo(o1.priority())
        }
    }

    abstract class AbstractHandler<E : AbstractEvent> : IHandler<E> {
        abstract override fun handle(event: E)
        override fun priority(): Short = 0
    }
}
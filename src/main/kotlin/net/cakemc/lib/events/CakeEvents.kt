package net.cakemc.lib.events

typealias Consumer<T> = (T) -> Unit

@Suppress("UNUSED")
open class CakeEvents(val onError: Consumer<Throwable> = Throwable::printStackTrace) {
    private val handlersIndices = mutableMapOf<EventHandlers.IHandler<out AbstractEvent>, Int>()
    val handlers = mutableMapOf<Class<out AbstractEvent>, Array<EventHandlers.IHandler<out AbstractEvent>>>()

    /**
     * @param clazz   The class-group of our handlers.
     * @param handler The handler we want to add.
     * @param <H>     The type of the handler.
     * @param <E>     The type of the event for our handler.
     */
    @Suppress("UNCHECKED_CAST")
    fun <H : EventHandlers.IHandler<E>, E : AbstractEvent> register(clazz: Class<E>, handler: H) = try {
        val current = handlers[clazz] ?: emptyArray()

        val updated = current.copyOf(current.size + 1)
        updated[updated.size - 1] = handler

        updated.sortWith(compareBy { it?.priority() })

        handlers[clazz] = updated as Array<EventHandlers.IHandler<out AbstractEvent>>
        handlersIndices[handler] = updated.indexOf(handler)
    } catch (throwable: Throwable) {
        onError(throwable)
    }


    /**
     * @param clazz   The class-group of our handlers.
     * @param handler The handler we want to remove.
     * @param <H>     The type of the handler.
     * @param <E>     The type of the event for our handler.
     */
    fun <H : EventHandlers.IHandler<E>, E : AbstractEvent> unregister(clazz: Class<E>, handler: H) {
        try {
            val current = handlers[clazz] ?: return

            if (current.isEmpty()) {
                handlers.remove(clazz)
                return
            }

            val index = handlersIndices[handler] ?: run {
                onError(NoSuchFieldError("The handler $handler doesn't exist."))
                return
            }

            // Remove the handler from the array
            val updated = current.filterIndexed { idx, _ -> idx != index }.toTypedArray()

            // Update the handlers map or remove the event class entry
            if (updated.isNotEmpty()) {
                handlers[clazz] = updated
            } else {
                handlers.remove(clazz)
            }

            handlersIndices.remove(handler)
        } catch (throwable: Throwable) {
            onError(throwable)
        }
    }

    @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
    inline fun <E : AbstractEvent> call(event: E) {
        for (handler in handlers[event::class.java]!! as Array<EventHandlers.IHandler<E>>? ?: return) {
            try {
                handler.handle(event)
            } catch (throwable: Throwable) {
                onError(throwable)
                continue
            }
        }
    }
}

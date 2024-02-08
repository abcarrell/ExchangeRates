package com.tc.exchangerates.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

interface MVI<State, Event, Effect> {
    val state: StateFlow<State>
    val effects: Flow<Effect>

    fun postEvent(event: Event)
}

interface MVIActor<State, Event, Effect> : MVI<State, Event, Effect> {
    fun setState(reduce: State.() -> State)
    fun setEffect(effect: () -> Effect)
    val events: SharedFlow<Event>
}

class MVIDelegate<State, Event, Effect> internal constructor(
    initialState: State
) : MVIActor<State, Event, Effect> {
    private val _events: MutableSharedFlow<Event> by lazy {
        MutableSharedFlow()
    }
    override val events = _events.asSharedFlow()

    private val _uiState: MutableStateFlow<State> by lazy {
        MutableStateFlow(initialState)
    }
    override val state = _uiState.asStateFlow()

    private val _effects: Channel<Effect> by lazy {
        Channel(capacity = Channel.UNLIMITED)
    }
    override val effects = _effects.receiveAsFlow()

    override fun setState(reduce: State.() -> State) {
        _uiState.update(reduce)
    }

    override fun setEffect(effect: () -> Effect) {
        _effects.trySend(effect())
    }

    override fun postEvent(event: Event) {
        _events.tryEmit(event)
    }
}

fun <State, Event, Effect> mvi(initialState: State): MVIActor<State, Event, Effect> =
    MVIDelegate(initialState)

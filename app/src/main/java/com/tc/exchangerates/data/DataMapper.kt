package com.tc.exchangerates.data

import retrofit2.Response
import java.io.IOException

fun interface DataMapper<S, T> : (S) -> T

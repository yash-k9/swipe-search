package com.github.theapache64.swipesearch.util.calladapter.flow

import com.github.theapache64.swipesearch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.awaitResponse
import java.lang.reflect.Type


class FlowResourceCallAdapter<R>(
    private val responseType: Type,
    private val isSelfExceptionHandling: Boolean
) : CallAdapter<R, Flow<Resource<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>) = flow<Resource<R>> {

        // Firing loading resource
        emit(Resource.Loading())

        val resp = call.awaitResponse()

        if (resp.isSuccessful) {
            resp.body()?.let { data ->
                // Success
                emit(Resource.Success(data, null))
            } ?: kotlin.run {
                // Error
                emit(Resource.Error("Response can't be null"))
            }
        } else {
            // Error
            val errorBody = resp.message()
            emit(Resource.Error(errorBody))
        }

    }.catch { error: Throwable ->
        if (isSelfExceptionHandling) {
            emit(Resource.Error(error.message ?: "Something went wrong"))
        } else {
            throw error
        }
    }
}

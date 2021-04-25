package org.jellyfin.client.android.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.jellyfin.client.android.domain.models.Resource

abstract class BaseUseCase<T, in Params>(private val dispatcher: CoroutineDispatcher) {
    suspend fun invoke(params: Params? = null): Flow<Resource<T>> {
        return invokeInternal(params).flowOn(dispatcher)
    }

    protected abstract suspend fun invokeInternal(params: Params? = null): Flow<Resource<T>>

}

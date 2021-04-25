package org.jellyfin.client.android.network

enum class Status {
    LOADING,
    SUCCESS,
    ERROR,
}

data class Error(val httpErrorResponseCode: Int?,
                        val code: Int?,
                        val message: String?,
                        val exception: Throwable?)

data class Resource<T>(val status: Status, val data: T?, val messages: List<Error>?) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: List<Error>?): Resource<T> {
            return Resource(Status.ERROR, null, msg)
        }

        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING, null, null)
        }
    }
}

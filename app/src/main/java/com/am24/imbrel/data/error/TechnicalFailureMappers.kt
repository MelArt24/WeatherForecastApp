package com.am24.imbrel.data.error

import com.am24.imbrel.domain.error.ApiErrorReason
import com.am24.imbrel.domain.error.DomainError
import com.am24.imbrel.domain.error.DomainFailureException
import com.am24.imbrel.domain.error.LocationErrorReason
import com.am24.imbrel.domain.error.NetworkErrorReason
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal fun Throwable.toWeatherDomainError(): DomainError =
    when (this) {
        is DomainFailureException -> error
        is SocketTimeoutException -> DomainError.Network(NetworkErrorReason.Timeout)
        is UnknownHostException, is ConnectException, is IOException ->
            DomainError.Network(NetworkErrorReason.ConnectionFailed)
        is HttpException ->
            DomainError.Api(
                reason =
                    when (code()) {
                        401, 403 -> ApiErrorReason.Unauthorized
                        404 -> ApiErrorReason.NotFound
                        429 -> ApiErrorReason.RateLimited
                        in 500..599 -> ApiErrorReason.ServerError
                        else -> ApiErrorReason.RequestFailed
                    },
                statusCode = code(),
            )
        is SerializationException, is InvalidWeatherResponseException ->
            DomainError.Api(ApiErrorReason.InvalidResponse)
        else -> DomainError.Unknown
    }

internal fun Throwable.toLocationDomainError(): DomainError =
    when (this) {
        is DomainFailureException -> error
        is SecurityException -> DomainError.Location(LocationErrorReason.PermissionDenied)
        else -> DomainError.Location(LocationErrorReason.ResolutionFailed)
    }

internal class InvalidWeatherResponseException : Exception()

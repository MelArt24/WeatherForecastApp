package com.am24.weatherforecastapp.domain.error

/**
 * A failure expressed in terms meaningful to the application's domain.
 *
 * Domain errors intentionally contain no user-facing text or platform types. Callers at the
 * application boundary can translate technical failures into these values, while presentation
 * code can independently decide how each value should be displayed.
 */
sealed interface DomainError {
    data class Network(val reason: NetworkErrorReason) : DomainError

    data class Api(
        val reason: ApiErrorReason,
        val statusCode: Int? = null
    ) : DomainError

    data class Location(val reason: LocationErrorReason) : DomainError

    data object Unknown : DomainError
}

/** Carries a domain failure through suspending APIs without leaking its technical cause. */
class DomainFailureException(val error: DomainError) : Exception()

enum class NetworkErrorReason {
    Offline,
    Timeout,
    ConnectionFailed
}

enum class ApiErrorReason {
    Unauthorized,
    NotFound,
    RateLimited,
    InvalidResponse,
    ServerError,
    RequestFailed
}

enum class LocationErrorReason {
    PermissionDenied,
    Unavailable,
    ResolutionFailed
}

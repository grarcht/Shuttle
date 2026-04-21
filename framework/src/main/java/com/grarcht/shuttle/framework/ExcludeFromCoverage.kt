package com.grarcht.shuttle.framework

/**
 * Marks a function or class as intentionally excluded from code coverage reporting.
 *
 * Use this on functions that contain SDK-version dispatch branches (e.g. TIRAMISU guards)
 * or other code paths that are structurally unreachable in JVM unit tests. The calling code
 * must still be covered; only the annotated function body itself is excluded from Kover's report.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
internal annotation class ExcludeFromCoverage

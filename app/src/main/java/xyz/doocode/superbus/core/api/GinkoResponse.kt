package xyz.doocode.superbus.core.api

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for the Ginko API response.
 * The API always returns an object containing an "objets" field.
 *
 * @param T The type of data returned (can be a List or a single object).
 */
data class GinkoResponse<T>(
    @SerializedName("objets")
    val objects: T
)

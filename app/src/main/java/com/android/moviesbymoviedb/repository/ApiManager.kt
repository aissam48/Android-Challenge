package com.android.moviesbymoviedb.repository


import android.content.Context
import com.android.moviesbymoviedb.models.EventRepo
import com.android.moviesbymoviedb.R
import com.android.moviesbymoviedb.models.APIError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.json.JSONObject
import org.json.JSONTokener
import javax.inject.Inject

class ApiManager @Inject constructor(
    private val client: HttpClient,
    private val appContext: Context
) {

    private fun List<*>.toJsonElement(): JsonElement {
        val list: MutableList<JsonElement> = mutableListOf()
        this.forEach { value ->
            when (value) {
                null -> list.add(JsonNull)
                is Map<*, *> -> list.add(value.toJsonElement())
                is List<*> -> list.add(value.toJsonElement())
                is Boolean -> list.add(JsonPrimitive(value))
                is Number -> list.add(JsonPrimitive(value))
                is String -> list.add(JsonPrimitive(value))
                is Enum<*> -> list.add(JsonPrimitive(value.toString()))
                else -> throw IllegalStateException("Can't serialize unknown collection type: $value")
            }
        }
        return JsonArray(list)
    }

    private fun Map<*, *>.toJsonElement(): JsonElement {
        val map: MutableMap<String, JsonElement> = mutableMapOf()
        this.forEach { (key, value) ->
            key as String
            when (value) {
                null -> map[key] = JsonNull
                is Map<*, *> -> map[key] = value.toJsonElement()
                is List<*> -> map[key] = value.toJsonElement()
                is Boolean -> map[key] = JsonPrimitive(value)
                is Number -> map[key] = JsonPrimitive(value)
                is String -> map[key] = JsonPrimitive(value)
                is Enum<*> -> map[key] = JsonPrimitive(value.toString())
                else -> throw IllegalStateException("Can't serialize unknown type: $value")
            }
        }
        return JsonObject(map)
    }

    suspend fun makeRequest(
        url: String,
        reqMethod: HttpMethod?,
        bodyMap: HashMap<String, Any>? = null,
        multiPartForm: MultiPartFormDataContent? = null,
        parameterFormData: ArrayList<Pair<String, Any>>? = null,
        successCallback: suspend (response: JSONObject) -> Unit,
        failureCallback: suspend (error: EventRepo.Error<*>) -> Unit,
    ) {

        try {

            val httpResponse: HttpResponse? =
                when (reqMethod) {
                    HttpMethod.Post -> {
                        client.post<HttpResponse?>(url) {
                            if (multiPartForm != null) {
                                body = multiPartForm
                            }
                            if (bodyMap != null) {
                                contentType(ContentType.Application.Json)
                                body = bodyMap.toJsonElement()
                            }
                            if (parameterFormData != null) {
                                formData {
                                    for (param in parameterFormData) {
                                        parameter(param.first, param.second)
                                    }
                                }
                            }
                        }
                    }

                    HttpMethod.Put -> {
                        client.put(url) {
                            if (multiPartForm != null) {
                                body = multiPartForm
                            }
                            if (bodyMap != null) {
                                contentType(ContentType.Application.Json)
                                body = bodyMap.toJsonElement()
                            }
                            if (parameterFormData != null) {
                                formData {
                                    for (param in parameterFormData) {
                                        parameter(param.first, param.second)
                                    }
                                }
                            }
                        }
                    }

                    HttpMethod.Patch -> {
                        client.patch(url) {
                            if (multiPartForm != null) {
                                body = multiPartForm
                            }
                            if (bodyMap != null) {
                                contentType(ContentType.Application.Json)
                                body = bodyMap.toJsonElement()
                            }
                            if (parameterFormData != null) {
                                formData {
                                    for (param in parameterFormData) {
                                        parameter(param.first, param.second)
                                    }
                                }
                            }
                        }
                    }

                    HttpMethod.Get -> {
                        client.get(url) {
                            contentType(ContentType.Application.Json)
                            if (parameterFormData != null) {
                                formData {
                                    for (param in parameterFormData) {
                                        parameter(param.first, param.second)
                                    }
                                }
                            }
                        }
                    }

                    HttpMethod.Delete -> {
                        client.delete(url) {
                            contentType(ContentType.Application.Json)
                            if (parameterFormData != null) {
                                formData {
                                    for (param in parameterFormData) {
                                        parameter(param.first, param.second)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        null
                    }
                }

            val response: String? = httpResponse?.receive()
            val jsonObject = JSONTokener(response).nextValue() as JSONObject

            successCallback(jsonObject)

        } catch (e: ClientRequestException) {
            // 4xx - Response
            val json = JSONObject(e.response.receive<String>())
            var errorMessage = appContext.getString(R.string.error_message_at_server)
            if (json.has("status_message")) {
                errorMessage = json.getString("status_message")
            }
            failureCallback(
                EventRepo.Error<Any>(
                    apiError = APIError(e.response.status.value, errorMessage)
                )
            )

        } catch (e: ServerResponseException) {
            // 5xx - Response
            failureCallback(
                EventRepo.Error<Any>(
                    apiError = APIError(
                        e.response.status.value,
                        appContext.getString(R.string.error_message_at_server),
                    )
                )
            )
        } catch (e: Exception) {
            failureCallback(
                EventRepo.Error<Any>(
                    apiError = APIError(500, appContext.getString(R.string.error_message_at_server))
                )
            )
        }
    }
}

enum class BaseUrl(val value: String) {
    URL("https://api.themoviedb.org/3/search/tv"),
}

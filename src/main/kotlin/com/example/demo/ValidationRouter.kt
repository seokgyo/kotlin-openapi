package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openapi4j.core.validation.ValidationException
import org.openapi4j.operation.validator.model.Request
import org.openapi4j.operation.validator.model.impl.Body
import org.openapi4j.operation.validator.model.impl.DefaultRequest
import org.openapi4j.operation.validator.model.impl.DefaultResponse
import org.openapi4j.operation.validator.validation.RequestValidator
import org.openapi4j.parser.OpenApi3Parser
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.function.server.*

open class ValidationRouter(val basePath: String) {
    private val objectMapper = ObjectMapper()
    private val openApi = OpenApi3Parser().parse(
            ClassPathResource("static/oas${basePath}.yaml").url,
            true
    )!!
    private val validator = RequestValidator(openApi)

    private suspend fun validationHandler(
            pattern: String, req: ServerRequest, f: suspend (ServerRequest, ObjectNode) -> Any
    ): ServerResponse {
        val path = openApi.paths[pattern] ?: throw Exception("OAS not found for path: $pattern")
        val httpMethod = req.methodName().toLowerCase()
        val operation = path.getOperation(httpMethod)
                ?: throw Exception("OAS not found for: $httpMethod $pattern")
        val body = req.awaitBodyOrNull() ?: JsonNodeFactory.instance.objectNode()
        try {
            val requestBuilder = DefaultRequest.Builder(
                    Request.Method.getMethod(httpMethod),
                    // FIXME: maybe better way?
                    req.path().removePrefix(basePath)
            )
            body?.let {
                requestBuilder.body(Body.from(it))
                        .header("Content-Type", "application/json")
            }
            validator.validate(requestBuilder.build(), path, operation)
        } catch (e: ValidationException) {
            // FIXME: enhance error message
            return ServerResponse.badRequest().bodyValueAndAwait(e.toString())
        }
        val bodyNode = objectMapper.valueToTree<ObjectNode>(f(req, body))
        try {
            val responseBuilder = DefaultResponse.Builder(200)
                    .body(Body.from(bodyNode))
                    .header("Content-Type", "application/json")
            validator.validate(responseBuilder.build(), path, operation)
        } catch (e: ValidationException) {
            println(e)
        }
        return ServerResponse.ok().bodyValueAndAwait(bodyNode)
    }

    fun CoRouterFunctionDsl.GETV(pattern: String, f: suspend (ServerRequest) -> Any) {
        GET(pattern) {
            validationHandler(pattern, it) { req, _ -> f(req) }
        }
    }

    fun CoRouterFunctionDsl.POSTV(pattern: String, f: suspend (ServerRequest, ObjectNode) -> Any) {
        POST(pattern) {
            validationHandler(pattern, it, f)
        }
    }

    fun CoRouterFunctionDsl.PUTV(pattern: String, f: suspend (ServerRequest, ObjectNode) -> Any) {
        PUT(pattern) {
            validationHandler(pattern, it, f)
        }
    }

    fun CoRouterFunctionDsl.PATCHV(pattern: String, f: suspend (ServerRequest, ObjectNode) -> Any) {
        PATCH(pattern) {
            validationHandler(pattern, it, f)
        }
    }
}

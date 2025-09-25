package br.com.fiap.techchallenge

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.google.gson.Gson

class LambdaHandler : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private val gson = Gson()

    override fun handleRequest(request: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent {
        val logger = context?.logger
        logger?.log("Recebida requisição: ${request?.body}")

        val responseBody = mapOf(
            "message" to "Olá da sua Lambda Kotlin!",
            "input" to request?.body
        )

        return APIGatewayProxyResponseEvent().apply {
            statusCode = 200
            headers = mapOf("Content-Type" to "application/json")
            body = gson.toJson(responseBody)
        }
    }
}
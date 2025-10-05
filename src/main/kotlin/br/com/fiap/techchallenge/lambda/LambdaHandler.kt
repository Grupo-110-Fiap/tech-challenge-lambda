package br.com.fiap.techchallenge.lambda

import br.com.fiap.techchallenge.lambda.service.AwsCognitoService
import br.com.fiap.techchallenge.lambda.service.CognitoSecretsProvider
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient

data class Request(val cpf: String? = "", val email: String? = "", val name: String? = "")

class LambdaHandler(
    private val cognitoService: AwsCognitoService = AwsCognitoService(
        CognitoIdentityProviderClient.create(),
        CognitoSecretsProvider().load()
    )
): RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private val mapper = jacksonObjectMapper().registerModule(KotlinModule())

    override fun handleRequest(event: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse {
        val path = event.requestContext.http.path
        println("path: $path")
        return when (path) {
            "/login" -> handleLogin(event)
            "/register" -> handleRegister(event)
            "/anonymous" -> handleAnon()
            else -> handleNotFound()
        }
    }

    private fun handleLogin(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val request = mapper.readValue(event.body, Request::class.java)
            val token = cognitoService.getUserAndToken(request)

            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"message": "Login succeeded!", "token": "$token"}""")
                .build()
        }
        catch (e: IllegalArgumentException) {
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(400)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"error": "${e.message}"}""")
                .build()
        } catch (e: Exception) { LAMBDA_ERROR }
    }

    private fun handleRegister(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val request = mapper.readValue(event.body, Request::class.java)
            val token = cognitoService.createUserAndToken(request)

            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"message": "Login succeeded!", "token": "$token"}""")
                .build()
        }
        catch (e: IllegalArgumentException) {
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(400)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"error": "${e.message}"}""")
                .build()
        } catch (e: Exception) { LAMBDA_ERROR }
    }

    private fun handleAnon(): APIGatewayV2HTTPResponse {
        return try {
            val token = cognitoService.getAnonUser()

            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"message": "Login succeeded!", "token": "$token"}""")
                .build()
        }
        catch (e: IllegalArgumentException) {
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(400)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody("""{"error": "${e.message}"}""")
                .build()
        } catch (e: Exception) { LAMBDA_ERROR }
    }

    private fun handleNotFound(): APIGatewayV2HTTPResponse {
        return APIGatewayV2HTTPResponse.builder()
            .withStatusCode(400)
            .withHeaders(mapOf("Content-Type" to "application/json"))
            .withBody("""{"Route not found"}""")
            .build()
    }

    companion object {
        val LAMBDA_ERROR = APIGatewayV2HTTPResponse.builder()
            .withStatusCode(500)
            .withHeaders(mapOf("Content-Type" to "application/json"))
            .withBody("""{"error": "Lambda error"}""")
            .build()
    }
}
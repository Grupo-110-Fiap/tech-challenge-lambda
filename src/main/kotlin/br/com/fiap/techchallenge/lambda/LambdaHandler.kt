package br.com.fiap.techchallenge.lambda

import br.com.fiap.techchallenge.lambda.service.CognitoMockService
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

data class Request(val cpf: String = "")
data class Response(val clientId: String? = "", val token: String? = "", val message: String = "")

class LambdaHandler: RequestHandler<Request, Response> {
    private val cognitoService = CognitoMockService() // trocar por injeção em prod

    override fun handleRequest(input: Request, context: Context): Response {
        val cpf = input.cpf
        if (cognitoService.existsCpf(cpf)) {
            return Response(null, null, "CPF already registered")
        }
        val (clientId, token) = cognitoService.createUserWithCpf(cpf)
        return Response(clientId, token, "OK")
    }
}
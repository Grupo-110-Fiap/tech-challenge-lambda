package br.com.fiap.techchallenge.lambda.service

class CognitoMockService: CognitoService {
    private val store = mutableSetOf<String>()

    override fun existsCpf(cpf: String): Boolean = store.contains(cpf)

    override fun createUserWithCpf(cpf: String): Pair<String, String> {
        store.add(cpf)
        val clientId = "mock-client-${cpf.takeLast(4)}"
        val token = "mock-token-${cpf.hashCode()}"
        return Pair(clientId, token)
    }
}
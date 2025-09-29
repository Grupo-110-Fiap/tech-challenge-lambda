package br.com.fiap.techchallenge.lambda.service

interface CognitoService {
    fun existsCpf(cpf: String): Boolean
    fun createUserWithCpf(cpf: String): Pair<String, String> // clientId, token
}
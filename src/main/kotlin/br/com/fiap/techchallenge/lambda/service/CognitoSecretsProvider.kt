package br.com.fiap.techchallenge.lambda.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

@JsonIgnoreProperties(ignoreUnknown = true)
data class CognitoSecrets(
    val clientId: String = "",
    val userPoolId: String = "",
    val password: String = "",
    val anonymousEmail: String? = ""
)

class CognitoSecretsProvider {
    private val objectMapper = ObjectMapper()

    private val secretsManager: SecretsManagerClient by lazy {
        SecretsManagerClient.builder()
            .region(Region.of(System.getenv("AWS_REGION") ?: "us-east-1"))
            .build()
    }

    fun load(secretName: String = System.getenv("COGNITO_SECRET_NAME") ?:
    "cognito/secret-mock"
    ): CognitoSecrets {

        mockSecrets(secretName)?.let { return it }

        val request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build()

        val response = secretsManager.getSecretValue(request)
        val secretString = response.secretString() ?: ""

        return objectMapper.readValue(secretString, CognitoSecrets::class.java)
    }

    private fun mockSecrets(secretName: String): CognitoSecrets? = when (secretName) {
        "cognito/secret-mock" -> CognitoSecrets(
            clientId = "mock-client-id",
            userPoolId = "mock-user-pool-id",
            password = "mock-password",
            anonymousEmail = "anonimo@tehchchallenge.com"
        )
        else -> null
    }
}
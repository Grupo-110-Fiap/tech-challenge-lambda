package br.com.fiap.techchallenge.lambda.service

import br.com.fiap.techchallenge.lambda.Request
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException

open class AwsCognitoService(
    private val cognito: CognitoIdentityProviderClient,
    private val cognitoSecrets: CognitoSecrets
) {

    fun createUserAndToken(request: Request): String {
        createUserWithCpf(request)
        return authenticateExistingUser(request.cpf!!)
    }

    fun getUserAndToken(request: Request): String {
        val email = findByCPF(request.cpf!!)
        return authenticateExistingUser(request.cpf)
    }

    fun getAnonUser(): String {
        val email = cognitoSecrets.anonymousEmail
            ?: throw IllegalStateException("Anonymous email not configured")

        return authenticateExistingUser(email)
    }

    fun findByCPF(cpf: String): String {
        require(cpf.isNotBlank()) { "CPF cannot be empty" }

        val users = cognito.listUsers(
            ListUsersRequest.builder()
                .userPoolId(cognitoSecrets.userPoolId)
                .filter("custom:cpf = \"$cpf\"")
                .limit(1)
                .build()
        )

        if (users.users().isEmpty()) {
            throw IllegalArgumentException("CPF $cpf not found in Cognito")
        }

        return users.users().first().attributes().first { it.name() == "email" }.value()
    }

    fun createUserWithCpf(request: Request) {
        require(!request.cpf.isNullOrBlank()) { "CPF cannot be empty" }
        require(!request.email.isNullOrBlank()) { "E-mail cannot be empty" }
        require(!request.name.isNullOrBlank()) { "Name cannot be empty" }

        try {
            cognito.adminCreateUser(
                AdminCreateUserRequest.builder()
                    .userPoolId(cognitoSecrets.userPoolId)
                    .username(request.cpf)
                    .temporaryPassword(cognitoSecrets.password)
                    .userAttributes(
                        AttributeType.builder().name("email").value(request.email).build(),
                        AttributeType.builder().name("name").value(request.name).build()
                    )
                    .messageAction(MessageActionType.SUPPRESS)
                    .build()
            )

            cognito.adminSetUserPassword(
                AdminSetUserPasswordRequest.builder()
                    .userPoolId(cognitoSecrets.userPoolId)
                    .username(request.cpf)
                    .password(cognitoSecrets.password)
                    .permanent(true)
                    .build()
            )

        } catch (e: Exception) {
            throw RuntimeException("Error creating user in Cognito: ${e.message}", e)
        }
    }

    fun authenticateExistingUser(cpf: String): String {
        return try {
            val authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(cognitoSecrets.userPoolId)
                .clientId(cognitoSecrets.clientId)
                .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .authParameters(
                    mapOf(
                        "USERNAME" to cpf,
                        "PASSWORD" to cognitoSecrets.password
                    )
                )
                .build()

            println(authRequest)
            val response = cognito.adminInitiateAuth(authRequest)
            response.authenticationResult().idToken()
        } catch (e: UserNotFoundException) {
            throw RuntimeException("User not found in Cognito", e)
        } catch (e: NotAuthorizedException) {
            throw RuntimeException("Incorrect password or user not confirmed", e)
        } catch (e: Exception) {
            throw RuntimeException("Error authenticating user: ${e.message}", e)
        }
    }
}
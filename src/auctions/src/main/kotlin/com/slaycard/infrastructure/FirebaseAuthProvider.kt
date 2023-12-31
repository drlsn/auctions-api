package com.slaycard.infrastructure

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object FirebaseAdmin {
    private val serviceAccount: InputStream? =
        this::class.java.classLoader.getResourceAsStream("slaycard-auction-firebase-adminsdk.json")

    private val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    fun init(): FirebaseApp = FirebaseApp.initializeApp(options)
}

class FirebaseConfig(name: String?) : AuthenticationProvider.Config(name) {
    internal var authHeader: (ApplicationCall) -> HttpAuthHeader? =
        { call -> call.request.parseAuthorizationHeaderOrNull() }

    var firebaseAuthenticationFunction: AuthenticationFunction<FirebaseToken> = {
        throw NotImplementedError(FirebaseImplementationError)
    }

    fun validate(validate: suspend ApplicationCall.(FirebaseToken) -> User?) {
        firebaseAuthenticationFunction = validate
    }
}

fun ApplicationRequest.parseAuthorizationHeaderOrNull(): HttpAuthHeader? = try {
    parseAuthorizationHeader()
} catch (ex: IllegalArgumentException) {
    println("failed to parse token")
    null
}

private const val FirebaseImplementationError =
    "Firebase  auth validate function is not specified, use firebase { validate { ... } } to fix this"

data class User(val userId: String = "", val displayName: String = "") : Principal

class FirebaseAuthProvider(config: FirebaseConfig) : AuthenticationProvider(config) {
    val authHeader: (ApplicationCall) -> HttpAuthHeader? = config.authHeader
    private val authFunction = config.firebaseAuthenticationFunction

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val token = authHeader(context.call)

        if (token == null) {
            context.challenge(
                FirebaseJWTAuthKey,
                AuthenticationFailedCause.InvalidCredentials) {
                challengeFunc, call ->
                challengeFunc.complete()
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(realm = FIREBASE_AUTH)))
            }
            return
        }

        try {
            val principal = verifyFirebaseIdToken(context.call, token, authFunction)

            if (principal != null)
                context.principal(principal)

        } catch (cause: Throwable) {
            val message = cause.message ?: cause.javaClass.simpleName
            context.error(FirebaseJWTAuthKey, AuthenticationFailedCause.Error(message))
        }
    }
}

suspend fun verifyFirebaseIdToken(
    call: ApplicationCall,
    authHeader: HttpAuthHeader,
    tokenData: suspend ApplicationCall.(FirebaseToken) -> Principal?
): Principal? {
    val token: FirebaseToken = try
    {
        if (authHeader.authScheme == "Bearer" && authHeader is HttpAuthHeader.Single)
        {
            withContext(Dispatchers.IO)
            {
                FirebaseAuth.getInstance().verifyIdToken(authHeader.blob)
            }
        }
        else
            null
    } catch (ex: Exception) {
        ex.printStackTrace()
        return null
    } ?: return null
    return tokenData(call, token)
}

fun HttpAuthHeader.Companion.bearerAuthChallenge(realm: String): HttpAuthHeader =
    HttpAuthHeader.Parameterized("Bearer", mapOf(HttpAuthHeader.Parameters.Realm to realm))

const val FIREBASE_AUTH = "FIREBASE_AUTH"
const val FirebaseJWTAuthKey: String = "FirebaseAuth"

fun AuthenticationConfig.firebase(
    name: String? = FIREBASE_AUTH,
    configure: FirebaseConfig.() -> Unit) {

    val provider = FirebaseAuthProvider(FirebaseConfig(name).apply(configure))
    register(provider)
}

fun Application.configureFirebaseAuth() {
    install(Authentication) {
        firebase {
            validate {
                // TODO look up user profile from DB
                User(it.uid, it.name.orEmpty())
            }
        }
    }
}

package ipn.mx.isc.frontend.data.model

data class User(
    val id: String,
    val nombre: String,
    val apellido: String,
    val email: String,
    val photoUrl: String? = null,
    val telefono: String? = null
) {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}
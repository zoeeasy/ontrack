package net.nemerosa.ontrack.model.security

import java.util.function.Supplier

fun <T> SecurityService.callAsAdmin(call: () -> T): T =
        asAdmin(Supplier { call() })

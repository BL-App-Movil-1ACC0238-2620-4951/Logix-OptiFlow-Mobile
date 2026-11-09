package com.logix.optiflow.domain.model.searchbooking

/** Roles del personal que devuelve el backend al iniciar sesión. */
enum class StaffRole {
    OPTOMETRIST,
    SALES_ADVISOR,
    TECHNICIAN,
    ADMIN,
    ;

    companion object {
        fun from(value: String?): StaffRole? = entries.firstOrNull { it.name == value }
    }
}

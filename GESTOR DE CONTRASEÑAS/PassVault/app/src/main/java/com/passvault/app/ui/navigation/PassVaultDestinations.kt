package com.passvault.app.ui.navigation

object PassVaultDestinations {
    const val ARG_ENTRY_ID = "entryId"

    const val CREATE_MASTER_PASSWORD_ROUTE = "create_master_password"
    const val LOGIN_ROUTE = "login"
    const val SHOW_RECOVERY_CODE_ROUTE = "show_recovery_code"
    const val RECOVER_ACCESS_ROUTE = "recover_access"
    const val RESET_MASTER_PASSWORD_ROUTE = "reset_master_password"

    const val LIST_ROUTE = "list"
    const val SETTINGS_ROUTE = "settings"
    const val DETAIL_ROUTE = "detail/{$ARG_ENTRY_ID}"
    const val EDIT_ROUTE = "edit?$ARG_ENTRY_ID={$ARG_ENTRY_ID}"

    const val SECURITY_AUDIT_ROUTE = "security_audit"
    const val EXPORT_ROUTE = "export"
    const val IMPORT_ROUTE = "import"

    fun detail(entryId: Long) = "detail/$entryId"
    fun editNew() = "edit"
    fun editExisting(entryId: Long) = "edit?$ARG_ENTRY_ID=$entryId"
}
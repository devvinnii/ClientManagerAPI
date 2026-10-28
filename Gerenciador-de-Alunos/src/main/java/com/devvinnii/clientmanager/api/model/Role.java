package com.devvinnii.clientmanager.api.model;

public enum Role {
    ADMIN,
    CHEFE,
    FUNCIONARIO,

    /**
     * Perfil legado mantido para usuários existentes durante a transição.
     * Novos usuários devem receber CHEFE ou FUNCIONARIO conforme suas atribuições.
     */
    @Deprecated(since = "1.0", forRemoval = false)
    OPERADOR
}

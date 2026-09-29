package com.techup.gestao_patrimonio_imobiliario.core.auth;

/** Dados do usuario extraidos de um ID token do Google ja validado. */
public record GoogleIdentidade(String sub, String email, String nome) {
}

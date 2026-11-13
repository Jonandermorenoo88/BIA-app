package com.bia.app.bia_app.security;

import java.security.SecureRandom;

/** Política de contraseñas y generación de contraseñas temporales. */
public final class PasswordPolicy {

    public static final int LONGITUD_MINIMA = 12;

    private static final String ALFABETO =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789-_.!@#";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordPolicy() {
    }

    /** @return mensaje de error, o null si la contraseña cumple la política. */
    public static String validar(String password) {
        if (password == null || password.length() < LONGITUD_MINIMA) {
            return "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres";
        }
        if (password.length() > 72) {
            return "La contraseña no puede superar 72 caracteres";
        }
        boolean letra = password.chars().anyMatch(Character::isLetter);
        boolean digito = password.chars().anyMatch(Character::isDigit);
        if (!letra || !digito) {
            return "La contraseña debe combinar letras y números";
        }
        return null;
    }

    /** Contraseña aleatoria que cumple la política (para altas y reseteos). */
    public static String generarTemporal() {
        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
            }
            String pwd = sb.toString();
            if (validar(pwd) == null) {
                return pwd;
            }
        }
    }
}

package com.B.carrasco.burgerapp.utils;

import android.content.Context;
import android.widget.Toast;
import java.util.regex.Pattern;

public class ValidationUtils {

    public static boolean validateLoginFields(Context context, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 3) {
            Toast.makeText(context, "El usuario debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 4) {
            Toast.makeText(context, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        return pattern.matcher(email).matches();
    }

    public static boolean validatePhone(String phone) {
        // Formato chileno: +56912345678
        return phone.matches("^\\+569[0-9]{8}$");
    }

    public static boolean validatePasswordStrength(String password) {
        // Mínimo 6 caracteres, al menos una letra y un número
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }
}
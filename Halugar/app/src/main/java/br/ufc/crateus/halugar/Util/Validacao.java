package br.ufc.crateus.halugar.Util;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validacao {

    public static boolean validarEmail(String email){

        return email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$");
    }

    public static boolean validarTelefone(String telefone){

        return telefone.matches("([1-9][0-9][9][1-9][0-9]{3}[0-9]{4})|([1-9][0-9][1-9][0-9]{3}[0-9]{4})|([1-9][0-9][0-9]{4}[0-9]{4})$");
    }

    public static boolean validarNome(String nomeCompleto){

        Log.i("NOME", nomeCompleto);

        // Procurando por caracteres inválidos como emojis:

        for (int i = 0; i < nomeCompleto.length(); i++)
        {
            final char hs = nomeCompleto.charAt(i);

            if (0xd800 <= hs && hs <= 0xdbff)
            {
                final char ls = nomeCompleto.charAt(i + 1);
                final int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;

                if (0x1d000 <= uc && uc <= 0x1f77f)
                {
                    return false;
                }
            }
            else if (Character.isHighSurrogate(hs))
            {
                final char ls = nomeCompleto.charAt(i + 1);

                if (ls == 0x20e3)
                {
                    return false;
                }
            }
            else
            {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff)
                {
                    return false;
                }
                else if (0x2B05 <= hs && hs <= 0x2b07)
                {
                    return false;
                }
                else if (0x2934 <= hs && hs <= 0x2935)
                {
                    return false;
                }
                else if (0x3297 <= hs && hs <= 0x3299)
                {
                    return false;
                }
                else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c || hs == 0x2b1b || hs == 0x2b50)
                {
                    return false;
                }
            }
        }

        if (!nomeCompleto.contains(" ")){
            return false;
        }
        else{

            return nomeCompleto.matches( "^[\\p{L} .'-]+$" );
        }
    }

    public static boolean validarInput(String texto){

        for (int i = 0; i < texto.length(); i++)
        {
            final char hs = texto.charAt(i);

            if (0xd800 <= hs && hs <= 0xdbff)
            {
                final char ls = texto.charAt(i + 1);
                final int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;

                if (0x1d000 <= uc && uc <= 0x1f77f)
                {
                    return false;
                }
            }
            else if (Character.isHighSurrogate(hs))
            {
                final char ls = texto.charAt(i + 1);

                if (ls == 0x20e3)
                {
                    return false;
                }
            }
            else
            {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff)
                {
                    return false;
                }
                else if (0x2B05 <= hs && hs <= 0x2b07)
                {
                    return false;
                }
                else if (0x2934 <= hs && hs <= 0x2935)
                {
                    return false;
                }
                else if (0x3297 <= hs && hs <= 0x3299)
                {
                    return false;
                }
                else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c || hs == 0x2b1b || hs == 0x2b50)
                {
                    return false;
                }
            }
        }

        return true;
    }
}

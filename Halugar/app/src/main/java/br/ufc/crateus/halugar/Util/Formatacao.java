package br.ufc.crateus.halugar.Util;

public class Formatacao {

    public static String formatarCep(String cep){ ;

        return cep.substring(0, 5) + "-" + cep.substring(5);
    }

    public static String formatarTelefone(String telefone){

        if(telefone.length()==11){
            return "(" + telefone.substring(0,2) + ") " + telefone.substring(2,7) + "-" + telefone.substring(7);
        }

        if(telefone.length()==10){
            return "(" + telefone.substring(0,2) + ") " + telefone.substring(2,6) + "-" + telefone.substring(6);
        }

        return null;
    }
}

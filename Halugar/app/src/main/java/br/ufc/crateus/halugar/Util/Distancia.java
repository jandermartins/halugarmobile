package br.ufc.crateus.halugar.Util;

public class Distancia {

    public static double calcularDistancia(double latitudeUsuario, double longitudeUsuario, double latitudeAnuncio, double longitudeAnuncio){

        double pk = (double) (180.f/Math.PI);

        double a1 = latitudeUsuario / pk;
        double a2 = longitudeUsuario / pk;
        double b1 = latitudeAnuncio / pk;
        double b2 = longitudeAnuncio / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return (6366000 * tt)/1000;
    }
}

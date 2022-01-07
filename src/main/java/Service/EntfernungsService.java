/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

/**
 * <h1>Service zum Berechnen von Entfernungen</h1>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
public class EntfernungsService{

    /**
     * Diese Methode berechnet die Entfernung zwischen zwei Punkten un Kilometern
     *
     * @param a Koordinaten des ersten Punktes
     * @param b Koordinaten des zweiten Punktes
     * @return Entfenrung in Kilometern
     */
    public double berechneEntfernung(Double[] a, Double[] b){

        final int R = 6371; // Radious of the earth
        Double lat1 = a[0];
        Double lon1 = a[1];
        Double lat2 = b[0];
        Double lon2 = b[1];
        Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lon2 - lon1);
        Double v = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(v), Math.sqrt(1 - v));
        Double distance = R * c;

        return distance;
    }

    final double pi = 3.1415;

    /**
     * gute Frage xD
     *
     * @param value ?
     * @return ?
     */
    private static Double toRad(Double value){
        return value * Math.PI / 180;
    }

}

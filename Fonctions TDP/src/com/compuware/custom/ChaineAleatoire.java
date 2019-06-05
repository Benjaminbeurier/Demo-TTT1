package com.compuware.custom;

import java.util.List;
import com.compuware.dataprivacy.userdefined.IUserDefinedFunction;


public class ChaineAleatoire implements IUserDefinedFunction

{
	public Object evaluate(List<Object> params) throws Exception {

	Integer nbcar = (Integer)params.get(0);
	String liste_car = (String)params.get(1);
	String resultat = "";
//	String liste_car = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	for(int i = 0; i <= nbcar - 1; i++) { 
		int j = (int)Math.floor(Math.random() * (liste_car.length() -1));
	    resultat = resultat + liste_car.charAt(j);
	}
	  return resultat;
//	  return "ABCDEF";
    }
}




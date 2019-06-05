package com.compuware.custom;

import java.util.List;
import com.compuware.dataprivacy.userdefined.IUserDefinedFunction;


public class CarteCle implements IUserDefinedFunction
// Format : 1 = entrée/sortie = numéro de carte + clé (16 chiffres)
// Format : 2 = entrée numéro de carte (15 chiffres) / sortie Clé (1 chiffre)

{
	public Object evaluate(List<Object> params) throws Exception {
		Integer format_fonction = (Integer)params.get(0);
		String  num_carte = (String)params.get(1);

		int cle = 0;
		boolean alternate = false;
		for (int i = num_carte.length() - 1; i >= 0; i--)
		     {
		       int n = Integer.parseInt(num_carte.substring(i, i + 1));
		       if (alternate)
		         {
		            n *= 2;
		            if (n > 9)
		               {
		                 n = (n % 10) + 1;
		               }
	             }
		      cle += n;
		      alternate = !alternate;
		   }
	String Cle_X = String.valueOf(cle);
	Cle_X = Cle_X.substring(Cle_X.length() - 1);
			
			
    if (format_fonction == 2) { 
	  return String.valueOf(Cle_X);}
    else {
          return 	num_carte.substring(0,14) + Cle_X; }

    }
}



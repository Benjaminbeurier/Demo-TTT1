package com.compuware.custom;

import java.util.List;
import com.compuware.dataprivacy.userdefined.IUserDefinedFunction;


public class CompteCléRib implements IUserDefinedFunction
// Format : 1 = entrée/sortie = compte + clé
// Format : 2 = entrée compte / sortie Clé rib

{
	public Object evaluate(List<Object> params) throws Exception {

	Integer format_fonction = (Integer)params.get(0);
	String  compte = (String)params.get(1);
	String cle_rib_x = "";
	
	String Transc_In  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	String Transc_Out = "123456789123456789234567890123456789";

	compte = compte + "000000000000000000";
	compte = compte.substring(0,22);
	final StringBuilder buffer = new StringBuilder(compte);
	
	for(int i = 0 ;i<20;i++)
		{

		buffer.setCharAt(i,Transc_Out.charAt( Transc_In.indexOf(buffer.charAt(i))));
		}
	
	int n1 = Integer.parseInt(buffer.substring(0,7));
	int n2 = Integer.parseInt(buffer.substring(7,14));
	int n3 = Integer.parseInt(buffer.substring(14,21));
	
	int cle_rib_9 = 07 - (62 * n1 + 34 * n2 + 3 * n3) % 97;
	if (cle_rib_9 < 10) {
		cle_rib_x =  "0" + String.valueOf(cle_rib_9);}
	else {
		cle_rib_x =  String.valueOf(cle_rib_9);}

	
    if (format_fonction == 2) { 
	  return cle_rib_x;}
    else {
      return 	compte.substring(0,22) + cle_rib_x; }

    }
}


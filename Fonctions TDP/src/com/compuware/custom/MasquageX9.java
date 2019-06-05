package com.compuware.custom;


	import java.util.List;
	import com.compuware.dataprivacy.userdefined.IUserDefinedFunction;


	public class MasquageX9 implements IUserDefinedFunction
		{
			public Object evaluate(List<Object> params) throws Exception {

			String Valeur_clair = (String)params.get(0);
			String Valeur_anonyme = "";
				
			for(int i = 0; i <= Valeur_clair.length() - 1; i++) {
				char octet = Valeur_clair.charAt(i);
				int ascii = (int) octet;
				if (97 <= ascii &&  ascii <= 122){Valeur_anonyme = Valeur_anonyme + "x";}
				else if (65 <= ascii &&  ascii <= 90){Valeur_anonyme = Valeur_anonyme + "X";}
				else if (48 <= ascii &&  ascii <= 57){Valeur_anonyme = Valeur_anonyme + "9";}
				else if (192 <= ascii &&  ascii <= 223){Valeur_anonyme = Valeur_anonyme + "X";}
				else if (224 <= ascii &&  ascii <= 255){Valeur_anonyme = Valeur_anonyme + "X";}
				else {Valeur_anonyme = Valeur_anonyme + octet;}
			}	
			return Valeur_anonyme;
			}
		}


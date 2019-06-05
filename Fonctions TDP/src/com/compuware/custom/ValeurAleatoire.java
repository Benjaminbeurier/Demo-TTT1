package com.compuware.custom;
import java.util.List;
import java.util.Random;

import com.compuware.dataprivacy.userdefined.IUserDefinedFunction;

public class ValeurAleatoire implements IUserDefinedFunction
{
	public Object evaluate(List<Object> params) throws Exception {

	Integer min = (Integer)params.get(0);
	Integer max = (Integer)params.get(1);
		
	Random rand = new Random();
	int nombreAleatoire = rand.nextInt(max - min + 1) + min;
	
	return nombreAleatoire;
	}
}

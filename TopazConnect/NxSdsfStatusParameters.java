/**
 * These materials contain confidential information and trade secrets of Compuware Corporation. You shall maintain the materials 
 * as confidential and shall not disclose its contents to any third party except as may be required by law or regulation. Use, 
 * disclosure, or reproduction is prohibited without the prior express written permission of Compuware Corporation.
 *  
 * All Compuware products listed within the materials are trademarks of Compuware Corporation. All other company or product 
 * names are trademarks of their respective owners.
 *  
 * Copyright (c) 2017 Compuware Corporation. All rights reserved.
 */
package com.compuware.topazc.commands;

import com.compuware.topazc.model.NxSdsfStatus;

/**
 * @author Stephen
 */
public class NxSdsfStatusParameters implements NxReadParameters<NxSdsfStatus> {

	private String jobPrefix;
	private String jobOwner;
	private String token;

	public NxSdsfStatusParameters(String jobPrefix, String jobOwner) {
		this.jobPrefix = jobPrefix;
		this.jobOwner = jobOwner;
	}

	@Override
	public String[] getParameters() {
		return new String[]{
				jobPrefix,
				jobOwner
		};
	}

	@Override
	public String getTranslateMode() {
		return null;
	}

	@Override
	public String[] getReadParameters() {
		return new String[] {token};
	}

	public void setToken(String token) {
		this.token = token;
	}

}

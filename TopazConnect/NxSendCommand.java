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
package com.compuware.topazc.util;

import java.io.OutputStream;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

public class NxSendCommand
  extends Thread
{
  private NxSocket socket;
  private String cmd;
  
  NxSendCommand(NxSocket socket, String cmd)
  {
    this.socket = socket;
    
    this.cmd = cmd.replaceAll("([^\r])\n", "$1\r\n").replaceAll("^\n", 
      "\r\n");
  }
  
  public void run()
  {
    if (this.socket.isConnected())
    {
      OutputStream remoteOutput = this.socket.getOutputStream();
      try
      {
        if ((this.cmd != null) && (this.socket.isConnected()))
        {
          remoteOutput.write(this.cmd.getBytes());
          remoteOutput.flush();
        }
      }
      catch (Exception e)
      {
        StatusManager.getManager().handle(
          new Status(4, "com.compuware.topazc", "Communication error with Topaz Connect", e), 3);
      }
    }
  }
}

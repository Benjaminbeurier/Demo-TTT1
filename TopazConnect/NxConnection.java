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

import com.compuware.topazc.exceptions.NxConnectionException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.osgi.framework.FrameworkUtil;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stephen
 */
public class NxConnection {

    private NxSocket socket;
    private NxSendCommand sendCommand;
    private String commandThread;
    private InputStream inputStream;
    private IDialogSettings settings;
    private NxOutput prompt;
    private NxOutput response;
    private BufferedReader topazCReader;

    private static final String CDATA_MATCH = ".*!\\[CDATA\\[.*";

    public NxConnection(IDialogSettings settings) {
        this.settings = settings;
    }


    public void connect() throws NxConnectionException {

        String ipAddress = settings.get(NxInfo.KEY_CONNECTION_SERVER);
        String str_port = settings.get(NxInfo.KEY_CONNECTION_PORT);
        
        int port;
        try {
        	port = Integer.parseInt(str_port);
        } catch (Exception e) {
        	throw new NxConnectionException("Port number is 0 or not specified. Check connection parameters.", e);
        }

        socket = new NxSocket(settings.getBoolean(NxInfo.KEY_CONNECTION_TLS));

        try {
            socket.connect(ipAddress, port);
            inputStream = null;
            topazCReader = null;
        } catch (Exception ex) {
            disconnect();
            throw new NxConnectionException("TopazC is unreachable.", ex);
        }
    }


    public void disconnect() {
        if (socket != null && socket.isConnected()) {
            socket.disconnect();
            inputStream = null;
            topazCReader = null;
        }

    }

    public void readResponse() throws IOException {
        if (inputStream == null) {
            inputStream = socket.getInputStream();
        }

        String[] array = readInputStreamToArray(inputStream);

        if (array[1] != null) {
            prompt = getNxOutput(array[1]);
            response = getNxOutput(array[0]);

            //Clean up CDATA XML if present in string
            if (response.getBuffer().matches(CDATA_MATCH)) {
                response.setBuffer(cleanCdataBlock(response.getBuffer()));
            }
        } else {
            prompt = getNxOutput(array[0]);
            response = null;
        }
    }

    private Pattern cdataPattern = Pattern.compile("(!\\[CDATA\\[)(.*?)(\\]\\])");

    private String cleanCdataBlock(String inputBuffer) {

        Matcher matcher = cdataPattern.matcher(inputBuffer);

        StringBuffer buffer = new StringBuffer(inputBuffer.length());
        while (matcher.find()) {
            String content = matcher.group(2);
            content = content.replaceAll("&","&amp;");
            content = content.replaceAll("<","&lt;");
            content = content.replaceAll(">","&gt;");
            matcher.appendReplacement(buffer,Matcher.quoteReplacement(content));
        }

        matcher.appendTail(buffer);

        return buffer.toString();
    }

    public void login(NxOutput prompt) {
		String vrmArray[] = FrameworkUtil.getBundle(getClass()).getVersion().toString().split("\\.");
		String vrm;
		if (vrmArray.length == 4) {
			vrm = "V"+vrmArray[0]+"R"+vrmArray[1]+"M"+vrmArray[2];
		} else {
			vrm = "";
		}
	       String command = String.format("%s,%s,%s,%s,%s,%s\n",
	                prompt.getSequence(),
	                NxMatrix.CMD_LOGIN,
	                settings.get(NxInfo.KEY_CONNECTION_USER),
	                StringObfuscator.aesDecrypt(settings.get(NxInfo.KEY_CONNECTION_PASSWORD)),
	                vrm,
	                StringObfuscator.aesDecrypt(settings.get(NxInfo.KEY_CONNECTION_NEW_PASSWORD))
	        		);

        this.sendCommand = new NxSendCommand(socket, command);
        this.sendCommand.setPriority(Thread.currentThread().getPriority() + 1);
        this.sendCommand.start();
        this.commandThread = this.sendCommand.getName();
    }

     
    public void logoff(NxOutput prompt) {
        String command = String.format("%s,%s\n",
                prompt.getSequence(),
                NxMatrix.CMD_LOGOFF);
        this.sendCommand = new NxSendCommand(socket, command);
        this.sendCommand.setPriority(Thread.currentThread().getPriority() + 1);
        this.sendCommand.setName(this.commandThread);
        this.sendCommand.start();
        try {
            this.sendCommand.join();
        } catch (InterruptedException ignored) { }
    }

    public void sendCommand(NxOutput prompt, String code, String operation, String... parameters) throws IOException, NxConnectionException {

        //open
        String command = String.format("%s,%s,%s", prompt.getSequence(), code, operation.isEmpty() ? "" : operation + " ");
        command = insertParameters(command, parameters);
 
        command += "\n";
        this.sendCommand = new NxSendCommand(socket, command);
        this.sendCommand.setPriority(Thread.currentThread().getPriority() + 1);
        this.sendCommand.setName(this.commandThread);
        this.sendCommand.start();
        readResponse();
    }

    private String insertParameters(String command, String[] parameters) {
        StringBuilder builder = new StringBuilder(command);

        for (String param : parameters) {
            builder.append(param).append(" ");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    /**
     * Reads a new input stream into a String array.
     *
     * @param stream The input stream to read.
     * @return The new string array of size 2
     * @throws java.io.IOException
     */
    private String[] readInputStreamToArray(InputStream stream) throws IOException {


        if (topazCReader == null) {
           topazCReader = new BufferedReader(new InputStreamReader(stream));
        }

        String[] buffer = new String[2];
        int index = 0;

        String line;
        line = NxUtils.readLine(topazCReader);
        // Read lines until we don't begin with a word character: [a-zA-Z_0-9]

        while (!line.matches(NxPatternStr.wordBeginPattern)) {
            line = NxUtils.readLine(topazCReader);
        }

        buffer[index] = line;
        if (line.matches(NxPatternStr.nxTOutputPattern)) {
            String moreLine;

            if (line.matches(NxPatternStr.nxCoOutputPattern)) {
                for (; ; ) {
                    moreLine = NxUtils.readLine(topazCReader);

                    if (moreLine.matches(NxPatternStr.nxGOutputPattern)) {
                        buffer[(index + 1)] = moreLine;
                        break;
                    }
                    line = line + System.getProperty("line.separator") + moreLine;
                    buffer[index] = line;
                }
            }
            if (((line.matches(NxPatternStr.nxFOutputPattern)) || (line.matches(NxPatternStr.nxTOutputPattern)))
                    && (!line.matches(NxPatternStr.nxTerminatingOutputPattern))) {
                line = null;
                while (line == null) {
                    line = NxUtils.readLine(topazCReader);
                    buffer[(index + 1)] = line;
                }
            }
        }
        return buffer;
    }

    private static Pattern outputPattern = Pattern.compile("(\\d+),(\\d+),?((?s).*)");

    private NxOutput getNxOutput(String buffer) {
        NxOutput output = new NxOutput();
        if (buffer == null) {
            return null;
        }
        String inputStr = "";
        Matcher matcher = outputPattern.matcher(inputStr);
        matcher.reset(buffer);
        if (matcher.matches()) {
            output.setSequence(matcher.group(1));
            output.setCommand(matcher.group(2));
            output.setBuffer(matcher.group(3));
        } else {
            return null;
        }
        return output;
    }

    public NxOutput getPrompt() {
        return prompt;
    }

    public NxOutput getResponse() {
        return response;
    }

    public boolean isConnected() {
        return socket.isConnected();
    }
}

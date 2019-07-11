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

import com.compuware.topazc.NxActivator;
import com.compuware.topazc.exceptions.TopazCException;
import com.compuware.topazc.exceptions.NxConnectionException;
import com.compuware.topazc.model.*;
import com.compuware.topazc.util.*;
import com.compuware.topazc.view.NxLogView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import javax.xml.bind.JAXBException;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Stephen
 */
public abstract class NxReadCommand<T extends Mergable<T> & HasStatus, P extends NxReadParameters<T>> extends NxCommand<T, P> {

    protected Operation currentOp;
    public NxReadCommand(ConnectionContext context) {
        super(context);
    }

    @Override
    public NxResult<T> runCommand(final Display display, final P parameters) throws InvocationTargetException, InterruptedException {

        final NxResult<T> result = createResult();

        final IRunnableWithProgress op = new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                runWithMonitor(display, monitor, result, parameters);
            }
        };

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(display.getActiveShell());
        dialog.run(true, true, op);

        return result;
    }

    @Override
    public NxResult<T> runCommand(Display display, P parameters, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        final NxResult<T> result = createResult();
        runWithMonitor(display, monitor, result, parameters);
        return result;
    }

    private NxResult<T> runWithMonitor(Display display, IProgressMonitor monitor, final NxResult<T> result, P parameters) {

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }

        result.setStatus(NxStatus.OK);
        monitor.beginTask(commandMessage(), 80);
        NxOutput response = null;
        try {
            T model;

            currentOp = Operation.OPEN;
            do {
                response = context.sendCommand(getCommandCode(), currentOp.getOpCode(), (parameters != null ? parameters.getParameters() : new String[]{}));

                model = parseResponse(response.getBuffer());
                advanceProgress(monitor);
            } while (!validateOpen(display, parameters, model));

            boolean eof = false;

            if(isLoggerInactive()){
                response = context.sendCommand(NxMatrix.CMD_DEACTIVATE_LOGGER, "");
                if (!response.getBuffer().matches(NxMatrix.SUCCESS_PATTERN_0)) {
                    throw new TopazCException("Cannot pause Logger");
                }
            }

            currentOp = Operation.READ;
            while (!eof) {
                response = context.sendCommand(getCommandCode(), currentOp.getOpCode(), parameters.getReadParameters());

                model = parseResponse(response.getBuffer());

                switch (model.getStatus()) {
                    case OK:
                        if (result.getModel() == null) {
                            result.setModel(model);
                        } else {
                            result.getModel().mergeModel(model);
                        }
                        break;
                    case EOF:
                        eof = true;
                        break;
                    case WARNING:
                    	break;
                    case FAIL:
                    case ERROR:
                        if (model.getMessage() != null) {
                            throw new NxConnectionException(model.getMessage());
                        }
                        throw new NxConnectionException("Topaz Connect status: " + model.getStatus().toString());

                }

                advanceProgress(monitor);
            }

            if(isLoggerInactive()){
                response = context.sendCommand(NxMatrix.CMD_ACTIVATE_LOGGER, "");
            }
            currentOp = Operation.CLOSE;

            response = context.sendCommand(getCommandCode(), currentOp.getOpCode());
            model = parseResponse(response.getBuffer());
            if (model.getStatus() != NxStatus.OK) {
                throw new NxConnectionException(model.getMessage());
            }
        } catch (NxConnectionException e) {
            result.setStatus(NxStatus.ERROR);
            result.setMessage(e.getMessage());
        } catch (TopazCException e) {
            result.setStatus(e.getStatus());
            result.setMessage(e.getMessage());
        } catch (final JAXBException e) {
            result.setStatus(NxStatus.ERROR);
            if (e.getCause() != null) {
                result.setMessage(String.format("Cannot read Topaz Connect XML data: %s", e.getCause().getMessage()));
            } else {
                result.setMessage(String.format("Cannot read Topaz Connect XML data: %s", e.getMessage()));
            }
            NxActivator.getDefault().getLog().log(new Status(Status.ERROR, "com.compuware.topazc", "XML Error: " + response.getBuffer()));
        } finally {
            monitor.done();
            display.asyncExec(new Runnable() {
                @Override
                public void run() {

                	if (result.getMessage() != null) {
                        if (result.getStatus() == NxStatus.OK) {
                            NxLogView.displayToLogView("I", result.getMessage());
                        } else if (result.getStatus() == NxStatus.WARNING) {
                            NxLogView.displayToLogView("W", result.getMessage());
                        } else {
                        	NxLogView.displayToLogView("E", result.getMessage());
                        }
                    }
                    else {
                        if (result.getStatus() != NxStatus.OK) {
                            NxLogView.displayToLogView("E", commandMessage() + " failed");
                        } else {
                            NxLogView.displayToLogView("I", commandMessage() + " succeeded");
                        }
                    }
                }
            });
        }

        return result;
    }
}

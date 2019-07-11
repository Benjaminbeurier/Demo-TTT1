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

import com.compuware.topazc.exceptions.TopazCException;
import com.compuware.topazc.exceptions.NxConnectionException;
import com.compuware.topazc.model.*;
import com.compuware.topazc.util.ConnectionContext;
import com.compuware.topazc.util.NxResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Display;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.StringReader;

/**
 * @author Stephen
 */
public abstract class NxCommand<T extends HasStatus, P extends NxParameters<T>> implements TopazCCommand<T,P> {
    protected static final String PUT = "PUT";
    protected static final String PUT_BIN = "PUTBIN";
    protected static final String READ = "READ";
    protected static final String READ_BIN = "READBIN";
    protected static final String OPEN = "OPEN";
    private boolean deactivateLogger = false;

    protected ConnectionContext context;


    public NxCommand(ConnectionContext context) {
        this.context = context;
    }

    protected static void advanceProgress(IProgressMonitor monitor) {
        monitor.worked(10);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }
    protected NxResult<T> createResult(){
        return new NxResult<T>();
    }

    protected NxResult<T> createResult(NxStatus status){
        return new NxResult<T>(status);
    }


    protected abstract String getCommandCode();

    protected abstract String commandMessage();

    protected boolean validateOpen(Display display, P parameters, T model) throws TopazCException {

        if(model != null && model.getStatus() == NxStatus.OK) {
            return true;
        }
        
        if(model != null && model.getStatus() == NxStatus.WARNING) {
            throw new TopazCException(model.getMessage(), model.getStatus());
        }
        

        if(model.getMessage() != null){
            throw new NxConnectionException(model.getMessage());
        }
        throw new NxConnectionException("Topaz Connect status code: " + model.getStatus().toString());

    }

    protected boolean validateClose(Display display, P parameters, T model) throws NxConnectionException {
        return true;
    }

    protected T parseResponse(String buffer) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(
                NxBasePackagesInstall.class,
                NxBaseListPackageInstallSites.class,
                NxListStage.class,
                NxListEnvironment.class,
                NxListSystem.class,
                NxListSubsystem.class,
                NxListType.class,
                NxListProcessorGroups.class,
                NxBrowseElement.class,
                NxBrowseElementInBatch.class,
                NxListPackages.class,
                NxListPackageCastReport.class,
                NxCheckOutElement.class,
                NxCheckInElement.class,
                NxListElementMaster.class,
                NxDisplayElementChanges.class,
                NxListElementComponents.class,
                NxElementComponents.class,
                NxDisplayListing.class,
                NxListPackageSummary.class,
                NxPackagesInstall.class,
                NxListPackageInstallSites.class,
                NxListPackageApproverGroups.class,
                NxApproverGroup.class,
                NxApprover.class,
                NxGetLongMessageDdname.class,
                NxModifyPackageSclEditor.class,
                NxModifyPackageHeader.class,
                NxModifyPackageFromScl.class,
                NxModifyPackageFromTreeList.class,
                NxGenerateElement.class,
                NxMoveElement.class,
                NxListPackageSCL.class,
                NxCastPackage.class,
                NxListPromotionSiteAreas.class,
                NxRemotePromoteComponent.class,
                NxRemotePromoteComponentSubmit.class,
                NxReviewPackage.class,
                NxResetPackage.class,
                NxPackageSubmit.class,
                NxDeletePackage.class,
                NxSdsfStatus.class,
                NxSdsfBrowseDdname.class,
                NxSdsfBrowseJob.class,
                NxSdsfListDdnames.class,
                NxDeleteElement.class,
                NxListElementSummary.class,
                NxSigninElement.class,
                NxTypeInfo.class,
                NxElementProcessorGroup.class,
                PutBulkSize.class,
                NxBOACreatePackage.class,
                NxCreatePackage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        buffer = cleanXML(buffer);

        T model = (T) unmarshaller.unmarshal(new StringReader(buffer));

        return model;
    }

    /**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public String cleanXML(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    public boolean isLoggerInactive() {
        return deactivateLogger;
    }

    public void setLoggerInactive(boolean deactivateLogger) {
        this.deactivateLogger = deactivateLogger;
    }

    protected enum Operation{
        OPEN("OPEN"),
        READ("READ"),
        PUT("PUT"),
        CLOSE("CLOSE"),
        NULL("");

        private String opCode;
        Operation(String opCode) {
            this.opCode = opCode;
        }

        public String getOpCode() {
            return opCode;
        }
    }
}


package eu.planets_project.ifr.core.wdt.common.services.dataRegistry;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


/**
 * JBossWS Generated Source
 * 
 * Generation Date: Wed Apr 23 14:12:08 CEST 2008
 * 
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 * 
 * JAX-WS Version: 2.0
 * 
 */
@WebService(name = "DataManager", targetNamespace = "http://planets-project.eu/ifr/core/storage/data")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface DataManager {


    /**
     * 
     * @param arg1
     * @param arg0
     * @throws SOAPException_Exception
     */
    @WebMethod
    public void addFile(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0,
        @WebParam(name = "arg1", partName = "arg1")
        String arg1)
        throws SOAPException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns net.java.dev.jaxb.array.AnyURIArray
     * @throws SOAPException_Exception
     */
    @WebMethod
    @WebResult(partName = "return")
    public AnyURIArray list(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0)
        throws SOAPException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     * @throws SOAPException_Exception
     */
    @WebMethod
    @WebResult(partName = "return")
    public String listDownladURI(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0)
        throws SOAPException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     * @throws SOAPException_Exception
     */
    @WebMethod
    @WebResult(partName = "return")
    public String read(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0)
        throws SOAPException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @throws SOAPException_Exception
     */
    @WebMethod
    public void store(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0,
        @WebParam(name = "arg1", partName = "arg1")
        String arg1)
        throws SOAPException_Exception
    ;

}

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.webadmin.httpd;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.openejb.webadmin.HttpRequest;
import org.apache.openejb.webadmin.HttpResponse;
import org.apache.openejb.webadmin.HttpSession;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.OpenEjbVersion;

/** This class takes care of HTTP Responses.  It sends data back to the browser.
 */
public class HttpResponseImpl implements HttpResponse {

    /** Response string */
    private String responseString = "OK";

    /** Code */
    private int code = 200;

    /** Response headers */
    private HashMap headers;

    /** Response body */
    private byte[] body = new byte[0];

    /** the writer for the response */
    private transient PrintWriter writer;
    /** the raw body */
    private transient ByteArrayOutputStream baos;

    /** the HTTP version */
    public static final String HTTP_VERSION = "HTTP/1.1";
    /** a line feed character */
    public static final String CRLF = "\r\n";
    /** a space character */
    public static final String SP = " ";
    /** a colon and space */
    public static final String CSP = ": ";
    /** the server to send data from */
    public static String server;

    private HttpRequestImpl request;
    private URLConnection content;

    protected void setRequest(HttpRequestImpl request){
        this.request = request;
    }

    /** sets a header to be sent back to the browser
     * @param name the name of the header
     * @param value the value of the header
     */
    public void setHeader(String name, String value){
        headers.put(name, value);
    }

    /** Gets a header based on the name passed in
     * @param name The name of the header
     * @return the value of the header
     */
    public String getHeader(String name){
        return (String) headers.get(name);
    }

    /** Gets the PrintWriter to send data to the browser
     * @return the PrintWriter to send data to the browser
     */
    public PrintWriter getPrintWriter(){
        return writer;
    }

    /** gets the OutputStream to send data to the browser
     * @return the OutputStream to send data to the browser
     */
    public OutputStream getOutputStream(){
        return baos;
    }

    /** sets the HTTP response code to be sent to the browser.  These codes are:
     *
     * OPTIONS = 0
     * GET     = 1
     * HEAD    = 2
     * POST    = 3
     * PUT     = 4
     * DELETE  = 5
     * TRACE   = 6
     * CONNECT = 7
     * UNSUPPORTED = 8
     * @param code the code to be sent to the browser
     */
    public void setCode(int code){
        this.code = code;
    }

    /** gets the HTTP response code
     * @return the HTTP response code
     */
    public int getCode(){
        return code;
    }

    /** sets the content type to be sent back to the browser
     * @param type the type to be sent to the browser (i.e. "text/html")
     */
    public void setContentType(String type){
        setHeader("Content-Type", type);
    }

    /** gets the content type that will be sent to the browser
     * @return the content type (i.e. "text/html")
     */
    public String getContentType(){
        return getHeader("Content-Type");
    }

    /** Sets the response string to be sent to the browser
     * @param responseString the response string
     */
    public void setResponseString(String responseString){
       this.responseString = responseString;
    }

    /** Sets the response string to be sent to the browser
     * @return the response string
     */
    public String getResponseString(){
        return responseString;
    }

    /** resets the data to be sent to the browser */
    public void reset(){
        initBody();
    }

    /** resets the data to be sent to the browser with the response code and response
     * string
     * @param code the code to be sent to the browser
     * @param responseString the response string to be sent to the browser
     */
    public void reset(int code, String responseString){
        setCode(code);
        setResponseString(responseString);
        initBody();
    }

    /*------------------------------------------------------------*/
    /*  Methods for writing out a response                        */
    /*------------------------------------------------------------*/
    /** creates a new instance of HttpResponseImpl with default values */
    protected HttpResponseImpl(){
        this(200, "OK", "text/html");
    }

    /** Creates a new HttpResponseImpl with user provided parameters
     * @param code the HTTP Response code, see <a href="http://www.ietf.org/rfc/rfc2616.txt">http://www.ietf.org/rfc/rfc2616.txt</a>
     * for these codes
     * @param responseString the response string to be sent back
     * @param contentType the content type to be sent back
     */
    protected HttpResponseImpl(int code, String responseString, String contentType){
        this.responseString = responseString;
        this.headers = new HashMap();
        this.code = code;

        // Default headers
        setHeader("Server", getServerName());
        setHeader("Connection","close");
        setHeader("Content-Type",contentType);

        // create the body.
        initBody();
    }

    /** Takes care of sending the response line, headers and body
     *
     * HTTP/1.1 200 OK
     * Server: Netscape-Enterprise/3.6 SP3
     * Date: Thu, 07 Jun 2001 17:30:42 GMT
     * Content-Type: text/html
     * Connection: close
     * @param output the output to send the response to
     * @throws IOException if an exception is thrown
     */
    protected void writeMessage(OutputStream output) throws IOException{
        DataOutput out = new DataOutputStream(output);
        DataOutput log = new DataOutputStream(System.out);
        //System.out.println("\nRESPONSE");
        closeMessage();
        //writeResponseLine(log);
//        writeHeaders(log);
//        writeBody(log);
        writeResponseLine(out);
        writeHeaders(out);
        writeBody(out);
    }

     /** initalizes the body */
    private void initBody(){
        baos = new ByteArrayOutputStream();
        writer = new PrintWriter( baos );
    }

    /** Creates a string version of the response similar to:
     *
     * HTTP/1.1 200 OK
     * @return the string value of this HttpResponseImpl
     */
    public String toString(){
        StringBuffer buf = new StringBuffer(40);

        buf.append(HTTP_VERSION);
        buf.append(SP);
        buf.append(code+"");
        buf.append(SP);
        buf.append(responseString);

        return buf.toString();
    }

    /** closes the message sent to the browser
     * @throws IOException if an exception is thrown
     */
    private void closeMessage() throws IOException{
        setContentLengthHeader();
        setCookieHeader();
    }


    private void setContentLengthHeader() {
        if (content == null){
            writer.flush();
            writer.close();
            body = baos.toByteArray();
            setHeader("Content-Length", body.length+"");
        } else {
            setHeader("Content-Length", content.getContentLength()+"");
        }
    }

    private void setCookieHeader() {
        if (request == null || request.getSession() == null) return;

        HttpSession session = request.getSession(false);

        if (session == null) return;

        StringBuffer cookie = new StringBuffer();
        cookie.append(HttpRequestImpl.EJBSESSIONID);
        cookie.append('=');
        cookie.append(session.getId());
        cookie.append("; Path=/");

        headers.put(HttpRequest.HEADER_SET_COOKIE, cookie.toString());
    }

    /** Writes a response line similar to this:
     *
     * HTTP/1.1 200 OK
     *
     * to the browser
     * @param out the output stream to write the response line to
     * @throws IOException if an exception is thrown
     */
    private void writeResponseLine(DataOutput out) throws IOException{
        out.writeBytes(HTTP_VERSION);
        out.writeBytes(SP);
        out.writeBytes(code+"");
        out.writeBytes(SP);
        out.writeBytes(responseString);
        out.writeBytes(CRLF);
    }

    /** writes the headers out to the browser
     * @param out the output stream to be sent to the browser
     * @throws IOException if an exception is thrown
     */
    private void writeHeaders(DataOutput out) throws IOException{
        Iterator it =  headers.entrySet().iterator();

        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            out.writeBytes(""+entry.getKey());
            out.writeBytes(CSP);
            out.writeBytes(""+entry.getValue());
            out.writeBytes(CRLF);
        }
    }

    /** writes the body out to the browser
     * @param out the output stream that writes to the browser
     * @throws IOException if an exception is thrown
     */
    private void writeBody(DataOutput out) throws IOException{
        out.writeBytes(CRLF);
        if (content == null){
            out.write(body);
        } else {
            InputStream in = content.getInputStream();
            byte buf[] = new byte[1024];
            for(int i = 0; (i = in.read(buf)) != -1; out.write(buf, 0, i));
        }
    }

    /** gets the name of the server being used
     * @return the name of the server
     */
    public String getServerName(){
        if (server == null) {
            String version = "???";
            String os = "(unknown os)";

            OpenEjbVersion openejbInfo = OpenEjbVersion.get();

            version = openejbInfo.getVersion();
            os = System.getProperty("os.name")+"/"+System.getProperty("os.version")+" ("+System.getProperty("os.arch")+")";

            server = "OpenEJB/" +version+ " "+os;
        }
        return server;
    }


    /** This could be improved at some day in the future
     * to also include a stack trace of the exceptions
     * @param message the error message to be sent
     * @return the HttpResponseImpl that this error belongs to
     */
    protected static HttpResponseImpl createError(String message){
        return createError(message, null);
    }

    /** creates an error with user defined variables
     * @param message the message of the error
     * @param t a Throwable to print a stack trace to
     * @return the HttpResponseImpl that this error belongs to
     */
    protected static HttpResponseImpl createError(String message, Throwable t){
        HttpResponseImpl res = new HttpResponseImpl(500, "Internal Server Error", "text/html");
        java.io.PrintWriter body = res.getPrintWriter();

        body.println("<html>");
        body.println("<body>");
        body.println("<h3>Internal Server Error</h3>");
        body.println("<br><br>");

        if (message != null) {
            StringTokenizer msg = new StringTokenizer(message, "\n\r");

            while (msg.hasMoreTokens()) {
                body.print( msg.nextToken() );
                body.println("<br>");
            }
        }

        if (t != null) {
            try{
                body.println("<br><br>");
                body.println("Stack Trace:<br>");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter( baos );
                t.printStackTrace(writer);
                writer.flush();
                writer.close();
                message = new String(baos.toByteArray());
                StringTokenizer msg = new StringTokenizer(message, "\n\r");

                while (msg.hasMoreTokens()) {
                    body.print( msg.nextToken() );
                    body.println("<br>");
                }
            } catch (Exception e){
            }
        }

        body.println("</body>");
        body.println("</html>");

        return res;
    }

    /** Creates a forbidden response to be sent to the browser using IP authentication
     * @param ip the ip that is forbidden
     * @return the HttpResponseImpl that this error belongs to
     */
    protected static HttpResponseImpl createForbidden(String ip){
        HttpResponseImpl res = new HttpResponseImpl(403, "Forbidden", "text/html");
        java.io.PrintWriter body = res.getPrintWriter();

        body.println("<html>");
        body.println("<body>");
        body.println("<h3>Forbidden</h3>");
        body.println("<br><br>");
        // Add more text here
        // IP not allowed, etc.
        body.println("IP address: " + ip + " is not registered on this server, please contact your system administrator.");
        body.println("</body>");
        body.println("</html>");

        return res;
    }

    /** writes this object out to a file
     * @param out the ObjectOutputStream to write to
     * @throws IOException if an exception is thrown
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException{
        /** Response string */
        out.writeObject( responseString );

        /** Code */
        out.writeInt( code );

        /** Response headers */
        out.writeObject( headers );

        /** Response body */
        writer.flush();
        body = baos.toByteArray();
        //System.out.println("[] body "+body.length );
        out.writeObject( body );
    }

    /** Reads in a serilized HttpResponseImpl object from a file
     * @param in the input to read the object from
     * @throws IOException if an exception is thrown
     * @throws ClassNotFoundException if an exception is thrown
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        /** Response string */
        this.responseString = (String)in.readObject();

        /** Code */
        this.code = in.readInt();

        /** Response headers */
        this.headers = (HashMap) in.readObject();

        /** Response body */
        body = (byte[]) in.readObject();
        //System.out.println("[] body "+body.length );
        baos = new ByteArrayOutputStream();
        baos.write( body );
        writer = new PrintWriter( baos );

    }
    /**
     * @param content The content to set.
     */
    public void setContent(URLConnection content) {
        this.content = content;
    }

}
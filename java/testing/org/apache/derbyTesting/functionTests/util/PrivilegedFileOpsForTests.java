/*

   Derby - Class org.apache.derbyTesting.functionTests.util.PrivilegedFileOpsorTests

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.derbyTesting.functionTests.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * A set of operations on {$@link java.io.File} that wraps the
 * operations in privileged block of code. This class is intended to provide
 * these methods for testcases to reduce the hassle of having to wrap file
 * operations in privileged code blocks.
 * <p>
 * Derby needs to use privileged blocks in some places to avoid
 * {@link SecurityException}s being thrown, as the required privileges are
 * often granted to Derby itself, but not the higher level application code.
 * <p>
 */
public class PrivilegedFileOpsForTests {
	
	/**
     * Get the file length.
     *
     * @return byte length of the file.
     * @throws SecurityException if the required permissions to read the file,
     *      or the path it is in, are missing
     * @see File#length
     */
    public static long length(final File file)
            throws SecurityException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((Long)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException {
                                return new Long(file.length());
                            }
                        })).longValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }
    
    public static FileInputStream getFileInputStream(final File file) 
    	throws SecurityException, FileNotFoundException {
    	if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((FileInputStream)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException, FileNotFoundException {
                                return new FileInputStream(file);
                            }
                        }));
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }

    /**
     * Check if the file exists.
     *
     * @return <code>true</code> if file exists, <code>false</code> otherwise
     * @throws SecurityException if the required permissions to read the file,
     *      or the path it is in, are missing
     * @see File#exists
     */
    public static boolean exists(final File file)
            throws SecurityException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((Boolean)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException {
                                return new Boolean(file.exists());
                            }
                        })).booleanValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }
    /**
     * Creates the directory named by this abstract pathname and
     * parent directories
     * 
     * @param file   directory to create
     * @return {@code true} if directory was created.
     */
    public static boolean mkdirs(final File file) {
     
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return ((Boolean) AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws SecurityException {
                                return new Boolean(file.mkdirs());
                            }
                        })).booleanValue();
        } catch (PrivilegedActionException pae) {
            throw (SecurityException)pae.getException();
        }
    }

    /**
     * Returns a file output stream for the specified file.
     *
     * @param file the file to create a stream for
     * @return An output stream.
     * @throws FileNotFoundException if the specified file does not exist
     * @throws SecurityException if the required permissions to write the file,
     *      or the path it is in, are missing
     */
    public static FileOutputStream getFileOutputStream(final File file)
            throws FileNotFoundException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be <null>");
        }
        try {
            return (FileOutputStream)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run()
                                throws FileNotFoundException {
                            return new FileOutputStream(file);
                        }
                    });
        } catch (PrivilegedActionException pae) {
            throw (FileNotFoundException)pae.getCause();
        }
    }

    /**
     * In a priv block, do a recursive copy from source to target.  
     * If target exists it will be overwritten. Parent directory for 
     * target will be created if it does not exist. 
     * If source does not exist this will be a noop.
     * 
     * @param source  Source file or directory to copy
     * @param target  Target file or directory to copy
     * @throws IOException
     * @throws SecurityException
     */    
    public static void copy(final File source, final File target) throws IOException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    recursiveCopy(source,target);
                    return null;
                }
                });
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getException();
        
        }
        
    }
    /**
     * Do a recursive copy from source to target.  If target exists it will 
     * be overwritten. Parent directory for target will be created if it does
     * not exist. If source does not exist this will be a noop.
     * 
     * @param source  Source file or directory to copy
     * @param target  Target file or directory to copy
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void  recursiveCopy(File source, File target) throws IOException, FileNotFoundException{
    
        if (source.isFile()) {
            copySingleFile(source,target);
            return;
        }
            
        String[] list = source.list();

        // Some JVMs return null for File.list() when the
        // directory is empty.
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(source, list[i]);
                File targetEntry = new File(target, list[i]);
                if (entry.isDirectory()) {
                    copy(entry,targetEntry);
                } else {
                    copySingleFile(entry, targetEntry);
                }
            }

        }
    }

    /**
     * Copy a single file from source to target.  If target exists it will be 
     * overwritten.  If source does not exist, this will be a noop.
     * 
     * @param source  Source file to copy
     * @param target  Destination file for copy
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void copySingleFile (File source, File target) throws IOException, FileNotFoundException {

        File targetParent = target.getParentFile();
        if (targetParent != null && ! targetParent.exists())
            target.getParentFile().mkdirs();
        
                
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);
        byte[] buf = new byte[32 * 1024];
        
        for (;;) {
            int read = in.read(buf);
            if (read == -1)
                break;
            out.write(buf, 0, read);
        }
        in.close();
        out.close();
    }
    


}

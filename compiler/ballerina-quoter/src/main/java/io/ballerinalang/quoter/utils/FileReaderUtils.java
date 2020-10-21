/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.quoter.utils;

import io.ballerinalang.quoter.BallerinaQuoter;
import io.ballerinalang.quoter.QuoterException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class FileReaderUtils {
    /**
     * Reads a file path content from the resources directory.
     */
    public static String readFileAsResource(String path) {
        InputStream inputStream = readResourceAsInputStream(path);
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }


    /**
     * Reads a file path content from cwd.
     */
    public static String readFile(String path) {
        try (InputStream inputStream = new FileInputStream(path)) {
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + path + ". Error: " + e.getMessage(), e);
        }
    }


    /**
     * Reads a resource file as a input stream.
     */
    public static InputStream readResourceAsInputStream(String path) {
        ClassLoader classLoader = BallerinaQuoter.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(path)) {
            if (inputStream == null) throw new QuoterException("File not found: " + path);
            return inputStream;
        } catch (IOException e) {
            throw new QuoterException("Failed to read " + path + ". Error: " + e.getMessage(), e);
        }
    }
}

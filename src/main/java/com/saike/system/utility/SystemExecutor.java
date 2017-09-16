/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saike.system.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SystemExecutor {

    private static Logger logger = LoggerFactory.getLogger(SystemExecutor.class);

    public List<String> execute(List<String> commands) throws IOException, InterruptedException {
        return execute(commands, null);
    }

    public List<String> execute(List<String> commands, OutputFilter filter) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            Process process = pb.start();
            LoggingThread stdout = new LoggingThread(process.getInputStream(), filter);
            stdout.setName("STDOUT");
            LoggingThread stderr = new LoggingThread(process.getErrorStream(), filter);
            stderr.setName("STDERR");
            stdout.start();
            stderr.start();
            int ret = process.waitFor();
            stdout.join();
            stderr.join();
            if (logger.isDebugEnabled()) {
                logger.debug("system return code:" + ret);
            }
            if (stdout.getOutput().size() > 0) {
                return stdout.getOutput();
            } else if (stderr.getOutput().size() > 0) {
                return stderr.getOutput();
            } else {
                return new ArrayList<>(0);
            }
        } catch (IOException e) {
            logger.error("command execute error", e);
            throw e;
        } catch (InterruptedException e) {
            logger.error("command execute interrupted", e);
            throw e;
        }
    }

    static class LoggingThread extends Thread {
        private InputStream is;
        private OutputFilter filter;
        private List<String> output = new ArrayList<>();

        public LoggingThread(InputStream is, OutputFilter filter) {
            this.is = is;
            this.filter = filter;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(getName() + ":" + line);
                    }
                    if ("".equals(line.trim())) {
                        continue;
                    }
                    if (filter != null) {
                        output.add(filter.filter(line));
                    } else {
                        output.add(line);
                    }
                }
            } catch (IOException e) {
                logger.error("io error", e);
            }
        }

        public List<String> getOutput() {
            return output;
        }
    }

}

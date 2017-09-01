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
import org.slf4j.event.Level;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemExecutor {

    private static Logger logger = LoggerFactory.getLogger(SystemExecutor.class);

    public void execute(List<String> commands){
        execute(commands, 0);
    }

    public void execute(List<String> commands, int timeout){
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            Process process = pb.start();
            LoggingThread stdout = new LoggingThread(process.getInputStream(), Level.INFO);
            LoggingThread stderr = new LoggingThread(process.getErrorStream(), Level.ERROR);
            if (timeout > 0){
                process.waitFor(timeout, TimeUnit.SECONDS);
            }else {
                process.waitFor();
            }
            stdout.join();
            stderr.join();
        } catch (IOException e) {
            logger.error("command execute error", e);
        } catch (InterruptedException e){
            logger.error("command execute interrupted", e);
        }

    }

    static class LoggingThread extends Thread {
        private InputStream is;
        private Level level;

        public LoggingThread(InputStream is, Level level){
            this.is = is;
            this.level = level;
        }

        @Override
        public void run() {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF8"))){
                String line;
                while((line = reader.readLine()) != null){
                    if (level == Level.INFO){
                        logger.info(line);
                        continue;
                    }

                    if (level == Level.ERROR){
                        logger.error(line);
                    }

                }
            }catch (IOException e){
                logger.error("", e);
            }


        }
    }

}

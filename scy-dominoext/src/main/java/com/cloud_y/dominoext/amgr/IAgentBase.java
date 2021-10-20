/*
 * Copyright © Oct 1, 2021 Factor-y S.r.l. (daniele.vistalli@factor-y.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloud_y.dominoext.amgr;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import lotus.domino.AgentBase;
import lotus.domino.Session;

/**
 * This interface just defines methods that are part of the AgentBase class.
 * 
 * It is used as a guidance tool for implementing AgentBase compatible classes.
 * 
 * @author Daniele Vistalli <daniele.vistalli@factor-y.com>
 *
 */
public interface IAgentBase {

    void NotesMain();

    void dbgMsg(String msg);
    void dbgMsg(String msg, PrintStream ps);
    void dbgMsg(String msg, PrintWriter pw);

    PrintWriter getAgentOutput();

    OutputStream getAgentOutputStream();

    Session getSession();

    boolean isRestricted();

    void setDebug(boolean debug);
    void setTrace(boolean trace);

    void run();
    void setBase(AgentBase base);
    
}

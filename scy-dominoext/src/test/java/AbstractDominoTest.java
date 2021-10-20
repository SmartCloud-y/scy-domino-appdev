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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import lotus.domino.NotesThread;

public abstract class AbstractDominoTest {

    @BeforeAll
    @DisplayName("Initialize Notes API")
    public static void setup() {
	NotesThread.sinitThread();
    }
    
    @AfterAll
    @DisplayName("Shutting down Notes API")
    static void teardown() {
	System.out.println("NotesThread Shutdown");
	NotesThread.stermThread();
	System.out.println("NotesThread isLoaded: " + NotesThread.isLoaded);
    }
    
}

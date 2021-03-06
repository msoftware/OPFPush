/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.opfpush.pushprovider;

import android.support.annotation.NonNull;

import org.onepf.opfpush.model.Message;

/**
 * PushProvider that can send messages to server.
 *
 * @author Kirill Rozov
 * @since 10/13/14.
 */
public interface SenderPushProvider {

    /**
     * Send message to server. Method must be consumed async.
     *
     * @throws IllegalStateException If try send message when provider isn't registered.
     */
    void send(@NonNull Message msg);
}

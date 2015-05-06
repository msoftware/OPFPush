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

package org.onepf.pushchat.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import org.onepf.pushchat.R;

import java.util.Date;

import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.MESSAGE;
import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.RECEIVED_TIME;
import static org.onepf.pushchat.db.ContentDescriptor.MessagesContract.MessageEntry.SENDER_UUID;

/**
 * @author Roman Savin
 * @since 06.05.2015
 */
public class MessagesCursorAdapter extends CursorAdapter {

    public MessagesCursorAdapter(@NonNull final Context context) {
        super(context, null, true);
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final String sender = cursor.getString(cursor.getColumnIndexOrThrow(SENDER_UUID));
        final String message = cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE));
        final long time = cursor.getLong(cursor.getColumnIndexOrThrow(RECEIVED_TIME));

        final TextView senderTextView = (TextView) view.findViewById(R.id.sender);
        final TextView messageTextView = (TextView) view.findViewById(R.id.message_text);
        final TextView timeTextView = (TextView) view.findViewById(R.id.time);

        senderTextView.setText(context.getString(R.string.sender_fmt, sender));
        messageTextView.setText(message);
        timeTextView.setText(context.getString(R.string.time_fmt, new Date(time).toString()));
    }
}
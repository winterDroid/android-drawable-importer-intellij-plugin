/*
 * Copyright 2015 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * 			http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.mprengemann.intellij.plugin.androidicons.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mprengemann.intellij.plugin.androidicons.util.TextUtils;

import java.lang.reflect.Type;

public enum Resolution {
    LDPI,
    MDPI,
    HDPI,
    XHDPI,
    XXHDPI,
    XXXHDPI,
    TVDPI;

    public static Resolution from(String value) {
        if (TextUtils.isEmpty(value)) {
            throw new IllegalArgumentException();
        }
        if (value.equalsIgnoreCase(LDPI.toString())) {
            return LDPI;
        } else if (value.equalsIgnoreCase(MDPI.toString())) {
            return MDPI;
        } else if (value.equalsIgnoreCase(HDPI.toString())) {
            return HDPI;
        } else if (value.equalsIgnoreCase(XHDPI.toString())) {
            return XHDPI;
        } else if (value.equalsIgnoreCase(XXHDPI.toString())) {
            return XXHDPI;
        } else if (value.equalsIgnoreCase(XXXHDPI.toString())) {
            return XXXHDPI;
        } else if (value.equalsIgnoreCase(TVDPI.toString())) {
            return TVDPI;
        }
        return null;
    }

    public static class Deserializer implements JsonDeserializer<Resolution> {
        @Override
        public Resolution deserialize(JsonElement jsonElement,
                                  Type type,
                                  JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Resolution.from(jsonElement.getAsString());
        }
    }
}

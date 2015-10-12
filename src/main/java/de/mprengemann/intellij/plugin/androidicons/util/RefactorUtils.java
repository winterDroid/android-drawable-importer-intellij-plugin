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

package de.mprengemann.intellij.plugin.androidicons.util;

import de.mprengemann.intellij.plugin.androidicons.model.Resolution;

public class RefactorUtils {

        public static float getScaleFactor(Resolution target, Resolution baseLine) {
        switch (baseLine) {
            case MDPI:
                switch (target) {
                    case LDPI:
                        return 0.5f;
                    case MDPI:
                        return 1f;
                    case HDPI:
                        return 1.5f;
                    case XHDPI:
                        return 2f;
                    case XXHDPI:
                        return 3f;
                    case XXXHDPI:
                        return 4f;
                    case TVDPI:
                        return 4f / 3f;
                }
                break;
            case LDPI:
                switch (target) {
                    case LDPI:
                        return 2f * 0.5f;
                    case MDPI:
                        return 2f * 1f;
                    case HDPI:
                        return 2f * 1.5f;
                    case XHDPI:
                        return 2f * 2f;
                    case XXHDPI:
                        return 2f * 3f;
                    case XXXHDPI:
                        return 2f * 4f;
                    case TVDPI:
                        return 2f * 4f / 3f;
                }
                break;
            case HDPI:
                switch (target) {
                    case LDPI:
                        return 2f / 3f * 0.5f;
                    case MDPI:
                        return 2f / 3f * 1f;
                    case HDPI:
                        return 2f / 3f * 1.5f;
                    case XHDPI:
                        return 2f / 3f * 2f;
                    case XXHDPI:
                        return 2f / 3f * 3f;
                    case XXXHDPI:
                        return 2f / 3f * 4f;
                    case TVDPI:
                        return 2f / 3f * 4f / 3f;
                }
                break;
            case XHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 2f * 0.5f;
                    case MDPI:
                        return 1f / 2f * 1f;
                    case HDPI:
                        return 1f / 2f * 1.5f;
                    case XHDPI:
                        return 1f / 2f * 2f;
                    case XXHDPI:
                        return 1f / 2f * 3f;
                    case XXXHDPI:
                        return 1f / 2f * 4f;
                    case TVDPI:
                        return 1f / 2f * 4f / 3f;
                }
                break;
            case XXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 3f * 0.5f;
                    case MDPI:
                        return 1f / 3f * 1f;
                    case HDPI:
                        return 1f / 3f * 1.5f;
                    case XHDPI:
                        return 1f / 3f * 2f;
                    case XXHDPI:
                        return 1f / 3f * 3f;
                    case XXXHDPI:
                        return 1f / 3f * 4f;
                    case TVDPI:
                        return 1f / 3f * 4f / 3f;
                }
                break;
            case XXXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / 4f * 0.5f;
                    case MDPI:
                        return 1f / 4f * 1f;
                    case HDPI:
                        return 1f / 4f * 1.5f;
                    case XHDPI:
                        return 1f / 4f * 2f;
                    case XXHDPI:
                        return 1f / 4f * 3f;
                    case XXXHDPI:
                        return 1f / 4f * 4f;
                    case TVDPI:
                        return 1f / 4f * 4f / 3f;
                }
                break;
            case TVDPI:
                switch (target) {
                    case LDPI:
                        return 3f / 4f * 0.5f;
                    case MDPI:
                        return 3f / 4f * 1f;
                    case HDPI:
                        return 3f / 4f * 1.5f;
                    case XHDPI:
                        return 3f / 4f * 2f;
                    case XXHDPI:
                        return 3f / 4f * 3f;
                    case XXXHDPI:
                        return 3f / 4f * 4f;
                    case TVDPI:
                        return 3f / 4f * 4f / 3f;
                }
        }
        throw new IllegalArgumentException();
    }
}

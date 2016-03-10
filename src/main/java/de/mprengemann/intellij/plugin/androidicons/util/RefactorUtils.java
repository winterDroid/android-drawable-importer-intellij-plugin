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

    private static final float FACTOR_LDPI = 0.75f;
    private static final float FACTOR_MDPI = 1f;
    private static final float FACTOR_HDPI = 1.5f;
    private static final float FACTOR_XHDPI = 2f;
    private static final float FACTOR_XXHDPI = 3f;
    private static final float FACTOR_XXXHDPI = 4f;
    private static final float FACTOR_TVDPI = 4f / 3f;

    public static float getScaleFactor(Resolution target, Resolution baseLine) {
        switch (baseLine) {
            case MDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_MDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_MDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_MDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_MDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_MDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_MDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_MDPI * FACTOR_TVDPI;
                }
                break;
            case LDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_LDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_LDPI  * FACTOR_TVDPI;
                }
                break;
            case HDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_HDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_HDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_HDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_HDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_HDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_HDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_HDPI * FACTOR_TVDPI;
                }
                break;
            case XHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_XHDPI * FACTOR_TVDPI;
                }
                break;
            case XXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_XXHDPI * FACTOR_TVDPI;
                }
                break;
            case XXXHDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_XXXHDPI * FACTOR_TVDPI;
                }
                break;
            case TVDPI:
                switch (target) {
                    case LDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_LDPI;
                    case MDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_MDPI;
                    case HDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_HDPI;
                    case XHDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_XHDPI;
                    case XXHDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_XXHDPI;
                    case XXXHDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_XXXHDPI;
                    case TVDPI:
                        return 1f / FACTOR_TVDPI * FACTOR_TVDPI;
                }
        }
        throw new IllegalArgumentException();
    }
}

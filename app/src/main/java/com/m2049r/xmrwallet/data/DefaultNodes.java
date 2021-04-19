/*
 * Copyright (c) 2020 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DefaultNodes {
// from https://git.wownero.com/wownero/wowlet-backend/src/branch/master/data/nodes.json#L8-L19
    WOWNODES("global.wownodes.com:34568"),
    SUPERFAST("super.fast.node.xmr.pm:34568"),
    WOWCLUB("node.wownero.club:34568"),
    SUCHWOW("node.suchwow.xyz:34568"),
    EUWOW1("eu-west-1.wow.xmr.pm:34568"),
    EUWOW2("eu-west-2.wow.xmr.pm:34568"),
    EUWOW3("eu-west-3.wow.xmr.pm:34568"),
    EUWOW4("eu-west-4.wow.xmr.pm:34568"),
    EUWOW5("eu-west-5.wow.xmr.pm:34568"),
    EUWOW6("eu-west-6.wow.xmr.pm:34568"),
    NAWOW1("na-west-1.wow.xmr.pm:34568"),
    PWNED("wow.pwned.systems:34568");

    @Getter
    private final String uri;
}

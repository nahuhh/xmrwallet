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

package one.mayumi.shruum.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Nodes stolen from https://moneroworld.com/#nodes

@AllArgsConstructor
public enum DefaultNodes {
    MONEROEXPLORER("xrft3jrcdrfwvry4aocxcswvwturaqxelesmtdk34j4646tajmc6rxyd.onion:18089/mainnet/xrft3jrcdrfwvry4aocxcswvwturaqxelesmtdk34j4646tajmc6rxyd.onion"),
    MONERUJO_ONION("monerujods7mbghwe6cobdr6ujih6c22zu5rl7zshmizz2udf7v7fsad.onion:18081/mainnet/monerujo.onion"),
    Criminales78("56wl7y2ebhamkkiza4b7il4mrzwtyvpdym7bm2bkg3jrei2je646k3qd.onion:18089/mainnet/Criminales78.onion"),
    xmrfail("mxcd4577fldb3ppzy7obmmhnu3tf57gbcbd4qhwr2kxyjj2qi3dnbfqd.onion:18081/mainnet/xmrfail.onion"),
    boldsuck("6dsdenp6vjkvqzy4wzsnzn6wixkdzihx3khiumyzieauxuxslmcaeiad.onion:18081/mainnet/boldsuck.onion"),
    XMRNODES("xmrnodesarnt4w35aqmu66aart3o324yw6qbnv6pglpof6uqaydzk5id.onion:18081/mainnet/xmrnodesarnt4w35aqmu66aart3o324yw6qbnv6pglpof6uqaydzk5id.onion"),
    RANDOM_ONION1("mhfsxznn5pi4xuxohj5k7unqp73sa6d44mbeewbpxnm25z3wzfogcfyd.onion:18081/mainnet/mhfsxznn5pi4xuxohj5k7unqp73sa6d44mbeewbpxnm25z3wzfogcfyd.onion"),
    RANDOM_ONION2("sfprpc5klzs5vyitq2mrooicgk2wcs5ho2nm3niqduvzn5o6ylaslaqd.onion:18089/mainnet/sfprpc5klzs5vyitq2mrooicgk2wcs5ho2nm3niqduvzn5o6ylaslaqd.onion"),
    RANDOM_ONION3("moneroxmrxw44lku6qniyarpwgznpcwml4drq7vb24ppatlcg4kmxpqd.onion:18089/mainnet/moneroxmrxw44lku6qniyarpwgznpcwml4drq7vb24ppatlcg4kmxpqd.onion"),
    RANDOM_ONION4("melo7jwjspngus3xhbt5kxeqc4njhokyvh55jfmehplglgmb7a6rb6yd.onion:18081/mainnet/melo7jwjspngus3xhbt5kxeqc4njhokyvh55jfmehplglgmb7a6rb6yd.onion"),
    RANDOM_ONION5("ip4zpbps7unk6xhlanqtw24f75akfbl3upeckfjqjks7ftfnk4i73oid.onion:18081/mainnet/ip4zpbps7unk6xhlanqtw24f75akfbl3upeckfjqjks7ftfnk4i73oid.onion"),
    RANDOM_ONION6("majesticrepik35vnngouksfl7jiwf6sj7s2doj3bvdffq27tgqoeayd.onion:18089/mainnet/majesticrepik35vnngouksfl7jiwf6sj7s2doj3bvdffq27tgqoeayd.onion"),
    RANDOM_ONION7("axtkarghpvfajpr46bg5l3gnk4dyib2q7cw2de6j6fizyedwr2tssead.onion:18081/mainnet/axtkarghpvfajpr46bg5l3gnk4dyib2q7cw2de6j6fizyedwr2tssead.onion"),
    RANDOM_ONION8("monero6hugskv6ommvjbnocei4jlp72rnkbirfewnzvih6hq4gfaa4qd.onion:18089/mainnet/monero6hugskv6ommvjbnocei4jlp72rnkbirfewnzvih6hq4gfaa4qd.onion");

    @Getter
    private final String uri;
}

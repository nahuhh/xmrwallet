package one.mayumi.xmrwallet.util;

import one.mayumi.xmrwallet.model.NetworkType;
import one.mayumi.xmrwallet.model.WalletManager;

import okhttp3.HttpUrl;

public class ServiceHelper {
    public static String ASSET = null;

    static public HttpUrl getXmrToBaseUrl() {
        if ((WalletManager.getInstance() == null)
                || (WalletManager.getInstance().getNetworkType() != NetworkType.NetworkType_Mainnet)) {
            throw new IllegalStateException("Only mainnet not supported");
        } else {
            return HttpUrl.parse("https://sideshift.ai/api/v1/");
        }
    }
}

/*
 * Copyright © 2013-2016 The rcc Core Developers.
 * Copyright © 2016-2022 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the rcc software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package rcc.http;

import rcc.Account;
import rcc.DigitalGoodsStore;
import rcc.rccException;
import rcc.crypto.Crypto;
import rcc.util.Convert;
import rcc.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static rcc.http.JSONResponses.DECRYPTION_FAILED;

public final class GetDGSPurchase extends APIServlet.APIRequestHandler {

    static final GetDGSPurchase instance = new GetDGSPurchase();

    private GetDGSPurchase() {
        super(new APITag[] {APITag.DGS}, "purchase", "secretPhrase", "sharedKey");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        DigitalGoodsStore.Purchase purchase = ParameterParser.getPurchase(req);
        JSONObject response = JSONData.purchase(purchase);

        byte[] sharedKey = ParameterParser.getBytes(req, "sharedKey", false);
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        if (sharedKey.length == 0 && secretPhrase == null) {
            return response;
        }
        if (purchase.getEncryptedGoods() != null) {
            byte[] data = purchase.getEncryptedGoods().getData();
            try {
                byte[] decrypted = Convert.EMPTY_BYTE;
                if (data.length != 0) {
                    if (secretPhrase != null) {
                        byte[] readerPublicKey = Crypto.getPublicKey(secretPhrase);
                        byte[] sellerPublicKey = Account.getPublicKey(purchase.getSellerId());
                        byte[] buyerPublicKey = Account.getPublicKey(purchase.getBuyerId());
                        byte[] publicKey = Arrays.equals(sellerPublicKey, readerPublicKey) ? buyerPublicKey : sellerPublicKey;
                        if (publicKey != null) {
                            decrypted = Account.decryptFrom(publicKey, purchase.getEncryptedGoods(), secretPhrase, true);
                        }
                    } else {
                        decrypted = Crypto.aesDecrypt(purchase.getEncryptedGoods().getData(), sharedKey);
                        decrypted = Convert.uncompress(decrypted);
                    }
                }
                response.put("decryptedGoods", Convert.toString(decrypted, purchase.goodsIsText()));
            } catch (RuntimeException e) {
                Logger.logDebugMessage(e.toString());
                return DECRYPTION_FAILED;
            }
        }
        return response;
    }

}

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
import rcc.rccException;
import rcc.crypto.EncryptedData;
import rcc.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static rcc.http.JSONResponses.INCORRECT_MESSAGE_TO_ENCRYPT;
import static rcc.http.JSONResponses.INCORRECT_RECIPIENT;
import static rcc.http.JSONResponses.MISSING_MESSAGE_TO_ENCRYPT;

public final class EncryptTo extends APIServlet.APIRequestHandler {

    static final EncryptTo instance = new EncryptTo();

    private EncryptTo() {
        super(new APITag[] {APITag.MESSAGES}, "recipient", "messageToEncrypt", "messageToEncryptIsText", "compressMessageToEncrypt", "secretPhrase");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        long recipientId = ParameterParser.getAccountId(req, "recipient", true);
        byte[] recipientPublicKey = Account.getPublicKey(recipientId);
        if (recipientPublicKey == null) {
            return INCORRECT_RECIPIENT;
        }
        boolean isText = !"false".equalsIgnoreCase(req.getParameter("messageToEncryptIsText"));
        boolean compress = !"false".equalsIgnoreCase(req.getParameter("compressMessageToEncrypt"));
        String plainMessage = Convert.emptyToNull(req.getParameter("messageToEncrypt"));
        if (plainMessage == null) {
            return MISSING_MESSAGE_TO_ENCRYPT;
        }
        byte[] plainMessageBytes;
        try {
            plainMessageBytes = isText ? Convert.toBytes(plainMessage) : Convert.parseHexString(plainMessage);
        } catch (RuntimeException e) {
            return INCORRECT_MESSAGE_TO_ENCRYPT;
        }
        String secretPhrase = ParameterParser.getSecretPhrase(req, true);
        EncryptedData encryptedData = Account.encryptTo(recipientPublicKey, plainMessageBytes, secretPhrase, compress);
        return JSONData.encryptedData(encryptedData);

    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

}

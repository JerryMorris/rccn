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
import rcc.Attachment;
import rcc.rccException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class UploadTaggedData extends CreateTransaction {

    static final UploadTaggedData instance = new UploadTaggedData();

    private UploadTaggedData() {
        super("file", new APITag[] {APITag.DATA, APITag.CREATE_TRANSACTION},
                "name", "description", "tags", "type", "channel", "isText", "filename", "data");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        Account account = ParameterParser.getSenderAccount(req);
        Attachment.TaggedDataUpload taggedDataUpload = ParameterParser.getTaggedData(req);
        return createTransaction(req, account, taggedDataUpload);

    }

}

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
import rcc.Constants;
import rcc.DigitalGoodsStore;
import rcc.rccException;
import rcc.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static rcc.http.JSONResponses.INCORRECT_DELTA_QUANTITY;
import static rcc.http.JSONResponses.MISSING_DELTA_QUANTITY;
import static rcc.http.JSONResponses.UNKNOWN_GOODS;

public final class DGSQuantityChange extends CreateTransaction {

    static final DGSQuantityChange instance = new DGSQuantityChange();

    private DGSQuantityChange() {
        super(new APITag[] {APITag.DGS, APITag.CREATE_TRANSACTION},
                "goods", "deltaQuantity");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        Account account = ParameterParser.getSenderAccount(req);
        DigitalGoodsStore.Goods goods = ParameterParser.getGoods(req);
        if (goods.isDelisted() || goods.getSellerId() != account.getId()) {
            return UNKNOWN_GOODS;
        }

        int deltaQuantity;
        try {
            String deltaQuantityString = Convert.emptyToNull(req.getParameter("deltaQuantity"));
            if (deltaQuantityString == null) {
                return MISSING_DELTA_QUANTITY;
            }
            deltaQuantity = Integer.parseInt(deltaQuantityString);
            if (deltaQuantity > Constants.MAX_DGS_LISTING_QUANTITY || deltaQuantity < -Constants.MAX_DGS_LISTING_QUANTITY) {
                return INCORRECT_DELTA_QUANTITY;
            }
        } catch (NumberFormatException e) {
            return INCORRECT_DELTA_QUANTITY;
        }

        Attachment attachment = new Attachment.DigitalGoodsQuantityChange(goods.getId(), deltaQuantity);
        return createTransaction(req, account, attachment);

    }

}

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
import rcc.Asset;
import rcc.Attachment;
import rcc.Constants;
import rcc.HoldingType;
import rcc.rcc;
import rcc.rccException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class DividendPayment extends CreateTransaction {

    static final DividendPayment instance = new DividendPayment();

    private DividendPayment() {
        super(new APITag[] {APITag.AE, APITag.CREATE_TRANSACTION}, "holding", "holdingType", "asset", "height", "amountNQTPerQNT");
    }

    @Override
    protected JSONStreamAware processRequest(final HttpServletRequest request)
            throws rccException
    {
        final int height = ParameterParser.getHeight(request, true);
        final long amountNQTPerQNT = ParameterParser.getAmountNQTPerQNT(request);
        final Account account = ParameterParser.getSenderAccount(request);
        final Asset asset = ParameterParser.getAsset(request);
        if (Asset.getAsset(asset.getId(), height) == null) {
            return JSONResponses.ASSET_NOT_ISSUED_YET;
        }
        final HoldingType holdingType = ParameterParser.getHoldingType(request);
        final long holdingId = holdingType != HoldingType.rcc ? ParameterParser.getHoldingId(request, holdingType) : 0;

        final Attachment attachment;
        if (rcc.getBlockchain().getHeight() < Constants.ASSET_INCREASE_BLOCK) {
            if (holdingType != HoldingType.rcc) {
                return JSONResponses.FEATURE_NOT_AVAILABLE;
            }
            attachment = new Attachment.ColoredCoinsDividendPayment(asset.getId(), height, amountNQTPerQNT);
        } else {
            attachment = new Attachment.ColoredCoinsDividendPayment(holdingId, holdingType, asset.getId(), height, amountNQTPerQNT);
        }

        try {
            return this.createTransaction(request, account, attachment);
        } catch (rccException.InsufficientBalanceException e) {
            return JSONResponses.NOT_ENOUGH_FUNDS;
        }
    }

}

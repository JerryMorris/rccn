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
import rcc.Shuffling;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class ShufflingRegister extends CreateTransaction {

    static final ShufflingRegister instance = new ShufflingRegister();

    private ShufflingRegister() {
        super(new APITag[] {APITag.SHUFFLING, APITag.CREATE_TRANSACTION}, "shufflingFullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {
        byte[] shufflingFullHash = ParameterParser.getBytes(req, "shufflingFullHash", true);

        Attachment attachment = new Attachment.ShufflingRegistration(shufflingFullHash);

        Account account = ParameterParser.getSenderAccount(req);
        if (account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
            return JSONResponses.error("Accounts under phasing only control cannot join a shuffling");
        }
        try {
            return createTransaction(req, account, attachment);
        } catch (rccException.InsufficientBalanceException e) {
            Shuffling shuffling = Shuffling.getShuffling(shufflingFullHash);
            if (shuffling == null) {
                return JSONResponses.NOT_ENOUGH_FUNDS;
            }
            return JSONResponses.notEnoughHolding(shuffling.getHoldingType());
        }
    }

}

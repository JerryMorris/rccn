/*
 * Copyright © 2016-2022 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package com.jelurida.ardor.client.api;

import rcc.rcc;
import rcc.VoteWeighting;
import rcc.addons.JO;
import rcc.http.callers.GetBlockCall;
import rcc.http.callers.SendMoneyCall;
import rcc.http.responses.BlockResponse;
import rcc.http.responses.TransactionResponse;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample Java program which demonstrates how to submit a phased transaction
 */
public class PhasedTransaction {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state";

    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("https://testrcc.redcobracoina.com/rcc");

        PhasedTransaction phasedTransaction = new PhasedTransaction();
        phasedTransaction.submitPhasedTransaction(url);
    }

    private void submitPhasedTransaction(URL url) {
        JO block = GetBlockCall.create().remote(url).call();
        BlockResponse blockResponse = BlockResponse.create(block);
        int height = blockResponse.getHeight();

        JO signedTransactionResponse = SendMoneyCall.create().
                recipient("rcc-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                secretPhrase(SECRET_PHRASE).
                deadline(15).
                feeNQT(200000000).
                phased(true).
                phasingVotingModel(VoteWeighting.VotingModel.ACCOUNT.getCode()). // Another account will need to approve this
                phasingQuorum(1). // One approver account is enough
                phasingWhitelisted("rcc-EVHD-5FLM-3NMQ-G46NR"). // This is the account that needs to approve
                phasingFinishHeight(height + 100). // It has 100 blocks to submit the approval
                phasingMinBalanceModel(VoteWeighting.MinBalanceModel.NONE.getCode()). // There is no minimum balance requirement
                remote(url).
                call();

        System.out.printf("SendMoney response: %s\n", signedTransactionResponse.toJSONString());
        TransactionResponse transactionResponse = TransactionResponse.create(signedTransactionResponse.getJo("transactionJSON"));
        System.out.printf("Phased: %s\n", transactionResponse.isPhased());
    }

}

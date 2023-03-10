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

package rcc.http.shuffling;

import rcc.BlockchainTest;
import rcc.Constants;
import rcc.rcc;
import rcc.Shuffling;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static rcc.http.shuffling.ShufflingUtil.ALICE_RECIPIENT;
import static rcc.http.shuffling.ShufflingUtil.BOB_RECIPIENT;
import static rcc.http.shuffling.ShufflingUtil.CHUCK_RECIPIENT;
import static rcc.http.shuffling.ShufflingUtil.DAVE_RECIPIENT;
import static rcc.http.shuffling.ShufflingUtil.broadcast;
import static rcc.http.shuffling.ShufflingUtil.cancel;
import static rcc.http.shuffling.ShufflingUtil.create;
import static rcc.http.shuffling.ShufflingUtil.defaultShufflingAmount;
import static rcc.http.shuffling.ShufflingUtil.getShuffling;
import static rcc.http.shuffling.ShufflingUtil.getShufflingParticipants;
import static rcc.http.shuffling.ShufflingUtil.process;
import static rcc.http.shuffling.ShufflingUtil.register;
import static rcc.http.shuffling.ShufflingUtil.verify;


public class TestShuffling extends BlockchainTest {

    @Test
    public void successfulShuffling() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        Assert.assertEquals(-Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_rcc), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_rcc), DAVE.getUnconfirmedBalanceDiff());

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        verify(shufflingId, CHUCK, shufflingStateHash);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.DONE.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_rcc), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(48 * Constants.ONE_rcc, FORGY.getBalanceDiff());
        Assert.assertEquals(48 * Constants.ONE_rcc, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void registrationNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        for (int i = 0; i < 9; i++) {
            generateBlock();
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(2, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());

        Assert.assertEquals(2 * Constants.ONE_rcc, FORGY.getBalanceDiff());
        Assert.assertEquals(2 * Constants.ONE_rcc, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void processingNotStarted() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        for (int i = 0; i < 10; i++) {
            generateBlock();
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(4 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(4 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void tooManyParticipants() {
        JSONObject shufflingCreate = create(ALICE, 3);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        register(shufflingFullHash, CHUCK);
        register(shufflingFullHash, DAVE);
        register(shufflingFullHash, FORGY);
        for (int i = 0; i < 10; i++) {
            generateBlock();
        }
        rcc.getTransactionProcessor().clearUnconfirmedTransactions();
        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(3, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE.getBalanceDiff());
        Assert.assertEquals(0, DAVE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(3 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(3 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());
    }

    @Test
    public void processingNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 10; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(34 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void verifyNotStarted() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);

        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(45 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(45 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void verifyNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-12 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(47 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(47 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId());
        cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeys() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        JSONObject cancelResponse = cancel(shufflingId, CHUCK, shufflingStateHash, 0, false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId());
        cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeysAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        JSONObject cancelResponse = cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId(), false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        generateBlock();
        cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeysAlice2() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        verify(shufflingId, BOB, shufflingStateHash);
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        JSONObject cancelResponse = cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId(), false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(1);
        keySeeds.set(1, "0000000000" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        generateBlock();
        cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void badProcessDataAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        JSONObject processResponse = process(shufflingId, ALICE, ALICE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_rcc), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(24 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedProcessDataBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        JSONObject processResponse = process(shufflingId, BOB, BOB_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, BOB);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId());
        JSONObject cancelResponse = cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        boolean bobCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * Constants.ONE_rcc), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * Constants.ONE_rcc), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((bobCancelFailed ? 44 : 54) * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals((bobCancelFailed ? 44 : 54) * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        JSONObject processResponse = process(shufflingId, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, ALICE, shufflingStateHash, DAVE.getId());
        cancel(shufflingId, BOB, shufflingStateHash, DAVE.getId());
        JSONObject cancelResponse = cancel(shufflingId, CHUCK, shufflingStateHash, DAVE.getId());
        boolean chuckCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        JSONObject processResponse = process(shufflingId, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        if (!s.equals(DAVE_RECIPIENT.getPublicKeyStr())) {
            data.set(0, "0000000000" + s.substring(10));
        } else {
            s = (String)data.get(1);
            data.set(1, "0000000000" + s.substring(10));
        }
        broadcast(transactionJSON, DAVE);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, ALICE, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, BOB, shufflingStateHash, ALICE.getId());
        cancel(shufflingId, CHUCK, shufflingStateHash, ALICE.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_rcc), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_rcc), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getUnconfirmedBalanceDiff() == 0);

        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        JSONObject processResponse = process(shufflingId, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, DAVE);
        Assert.assertNotNull(broadcastResponse.get("error"));
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(34 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        JSONObject processResponse = process(shufflingId, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, CHUCK);
        Assert.assertNotNull(broadcastResponse.get("error"));
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(24 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateRecipientsBobChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, BOB_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));

        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        cancel(shufflingId, ALICE, shufflingStateHash, DAVE.getId());
        cancel(shufflingId, BOB, shufflingStateHash, DAVE.getId());
        cancel(shufflingId, CHUCK, shufflingStateHash, DAVE.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateRecipientsAliceBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        process(shufflingId, ALICE, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, BOB, ALICE_RECIPIENT);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        generateBlock();
        process(shufflingId, DAVE, DAVE_RECIPIENT);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));

        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        cancel(shufflingId, ALICE, shufflingStateHash, DAVE.getId());
        cancel(shufflingId, BOB, shufflingStateHash, DAVE.getId());
        cancel(shufflingId, CHUCK, shufflingStateHash, DAVE.getId());
        for (int i = 0; i < 14; i++) {
            generateBlock();
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_rcc), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_rcc, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_rcc, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(75 * Constants.ONE_rcc + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

}

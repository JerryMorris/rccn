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

package rcc.env;

public enum ServerStatus {
    BEFORE_DATABASE("Loading Database"), AFTER_DATABASE("Loading Resources"), STARTED("Online");

    private final String message;

    ServerStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

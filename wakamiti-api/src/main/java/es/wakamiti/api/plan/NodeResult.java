/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.plan;


import java.util.List;


/**
 * Represents the possible results of executing a node in a plan.
 * The results are ordered by severity in descending order.
 */
public enum NodeResult implements Comparable<NodeResult> {

    /**
     * Indicates that the node and all its children executed
     * successfully.
     */
    PASSED,

    /**
     * Indicates that the node was not executed because it has
     * no children.
     */
    NOT_IMPLEMENTED,

    /**
     * Indicates that the node was not executed.
     */
    SKIPPED,

    /**
     * Indicates that the node or any of its children was not
     * executed due
     * to a malformed definition.
     */
    UNDEFINED,

    /**
     * Indicates that the node or any of its children did not
     * pass validation.
     */
    FAILED,

    /**
     * Indicates that the node or any of its children encountered
     * a fatal error.
     */
    ERROR;


    /**
     * Checks if the result represents a successful execution.
     *
     * @return {@code true} if the result is {@link #PASSED},
     * otherwise {@code false}.
     */
    public boolean isPassed() {
        return this == PASSED;
    }

    /**
     * Checks if the result represents an executed state.
     *
     * @return {@code true} if the result is {@link #PASSED},
     * {@link #FAILED}, or {@link #ERROR}, otherwise {@code false}.
     */
    public boolean isExecuted() {
        return List.of(PASSED, FAILED, ERROR).contains(this);
    }

}

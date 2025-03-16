/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.plan;


/**
 * Enum representing different types of nodes in a plan.
 */
public enum NodeType {

    /**
     * Regular node that aggregates other nodes.
     */
    AGGREGATOR,

    /**
     * Root node for an individual test case.
     */
    TEST_CASE,

    /**
     * Aggregator node within a test case.
     */
    STEP_AGGREGATOR,

    /**
     * Executable final node within a test case.
     * Cannot have children.
     */
    STEP,

    /**
     * Non-executable final node within a test case.
     * Cannot have children.
     */
    VIRTUAL_STEP;

    /**
     * Returns the NodeType corresponding to the given ordinal value.
     *
     * @param value the ordinal value of the NodeType
     * @throws IllegalArgumentException if the value does not correspond to any NodeType
     */
    public static NodeType of(
            int value
    ) {
        for (var nodeType : NodeType.values()) {
            if (nodeType.ordinal() + 1 == value) {
                return  nodeType;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Checks if the current node type matches any of
     * the specified node types.
     *
     * @param nodeTypes the node types to check against
     * @return {@code true} if the current node type matches
     * any of the specified types, {@code false} otherwise
     */
    public boolean isAnyOf(
            NodeType... nodeTypes
    ) {
        for (NodeType nodeType : nodeTypes) {
            if (nodeType == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current node type does not match any
     * of the specified node types.
     *
     * @param nodeTypes the node types to check against
     * @return {@code true} if the current node type does not
     * match any of the specified types, {@code false} otherwise
     */
    public boolean isNoneOf(
            NodeType... nodeTypes
    ) {
        for (NodeType nodeType : nodeTypes) {
            if (nodeType == this) {
                return false;
            }
        }
        return true;
    }

}

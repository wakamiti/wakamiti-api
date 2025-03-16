package es.wakamiti.api.plan;


import java.util.function.UnaryOperator;


/**
 * Represents an argument for a node in a plan.
 * This interface is sealed and permits only {@link DataTable}
 * and {@link Document} implementations.
 */
public sealed interface NodeArgument permits DataTable, Document {

    /**
     * Creates a copy of the {@code NodeArgument}.
     *
     * @return a new instance of {@code NodeArgument}
     */
    default NodeArgument copy() {
        return copy(UnaryOperator.identity());
    }

    /**
     * Creates a copy of the {@code NodeArgument} with variables
     * replaced using the provided method.
     *
     * @param replacingVariablesMethod a UnaryOperator that defines
     *                                 how to replace variables in the
     *                                 {@code NodeArgument}
     * @return a new instance of {@code NodeArgument} with variables replaced
     */
    NodeArgument copy(
            UnaryOperator<String> replacingVariablesMethod
    );

}

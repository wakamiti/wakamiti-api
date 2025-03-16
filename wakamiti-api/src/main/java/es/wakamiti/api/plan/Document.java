package es.wakamiti.api.plan;


import lombok.*;

import java.util.function.UnaryOperator;


@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class Document implements NodeArgument {

    @NonNull
    private String content;
    private String contentType;

    @Override
    public NodeArgument copy(
            UnaryOperator<String> replacingVariablesMethod
    ) {
        return new Document(contentType, replacingVariablesMethod.apply(content));
    }

}

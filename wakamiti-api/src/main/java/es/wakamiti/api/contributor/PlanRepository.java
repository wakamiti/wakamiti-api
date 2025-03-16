package es.wakamiti.api.contributor;


import es.wakamiti.api.lang.WakamitiException;
import es.wakamiti.api.plan.PlanNode;
import es.wakamiti.api.repository.PlanNodeCriteria;
import es.wakamiti.extension.annotation.ExtensionPoint;
import es.wakamiti.extension.annotation.LoadStrategy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@ExtensionPoint(loadStrategy = LoadStrategy.SINGLETON)
public interface PlanRepository extends Contributor {


    Optional<PlanNode> getNode(
            UUID id
    );

    boolean existsNode(
            UUID id
    );

    Optional<UUID> getParentNodeID(
            UUID id
    );

    Optional<PlanNode> getParentNode(
            UUID id
    );

    /**
     * Delete completely a plan node, including its child nodes.
     * If the node was a child of another node, it will be detached.
     *
     * @throws WakamitiException if the id does not exist in the repository
     */
    void deleteNode(
            UUID id
    );

    /**
     * Attach a plan node as child of another node, at the end of the existing child list.
     * If the child node was already in the child list, this operation will have no effect.
     *
     * @throws WakamitiException if either the parent id or the child id do not exist in the repository
     */
    void attachChildNode(
            UUID parent,
            UUID child
    );

    /**
     * Attach a plan node as child of another node, at the beginning of the existing child list.
     * If the child node was already in the child list, this operation will have no effect.
     *
     * @throws WakamitiException if either the parent id or the child id do not exist in the repository
     */
    void attachChildNodeFirst(
            UUID parent,
            UUID child
    );

    /**
     * Detach a plan node as a child of another node, keeping it in the repository as an orphan node.
     * If the child node was not already in the child list, this operation will have no effect.
     *
     * @throws WakamitiException if either the parent id or the child id do not exist in the repository
     */
    void detachChildNode(
            UUID parent,
            UUID child
    );

    Optional<UUID> getRootNodeID(
            UUID id
    );

    Optional<PlanNode> getRootNode(
            UUID id
    );

    List<UUID> getNodeChildrenID(
            UUID id
    );

    List<PlanNode> getNodeChildren(
            UUID id
    );

    Stream<UUID> getNodeDescendantsID(
            UUID id
    );

    Stream<PlanNode> getNodeDescendants(
            UUID id
    );

    Stream<UUID> getNodeAncestorsID(
            UUID id
    );

    Stream<PlanNode> getNodeAncestors(
            UUID id
    );

    /**
     * Persist a plan node in the repository. If the node id did exist previously, it
     * will update the node content; otherwise, it will create a new record and assign a
     * unique id.
     *
     * @return The assigned node id
     */
    UUID persistNode(
            PlanNode node
    );

    Stream<PlanNode> searchNodes(
            PlanNodeCriteria criteria
    );

    void commit();

}

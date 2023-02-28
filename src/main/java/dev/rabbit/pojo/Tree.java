package dev.rabbit.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Tree implements Serializable {

    @Getter
    @JsonProperty
    private Node node;
    @JsonProperty
    private Swap swap;
    @JsonProperty
    private List<Tree> children = new ArrayList<>();

    public Tree(Node node) {
        this.node = node;
    }

    public static Tree of(Node node) {
        return new Tree(node);
    }

    public void appendPath(String rootPool, boolean ordered, Map<String, Object> path) {
        List<Map<String, Object>> tokenPath = (List<Map<String, Object>>) path.get("tokenPath");
        tokenPath.remove(0);
        List<Map<String, Object>> swapPath = (List<Map<String, Object>>) path.get("swapPath");

        AtomicBoolean includePath = new AtomicBoolean(false);
        swapPath.forEach(swapMap -> {
            Swap swap = Swap.of(swapMap);
            if (swap.containsPool(rootPool)) {
                if (swap.isOrdered() == ordered) {
                    includePath.set(true);
                }
            }
        });

        if (includePath.get()) appendInternally(rootPool, ordered, tokenPath, swapPath);
    }

    private void appendInternally(String rootPool, boolean ordered, List<Map<String, Object>> tokenPath, List<Map<String, Object>> swapPath) {
        if (tokenPath.isEmpty() || swapPath.isEmpty()) return;

        Node node = Node.of(tokenPath.get(0));
        Swap swap = Swap.of(swapPath.get(0));
        Tree tree;
        if (containsTreeWithRootNode(node, this.children)) {
            tree = getTreeWithRootNode(node, this.children);
        } else {
            tree = Tree.of(node);
            tree.setSwap(swap, rootPool, ordered);
            this.children.add(tree);
        }

        tokenPath.remove(0);
        swapPath.remove(0);

        tree.appendInternally(rootPool, ordered, tokenPath, swapPath);
    }

    private void setSwap(Swap swap, String rootPool, boolean ordered) {
        if (swap.containsPool(rootPool) && swap.isOrdered() == ordered) {
            this.swap = swap.trim(rootPool);
        } else {
            this.swap = swap;
        }
    }

    public Map<String, Object> toMap() {
        List<Map<String, Object>> childrenField = this.children.stream().map(Tree::toMap).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("node", this.node.toMap());
        map.put("swap", this.swap != null ? this.swap.toMap() : null);
        map.put("children", childrenField);

        return map;
    }

    public static Tree getTreeWithRootNode(Node node, List<Tree> trees) {
        for (Tree tree : trees) {
            if (tree.getNode().equals(node)) return tree;
        }
        throw new RuntimeException();
    }

    public static boolean containsTreeWithRootNode(Node node, List<Tree> trees) {
        return trees.stream().anyMatch(tree -> tree.getNode().equals(node));
    }
}

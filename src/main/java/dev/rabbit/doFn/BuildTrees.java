package dev.rabbit.doFn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.services.bigquery.model.TableRow;
import dev.rabbit.pojo.Node;
import dev.rabbit.pojo.Tree;
import dev.rabbit.pojo.TreeList;
import dev.rabbit.util.Mapper;
import org.apache.beam.sdk.transforms.DoFn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.rabbit.pojo.Tree.containsTreeWithRootNode;
import static dev.rabbit.pojo.Tree.getTreeWithRootNode;

public class BuildTrees extends DoFn<TableRow, TreeList> {

    public static BuildTrees of() {
        return new BuildTrees();
    }

    @ProcessElement
    public void processElement(@Element TableRow element, OutputReceiver<TreeList> out) throws JsonProcessingException {
        String rootPool = (String) element.get("pool");
        List<Map<String, Object>> paths = (List<Map<String, Object>>) element.get("paths");

        if (((List<Map<String, Object>>) paths.get(0).get("tokenPath")).size() == 0) return;

        List<Tree> treesForOrderedSwap = buildTreeList(rootPool, Mapper.INSTANCE.deepCopy(paths), true);
        List<Tree> treesForUnorderedSwap = buildTreeList(rootPool, Mapper.INSTANCE.deepCopy(paths), false);

        out.output(TreeList.of(rootPool, true, treesForOrderedSwap));
        out.output(TreeList.of(rootPool, false, treesForUnorderedSwap));
    }

    private static List<Tree> buildTreeList(String rootPool, List<Map<String, Object>> paths, boolean ordered) {
        List<Tree> trees = new ArrayList<>();

        for (Map<String, Object> path : paths) {
            Node rootNode = Node.of(((List<Map<String, Object>>) path.get("tokenPath")).get(0));
            Tree tree;
            if (containsTreeWithRootNode(rootNode, trees)) {
                tree = getTreeWithRootNode(rootNode, trees);
            } else {
                tree = Tree.of(rootNode);
                trees.add(tree);
            }
            tree.appendPath(rootPool, ordered, path);
        }

        return trees;
    }
}

package dev.rabbit.function;

import dev.rabbit.pojo.Tree;
import dev.rabbit.pojo.TreeList;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ToMongoDocumentFn extends SimpleFunction<TreeList, Document> {

    public static ToMongoDocumentFn of() {
        return new ToMongoDocumentFn();
    }

    public Document apply(TreeList input) {
        Document doc = new Document();
        doc.append("_id", input.getPool());
        List<Document> treeDocuments = new ArrayList<>();
        input.getTrees().forEach(tree -> {
            treeDocuments.add(new Document(tree.toMap()));
        });
        doc.append("trees", treeDocuments);
        return doc;
    }
}

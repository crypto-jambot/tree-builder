package dev.rabbit;

import dev.rabbit.doFn.BuildTrees;
import dev.rabbit.function.ToMongoDocumentFn;
import dev.rabbit.io.MongoDbWrite;
import dev.rabbit.util.SecretManagerUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Reshuffle;

public class TreeBuilderPipeline {

    public static void run(TreeBuilderPipelineOptions options) {

        Pipeline pipeline = Pipeline.create(options);
        String mongoUri = "mongodb+srv://jam:" + SecretManagerUtils.of().accessSecretVersionString(options.getGcpProject(), "mongo-password") + "@ethereum.oovx2.mongodb.net/ethereum?retryWrites=true&w=majority";
        pipeline.apply("Query for path data", BigQueryIO.readTableRows()
                        .fromQuery(options.getQueryString())
                        .withoutValidation()
                        .withTemplateCompatibility()
                        .usingStandardSql())
                .apply("Build Trees", ParDo.of(BuildTrees.of()))
                .apply("Break Fusion", Reshuffle.viaRandomKey())
                .apply("Map to Mongo Documents", MapElements.via(ToMongoDocumentFn.of()))
                .apply("Write to Mongo", MongoDbWrite.builder()
                        .uri(mongoUri)
                        .database(options.getDatabase())
                        .collection(options.getCollection())
                        .batchSize(options.getMongoBatchSize())
                        .build());

        pipeline.run();
    }
}

package dev.rabbit;

import dev.rabbit.doFn.BuildTrees;
import dev.rabbit.function.ToMongoDocumentFn;
import dev.rabbit.util.SecretManagerUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Reshuffle;

public class TreeBuilderPipeline {

    public static void run(TreeBuilderPipelineOptions options) {

        Pipeline pipeline = Pipeline.create(options);

        String mongoUri = "mongodb://jam:" + SecretManagerUtils.of().accessSecretVersionString(options.getGcpProject(), "mongo-password") + "@sellsdev.com/ethereum?authSource=admin";

        pipeline.apply("Query for path data", BigQueryIO.readTableRows()
                        .fromQuery(options.getQueryString())
                        .withoutValidation()
                        .withTemplateCompatibility()
                        .usingStandardSql())
                .apply("Build Trees", ParDo.of(BuildTrees.of()))
                .apply("Break Fusion", Reshuffle.viaRandomKey())
                .apply("Map to Mongo Documents", MapElements.via(ToMongoDocumentFn.of()))
                .apply("Write to Mongo", MongoDbIO.write()
                        .withUri(mongoUri)
                        .withDatabase(options.getDatabase())
                        .withCollection(options.getCollection()));
//                .apply("Map to JSON", MapElements.into(TypeDescriptors.strings()).via(Mapper.INSTANCE::toJsonString))
//                .apply("Write to GCS", TextIO.write().to(options.getGcsBucket()).withNumShards(options.getShardNumber()).withSuffix(".jsonl"));

        pipeline.run();
    }
}

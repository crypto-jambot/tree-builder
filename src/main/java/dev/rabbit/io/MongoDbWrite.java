package dev.rabbit.io;

import com.mongodb.ConnectionString;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Slf4j
public class MongoDbWrite extends PTransform<PCollection<Document>, PDone> {

    private String uri;
    private String database;
    private String collection;
    private Integer batchSize;

    public PDone expand(PCollection<Document> input) {
        Preconditions.checkArgument(this.getUri() != null, "uri is required");
        Preconditions.checkArgument(this.getDatabase() != null, "database is required");
        Preconditions.checkArgument(this.getCollection() != null, "collection is required");
        Preconditions.checkArgument(this.getBatchSize() != null, "batchSize is required");
        input.apply(ParDo.of(new WriteFn(this)));
        return PDone.in(input.getPipeline());
    }

    static class WriteFn extends DoFn<Document, Void> {
        private final MongoDbWrite spec;
        private transient MongoClient client;
        private List<Document> batch;

        WriteFn(MongoDbWrite spec) {
            this.spec = spec;
        }

        @Setup
        public void createMongoClient() {
            ConnectionString connectionString = new ConnectionString(this.spec.uri);
            MongoClientSettings settings = MongoClientSettings
                    .builder()
                    .applyConnectionString(connectionString)
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    .build();
            this.client = MongoClients.create(settings);
        }

        @StartBundle
        public void startBundle() {
            this.batch = new ArrayList<>();
        }

        @ProcessElement
        public void processElement(@Element Document document) {
            this.batch.add(document);
            if (this.batch.size() >= spec.batchSize) {
                flush();
            }
        }

        @FinishBundle
        public void finishBundle() {
            flush();
        }

        private void flush() {
            if (this.batch.isEmpty()) return;
            MongoDatabase mongoDatabase = client.getDatabase(this.spec.database);
            MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(this.spec.collection);
            try {
                mongoCollection.insertMany(this.batch, new InsertManyOptions().ordered(false));
            } catch (MongoBulkWriteException mongoBulkWriteException) {
                List<BulkWriteError> bulkWriteErrors = mongoBulkWriteException.getWriteErrors()
                        .stream().filter(bulkWriteError -> bulkWriteError.getCode() == 11000).collect(Collectors.toList());
                if (bulkWriteErrors.size() > 0) {
                    bulkWriteErrors.forEach(bulkWriteError -> {
                        log.warn(bulkWriteError.getMessage());
                    });
                } else {
                    throw mongoBulkWriteException;
                }
            }
            this.batch.clear();
        }

        @Teardown
        public void closeMongoClient() {
            this.client.close();
            this.client = null;
        }
    }
}

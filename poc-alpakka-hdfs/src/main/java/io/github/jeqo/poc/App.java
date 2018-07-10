package io.github.jeqo.poc;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.hdfs.*;
import akka.stream.alpakka.hdfs.javadsl.HdfsFlow;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import io.github.jeqo.poc.schema.avro.MyOtherRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        ActorSystem actorSystem = ActorSystem.create("poc-alpakka-hdfs");
        ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

        Configuration conf = new Configuration();
        conf.set("fs.default.name", "hdfs://192.168.99.100:8020");

        FileSystem fs = FileSystem.get(conf);
        BiFunction<Long, Long, String> func =
                (rotationCount, timestamp) ->
                        "/tmp/alpakka1/" + rotationCount + "-" + timestamp;
        FilePathGenerator pathGenerator = FilePathGenerator.create(func);

        HdfsWritingSettings settings =
                HdfsWritingSettings.create()
                        .withOverwrite(true)
                        .withNewLine(false)
                        .withLineSeparator(System.getProperty("line.separator"))
                        .withPathGenerator(pathGenerator);

        Flow<HdfsWriteMessage<ByteString, NotUsed>, RotationMessage, NotUsed> flow =
                HdfsFlow.data(
                        fs,
                        SyncStrategy.count(500),
                        RotationStrategy.size(1, FileUnit.GB()),
                        settings);

        Source.single(MyOtherRecord.newBuilder().setF1("abc").build())
                .map(MyOtherRecord::toByteBuffer)
                .map(ByteString::fromByteBuffer)
                .map(s -> HdfsWriteMessage.create(s, NotUsed.getInstance()))
                .via(flow)
                .runWith(Sink.ignore(), materializer);
    }
}

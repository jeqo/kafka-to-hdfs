# Kafka to HDFS

Evaluating integration from Apache Kafka as a Source and HDFS as Sink.

Frameworks to evaluate:

* Kafka HDFS Connector
* Alpakka HDFS Connector

## Kafka HDFS Connector

> Source: <https://docs.confluent.io/current/connect/connect-hdfs/docs/hdfs_connector.html>

This Connector has been implemented and is supported by Confluent.

It supports HDFS as a Sink (only).

### How to run it

* Start Docker services (if you haven't done that yet)

```bash
docker-compose up -d
```

* Connect to Kafka Schema Registry and insert messages:

Connect to container

```bash
docker-compose exec schema-registry bash
```

Start Producer

```bash
kafka-avro-console-producer --broker-list broker:9092 --topic test_hdfs \
--property value.schema='{"type":"record","name":"myrecord","fields":[{"name":"f1","type":"string"}]}'
```

Then add messages

```bash
{"f1": "value1"}
{"f1": "value2"}
{"f1": "value3"}
```

* Install Connector:

Linux:

```bash
curl -X POST -H "Content-Type: application/json" --data @hdfs-connector.json http://192.168.99.100:8083/connectors
```

Windows:

```powershell
Invoke-RestMethod -Method Post -ContentType application/json -InFile hdfs-connector.json -Uri http://192.168.99.100:8083/connectors
```

> Where `192.168.99.100` is the Docker Host IP

* Check Connect worker logs to validate it is working fine:

```bash
docker-compose logs -f connect
...
connect            | [2018-07-10 11:36:09,177] INFO Committed hdfs://namenode:8020/topics/test_hdfs/partition=0/test_hdfs+0+0000000000+0000000002.avro for test_hdfs-0 (io.confluent.connect.hdfs.TopicPartitionWriter)
```

* Validate HDFS Avro file

Connect to `namenode` container:

```bash
docker-compose exec namenode bash
```

Download Avro Tools to deserialize messages:

```bash
curl -o avro-tools-1.8.2.jar http://mirror.metrocast.net/apache/avro/avro-1.8.2/java/avro-tools-1.8.2.jar
```

Check HDFS file is created:

```bash
hadoop fs -ls /topics/test_hdfs/partition=0
```

Copy file to local to test with Avro tools:

```bash
hadoop fs -copyToLocal /topics/test_hdfs/partition=0/test_hdfs+0+0000000000+0000000002.avro \
/tmp/test_hdfs+0+0000000000+0000000002.avro
```

Test with Avro Tools:

```bash
java -jar avro-tools-1.8.2.jar tojson /tmp/test_hdfs+0+0000000000+0000000002.avro
```

## Alpakka HDFS Connector

> Source: <https://developer.lightbend.com/docs/alpakka/current/hdfs.html>

[WIP]
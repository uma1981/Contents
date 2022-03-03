/* Spark Streaming Application to acquire live stream data from kafka, register as rdds and tables, join, calculate metrics 
 * and persist the driving pattern data to elasticsearch  
*/
// Import Spark core, Sql, Streaming, Kafka and Elastic search libraries

package org.inceptez.streaming
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.streaming.{Seconds, StreamingContext}
import StreamingContext._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.SQLContext._
import org.elasticsearch.spark.streaming._
import org.apache.spark.sql.Row;
import org.elasticsearch.spark._
import java.util.Date
import org.apache.spark.sql.hive.orc._
import org.apache.spark.sql._
object fleetstream {

println("**Starting the FLEET STREAMING PROGRAM****")

println("**Initializing case classes****")
  
//Define Case classes for events and elastic search rdds to impose schema on the data hence we can register as dataframes.
// events case class is defined to impose structure on the streaming data that we get from kafka and register as dataframes, then as a temp view for performing sql queries on it  

case class events(driverId: String, truckId: String, eventType: String, longitude: Float,latitude:Float, routeName: String, eventts: String,miles: String);

case class rddtoes (driverId: String,truckId: String,eventType: String,hrs :String,miles :String, total: String); 

case class drivedetailrddtoes (driverId: Integer,drivername:String,location:String,certified:String,truckId: Integer, eventType: String,tothrs :Float,totmiles :Float,numevents:Integer, curmiles: Integer,avgdriven: Integer);

// case class driveevents is to write the Elasticsearch with latitude and longitude positions generating geopoints for 
// plotting in realtime map which convert back the dataframes to rdd hence the stream data can be joined with the 
//sqooped data and write to elastic search 

case class driveevents(driverid: Integer, truckid: Integer, eventtype: String, longitude: String,latitude:String,location:String, drivername:String, routename: String, eventdate: Date);

def main(args:Array[String])
{

println("**Main program begins****")

//  Initialize Spark configuration object including Elastic search nodes and port info.
  
val sparkConf = new SparkConf().setAppName("fleetstream").setMaster("local[*]")
sparkConf.set("es.nodes", "localhost")
sparkConf.set("es.port", "9200")

println("**Initializing Spark context, Streaming Context, SQL context and Hive Contexts****")
//  Initialize Spark context.

val sparkcontext = new SparkContext(sparkConf)

// Show only errors in the console
  
sparkcontext.setLogLevel("ERROR")

//  Initialize Spark Streaming context stream once in 10 seconds.

val ssc = new StreamingContext(sparkcontext, Seconds(10))

//  Initialize Spark Sql context.
  
val sqlcontext = new SQLContext(sparkcontext)

import sqlcontext.implicits._
import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType}

//  Initialize Hive context.

val hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkcontext)


println("**Defining structure type for reading column name and defining schema explicitly****")

val driveSchema = StructType(Array(
    StructField("driverId", IntegerType, true),
    StructField("name", StringType, true),
    StructField("ssn", IntegerType, true),
    StructField("location", StringType, true),
    StructField("certified", StringType, true),
    StructField("wage", StringType, true)));

val tsSchema = StructType(Array(
    StructField("driverId", IntegerType, true),
    StructField("week", IntegerType, true),
    StructField("hours", IntegerType, true),
    StructField("miles", IntegerType, true)));

println("**Spark SQL Operations starts reading data from sqoop imported locations****")

// Spark SQL Operations - Read the static data drivers and timesheet from from sqoop imported locations and register as a temporary view and run an aggregate query to find 
// the driver wise total hrs and miles driven and create driverinfo, timesheet and aggregated drivingsummary temp views used later in the streaming.

val driverdetails = sqlcontext.read.option("header","false").schema(driveSchema).option("delimiter",",").csv("hdfs://localhost:54310/user/hduser/fleet/driver/part*")

val timedetails = sqlcontext.read.option("header","false").schema(tsSchema).option("delimiter",",").csv("hdfs://localhost:54310/user/hduser/fleet/ts/part*")

driverdetails.createOrReplaceTempView("driverinfo")
timedetails.createOrReplaceTempView("timesheet")

val driverdetail = sqlcontext.sql("select driverId,name,ssn,location,certified,wage from driverinfo");
val drivinginfo = sqlcontext.sql("select driverid,sum(hours) hrs,sum(miles) miles from timesheet group by driverid");

drivinginfo.createOrReplaceTempView("drivingsummary");

drivinginfo.show(2,false);
driverdetail.show(2,false) 

// Create a temporary view to store driverdetail salary calculated based on joined data and identified based on certifications 

val driverdetailts = sqlcontext.sql("select driverid,name,certified,wage,week,hrs,miles,case when certified='Y' then hrs*25 else miles*0.5 end as dollartopay from (select t.driverid,d.name,d.certified,d.wage,t.week,sum(t.hours) hrs,sum(t.miles) miles from timesheet t inner join driverinfo d on t.driverid=d.driverid group by t.driverid,d.name,d.certified,d.wage,t.week) temp ");

driverdetailts.createOrReplaceTempView("drivedetailstshive");

driverdetailts.show(2,false)

//println("**Hive SQL Operations starts reading data from the drivedetailstshive computed in spark SQL and write result in orc format in hdfs location****")
//
//val results = sqlcontext.sql("SELECT * FROM drivedetailstshive")
//       
//  if(!results.rdd.isEmpty)
//         {
//       results.write.format("orc").save("hdfs://localhost:54310/user/hduser/fleet/drivedetailstsorc")
//
//       println("**Display the orc data read using hive context****")
//       
//       val hivetbl = hiveContext.read.format("orc").load("hdfs://localhost:54310/user/hduser/fleet/drivedetailstsorc")
//       hivetbl.createOrReplaceTempView("hiveout")
//       hiveContext.sql("SELECT * from hiveout").collect.foreach(println)
//
//       }

// Checkpoint the lineages for streaming RDDs helps in case of failure of driver

ssc.checkpoint("file:/tmp/ckptdir/ckpt")


println("**Kafka config started****")

// Define Direct Kafka related Params

val kafkaParams = Map[String, Object](
"bootstrap.servers" -> "localhost:9092",
"key.deserializer" -> classOf[StringDeserializer],
"value.deserializer" -> classOf[StringDeserializer],
"group.id" -> "kafkatest1",
"auto.offset.reset" -> "latest"
)

val topics = Array ("truckevents1")

println("** steam rdd created as kafka reads the from truckevents1 topic ****")

val stream = KafkaUtils.createDirectStream[String, String](
ssc,
PreferConsistent,
Subscribe[String, String](topics, kafkaParams)
)


println("**Read the kafka streaming data and parse using Spark Core functions****")

// Read the kafka streaming data and parse using Spark Core functions

val kafkastream = stream.map(record => (record.key, record.value))

// Read only the message value and leave the header, split based on , delimiter and filter all blank messages if any

val inputStream = kafkastream.map(rec => rec._2).map(line => line.split(",")).filter { x => x.length>5 }

//inputStream.print

println("**Iterate on each and every dstream micro batch rdds and apply events case class to impose dataframe and convert as temp table****")

println("**Create driverstreamtable from kafka streaming data and create tables to find driving patterns, events with latitude/longitude****")

// Iterate on each and every dstream micro batch rdds and apply events case class to impose dataframe and convert as temp table
// Create driverstreamtable from kafka streaming data and create tables to find driving patterns, events with latitude/longitude

inputStream.foreachRDD(rdd => {
  
  if(!rdd.isEmpty)
            {
rdd.map(x => x.mkString(","))
val df = rdd.map(x => events(x(0).toString(),x(1).toString(), x(2).toString, x(3).toFloat,x(4).toFloat,x(5).toString,x(6).toString,x(7).toString));
val df1 = df.toDF()
df1.createOrReplaceTempView("driverstreamtable");

val drivingpattern = sqlcontext.sql("select distinct driverId,truckId,eventType,routeName,eventts from driverstreamtable where eventType<>'Perfect Driving'");

val driveevents1 = sqlcontext.sql("select distinct a.driverId driverid,a.truckId truckid,a.eventType eventype,a.longitude,a.latitude,concat(a.latitude,',',a.longitude) location,b.name drivername, a.routeName routename,a.eventts eventdate,a.miles from driverstreamtable a inner join driverinfo b on a.driverId=b.driverId where eventType<>'Perfect Driving'");

val drivingpatterncnt = sqlcontext.sql("select a.driverId,a.truckId,a.eventType,b.hrs,b.miles,count(1) total from driverstreamtable a inner join drivingsummary b on a.driverId=b.driverId where a.eventType<>'Perfect Driving' group by a.driverId,a.truckId,a.eventType,b.hrs,b.miles" );

val drivingpatterdetails = sqlcontext.sql("select c.driverId,c.name,c.location,c.certified,a.truckId,a.eventType,b.hrs total_hrs,b.miles total_miles,count(1) number_of_events,cast(sum(a.miles) as Int) current_miles, cast(sum(a.miles)/count(1) as Int) avg_driven from driverstreamtable a inner join drivingsummary b on a.driverId=b.driverId left join driverinfo c on a.driverId=c.driverId where a.eventType<>'Perfect Driving' group by c.driverId,c.name,c.location,c.certified,a.truckId,a.eventType,b.hrs,b.miles" );

// Print complete value of all columns, printing only first 2 rows 
drivingpattern.show(2,false);
drivingpatterncnt.show(2,false);
drivingpatterdetails.show(2,false);
driveevents1.show(2,false);

println("**Write the computed data to 3 Elastic Search indexes created seperately, if not created auto create will happen****")

// Write the computed data to totally 3 Elastic Search indexes- Convert the DataFrame to RDD (as DF cant be writtern directly to ES) and apply 
// case class and write to ES indexes created manually, if not created auto create will happen.

println("**Write to drivingpattern es for identifying cumulative realtime analytics of drivers driving the vehicles  ****")
 
val drivingpatternrdd = drivingpatterncnt.rdd;
val drivingpatternrddes = drivingpatternrdd.map(x => rddtoes(x(0).toString , x(1).toString().toString ,x(2).toString,x(3).toString,x(4).toString,x(5).toString));

drivingpatternrddes.saveToEs("drivingpattern/driver")  

println("**Write to drivingpattern detail es for identifying detailed realtime analytics of drivers driving the vehicles  ****")

val drivingpatterndetrdd = drivingpatterdetails.rdd;
val drivingpatterndetrddes = drivingpatterndetrdd.map(x => drivedetailrddtoes(x(0).toString.toInt , x(1).toString() ,x(2).toString,x(3).toString,x(4).toString.toInt,x(5).toString,x(6).toString.toFloat,x(7).toString.toFloat,x(8).toString.toInt,x(9).toString.toInt,x(10).toString().toInt));

drivingpatterndetrddes.saveToEs("drivingpatterndet/driver")

println("**Write to drive events es for identifying geo locations of where our drivers driving the vehicles  ****")

val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
val driveevents1rdd = driveevents1.rdd;
val driveevents1rddes = driveevents1rdd.map(x => driveevents(x(0).toString.toInt , x(1).toString().toInt ,x(2).toString,x(3).toString,x(4).toString,x(5).toString,x(6).toString,x(7).toString,format.parse(x(8).toString)));

driveevents1rddes.saveToEs("driveevents1/drivedocs")

println("Streaming data written into Elastic Search")

println("Continue running to read new events from kafka")
            }
})

ssc.start()
ssc.awaitTermination()
}
}


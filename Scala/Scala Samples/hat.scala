package org.inceptez.project

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.inceptez.hack.allmethods
import org.inceptez.hack.allmethods
import org.apache.spark.sql.functions.udf

case class insureclass (IssuerId:Int,IssuerId2:Int,BusinessDate:String,StateCode:String,SourceName:String,NetworkName:String,NetworkURL:String,custnum:String,MarketCoverage:String,DentalOnlyPlan:String)
object hackathon {
  def main(args:Array[String]){
    val spark = SparkSession.builder().master("local[*]")
                .appName("hackathon")
                .getOrCreate()
spark.sparkContext.setLogLevel("ERROR")
//1. Load the file1 (insuranceinfo1.csv) from HDFS using textFile API into an RDD insuredata     
    val insuredata = spark.sparkContext.textFile("hdfs://localhost:54310/user/hduser/sparkhack2/insuranceinfo1.csv")
//2. Remove the header line from the RDD contains column names.     
    val header = insuredata.first        
    val insdata = insuredata.filter(x => x != header) 
//3. Display the count and show few rows and check whether header is removed.    
    println("Count of lines with header: " + insuredata.count)
    println("Count of lines without header: " + insdata.count)
    insdata.first()
    insdata.takeOrdered(20)
//4. Remove the blank lines in the rdd.     
    val blank = ""
    val insdatanb = insdata.filter(x=> x.trim != blank)
//5. Map and split using ‘,’ delimiter.     
    val inssplit = insdatanb.map(x=> x.split(",",-1))
//6. Filter number of fields are equal to 10.  
    val inslen10 = inssplit.filter(x=>x.length ==10)
//7. Add case class namely insureclass with the field names used as per the header record in the file and apply to the above data to create schemaed RDD.    
    val inscase = inslen10.map(x=> insureclass(x(0).toInt, x(1).toInt,x(2),x(3),x(4),x(5),x(6),x(7),x(8),x(9)))
//8. Take the count of the RDD created in step 7 and step 1 and print how many rows are removed in the cleanup process of removing fields does not equals 10.
    val inpcnt = insuredata.count
    val cleancnt = inscase.count
    println("Count of input file : " + inpcnt)
    println("Count of cleaned up file : " + cleancnt)
    println("number of lines removed in clean up : " +(inpcnt - cleancnt))
//9. Create another RDD namely rejectdata and store the row that does not equals 10 fields, and analyze why and provide your view here.    
    val rejectdata = inssplit.filter(x=> x.length != 10)
    
    println("There are no reject data, filtering rows with 10 elements is an precaution")
//10. Load the file2 (insuranceinfo2.csv) from HDFS using textFile API into an RDD insuredata2    
    val insuredata2 = spark.sparkContext.textFile("hdfs://localhost:54310/user/hduser/sparkhack2/insuranceinfo2.csv")
//11. Repeat from step 2 to 9 for this file also and create the final rdd.    
    val header2 = insuredata2.first        
    val insdata2 = insuredata2.filter(x => x != header) 
    println("Count of lines with header: " + insuredata2.count)
    println("Count of lines without header: " + insdata2.count)
    insdata2.first()
    insdata2.takeOrdered(20)
    val insdatanb2 = insdata2.filter(x=> x.trim != blank)
    
    val inssplit2 = insdatanb2.map(x=> x.split(",",-1))
    val inslen102 = inssplit2.filter(x=>x.length ==10)
    
    val inscase2 = inslen102.map(x=> insureclass(if (x(0)==blank) 999999 else x(0).toInt, if (x(1)==blank) 999999 else x(1).toInt,x(2),x(3),x(4),x(5),x(6),x(7),x(8),x(9))) 
    val inscase2a = inscase2.filter(x => 'IssuerId != 999999 || 'IssuerId2 !=999999 )
    val inpcnt2 = insuredata2.count
    val cleancnt2 = inscase2a.count
    println("Count of input file : " + inpcnt2)
    println("Count of cleaned up file : " + cleancnt2)
    println("number of lines removed in clean up : " +(inpcnt2 - cleancnt2))
    
    
    val rejectdata2 = inssplit2.filter(x=> x.length != 10 || x(0) == blank || x(1) == blank || x(7) == blank )    
    println("There are no reject data, filtering rows with 10 elements is an precaution")
//12. Merge the both header removed RDDs derived in steps 7 and 11 into an RDD namely insuredatamerged    
    val insuredatamerged = inscase.union(inscase2a) 
    
    val mergecnt = insuredatamerged.count()
//13. Cache it either to memory or any other persistence levels you want, display only first few rows     
    insuredatamerged.cache()
    
    insuredatamerged.take(3).foreach(println)
//14. Calculate the count of rdds created in step 7+11 and rdd in step 12, check whether they are matching.    
    println("Clean count for ins1 : "+ cleancnt)
    println("Clean count for ins2 : "+ cleancnt2)
    println("Count of merged rdd : " + mergecnt)
//15. Remove duplicates from this merged RDD created in step 12 and print how many duplicate rows are there.    
    val insdist= insuredatamerged.distinct
    
    val insdistcnt=insdist.count
    
    println("Dulicates count in merged rdd : "+ (mergecnt - insdistcnt))
//16. Increase the number of partitions to 8 and name it as insuredatarepart.    
    val insuredatarepart = insdist.repartition(8)
    println("partion info of insuredatarepart" + insuredatarepart.getNumPartitions)
//17. Split the above RDD using the businessdate field into rdd_20191001 and rdd_ 20191002 based on the BusinessDate of 2019-10-01 and 2019-10-02 respectively    
     val rdd_20191001 = insdist.filter ( x => 'BusinessDate == "2019-10-01" )
     val rdd_20191002 = insdist.filter ( x => 'BusinessDate == "2019-10-02" )
//18. Store the RDDs created in step 9, 12, 17 into HDFS locations.
     
     //file rdd_20191001
  try {
    rdd_20191001.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rdd_20191001")
 }
   catch {
     case ex: org.apache.hadoop.mapred.FileAlreadyExistsException =>
          {
            println("Hadoop File already exist")
            val fs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:54310"),spark.sparkContext.hadoopConfiguration)
            fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/sparkhack2/rdd_20191001"),true)
            rdd_20191001.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rdd_20191001")
          }       
 
      //file rdd_20191002  
  try {
    rdd_20191002.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rdd_20191002")
 }
   catch {
     case ex: org.apache.hadoop.mapred.FileAlreadyExistsException =>
          {
            println("Hadoop File already exist")
            val fs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:54310"),spark.sparkContext.hadoopConfiguration)
            fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/sparkhack2/rdd_20191002"),true)
            rdd_20191002.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rdd_20191002")
          }       
      //file insuredatamerged
  try {
    insuredatamerged.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/insuredatamerged")
 }
   catch {
     case ex: org.apache.hadoop.mapred.FileAlreadyExistsException =>
          {
            println("Hadoop File already exist")
            val fs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:54310"),spark.sparkContext.hadoopConfiguration)
            fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/sparkhack2/insuredatamerged"),true)
            insuredatamerged.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/insuredatamerged")
          }       

      }
      }
    //file rejectdata2
  try {
    rejectdata2.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rejectdata2")
 }
   catch {
     case ex: org.apache.hadoop.mapred.FileAlreadyExistsException =>
          {
            println("Hadoop File already exist")
            val fs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:54310"),spark.sparkContext.hadoopConfiguration)
            fs.delete(new org.apache.hadoop.fs.Path("/user/hduser/sparkhack2/rejectdata2"),true)
            rejectdata2.saveAsTextFile("hdfs://localhost:54310/user/hduser/sparkhack2/rejectdata2")
          }       

      }
      }  
   
//   19. Convert the RDD created in step 16 above into Dataframe namely insuredaterepartdf
   import spark.implicits._
   
   val insuredaterepartdf = insuredatarepart.toDF
   
   insuredaterepartdf.show()  
// end of part 1 and 2
   
//20. Create structuretype for all the columns as per the insuranceinfo1.csv.
   
   val schemastring = "IssuerId:Int,IssuerId2:Int,BusinessDate:Date,StateCode:String,SourceName:String,NetworkName:String,NetworkURL:String,custnum:String,MarketCoverage:String,DentalOnlyPlan:String"
   val schemasplit = schemastring.split(",")
   val schemafields = schemasplit.map(x=>structure(x))  
   val schema = StructType(schemafields)
/*
   val schema1 = StructType(List(StructField("IssuerId",IntegerType,true)
                               , StructField("IssuerId2",IntegerType,true)
                               , StructField("BusinessDate",DateType,true)
                               , StructField("StateCode",StringType,true)
                               , StructField("SourceName",StringType,true)
                               , StructField("NetworkName",StringType,true)
                               , StructField("NetworkURL",StringType,true)
                               , StructField("custnum",StringType,true)
                               , StructField("MarketCoverage",StringType,true)
                               , StructField("DentalOnlyPlan",StringType,true)
                               )
                           )
*/
   println(schema)
//println(schema1)
//21. Create dataframes/datasets using the csv module with option to escape ‘,’ accessing the insuranceinfo1.csv and insuranceinfo2.csv files, apply the schema of the structure type   
   val ins1 = spark.read
                   .option("header","true")
                   .schema(schema)
                   .option("escape",",")
                   .csv("hdfs://localhost:54310/user/hduser/sparkhack2/insuranceinfo1.csv")
                   
   val ins2 = spark.read
                   .option("header","true")
                   .schema(schema)
                   .option("escape",",")
                   .csv("hdfs://localhost:54310/user/hduser/sparkhack2/insuranceinfo2.csv")
                     
ins1.show()
ins1.printSchema()

ins2.show()
ins2.printSchema()

//22. Apply the below DSL functions in the DFs created in step 21.
//a. Rename the fields StateCode and SourceName as stcd and srcnm respectively.
val ins1rename = ins1.withColumnRenamed("StateCode", "stcd").withColumnRenamed("SourceName", "srcnm")
val ins2rename = ins2.withColumnRenamed("StateCode", "stcd").withColumnRenamed("SourceName", "srcnm")

ins1rename.show()
ins2rename.show()

val ins1comp =ins1rename
              .withColumn("issueridcomposite",concat($"IssuerId".cast("String"),$"IssuerId2".cast("String")))
              .withColumn("sysdt",current_date)
              .withColumn("systs",current_timestamp)
              .drop($"DentalOnlyPlan")
              .na.drop
ins1comp.show()

val ins2comp =ins2rename
              .withColumn("issueridcomposite",concat($"IssuerId".cast("String"),$"IssuerId2".cast("String")))
              .withColumn("sysdt",current_date)
              .withColumn("systs",current_timestamp)
              .drop($"DentalOnlyPlan")
              .na.drop
ins2comp.show()

val x = new allmethods
val insudf = udf(x.remspecialchar _)
//26. Call the above udf in the DSL by passing NetworkName column as an argument to get the special characters removed. 
val ins1changed = ins1comp.withColumn("NetworkName_changed", insudf($"NetworkName"))
val ins2changed = ins2comp.withColumn("NetworkName_changed", insudf($"NetworkName"))
ins1changed.show(false)
ins2changed.show(false)
val insmerge = ins1changed.union(ins2changed).distinct()
//27. Save the DF generated in step 26 in JSON into HDFS with overwrite option.
ins1changed.write.format("json")
            .mode("overwrite")
            .save("hdfs://localhost:54310/user/hduser/sparkhack2/ins1CSV")
ins2changed.write.format("json")
            .mode("overwrite")          
            .save("hdfs://localhost:54310/user/hduser/sparkhack2/ins2CSV")

//28. Save the DF generated in step 26 into CSV format with header name as per the DF and delimited by ~ into HDFS with overwrite option.
ins1changed.write.format("csv")
            .mode("overwrite")
            .option("delimiter", "~")
            .option("enclosed", "\"")
            .option("header", "true")
            .save("hdfs://localhost:54310/user/hduser/sparkhack2/ins1CSV")
ins2changed.write.format("csv")
            .mode("overwrite")
            .option("delimiter", "~")
            .option("enclosed", "\"")
            .option("header", "true")
            .save("hdfs://localhost:54310/user/hduser/sparkhack2/ins2CSV")
//29. Save the DF generated in step 26 into hive table with append option. 
ins1changed.write.mode("append").saveAsTable("ins1")           
ins2changed.write.mode("append").saveAsTable("ins2")
//30. Load the file3 (custs_states.csv) from the HDFS location, using textfile API in an RDD custstates, this file contains 2 type of data one with 5 columns contains customer master info and other data with statecode and description of 2 columns.
val custstates = spark.sparkContext.textFile("hdfs://localhost:54310/user/hduser/sparkhack2/custs_states.csv")
                 .map(x=>x.split(",",-1))
                 
//31. Split the above data into 2 RDDs, first RDD namely custfilter should be loaded only with 5 columns data and second RDD namely statesfilter should be only loaded with 2 columns data.

val custfilter = custstates.filter(x=> x.length == 5)
val statesfilter = custstates.filter(x=> x.length == 2)
//32. Load the file3 (custs_states.csv) from the HDFS location, using CSV Module in a DF custstatesdf, this file contains 2 type of data one with 5 columns contains customer master info and other data with statecode and description of 2 columns.
val custstatesdf = spark.read.format("csv").option("delimiter",",").option("header","false").load("hdfs://localhost:54310/user/hduser/sparkhack2/custs_states.csv") 
//33. Split the above data into 2 DFs, first DF namely custfilterdf should be loaded only with 5 columns data and second DF namely statesfilterdf should be only loaded with 2 columns  data.
val custdf = custstatesdf.filter(col("_c0").isNotNull && col("_c1").isNotNull && col("_c2").isNotNull && col("_c3").isNotNull) 
            .toDF("custno","fname","lname","age","prof")
val statesdf = custstatesdf.filter(col("_c0").isNotNull && col("_c1").isNotNull && col("_c2").isNull && col("_c3").isNull && col("_c4").isNull)
              .select("_c0","_c1").toDF("sstcd","stdesc")  


//34. Register the above DFs as temporary views as custview and statesview.  

custdf.createOrReplaceTempView("custview")
statesdf.createOrReplaceTempView("statesview")

//35. Register the DF generated in step 22 as a tempview namely insureview
insmerge.createOrReplaceTempView("insureview")

//36. Import the package, instantiate the class and Register the method created in step 24 in the name of remspecialcharudf using spark udf registration.

spark.udf.register("remspecialcharudf",x.remspecialchar _)

val insdfudf = spark.sql("""select custno, fname, lname,age,prof, remspecialcharudf(NetworkName) as cleannetworkname
,case 
when substring(upper(NetworkURL),1,5)= 'HTTPS' then 'https' 
when substring(upper(NetworkURL),1,5)= 'HTTP:' then 'http' 
else 'noprotocol' 
end as PROTOCOL
,current_date as curdt
,current_timestamp curts
,substring(BusinessDate,1,4) as yr
,substring(BusinessDate,6,2) as mth  
,stdesc as statedesc
from insureview inner join statesview on stcd = sstcd
inner join custview on custnum = custno """)

insdfudf.show(false)

insdfudf.createOrReplaceTempView("conins")
//38. Store the above selected Dataframe in Parquet formats in a HDFS location as a single file.
insdfudf.coalesce(1).write.mode("overwrite").format("parquet").save("hdfs://localhost:54310/user/hduser/sparkhack2/joinparq")

//39. Write an SQL query to identify average age, count group by statedesc, protocol, profession.

val avgagecount = spark.sql("""select statedesc, PROTOCOL, prof, avg(age),count(*) from conins group by statedesc, PROTOCOL, prof""")
  
//40. Store the DF generated in step 39 into MYSQL table 	 if time permits.
val prop=new java.util.Properties();
prop.put("user", "root")
prop.put("password", "root")

avgagecount.write.mode("overwrite").jdbc("jdbc:mysql://localhost/custdb","insureinfo",prop)

  }  //end of main

  
   def structure(a:String):StructField = {
      
      val coltype = a.split(":")
      val type1 = coltype(1).trim
      var sfield:StructField = StructField(coltype(0),StringType,nullable = true)
      
           
      type1 match {
        case "String" =>    sfield = StructField(coltype(0),StringType,nullable = true)
        case "Long"   =>    sfield = StructField(coltype(0),LongType,nullable = true)
        case "Int"    =>    sfield = StructField(coltype(0),IntegerType,nullable = true)
        case "Date"    =>   sfield = StructField(coltype(0),DateType,nullable = true)
        case _        =>    sfield = StructField(coltype(0),StringType,nullable = true)             
      } 
      return sfield
           
    } 
/*   
   def regexreplace(a:String):String={
         val re = new Regex("[;\\/:*?\"<>|&'0-9]")
         val outputString = re.replaceAllIn(a, "")
         return outputString
     
   }*/

}//end of object

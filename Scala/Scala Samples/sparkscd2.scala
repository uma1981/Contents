package org.inceptez.spark.core
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrameReader
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types._

object sparkscd2 {
	def main(args: Array[String]) {
		val spark = SparkSession.builder().appName("Sample sql app").master("local[*]").getOrCreate();
		val sc = spark.sparkContext
				val sqlc = spark.sqlContext
				import sqlc.implicits._
				sc.setLogLevel("ERROR")
					val schemaforhistory = new StructType()
					.add("id", IntegerType, true)
					.add("ver", IntegerType, true)
					.add("exchang", StringType, true)
					.add("company", StringType, true)
					.add("timestamp", DateType, true)
					.add("value",DoubleType,true)
					val schemafornew = new StructType()
					.add("id", IntegerType, true)
					.add("exchang", StringType, true)
					.add("company", StringType, true)
					.add("timestamp", DateType, true)
					.add("value",DoubleType,true)
				def dfhandler(dfparam: DataFrame, tblname: String) {
			println("Inside the df function")
			dfparam.createOrReplaceTempView(tblname);
			dfparam.show
		}
		val dfsrc = spark.read.option("inferschema", "true")
				.option("delimiter", ",")
				.option("header", "true")
				.schema(schemaforhistory)
					.csv("C:\\Users\\BhagyaGanisetti\\Downloads\\scd\\scd2\\histdata2.csv")
				val dfnew = spark.read.option("inferschema", "true")
				.schema(schemafornew)
				.option("delimiter", ",")
				.option("header", "true")
				.csv("C:\\Users\\BhagyaGanisetti\\Downloads\\scd\\scd2\\newdata2.csv")
				dfhandler(dfsrc, "initialdata")
				dfhandler(dfnew, "incremental")
				try {
					//Incremental load
					if (!dfsrc.rdd.isEmpty()) {
						println("Historical data is available scd2")
						val historydf = sqlc.sql("select id,ver,exchang,company,timestamp,value from initialdata")
						historydf.show
						val historydfmax = sqlc.sql("select id ,max(ver) as max_ver from initialdata group by id")
						historydfmax.show
						historydfmax.createOrReplaceTempView("historymaxver");
						val newdf = sqlc.sql("""select i.id ,row_number() over(partition by i.id order by timestamp) + coalesce(max_ver,0) as ver,exchang,company,timestamp,value
								from incremental i left outer join historymaxver h on i.id=h.id union select * from initialdata""")
								newdf.show
								newdf.cache
								newdf.count
								newdf.coalesce(1).write.mode("overwrite").csv("C:\\Users\\BhagyaGanisetti\\Downloads\\scd\\scd2\\histdata2.csv")
					} else if (!dfnew.rdd.isEmpty()) {
						println("Loading Data for the first time")
						//first day load
						val newdf = sqlc.sql("select id,row_number() over(partition by i.id order by timestamp) as ver,exchang,company,timestamp,value from incremental orderby id ")
						newdf.show
						newdf.coalesce(1).write.mode("overwrite").csv("C:\\Users\\BhagyaGanisetti\\Downloads\\scd\\scd2\\histdata2.csv")
					} else {
						println("No data to process today")
					}
				} catch {
				case eaa: org.apache.spark.sql.AnalysisException => {
					println(s"Exception Occured $eaa")
				}
				case unknown: Exception => println(s"Unknown exception: $unknown")
				}
	}
}
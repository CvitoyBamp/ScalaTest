import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.rdd.RDD
import java.sql.Timestamp

object RDDs extends App{

  case class TaxiShema(
    VendorID: Int,
    tpep_pickup_datetime: String,
    tpep_dropoff_datetime: String,
    passenger_count: Int,
    trip_distance: Double,
    RatecodeID: Int,
    store_and_fwd_flag: String,
    PULocationID: Int,
    DOLocationID: Int,
    payment_type: Int,
    fare_amount: Double,
    extra: Double,
    mta_tax: Double,
    tip_amount: Double,
    tolls_amount: Double,
    improvement_surcharge: Double,
    total_amount: Double
                      )
  val spark = SparkSession.builder()
    .appName("Задание №2 RDD")
    .master("local[*]")
    .getOrCreate()

  val taxiDF: DataFrame = spark.read.load("src/main/resources/data/yellow_taxi_jan_25_2018")

  import spark.implicits._

  val taxiDS: Dataset[TaxiShema] = taxiDF.as[TaxiShema]

  val taxiRDD: RDD[TaxiShema] = taxiDS.rdd

  val taxiFinal: RDD[(String, Int)] = taxiRDD.map(x => (x.tpep_pickup_datetime, 1)).reduceByKey(_ + _).sortBy(_._2, ascending = false)

  taxiFinal.take(10).foreach(println)

  //taxiFinal.map{case (a,b) => (a, b.toString)}.toDF.write.text("D:\\scala\\DataFrame\\src\\main\\resources\\output")
  taxiFinal.repartition(1)
    .map(row => row._1.toString + " " + row._2.toString)
    .saveAsTextFile("\\src\\main\\resources\\output")
}


import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions.{col, count, max, mean, min, stddev}

object Dataset extends App {
  val spark = SparkSession.builder()
    .appName("Задание №3  Dataset")
    .master("local[*]")
    .getOrCreate()

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

  case class TaxiZone(
                       LocationID: String,
                       Borough: String,
                       Zone: String,
                       service_zone: String
                     )

  val taxiDF: DataFrame = spark.read.load("D:\\scala\\DataFrame\\src\\main\\resources\\data\\yellow_taxi_jan_25_2018")

  import spark.implicits._

  val taxiDS: Dataset[TaxiShema] = taxiDF.as[TaxiShema]

  val taxiZoneDF = spark.read
    .option("header", "true")
    .csv("D:\\scala\\DataFrame\\src\\main\\resources\\data\\taxi_zones.csv")

  val taxiZoneDS: Dataset[TaxiZone] = taxiZoneDF.as[TaxiZone]

  val finalTable = taxiDS
    .join(taxiZoneDS, taxiDS("DOLocationID") === taxiZoneDS("LocationID"), "left")
    .groupBy(taxiZoneDS("Borough"))
    .agg(
      count(taxiZoneDS("Borough")) as "Количество поездок",
      mean(taxiDS("trip_distance")) as "Среднее расстояние",
      stddev(taxiDS("trip_distance")) as "Среднеквадратичное отклонение",
      min(taxiDS("trip_distance")) as "Максимальная дистанция",
      max(taxiDS("trip_distance")) as "Минимальная дистанция"
    )
    .sort(col("Среднее расстояние").desc)

  finalTable
    .write
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost:5432/otus")
    .option("dbtable", "taxi")
    .option("user", "docker")
    .option("password", "docker")
    .option("driver", "org.postgresql.Driver")
    .mode("overwrite")
    .save

}

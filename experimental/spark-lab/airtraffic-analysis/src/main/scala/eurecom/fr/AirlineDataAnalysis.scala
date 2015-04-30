package eurecom.fr

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

object AirlineDataAnalysis {

  def main(args: Array[String]) {
    // Parameters
    //val INPUT_PATH = "local-input/AIRLINE/2008.csv"
    val INPUT_PATH = "local-input/AIRLINE/test.csv"
    val OUTPUT_DIR = "local-output/"

    // Create a default configuration with the AppName, use the Local Master
    val conf = new SparkConf().setAppName("AirlineDataAnalysis")
    conf.setMaster("local[2]") // remove this line if you want to run in your cluster
    // Create SparkContext using the Configuration
    val sparkContext = new SparkContext(conf)
    // Create SQLContext using the SparkContext
    val sqlContext = new SQLContext(sparkContext)
    // import some required libraries in SparkContext
    import sqlContext.implicits._

    // Read & Parse the input
    val data = sparkContext.textFile(INPUT_PATH).map(_.split(","))
      .map(l => new AirTraffic(l(0).tryGetInt, l(1).tryGetInt, l(2).tryGetInt, l(3).tryGetInt,
      l(4).tryGetInt, l(5).tryGetInt, l(6).tryGetInt, l(7).tryGetInt,
      l(8).trim, l(9).tryGetInt, l(10).trim, l(11).tryGetInt,
      l(12).tryGetInt, l(13).tryGetInt, l(14).tryGetInt, l(15).tryGetInt,
      l(16).trim, l(17).trim, l(18).tryGetInt, l(19).tryGetInt, l(20).tryGetInt,
      l(21).tryGetBoolean, l(22).trim, l(23).tryGetBoolean, l(24).tryGetInt,
      l(25).tryGetInt, l(26).tryGetInt, l(27).tryGetInt, l(28).tryGetInt)).toDF()

    // register table with SQLContext & cache this table in memory
    data.registerTempTable("AirTraffic")
    data.cache() // or sqlContext.cacheTable("AirTraffic")

    //=============================================================
    // Query 1 - Top 20 airports by total volume of flights
    //=============================================================
    ///*
    // Traffic by AirportCode
    sqlContext.sql(
      "(SELECT Dest AS AirportCode, COUNT(*) AS NumFlights " +
        "FROM AirTraffic " +
        "GROUP BY Dest) UNION ALL " +
        "(SELECT Origin AS AirportCode, COUNT(*) AS NumFlights " +
        "FROM AirTraffic " +
        "GROUP BY Origin)").registerTempTable("AirTrafficByAirportCode")

    //Get the top 20
    val top20TrafficByAirportCode = sqlContext.sql("SELECT AirportCode, SUM(NumFlights) AS NumFlights FROM AirTrafficByAirportCode GROUP BY AirportCode").orderBy($"NumFlights".desc).limit(20)

    //top20TrafficByAirportCode.collect().foreach(println)
    //top20TrafficByAirportCode.save(OUTPUT_DIR + "Query1/Top20/output.txt")// save as parquet file
    top20TrafficByAirportCode.rdd.saveAsTextFile(OUTPUT_DIR + "Query1/Top20")

    // Traffic by Month & AirportCode
    sqlContext.sql(
      "(SELECT Month, Dest AS AirportCode, COUNT(*) AS NumFlights " +
        "FROM AirTraffic " +
        "GROUP BY Month, Dest) UNION ALL " +
        "(SELECT Month, Origin AS AirportCode, COUNT(*) AS NumFlights " +
        "FROM AirTraffic " +
        "GROUP BY Month, Origin)").registerTempTable("AirTrafficByAirportCode")

    // Get the top 20
    val top20TrafficByMonth_AirportCode = sqlContext.sql("SELECT Month, AirportCode, SUM(NumFlights) AS NumFlights FROM AirTrafficByAirportCode GROUP BY Month, AirportCode").orderBy($"NumFlights".desc).limit(20)
    top20TrafficByMonth_AirportCode.rdd.saveAsTextFile(OUTPUT_DIR + "Query1/Top20WithGroups")
    //*/

    //=============================================================
    // Query 2 - Carrier Popularity
    //=============================================================
    ///*
    val trafficByMonth_Carrier = sqlContext.sql("SELECT Month, UniqueCarrier, COUNT(*) " +
      "FROM AirTraffic " +
      "GROUP BY Month, UniqueCarrier")
    val logTrafficByMonth_Carrier = trafficByMonth_Carrier.map(r => (r(0), r(1), math.log10(r(2).toString.toDouble)))
    logTrafficByMonth_Carrier.saveAsTextFile(OUTPUT_DIR + "Query2")
    //*/

    //=============================================================
    // Query 3 - Proportion of Flights Delayed
    //=============================================================
    ///*
    val flightDelays = sqlContext.sql("SELECT Month, DayOfMonth, DayOfWeek, (ArrTime - CRSArrTime) AS TimeDelay " +
                                        "FROM AirTraffic").registerTempTable("FlightDelays")
    val flightDelaysFraction = sqlContext.sql("SELECT Month, DayOfMonth, DayOfWeek, (SUM(CASE WHEN TimeDelay >= 15 THEN 1 ELSE 0 END) / COUNT(*)) AS DelayFraction " +
      "FROM FlightDelays " +
      "GROUP BY Month, DayOfMonth, DayOfWeek")
    flightDelaysFraction.rdd.saveAsTextFile(OUTPUT_DIR + "Query3")
    //*/

    //=============================================================
    // Query 4 - Carrier Delays
    //=============================================================
    ///*
    val carrierDelays = sqlContext.sql("SELECT Month, DayOfMonth, DayOfWeek, UniqueCarrier, (ArrTime - CRSArrTime) AS TimeDelay " +
      "FROM AirTraffic").registerTempTable("CarrierDelays")
    val carrierDelaysFraction = sqlContext.sql("SELECT Month, DayOfMonth, DayOfWeek, UniqueCarrier, (SUM(CASE WHEN TimeDelay >= 15 THEN 1 ELSE 0 END) / COUNT(*)) AS DelayFraction " +
      "FROM CarrierDelays " +
      "GROUP BY Month, DayOfMonth, DayOfWeek, UniqueCarrier")
    carrierDelaysFraction.rdd.saveAsTextFile(OUTPUT_DIR + "Query4")
    //*/


    //=============================================================
    // Query 5 - Busy Routes
    //=============================================================
    ///*
    val flightFrequencyByRoutes= sqlContext.sql("SELECT Origin, Dest, COUNT(*) AS NumFlights FROM AirTraffic GROUP BY Origin, Dest")
    val highestNumFlights = flightFrequencyByRoutes.orderBy($"NumFlights".desc).limit(1).collect()(0)(2)

    val busiestRoutes = flightFrequencyByRoutes.filter("NumFlights = " + highestNumFlights)
    busiestRoutes.rdd.saveAsTextFile(OUTPUT_DIR + "Query5")
    //*/
  }

  implicit class StringConverter(val s: String) extends AnyVal {
    def tryGetInt = Try(s.trim.toInt).toOption

    def tryGetString = {
      val res = s.trim
      if (res.isEmpty) None else res
    }

    def tryGetBoolean = Try(s.trim.toBoolean).toOption
  }

}


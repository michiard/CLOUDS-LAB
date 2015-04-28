package eurecom.fr

/*
// Define the schema to load the data, using case class
// The case class in Scala 2.10 can support only 22 fields, so we can use the interface Product
case class AirTraffic(Year:Option[Int], Month:Option[Int], DayOfMonth:Option[Int], DayOfWeek:Option[Int],
                      DepTime:Option[Int], CRSDepTime:Option[Int], ArrTime:Option[Int], CRSArrTime:Option[Int],
                      UniqueCarrier:String, FlightNum:Option[Int], TailNum:String, ActualElapsedTime:Option[Int],
                      CRSElapsedTime:Option[Int], AirTime:Option[Int], ArrDelay:Option[Int], DepDelay:Option[Int],
                      Origin:String, Dest:String, Distance:Option[Int], TaxiIn:Option[Int], TaxiOut:Option[Int],
                      Cancelled:Option[Boolean], CancellationCode:String, Diverted:Option[Boolean], CarrierDelay:Option[Int],
                      WeatherDelay:Option[Int], NASDelay:Option[Int], SecurityDelay:Option[Int], LateAircraftDelay:Option[Int])
*/
class AirTraffic(Year:Option[Int], Month:Option[Int], DayOfMonth:Option[Int], DayOfWeek:Option[Int],
                 DepTime:Option[Int], CRSDepTime:Option[Int], ArrTime:Option[Int], CRSArrTime:Option[Int],
                 UniqueCarrier:String, FlightNum:Option[Int], TailNum:String, ActualElapsedTime:Option[Int],
                 CRSElapsedTime:Option[Int], AirTime:Option[Int], ArrDelay:Option[Int], DepDelay:Option[Int],
                 Origin:String, Dest:String, Distance:Option[Int], TaxiIn:Option[Int], TaxiOut:Option[Int],
                 Cancelled:Option[Boolean], CancellationCode:String, Diverted:Option[Boolean], CarrierDelay:Option[Int],
                 WeatherDelay:Option[Int], NASDelay:Option[Int], SecurityDelay:Option[Int], LateAircraftDelay:Option[Int]) extends Product {

  // We declare field with Option[T] type to make that field null-able.
  override def productElement(n: Int): Any =
    n match {
      case 0 => Year
      case 1 => Month
      case 2 => DayOfMonth
      case 3 => DayOfWeek
      case 4 => DepTime
      case 5 => CRSDepTime
      case 6 => ArrTime
      case 7 => CRSArrTime
      case 8 => UniqueCarrier
      case 9 => FlightNum
      case 10 => TailNum
      case 11 => ActualElapsedTime
      case 12 => CRSElapsedTime
      case 13 => AirTime
      case 14 => ArrDelay
      case 15 => DepDelay
      case 16 => Origin
      case 17 => Dest
      case 18 => Distance
      case 19 => TaxiIn
      case 20 => TaxiOut
      case 21 => Cancelled
      case 22 => CancellationCode
      case 23 => Diverted
      case 24 => CarrierDelay
      case 25 => WeatherDelay
      case 26 => NASDelay
      case 27 => SecurityDelay
      case 28 => LateAircraftDelay
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }

  override def productArity: Int = 29

  override def canEqual(that: Any): Boolean = that.isInstanceOf[AirTraffic]
}

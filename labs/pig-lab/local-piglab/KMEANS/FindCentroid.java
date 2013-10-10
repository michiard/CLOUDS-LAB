import java.io.IOException;
import org.apache.pig.EvalFunc;
import org.apache.pig.data.Tuple;

public class FindCentroid extends EvalFunc<String>
{
  FindCentroid.Point[] centroids;

  public FindCentroid(String initialCentroid)
  {
    String[] centroidStrings = initialCentroid.split(":");
    this.centroids = new FindCentroid.Point[centroidStrings.length];

    for (int i = 0; i < centroidStrings.length; i++) {
      this.centroids[i] = new FindCentroid.Point();
    }

    for (int i = 0; i < centroidStrings.length; i++) {
      String[] values = centroidStrings[i].split(",");

      this.centroids[i].x = Double.parseDouble(values[0].replaceAll("\\(", ""));
      this.centroids[i].y = Double.parseDouble(values[1].replaceAll("\\)", ""));
    }
  }

  public String exec(Tuple input)
    throws IOException
  {
    double min_distance = 1.7976931348623157E+308D;
    FindCentroid.Point closest_centroid = new FindCentroid.Point();
    for (FindCentroid.Point centroid : this.centroids)
    {
      double x_axis = Math.pow(centroid.x - ((Double)input.get(0)).doubleValue(), 2.0D);
      double y_axis = Math.pow(centroid.y - ((Double)input.get(1)).doubleValue(), 2.0D);

      double distance = Math.sqrt(x_axis + y_axis);
      if (distance < min_distance) {
        min_distance = distance;
        closest_centroid = centroid;
      }
    }
    String value = closest_centroid.toString();
    return value;
  }

  public static void main(String[] args)
  {
    FindCentroid center = new FindCentroid("(12,122):(-1,91)");
  }

  private class Point
  {
    double x;
    double y;

    Point()
    {
      this.x = 0.0D;
      this.y = 0.0D;
    }

    public String toString() {
      return this.x + "," + this.y;
    }
  }
}
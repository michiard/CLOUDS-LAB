package fr.eurecom.dsg.mapreduce.utils;

public class TextSplitter {

  public static String[] split(String s) {
    return s.trim().split("[^A-Za-z0-9]+");
  }
}

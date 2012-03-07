package fr.eurecom.dsg.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import fr.eurecom.dsg.mapreduce.utils.LabConfigurator;


public class ReduceSideJoin extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {
    Configuration conf = getConf();
    int numberReducers = conf.getInt("wc_numred", 1);
    Path inputPath = new Path(conf.get("wc_input1"));
    Path outputPath = new Path(conf.get("wc_output"));
        
    return 0; // TODO: implement all the job components andconfigurations
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    LabConfigurator.parseArgs(args, conf, 1);
    int res = ToolRunner.run(conf, new ReduceSideJoin(), args);
    System.exit(res);
  }
  
}